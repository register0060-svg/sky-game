package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.S1Blue
import com.example.ui.theme.S2Purple
import com.example.ui.theme.S3Pink
import kotlin.random.Random

data class Star(
    var x: Float,
    var y: Float,
    val radius: Float,
    var alpha: Float,
    var dAlpha: Float,
    val vy: Float,
    val color: Color
)

@Composable
fun StarsBackground(modifier: Modifier = Modifier) {
    var width by remember { mutableStateOf(0f) }
    var height by remember { mutableStateOf(0f) }
    
    val stars = remember {
        List(80) {
            Star(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 1.5f + 0.5f,
                alpha = Random.nextFloat(),
                dAlpha = (Random.nextFloat() - 0.5f) * 0.05f,
                vy = Random.nextFloat() * 0.5f + 0.1f,
                color = when {
                    Random.nextFloat() > 0.66f -> S2Purple
                    Random.nextFloat() > 0.33f -> S1Blue
                    else -> S3Pink
                }
            )
        }
    }

    var time by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos {
                time = it
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val current = time // read time to animate
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        stars.forEach { star ->
            star.alpha += star.dAlpha
            if (star.alpha <= 0.1f || star.alpha >= 1f) {
                star.dAlpha *= -1f
            }
            star.y += star.vy
            if (star.y > canvasHeight) {
                star.y = 0f
            }
            
            drawCircle(
                color = star.color.copy(alpha = star.alpha.coerceIn(0f, 1f) * 0.6f),
                radius = star.radius,
                center = Offset(star.x * canvasWidth, star.y) // using relative width x initially, then fixed x? 
                // wait, if x is relative, it will scale. Let's just fix it.
            )
            // Wait, we need to correct X. If initialized with random 0..1, multiply by width.
        }
    }
}
