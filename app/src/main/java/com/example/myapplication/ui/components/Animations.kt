package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.ConfettiColors
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Confetti particle data class
 */
data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val rotation: Float,
    val velocityX: Float,
    val velocityY: Float,
    val rotationSpeed: Float
)

/**
 * Confetti animation composable for celebrations
 */
@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    particleCount: Int = 100,
    colors: List<Color> = ConfettiColors,
    durationMillis: Int = 3000,
    onComplete: () -> Unit = {}
) {
    var particles by remember { mutableStateOf(generateConfettiParticles(particleCount, colors)) }
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis, easing = LinearEasing)
        )
        onComplete()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val progress = animationProgress.value
        particles.forEach { particle ->
            val currentY = particle.y + (particle.velocityY * progress * size.height * 1.5f)
            val currentX = particle.x + (particle.velocityX * progress * size.width * 0.3f)
            val currentRotation = particle.rotation + (particle.rotationSpeed * progress * 360f)
            val alpha = 1f - (progress * 0.5f)
            
            if (currentY < size.height * 1.2f) {
                rotate(currentRotation, pivot = Offset(currentX, currentY)) {
                    drawRect(
                        color = particle.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                        topLeft = Offset(currentX - particle.size / 2, currentY - particle.size / 2),
                        size = androidx.compose.ui.geometry.Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}

private fun generateConfettiParticles(count: Int, colors: List<Color>): List<ConfettiParticle> {
    return (0 until count).map {
        ConfettiParticle(
            x = Random.nextFloat(),
            y = Random.nextFloat() * -0.5f,
            size = Random.nextFloat() * 12f + 6f,
            color = colors.random(),
            rotation = Random.nextFloat() * 360f,
            velocityX = (Random.nextFloat() - 0.5f) * 2f,
            velocityY = Random.nextFloat() * 0.5f + 0.5f,
            rotationSpeed = (Random.nextFloat() - 0.5f) * 4f
        )
    }
}

/**
 * Pulse animation for live tracking indicators
 */
@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 12.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier.size(size * 2),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(scale)
                .alpha(alpha)
                .background(color.copy(alpha = 0.3f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size)
                .background(color, CircleShape)
        )
    }
}

/**
 * Typing indicator animation (three bouncing dots)
 */
@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier,
    dotSize: Dp = 8.dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    spacing: Dp = 4.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 0
                -8f at 200
                0f at 400
                0f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot1"
    )
    
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 150
                -8f at 350
                0f at 550
                0f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot2"
    )
    
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                0f at 300
                -8f at 500
                0f at 700
                0f at 1200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(dot1Offset, dot2Offset, dot3Offset).forEach { offset ->
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .offset(y = offset.dp)
                    .background(dotColor, CircleShape)
            )
        }
    }
}

/**
 * Animated checkmark for success states
 */
@Composable
fun AnimatedCheckmark(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF4CAF50),
    size: Dp = 80.dp
) {
    val pathProgress = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scaleAnim.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        pathProgress.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
    }

    Canvas(
        modifier = modifier
            .size(size)
            .scale(scaleAnim.value)
    ) {
        val strokeWidth = size.toPx() * 0.08f
        val progress = pathProgress.value
        
        // Draw circle
        drawCircle(
            color = color.copy(alpha = 0.2f),
            radius = size.toPx() / 2 - strokeWidth / 2
        )
        
        drawCircle(
            color = color,
            radius = size.toPx() / 2 - strokeWidth / 2,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        
        // Draw checkmark
        val checkStart = Offset(size.toPx() * 0.28f, size.toPx() * 0.52f)
        val checkMid = Offset(size.toPx() * 0.44f, size.toPx() * 0.68f)
        val checkEnd = Offset(size.toPx() * 0.72f, size.toPx() * 0.36f)
        
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(checkStart.x, checkStart.y)
            if (progress <= 0.5f) {
                val p = progress * 2
                lineTo(
                    checkStart.x + (checkMid.x - checkStart.x) * p,
                    checkStart.y + (checkMid.y - checkStart.y) * p
                )
            } else {
                lineTo(checkMid.x, checkMid.y)
                val p = (progress - 0.5f) * 2
                lineTo(
                    checkMid.x + (checkEnd.x - checkMid.x) * p,
                    checkMid.y + (checkEnd.y - checkMid.y) * p
                )
            }
        }
        
        drawPath(
            path = path,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}

/**
 * Scale press effect modifier
 */
@Composable
fun Modifier.scaleOnPress(pressed: Boolean): Modifier {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scalePress"
    )
    return this.scale(scale)
}

/**
 * Bounce animation for elements entering view
 */
@Composable
fun rememberBounceAnimation(): Float {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    
    return scale.value
}

/**
 * Progress arc animation for tracking
 */
@Composable
fun AnimatedProgressArc(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "arcProgress"
    )

    Canvas(modifier = modifier) {
        val sweepAngle = 360f * animatedProgress
        val stroke = strokeWidth.toPx()
        
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}
