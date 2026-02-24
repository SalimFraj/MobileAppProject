package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.model.Booking
import com.example.myapplication.model.BookingStatus
import com.example.myapplication.ui.components.*
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onChat: (housekeeperId: String) -> Unit = {}
) {
    val bookings by viewModel.myBookings.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active", "Completed", "Cancelled")
    val haptics = LocalHapticFeedback.current

    val filteredBookings = remember(bookings, selectedTab) {
        when (selectedTab) {
            0 -> bookings.filter { it.status in listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.ON_WAY, BookingStatus.IN_PROGRESS) }
            1 -> bookings.filter { it.status == BookingStatus.COMPLETED }
            2 -> bookings.filter { it.status == BookingStatus.CANCELLED }
            else -> bookings
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = { 
                        Text("My Bookings", fontWeight = FontWeight.ExtraBold)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                // Tab row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { 
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedTab = index 
                            },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (filteredBookings.isEmpty()) {
            EmptyBookingsState(selectedTab)
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(filteredBookings, key = { _, b -> b.id }) { index, booking ->
                    StaggeredAnimatedItem(index = index) {
                        SwipeableCard(
                            onSwipeLeft = if (booking.status != BookingStatus.CANCELLED) {
                                { viewModel.cancelBooking(booking.id) }
                            } else null,
                            onSwipeRight = if (booking.status == BookingStatus.COMPLETED) {
                                { viewModel.rebookFromHistory(booking) }
                            } else null,
                            leftAction = if (booking.status == BookingStatus.COMPLETED) {
                                { SwipeFavoriteAction(isFavorite = false) }
                            } else null,
                            rightAction = if (booking.status != BookingStatus.CANCELLED) {
                                { SwipeDeleteAction() }
                            } else null
                        ) {
                            EnhancedBookingCard(
                                booking = booking,
                                onRebook = { viewModel.rebookFromHistory(booking) },
                                onRate = { rating -> viewModel.rateBooking(booking.id, rating) },
                                onCancel = { viewModel.cancelBooking(booking.id) },
                                onChat = { onChat(booking.housekeeperId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedBookingCard(
    booking: Booking,
    onRebook: () -> Unit,
    onRate: (Int) -> Unit,
    onCancel: () -> Unit,
    onChat: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with image and info
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = booking.housekeeperImageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.housekeeperName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = booking.dateTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                StatusBadge(status = booking.status)
            }

            // Live Tracking Section for active bookings
            AnimatedVisibility(
                visible = booking.status == BookingStatus.ON_WAY || booking.status == BookingStatus.IN_PROGRESS,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LiveTrackingSection(booking = booking)
            }

            // Timeline for active bookings
            AnimatedVisibility(
                visible = booking.status in listOf(BookingStatus.CONFIRMED, BookingStatus.ON_WAY, BookingStatus.IN_PROGRESS),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                BookingTimeline(status = booking.status)
            }

            // Rating section for completed bookings without rating
            AnimatedVisibility(
                visible = booking.status == BookingStatus.COMPLETED && booking.rating == null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                RatingPrompt(onClick = { showRatingDialog = true })
            }

            // Existing rating display
            booking.rating?.let { rating ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    color = AccentAmberLight.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Your rating: ", style = MaterialTheme.typography.bodyMedium)
                        repeat(rating) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = AccentAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // Price and actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${String.format("%.2f", booking.totalAmount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (booking.discountAmount > 0) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = SuccessGreenLight,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "-$${String.format("%.0f", booking.discountAmount)}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Details button
                    OutlinedButton(
                        onClick = { 
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isExpanded = !isExpanded 
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(if (isExpanded) "Less" else "Details")
                    }
                    
                    // Context-aware action button
                    when (booking.status) {
                        BookingStatus.CONFIRMED, BookingStatus.ON_WAY -> {
                            Button(
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onChat()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.Chat, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Chat")
                            }
                        }
                        BookingStatus.COMPLETED -> {
                            Button(
                                onClick = { 
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onRebook() 
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SecondaryTeal
                                )
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Rebook")
                            }
                        }
                        BookingStatus.PENDING -> {
                            OutlinedButton(
                                onClick = { onCancel() },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ErrorRed
                                )
                            ) {
                                Text("Cancel")
                            }
                        }
                        else -> { }
                    }
                }
            }
            
            // Expanded details
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (booking.services.isNotEmpty()) {
                        DetailRow(
                            icon = Icons.Outlined.CleaningServices,
                            label = "Services",
                            value = booking.services
                        )
                    }
                    if (booking.address.isNotEmpty()) {
                        DetailRow(
                            icon = Icons.Outlined.LocationOn,
                            label = "Address",
                            value = booking.address
                        )
                    }
                    if (booking.notes.isNotEmpty()) {
                        DetailRow(
                            icon = Icons.Outlined.Notes,
                            label = "Notes",
                            value = booking.notes
                        )
                    }
                    if (booking.tipAmount > 0) {
                        DetailRow(
                            icon = Icons.Outlined.Paid,
                            label = "Tip",
                            value = "$${String.format("%.2f", booking.tipAmount)}"
                        )
                    }
                }
            }
        }
    }

    // Rating Dialog
    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating ->
                onRate(rating)
                showRatingDialog = false
            }
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun LiveTrackingSection(booking: Booking) {
    val infiniteTransition = rememberInfiniteTransition(label = "tracking")
    val progress by infiniteTransition.animateFloat(
        initialValue = booking.trackingProgress - 0.05f,
        targetValue = booking.trackingProgress + 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progressPulse"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated progress circle
            Box(
                modifier = Modifier.size(60.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedProgressArc(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 6.dp
                )
                PulseAnimation(
                    size = 12.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (booking.status == BookingStatus.ON_WAY) 
                            Icons.Default.DirectionsRun 
                        else 
                            Icons.Default.CleaningServices,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (booking.status == BookingStatus.ON_WAY) 
                            "Arriving in 12 mins" 
                        else 
                            "Work in progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = if (booking.status == BookingStatus.ON_WAY)
                        "Your housekeeper is on the way"
                    else
                        "Estimated completion: 2 hours",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun BookingTimeline(status: BookingStatus) {
    val steps = listOf("Confirmed", "On the way", "In progress", "Completed")
    val currentStep = when (status) {
        BookingStatus.CONFIRMED -> 0
        BookingStatus.ON_WAY -> 1
        BookingStatus.IN_PROGRESS -> 2
        BookingStatus.COMPLETED -> 3
        else -> -1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            val isCompleted = index <= currentStep
            val isCurrent = index == currentStep
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 28.dp else 24.dp)
                        .background(
                            if (isCompleted) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    step,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCompleted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RatingPrompt(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = AccentAmberLight.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.StarOutline,
                null,
                tint = AccentAmber,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Rate your experience",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Earn 10 bonus points!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RatingDialog(onDismiss: () -> Unit, onSubmit: (Int) -> Unit) {
    var selectedRating by remember { mutableStateOf(0) }
    val haptics = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate your experience", fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "How was your cleaning service?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(5) { index ->
                        val starIndex = index + 1
                        IconButton(
                            onClick = { 
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedRating = starIndex 
                            }
                        ) {
                            Icon(
                                if (starIndex <= selectedRating) Icons.Default.Star else Icons.Default.StarOutline,
                                null,
                                tint = if (starIndex <= selectedRating) AccentAmber else Grey400,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (selectedRating > 0) onSubmit(selectedRating) 
                },
                enabled = selectedRating > 0
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyBookingsState(tabIndex: Int) {
    val (icon, title, subtitle) = when (tabIndex) {
        0 -> Triple(Icons.Default.EventAvailable, "No active bookings", "Your upcoming cleanings will appear here")
        1 -> Triple(Icons.Default.History, "No completed bookings", "Your past cleanings will appear here")
        else -> Triple(Icons.Default.Cancel, "No cancelled bookings", "Good! You haven't cancelled any bookings")
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatusBadge(status: BookingStatus) {
    val (color, label) = when (status) {
        BookingStatus.COMPLETED -> SuccessGreen to "Completed"
        BookingStatus.CONFIRMED -> PrimaryIndigo to "Confirmed"
        BookingStatus.ON_WAY -> InfoBlue to "On the way"
        BookingStatus.IN_PROGRESS -> DeepPurple to "In progress"
        BookingStatus.PENDING -> AccentAmber to "Pending"
        BookingStatus.CANCELLED -> Grey600 to "Cancelled"
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}
