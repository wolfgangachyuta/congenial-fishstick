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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import kotlinx.coroutines.delay

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
    var petStats by remember { mutableStateOf(PetStatsStore(context).load()) }
    LaunchedEffect(Unit) {
        while (true) {
            petStats = PetStatsStore(context).load()
            delay(5000)
        }
    }

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
            Spacer(modifier = Modifier.height(20.dp))

            Text("Energy", color = Color(0xFFB8B8B8), fontSize = 12.sp)
            LinearProgressIndicator(
                progress = petStats.energy,
                modifier = Modifier.fillMaxWidth(0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Happiness", color = Color(0xFFB8B8B8), fontSize = 12.sp)
            LinearProgressIndicator(
                progress = petStats.happiness,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "AURELION — Phase 5: touch, menu & pet needs",
                color = Color(0xFFE8C77E),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
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
fun EagleInteractionMenu(
    onMakeSound: () -> Unit,
    onFlySomewhere: () -> Unit,
    onSleep: () -> Unit,
    onWakeUp: () -> Unit,
    onFeed: () -> Unit,
    onPet: () -> Unit,
    onHide: () -> Unit,
    onOpenApp: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF2A2A2E),
        shadowElevation = 6.dp,
        modifier = Modifier
            .padding(top = 6.dp)
            .widthIn(max = 180.dp)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            MenuAction("Make a sound", onMakeSound)
            MenuAction("Fly somewhere", onFlySomewhere)
            MenuAction("Sleep", onSleep)
            MenuAction("Wake up", onWakeUp)
            MenuAction("Feed", onFeed)
            MenuAction("Pet", onPet)
            MenuAction("Hide companion", onHide)
            MenuAction("Open app", onOpenApp)
        }
    }
}

@Composable
private fun MenuAction(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        color = Color(0xFFEDEDED),
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp)
    )
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
