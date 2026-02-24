package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.math.absoluteValue

/**
 * Swipeable card with action reveal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableCard(
    modifier: Modifier = Modifier,
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    leftAction: @Composable (() -> Unit)? = null,
    rightAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var offsetX by remember { mutableFloatStateOf(0f) }
    val maxOffset = 150f
    
    Box(modifier = modifier) {
        // Background actions
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left action (revealed on right swipe)
            if (leftAction != null && offsetX > 0) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = (offsetX / maxOffset).coerceIn(0f, 1f)
                            scaleX = (offsetX / maxOffset).coerceIn(0.5f, 1f)
                            scaleY = (offsetX / maxOffset).coerceIn(0.5f, 1f)
                        }
                ) {
                    leftAction()
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            // Right action (revealed on left swipe)
            if (rightAction != null && offsetX < 0) {
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            alpha = (offsetX.absoluteValue / maxOffset).coerceIn(0f, 1f)
                            scaleX = (offsetX.absoluteValue / maxOffset).coerceIn(0.5f, 1f)
                            scaleY = (offsetX.absoluteValue / maxOffset).coerceIn(0.5f, 1f)
                        }
                ) {
                    rightAction()
                }
            }
        }
        
        // Foreground content
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.absoluteValue > maxOffset * 0.6f) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (offsetX > 0) {
                                        onSwipeRight?.invoke()
                                    } else {
                                        onSwipeLeft?.invoke()
                                    }
                                }
                                // Animate back to center
                                animate(
                                    initialValue = offsetX,
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) { value, _ ->
                                    offsetX = value
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                animate(
                                    initialValue = offsetX,
                                    targetValue = 0f,
                                    animationSpec = spring()
                                ) { value, _ ->
                                    offsetX = value
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = newOffset.coerceIn(-maxOffset * 1.2f, maxOffset * 1.2f)
                            
                            // Haptic at threshold
                            if (offsetX.absoluteValue > maxOffset * 0.6f && 
                                (offsetX - dragAmount).absoluteValue <= maxOffset * 0.6f) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    )
                }
        ) {
            content()
        }
    }
}

/**
 * Delete action button for swipe.
 */
@Composable
fun SwipeDeleteAction() {
    Surface(
        color = ErrorRed,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Favorite action button for swipe.
 */
@Composable
fun SwipeFavoriteAction(isFavorite: Boolean = false) {
    Surface(
        color = if (isFavorite) AccentAmber else SuccessGreen,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.size(56.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (isFavorite) Icons.Default.HeartBroken else Icons.Default.Favorite,
                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Staggered animation wrapper for list items.
 */
@Composable
fun StaggeredAnimatedItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = tween(
                durationMillis = 400,
                easing = FastOutSlowInEasing
            )
        ),
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Bounce on press modifier.
 */
@Composable
fun Modifier.bounceClick(
    scaleDown: Float = 0.95f
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )
    
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    isPressed = true
                    tryAwaitRelease()
                    isPressed = false
                }
            )
        }
}

/**
 * Entrance animation for screens.
 */
@Composable
fun ScreenEntranceAnimation(
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(400)
        ) + slideInVertically(
            initialOffsetY = { it / 10 },
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        content = content
    )
}

/**
 * Parallax scroll effect value calculator.
 */
@Composable
fun rememberParallaxScrollState(): ParallaxScrollState {
    return remember { ParallaxScrollState() }
}

class ParallaxScrollState {
    var scrollOffset by mutableFloatStateOf(0f)
    
    fun calculateParallax(ratio: Float = 0.5f): Float {
        return scrollOffset * ratio
    }
}

/**
 * Pulsing badge for notifications.
 */
@Composable
fun PulsingBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    Box(
        modifier = modifier
            .size(24.dp)
            .scale(if (count > 0) scale else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(ErrorRed),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

/**
 * Animated counter text.
 */
@Composable
fun AnimatedCounter(
    count: Int,
    modifier: Modifier = Modifier
) {
    var oldCount by remember { mutableIntStateOf(count) }
    
    SideEffect {
        oldCount = count
    }
    
    Row(modifier = modifier) {
        val countString = count.toString()
        val oldCountString = oldCount.toString()
        
        for (i in countString.indices) {
            val oldChar = oldCountString.getOrNull(i)
            val newChar = countString[i]
            val char = if (oldChar == newChar) oldChar else newChar
            
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { -it } + fadeIn() togetherWith
                        slideOutVertically { it } + fadeOut()
                    } else {
                        slideInVertically { it } + fadeIn() togetherWith
                        slideOutVertically { -it } + fadeOut()
                    }
                },
                label = "counter"
            ) { char ->
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    softWrap = false
                )
            }
        }
    }
}
