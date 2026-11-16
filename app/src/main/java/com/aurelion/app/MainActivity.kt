package com.aurelion.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            EagleCanvas(modifier = Modifier.size(260.dp))
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
 * Phase 1 eagle: procedurally drawn with Compose Canvas so there is no
 * dependency on external art assets yet. Breathing, blinking and a slow
 * head-tilt idle cycle are driven by independent infinite transitions so
 * later phases can swap this for sprite/skeletal animation without
 * touching the rest of the app.
 */
@Composable
fun EagleCanvas(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "eagle-idle")

    val breathe by infinite.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val headTilt by infinite.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headTilt"
    )

    val blinkCycle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )
    val eyeOpenAmount = if (blinkCycle > 0.94f) 0.1f else 1f

    Canvas(modifier = modifier) {
        drawEagle(
            bodyScale = breathe,
            headTiltDegrees = headTilt,
            eyeOpenAmount = eyeOpenAmount
        )
    }
}

private fun DrawScope.drawEagle(
    bodyScale: Float,
    headTiltDegrees: Float,
    eyeOpenAmount: Float
) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f

    val bodyColor = Color(0xFF8A5A2B)
    val wingColor = Color(0xFF4A3220)
    val beakColor = Color(0xFFE0A526)
    val eyeColor = Color(0xFFFFD54A)

    withTransform({
        scale(
            scaleX = bodyScale,
            scaleY = bodyScale,
            pivot = Offset(cx, cy)
        )
    }) {
        drawOval(
            color = bodyColor,
            topLeft = Offset(cx - w * 0.22f, cy - h * 0.05f),
            size = Size(w * 0.44f, h * 0.5f)
        )

        val wingPathLeft = Path().apply {
            moveTo(cx - w * 0.05f, cy - h * 0.02f)
            quadraticBezierTo(cx - w * 0.42f, cy + h * 0.1f, cx - w * 0.18f, cy + h * 0.42f)
            quadraticBezierTo(cx - w * 0.1f, cy + h * 0.25f, cx - w * 0.02f, cy + h * 0.3f)
            close()
        }
        drawPath(wingPathLeft, color = wingColor)

        val wingPathRight = Path().apply {
            moveTo(cx + w * 0.05f, cy - h * 0.02f)
            quadraticBezierTo(cx + w * 0.42f, cy + h * 0.1f, cx + w * 0.18f, cy + h * 0.42f)
            quadraticBezierTo(cx + w * 0.1f, cy + h * 0.25f, cx + w * 0.02f, cy + h * 0.3f)
            close()
        }
        drawPath(wingPathRight, color = wingColor)

        withTransform({
            rotate(
                degrees = headTiltDegrees,
                pivot = Offset(cx, cy - h * 0.28f)
            )
        }) {
            drawCircle(
                color = bodyColor,
                radius = w * 0.17f,
                center = Offset(cx, cy - h * 0.28f)
            )

            val beakPath = Path().apply {
                moveTo(cx - w * 0.03f, cy - h * 0.26f)
                lineTo(cx + w * 0.02f, cy - h * 0.3f)
                lineTo(cx - w * 0.03f, cy - h * 0.34f)
                close()
            }
            drawPath(beakPath, color = beakColor)

            val eyeRadius = w * 0.035f
            drawOval(
                color = Color.Black,
                topLeft = Offset(
                    cx - w * 0.06f - eyeRadius,
                    cy - h * 0.3f - eyeRadius * eyeOpenAmount
                ),
                size = Size(
                    eyeRadius * 2f,
                    eyeRadius * 2f * eyeOpenAmount
                )
            )
            drawCircle(
                color = eyeColor,
                radius = eyeRadius * 0.4f * eyeOpenAmount,
                center = Offset(cx - w * 0.06f, cy - h * 0.3f)
            )
        }

        drawLine(
            color = Color(0xFFBFA23A),
            start = Offset(cx - w * 0.08f, cy + h * 0.44f),
            end = Offset(cx - w * 0.1f, cy + h * 0.5f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFFBFA23A),
            start = Offset(cx + w * 0.08f, cy + h * 0.44f),
            end = Offset(cx + w * 0.1f, cy + h * 0.5f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }
}
