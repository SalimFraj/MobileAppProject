package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.model.Housekeeper
import com.example.myapplication.ui.theme.*

@Composable
fun HousekeeperCard(
    housekeeper: Housekeeper,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    index: Int = 0,
    animateEntrance: Boolean = true
) {
    val animatedProgress = remember { Animatable(0f) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current
    
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pressScale"
    )
    
    if (animateEntrance) {
        LaunchedEffect(Unit) {
            animatedProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(600, delayMillis = index * 80, easing = FastOutSlowInEasing)
            )
        }
    } else {
        LaunchedEffect(Unit) { animatedProgress.snapTo(1f) }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .graphicsLayer {
                alpha = animatedProgress.value
                translationY = (1f - animatedProgress.value) * 50f
                scaleX = pressScale
                scaleY = pressScale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { 
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick() 
            }
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp,
                pressedElevation = 8.dp
            )
        ) {
            Column {
                Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                    // Main image
                    AsyncImage(
                        model = housekeeper.imageUrl,
                        contentDescription = housekeeper.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp)),
                        contentScale = ContentScale.Crop
                    )
                    
                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0.0f to Color.Black.copy(alpha = 0.1f),
                                    0.5f to Color.Transparent,
                                    0.85f to Color.Black.copy(alpha = 0.7f)
                                )
                            )
                    )

                    // Top row - Badges and Favorite
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Badges column
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Rating badge with glassmorphism
                            Surface(
                                color = Color.Black.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.shadow(4.dp, RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star, 
                                        null, 
                                        tint = BadgeTopRated, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "${housekeeper.rating}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = " (${housekeeper.reviewCount})",
                                        color = Color.White.copy(alpha = 0.8f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                            
                            // Feature badges
                            if (housekeeper.badges.isNotEmpty()) {
                                housekeeper.badges.take(2).forEach { badge ->
                                    BadgeChip(badge)
                                }
                            }
                        }

                        // Favorite button with glassmorphism
                        FavoriteButton(
                            isFavorite = isFavorite,
                            onClick = onFavoriteToggle
                        )
                    }

                    // Bottom info overlay
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                housekeeper.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (housekeeper.isVerified) {
                                Spacer(Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    null,
                                    tint = BadgeVerified,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Text(
                            housekeeper.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Price tag
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 4.dp
                    ) {
                        Text(
                            text = "$${housekeeper.pricePerHour.toInt()}/hr",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }

                // Bottom info section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stats
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatItem(
                            value = "${housekeeper.completedJobs}",
                            label = "jobs"
                        )
                        StatItem(
                            value = "${housekeeper.experienceYears}y",
                            label = "exp"
                        )
                        StatItem(
                            value = housekeeper.responseTime,
                            label = "response"
                        )
                    }
                    
                    // Services preview
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        housekeeper.services.take(2).forEach { service ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    service,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeChip(badge: String) {
    val (color, textColor) = when {
        badge.contains("Top", ignoreCase = true) -> BadgeTopRated to Grey900
        badge.contains("Premium", ignoreCase = true) -> BadgePremium to Color.White
        badge.contains("Eco", ignoreCase = true) -> SuccessGreen to Color.White
        badge.contains("Quick", ignoreCase = true) -> BadgeQuickResponder to Color.White
        else -> MaterialTheme.colorScheme.secondary to Color.White
    }
    
    Surface(
        color = color.copy(alpha = 0.9f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = badge,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "favoriteScale"
    )
    
    IconButton(
        onClick = { 
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick() 
        },
        modifier = Modifier
            .size(44.dp)
            .scale(scale)
            .background(
                color = Color.Black.copy(alpha = 0.35f),
                shape = CircleShape
            )
            .shadow(4.dp, CircleShape)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) ErrorRed else Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
