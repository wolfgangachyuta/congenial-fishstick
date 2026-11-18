package com.aurelion.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.random.Random

/**
 * Hosts AURELION as a small draggable overlay bubble above every other
 * app. Combines drag-to-reposition (Phase 2), autonomous perch-to-perch
 * movement (Phase 3), unprompted speech (Phase 4), touch interactions and
 * pet-care stats (Phase 5), and — new here — charging-state awareness, a
 * top safety margin so perches/drag stay clear of the status bar/cutout
 * area, and a fixed-height slot for the speech bubble so the eagle no
 * longer shifts position while talking (Phase 7 polish).
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: ComposeView
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var prefs: SharedPreferences
    private lateinit var controller: EagleAnimationController
    private lateinit var petStatsController: PetStatsController
    private lateinit var serviceScope: CoroutineScope
    private val lifecycleOwner = OverlayLifecycleOwner()

    private var maxX: Int = 0
    private var maxY: Int = 0
    private var minY: Int = 0
    private var lastPerch: Pair<Int, Int>? = null

    private val powerConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_POWER_CONNECTED) {
                serviceScope.launch {
                    controller.say("Ah, a tether of lightning. I shall rest while you both recharge.")
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())

        serviceScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        controller = EagleAnimationController()
        petStatsController = PetStatsController(this)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        ContextCompat.registerReceiver(
            this,
            powerConnectionReceiver,
            IntentFilter(Intent.ACTION_POWER_CONNECTED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        refreshBounds()

        val savedX = prefs.getInt(KEY_X, maxX).coerceIn(0, maxX)
        val savedY = prefs.getInt(KEY_Y, maxY / 3).coerceIn(minY, maxY)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = savedX
            y = savedY
        }

        lifecycleOwner.performRestore()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        overlayView = ComposeView(this)
        overlayView.setViewTreeLifecycleOwner(lifecycleOwner)
        overlayView.setViewTreeViewModelStoreOwner(lifecycleOwner)
        overlayView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        overlayView.setContent {
            val coroutineScope = rememberCoroutineScope()
            var showMenu by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) { controller.run() }
            LaunchedEffect(Unit) { controller.runSpeechLoop() }
            LaunchedEffect(Unit) { runAutonomousMovement(controller) }
            LaunchedEffect(Unit) { petStatsController.run() }
            LaunchedEffect(controller.state) {
                petStatsController.isResting = (controller.state == EagleState.SLEEPING)
            }
            LaunchedEffect(petStatsController.stats) {
                controller.updatePetStats(petStatsController.stats)
            }
            LaunchedEffect(Unit) {
                val settingsStore = SettingsStore(this@OverlayService)
                while (true) {
                    controller.updateTalkativeness(settingsStore.loadTalkativeness())
                    delay(10_000)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Fixed-height slot: the bubble fades in/out inside it, so
                // the window's overall measured size — and therefore the
                // eagle's on-screen position — never changes, whether or
                // not the eagle is currently talking.
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .widthIn(max = 200.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    AnimatedVisibility(
                        visible = controller.speechText != null,
                        enter = fadeIn() + scaleIn(initialScale = 0.85f),
                        exit = fadeOut() + scaleOut(targetScale = 0.85f)
                    ) {
                        SpeechBubble(text = controller.speechText.orEmpty())
                    }
                }
                Box(
                    modifier = Modifier
                        .size(EAGLE_SIZE_DP.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    coroutineScope.launch {
                                        if (!controller.isPerformingAction) {
                                            controller.playExpression(
                                                listOf(
                                                    EagleState.LOOK_LEFT,
                                                    EagleState.LOOK_RIGHT,
                                                    EagleState.CURIOUS
                                                ).random()
                                            )
                                        }
                                    }
                                },
                                onDoubleTap = {
                                    coroutineScope.launch {
                                        if (!controller.isPerformingAction) {
                                            controller.playExpression(EagleState.EXCITED, holdMillis = 1200)
                                        }
                                    }
                                },
                                onLongPress = { showMenu = true }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                layoutParams.x = (layoutParams.x + dragAmount.x.roundToInt()).coerceIn(0, maxX)
                                layoutParams.y = (layoutParams.y + dragAmount.y.roundToInt()).coerceIn(minY, maxY)
                                windowManager.updateViewLayout(overlayView, layoutParams)
                            }
                        }
                ) {
                    EagleView(controller = controller, modifier = Modifier.fillMaxSize())
                }

                AnimatedVisibility(visible = showMenu) {
                    EagleInteractionMenu(
                        onMakeSound = {
                            showMenu = false
                            coroutineScope.launch { controller.say(EagleDialogue.pickLine()) }
                        },
                        onFlySomewhere = {
                            showMenu = false
                            coroutineScope.launch { flyToRandomPerch(controller) }
                        },
                        onSleep = {
                            showMenu = false
                            controller.forceSleep()
                        },
                        onWakeUp = {
                            showMenu = false
                            controller.forceWake()
                        },
                        onFeed = {
                            showMenu = false
                            petStatsController.feed()
                            coroutineScope.launch { controller.say("Much appreciated. More, if you insist.") }
                        },
                        onPet = {
                            showMenu = false
                            petStatsController.pet()
                            coroutineScope.launch { controller.playExpression(EagleState.CURIOUS, holdMillis = 1400) }
                        },
                        onHide = {
                            showMenu = false
                            stopSelf()
                        },
                        onOpenApp = {
                            showMenu = false
                            val launchIntent = Intent(this@OverlayService, MainActivity::class.java).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(launchIntent)
                        }
                    )
                }
            }
        }

        windowManager.addView(overlayView, layoutParams)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::windowManager.isInitialized && ::layoutParams.isInitialized) {
            refreshBounds()
            layoutParams.x = layoutParams.x.coerceIn(0, maxX)
            layoutParams.y = layoutParams.y.coerceIn(minY, maxY)
            if (::overlayView.isInitialized) {
                windowManager.updateViewLayout(overlayView, layoutParams)
            }
        }
    }

    override fun onDestroy() {
        if (::prefs.isInitialized && ::layoutParams.isInitialized) {
            prefs.edit().putInt(KEY_X, layoutParams.x).putInt(KEY_Y, layoutParams.y).apply()
        }
        runCatching { unregisterReceiver(powerConnectionReceiver) }
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        if (::serviceScope.isInitialized) {
            serviceScope.cancel()
        }
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }

    private fun refreshBounds() {
        val density = resources.displayMetrics.density
        val eagleSizePx = (EAGLE_SIZE_DP * density).roundToInt()
        val topSafetyMarginPx = (TOP_SAFETY_MARGIN_DP * density).roundToInt()
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)
        maxX = (metrics.widthPixels - eagleSizePx).coerceAtLeast(0)
        maxY = (metrics.heightPixels - eagleSizePx).coerceAtLeast(0)
        minY = topSafetyMarginPx.coerceAtMost(maxY)
    }

    private suspend fun runAutonomousMovement(controller: EagleAnimationController) {
        val settingsStore = SettingsStore(this)
        while (true) {
            val multiplier = settingsStore.loadMovementFrequency().coerceAtLeast(0.1f)
            val base = Random.nextLong(20_000, 50_000)
            delay((base / multiplier).roundToLong())
            flyToRandomPerch(controller)
        }
    }

    /** Flies the eagle to one perch point. Called both by the autonomous
     *  timer and by the menu's "Fly somewhere" action. */
    private suspend fun flyToRandomPerch(controller: EagleAnimationController) {
        val target = pickPerchPoint()
        controller.withExclusiveControl {
            controller.forceState(EagleState.TAKEOFF)
            delay(400)
            controller.forceState(EagleState.EXCITED)

            val startX = layoutParams.x
            val startY = layoutParams.y
            val steps = 24
            for (i in 1..steps) {
                val t = i / steps.toFloat()
                val eased = 1f - (1f - t) * (1f - t)
                layoutParams.x = (startX + (target.first - startX) * eased).roundToInt()
                layoutParams.y = (startY + (target.second - startY) * eased).roundToInt().coerceIn(minY, maxY)
                windowManager.updateViewLayout(overlayView, layoutParams)
                delay(20)
            }
        }
    }

    private fun pickPerchPoint(): Pair<Int, Int> {
        val midX = maxX / 2
        val midY = (minY + maxY) / 2
        val candidates = listOf(
            0 to minY,
            maxX to minY,
            0 to maxY,
            maxX to maxY,
            midX to minY,
            midX to maxY,
            0 to midY,
            maxX to midY
        ).filter { it != lastPerch }

        val choice = candidates.random()
        lastPerch = choice
        return choice
    }

    private fun buildNotification(): Notification {
        val channelId = "aurelion_companion"
        val channel = NotificationChannel(
            channelId,
            "AURELION companion",
            NotificationManager.IMPORTANCE_MIN
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        return Notification.Builder(this, channelId)
            .setContentTitle("AURELION is watching over your screen")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME = "aurelion_overlay_prefs"
        private const val KEY_X = "overlay_x"
        private const val KEY_Y = "overlay_y"
        const val EAGLE_SIZE_DP = 120
        private const val TOP_SAFETY_MARGIN_DP = 32
    }
}
