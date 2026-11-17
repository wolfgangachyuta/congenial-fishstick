package com.aurelion.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlin.math.roundToInt

/**
 * Hosts AURELION as a small draggable overlay bubble above every other
 * app, using WindowManager directly (no Activity involved) so the
 * companion keeps running after the main app is closed. Reuses the exact
 * same EagleAnimationController/EagleView from Phase 1 unchanged.
 */
class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: ComposeView
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var prefs: SharedPreferences
    private val lifecycleOwner = OverlayLifecycleOwner()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val density = resources.displayMetrics.density
        val eagleSizePx = (EAGLE_SIZE_DP * density).roundToInt()

        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(metrics)

        val maxX = (metrics.widthPixels - eagleSizePx).coerceAtLeast(0)
        val maxY = (metrics.heightPixels - eagleSizePx).coerceAtLeast(0)

        val savedX = prefs.getInt(KEY_X, maxX).coerceIn(0, maxX)
        val savedY = prefs.getInt(KEY_Y, metrics.heightPixels / 3).coerceIn(0, maxY)

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
            val controller = remember { EagleAnimationController() }
            LaunchedEffect(Unit) { controller.run() }

            Box(
                modifier = Modifier
                    .size(EAGLE_SIZE_DP.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            layoutParams.x = (layoutParams.x + dragAmount.x.roundToInt()).coerceIn(0, maxX)
                            layoutParams.y = (layoutParams.y + dragAmount.y.roundToInt()).coerceIn(0, maxY)
                            windowManager.updateViewLayout(overlayView, layoutParams)
                        }
                    }
            ) {
                EagleView(controller = controller, modifier = Modifier.fillMaxSize())
            }
        }

        windowManager.addView(overlayView, layoutParams)
    }

    override fun onDestroy() {
        if (::prefs.isInitialized && ::layoutParams.isInitialized) {
            prefs.edit().putInt(KEY_X, layoutParams.x).putInt(KEY_Y, layoutParams.y).apply()
        }
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
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
    }
}
