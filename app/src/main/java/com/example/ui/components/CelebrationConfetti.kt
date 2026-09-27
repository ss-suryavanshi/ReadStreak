package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ConfettiShape {
    CIRCLE, RECTANGLE, STAR, RIBBON
}

private class Particle(
    val id: Int,
    var x: Float, // normalized 0..1
    var y: Float, // normalized 0..1
    var vx: Float,
    var vy: Float,
    val size: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    val shape: ConfettiShape,
    val color: Color,
    var alpha: Float = 1f,
    val oscillationSpeed: Float,
    val oscillationAmplitude: Float
)

@Composable
fun CelebrationConfetti(
    modifier: Modifier = Modifier,
    particleCount: Int = 64,
    burstFromCenter: Boolean = true
) {
    val particles = remember(particleCount, burstFromCenter) {
        val colorList = arrayOf(
            Color(0xFFFF5722), // Fire Primary Orange
            Color(0xFFFFD700), // Shiny Gold
            Color(0xFF00E676), // Bright Green
            Color(0xFFD500F9), // Vibrant Magenta
            Color(0xFF00E5FF), // Neon Cyan
            Color(0xFFFF4081), // Vibrant Pink
            Color(0xFFFF9100)  // Deep Orange
        )
        val shapes = ConfettiShape.entries
        Array(particleCount) { index ->
            val angle = Random.nextFloat() * 2f * PI.toFloat()
            val speed = if (burstFromCenter) Random.nextFloat() * 1.8f + 0.8f else Random.nextFloat() * 0.8f + 0.3f

            Particle(
                id = index,
                x = if (burstFromCenter) 0.5f + (Random.nextFloat() - 0.5f) * 0.1f else Random.nextFloat(),
                y = if (burstFromCenter) 0.4f + (Random.nextFloat() - 0.5f) * 0.1f else -0.1f - (Random.nextFloat() * 0.3f),
                vx = if (burstFromCenter) cos(angle) * speed * 0.6f else (Random.nextFloat() - 0.5f) * 0.4f,
                vy = if (burstFromCenter) sin(angle) * speed * 0.8f - 0.8f else Random.nextFloat() * 0.8f + 0.4f,
                size = Random.nextFloat() * 14f + 8f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 400f,
                shape = shapes.random(),
                color = colorList.random(),
                oscillationSpeed = Random.nextFloat() * 3f + 1.5f,
                oscillationAmplitude = Random.nextFloat() * 0.003f + 0.001f
            )
        }
    }

    // Reusable unit star path to avoid allocating Path objects inside 60fps draw loop
    val reusableStarPath = remember { createUnitStarPath() }
    var frameTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(particles) {
        val startTimeNanos = System.nanoTime()
        var prevTimeNanos = startTimeNanos
        var isRunning = true

        while (isRunning) {
            withFrameNanos { frameTimeNanos ->
                val dt = ((frameTimeNanos - prevTimeNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                val totalTime = (frameTimeNanos - startTimeNanos) / 1_000_000_000f
                prevTimeNanos = frameTimeNanos

                if (totalTime > 3.5f) {
                    isRunning = false
                    return@withFrameNanos
                }

                val gravity = 1.6f
                val drag = 0.98f
                val fadeAlpha = if (totalTime > 2.0f) {
                    (1f - (totalTime - 2.0f) / 1.5f).coerceIn(0f, 1f)
                } else 1f

                for (p in particles) {
                    p.vx = p.vx * drag + sin(totalTime * p.oscillationSpeed) * p.oscillationAmplitude
                    p.vy = (p.vy + gravity * dt) * drag
                    p.x += p.vx * dt
                    p.y += p.vy * dt
                    p.rotation += p.rotationSpeed * dt
                    p.alpha = fadeAlpha
                }
                frameTick++
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Read frameTick to trigger Canvas redraw each animation frame without allocating new lists
        if (frameTick < 0) return@Canvas
        val w = size.width
        val h = size.height

        for (p in particles) {
            if (p.alpha <= 0f) continue

            val px = p.x * w
            val py = p.y * h

            if (px in -100f..(w + 100f) && py in -100f..(h + 100f)) {
                val drawColor = p.color.copy(alpha = p.alpha)

                when (p.shape) {
                    ConfettiShape.CIRCLE -> {
                        drawCircle(
                            color = drawColor,
                            radius = p.size / 2f,
                            center = Offset(px, py)
                        )
                    }
                    ConfettiShape.RECTANGLE -> {
                        rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                            drawRoundRect(
                                color = drawColor,
                                topLeft = Offset(px - p.size / 2f, py - p.size / 4f),
                                size = Size(p.size, p.size / 2f),
                                cornerRadius = CornerRadius(2f, 2f)
                            )
                        }
                    }
                    ConfettiShape.RIBBON -> {
                        rotate(degrees = p.rotation, pivot = Offset(px, py)) {
                            drawRoundRect(
                                color = drawColor,
                                topLeft = Offset(px - p.size / 4f, py - p.size),
                                size = Size(p.size / 2f, p.size * 2f),
                                cornerRadius = CornerRadius(3f, 3f)
                            )
                        }
                    }
                    ConfettiShape.STAR -> {
                        val scaleFactor = p.size / 1.2f
                        withTransform({
                            translate(left = px, top = py)
                            rotate(degrees = p.rotation, pivot = Offset.Zero)
                            scale(scaleX = scaleFactor, scaleY = scaleFactor, pivot = Offset.Zero)
                        }) {
                            drawPath(path = reusableStarPath, color = drawColor)
                        }
                    }
                }
            }
        }
    }
}

private fun createUnitStarPath(): Path {
    val path = Path()
    val outerRadius = 1f
    val innerRadius = 0.4f
    val points = 5
    var angle = -PI / 2.0
    val angleStep = PI / points

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = (r * cos(angle)).toFloat()
        val y = (r * sin(angle)).toFloat()

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
        angle += angleStep
    }
    path.close()
    return path
}
