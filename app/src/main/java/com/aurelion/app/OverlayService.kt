package com.aurelion.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ServiceInfo
import android.provider.Settings
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
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
 * app. Combines drag-to-reposition, autonomous perch-to-perch movement,
 * unprompted speech, touch interactions, pet-care stats, charging-state
 * awareness, and a fixed-height slot for the speech bubble.
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
    private var receiverRegistered = false
    private var overlayAttached = false

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

        // Permission may have been revoked after the Activity's preflight check.
        if (!Settings.canDrawOverlays(this)) {
            startupError = "Allow AURELION to display over other apps before starting the companion."
            stopSelf()
            return
        }

        try {
            startupError = null
            promoteToForeground()

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
        receiverRegistered = true

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
                petStatsController.isResting =
                    controller.state == EagleState.SLEEPING
            }

            LaunchedEffect(petStatsController.stats) {
                controller.updatePetStats(petStatsController.stats)
            }

            LaunchedEffect(Unit) {
                val settingsStore = SettingsStore(this@OverlayService)

                while (true) {
                    controller.updateTalkativeness(
                        settingsStore.loadTalkativeness()
                    )
                    delay(10_000)
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Fixed-height slot prevents speech bubbles from moving
                // the eagle while entering or leaving the screen.
                Box(
                    modifier = Modifier
                        .height(64.dp)
                        .widthIn(max = 200.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Explicitly select the enclosing ColumnScope overload.
                    // The BoxScope receiver otherwise makes this call ambiguous
                    // to Kotlin's implicit-receiver resolution.
                    this@Column.AnimatedVisibility(
                        visible = controller.speechText != null
                    ) {
                        SpeechBubble(
                            text = controller.speechText.orEmpty()
                        )
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
                                            controller.playExpression(
                                                EagleState.EXCITED,
                                                holdMillis = 1200
                                            )
                                        }
                                    }
                                },
                                onLongPress = {
                                    showMenu = true
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { _, dragAmount ->
                                layoutParams.x =
                                    (layoutParams.x + dragAmount.x.roundToInt())
                                        .coerceIn(0, maxX)

                                layoutParams.y =
                                    (layoutParams.y + dragAmount.y.roundToInt())
                                        .coerceIn(minY, maxY)

                                windowManager.updateViewLayout(
                                    overlayView,
                                    layoutParams
                                )
                            }
                        }
                ) {
                    EagleView(
                        controller = controller,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                AnimatedVisibility(visible = showMenu) {
                    EagleInteractionMenu(
                        onMakeSound = {
                            showMenu = false
                            coroutineScope.launch {
                                controller.say(EagleDialogue.pickLine())
                            }
                        },
                        onFlySomewhere = {
                            showMenu = false
                            coroutineScope.launch {
                                flyToRandomPerch(controller)
                            }
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
                            coroutineScope.launch {
                                controller.say(
                                    "Much appreciated. More, if you insist."
                                )
                            }
                        },
                        onPet = {
                            showMenu = false
                            petStatsController.pet()
                            coroutineScope.launch {
                                controller.playExpression(
                                    EagleState.CURIOUS,
                                    holdMillis = 1400
                                )
                            }
                        },
                        onHide = {
                            showMenu = false
                            stopSelf()
                        },
                        onOpenApp = {
                            showMenu = false
                            val launchIntent =
                                Intent(
                                    this@OverlayService,
                                    MainActivity::class.java
                                ).apply {
                                    addFlags(
                                        Intent.FLAG_ACTIVITY_NEW_TASK
                                    )
                                }

                            startActivity(launchIntent)
                        }
                    )
                }
            }
        }

        windowManager.addView(overlayView, layoutParams)
        overlayAttached = true
        isRunning = true
        } catch (error: Exception) {
            Log.e(TAG, "Unable to start floating companion", error)
            startupError = if (error is SecurityException) {
                "Android denied permission to show the floating companion. Check overlay and notification permissions."
            } else {
                "AURELION could not start the floating companion. Please try again."
            }
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
        } else if (!Settings.canDrawOverlays(this)) {
            startupError = "Overlay permission was revoked. The companion has been stopped."
            stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun promoteToForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (::windowManager.isInitialized &&
            ::layoutParams.isInitialized
        ) {
            refreshBounds()

            layoutParams.x = layoutParams.x.coerceIn(0, maxX)
            layoutParams.y =
                layoutParams.y.coerceIn(minY, maxY)

            if (::overlayView.isInitialized) {
                updateOverlayLayout()
            }
        }
    }

    override fun onDestroy() {
        if (::prefs.isInitialized && ::layoutParams.isInitialized) {
            prefs.edit()
                .putInt(KEY_X, layoutParams.x)
                .putInt(KEY_Y, layoutParams.y)
                .apply()
        }

        if (receiverRegistered) {
            runCatching {
                unregisterReceiver(powerConnectionReceiver)
            }
            receiverRegistered = false
        }

        if (overlayAttached && ::overlayView.isInitialized) {
            runCatching {
                windowManager.removeView(overlayView)
            }
            overlayAttached = false
        }

        isRunning = false

        if (::serviceScope.isInitialized) {
            serviceScope.cancel()
        }

        if (lifecycleOwner.lifecycle.currentState != Lifecycle.State.INITIALIZED &&
            lifecycleOwner.lifecycle.currentState != Lifecycle.State.DESTROYED
        ) {
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
        lifecycleOwner.viewModelStore.clear()

        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun updateOverlayLayout() {
        if (!Settings.canDrawOverlays(this)) {
            startupError = "Overlay permission was revoked. The companion has been stopped."
            stopSelf()
            return
        }
        try {
            windowManager.updateViewLayout(overlayView, layoutParams)
        } catch (error: Exception) {
            Log.w(TAG, "Overlay update failed; stopping companion", error)
            startupError = "The floating companion was interrupted by Android and has been stopped."
            stopSelf()
        }
    }

    private fun refreshBounds() {
        val density = resources.displayMetrics.density
        val eagleSizePx = (EAGLE_SIZE_DP * density).roundToInt()
        val topSafetyMarginPx = (TOP_SAFETY_MARGIN_DP * density).roundToInt()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds: Rect = metrics.bounds
            val insets = metrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout()
            )
            maxX = (bounds.right - insets.right - eagleSizePx)
                .coerceAtLeast(insets.left)
            maxY = (bounds.bottom - insets.bottom - eagleSizePx)
                .coerceAtLeast(insets.top)
            minY = (bounds.top + insets.top + topSafetyMarginPx)
                .coerceAtMost(maxY)
        } else {
            val metrics = android.util.DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealMetrics(metrics)
            maxX = (metrics.widthPixels - eagleSizePx).coerceAtLeast(0)
            maxY = (metrics.heightPixels - eagleSizePx).coerceAtLeast(0)
            minY = topSafetyMarginPx.coerceAtMost(maxY)
        }
    }

    private suspend fun runAutonomousMovement(
        controller: EagleAnimationController
    ) {
        val settingsStore = SettingsStore(this)

        while (true) {
            val multiplier =
                settingsStore.loadMovementFrequency()
                    .coerceAtLeast(0.1f)

            val base =
                Random.nextLong(20_000, 50_000)

            delay((base / multiplier).roundToLong())

            flyToRandomPerch(controller)
        }
    }

    /** Flies the eagle to one perch point. */
    private suspend fun flyToRandomPerch(
        controller: EagleAnimationController
    ) {
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
                val eased =
                    1f - (1f - t) * (1f - t)

                layoutParams.x =
                    (
                        startX +
                            (target.first - startX) * eased
                        ).roundToInt()

                layoutParams.y =
                    (
                        startY +
                            (target.second - startY) * eased
                        ).roundToInt()
                        .coerceIn(minY, maxY)

                updateOverlayLayout()

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
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "AURELION companion",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when your floating eagle companion is active."
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("AURELION is active")
            .setContentText("Your floating companion is perched on the screen.")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openAppPendingIntent)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setVisibility(Notification.VISIBILITY_PRIVATE)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(null, "Stop companion", stopPendingIntent).build()
            )
            .build()
    }

    companion object {
        var isRunning by mutableStateOf(false)
            private set
        var startupError by mutableStateOf<String?>(null)

        private const val TAG = "AURELION"
        private const val CHANNEL_ID = "aurelion_companion"
        private const val ACTION_STOP = "com.aurelion.app.action.STOP_COMPANION"
        private const val NOTIFICATION_ID = 1001
        private const val PREFS_NAME =
            "aurelion_overlay_prefs"
        private const val KEY_X = "overlay_x"
        private const val KEY_Y = "overlay_y"

        const val EAGLE_SIZE_DP = 120
        private const val TOP_SAFETY_MARGIN_DP = 32
    }
}
