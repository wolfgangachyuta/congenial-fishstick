package com.aurelion.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1B1B1F)) {
                    AurelionPreviewScreen()
                }
            }
        }
    }
}

@Composable
fun AurelionPreviewScreen() {
    val context = LocalContext.current
    val controller = remember { EagleAnimationController() }
    LaunchedEffect(Unit) { controller.run() }
    LaunchedEffect(Unit) { controller.runSpeechLoop() }

    var companionRunning by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        context.startForegroundService(Intent(context, OverlayService::class.java))
        companionRunning = true
    }

    fun startCompanion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            context.startForegroundService(Intent(context, OverlayService::class.java))
            companionRunning = true
        }
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(context)) {
            startCompanion()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            EagleWithSpeechBubble(controller = controller, eagleSize = 260.dp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "AURELION — Phase 4: the eagle can speak",
                color = Color(0xFFE8C77E),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Button(onClick = {
                    if (!Settings.canDrawOverlays(context)) {
                        overlayPermissionLauncher.launch(
                            Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    } else {
                        startCompanion()
                    }
                }) {
                    Text(if (companionRunning) "Restart companion" else "Start floating companion")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    context.stopService(Intent(context, OverlayService::class.java))
                    companionRunning = false
                }) {
                    Text("Stop")
                }
            }
        }
    }
}

/** Eagle of a fixed [eagleSize], with its speech bubble (if any) floating
 *  above it. Used by the in-app preview; the overlay builds its own
 *  version of this layout so it can also attach drag handling to just the
 *  eagle's bounds. */
@Composable
fun EagleWithSpeechBubble(controller: EagleAnimationController, eagleSize: Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedVisibility(
            visible = controller.speechText != null,
            enter = fadeIn() + scaleIn(initialScale = 0.85f),
            exit = fadeOut() + scaleOut(targetScale = 0.85f)
        ) {
            SpeechBubble(text = controller.speechText.orEmpty())
        }
        EagleView(controller = controller, modifier = Modifier.size(eagleSize))
    }
}

/** A small rounded speech-bubble label. Shared by the in-app preview and
 *  the floating overlay. */
@Composable
fun SpeechBubble(text: String) {
    Surface(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .widthIn(max = 200.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFF6E0),
        shadowElevation = 4.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = Color(0xFF3A2C14),
            fontSize = 13.sp
        )
    }
}

@Composable
fun EagleView(controller: EagleAnimationController, modifier: Modifier = Modifier) {
    Crossfade(
        targetState = controller.state,
        animationSpec = tween(220),
        label = "eagle-pose",
        modifier = modifier
    ) { pose ->
        Image(
            painter = painterResource(id = pose.drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize()
        )
    }
}
