package com.aurelion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val controller = remember { EagleAnimationController() }
    LaunchedEffect(Unit) { controller.run() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            EagleView(controller = controller, modifier = Modifier.size(260.dp))
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "AURELION — Phase 1: the eagle lives",
                color = Color(0xFFE8C77E),
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Renders whatever pose [controller] currently holds, cross-fading between
 * poses so state changes read as a reaction rather than a cut. No
 * Activity/Context dependency beyond the Composable itself, so this drops
 * into a floating-overlay ComposeView unchanged in Phase 2.
 */
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
