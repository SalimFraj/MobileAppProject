package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.data.MockData
import com.example.myapplication.model.Housekeeper
import com.example.myapplication.model.TimeSlot
import com.example.myapplication.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingCheckoutSheet(
    housekeeper: Housekeeper,
    onConfirm: (String, String, Int) -> Unit,
    onDismiss: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    var selectedDate by remember { mutableStateOf(MockData.nextSevenDays.first()) }
    var selectedTime by remember { mutableStateOf<TimeSlot?>(MockData.timeSlots.first { it.isAvailable }) }
    var hours by remember { mutableStateOf(3) }
    var selectedPaymentMethod by remember { mutableStateOf("Visa •••• 4242") }
    var promoCodeInput by remember { mutableStateOf("") }
    var specialNotes by remember { mutableStateOf("") }
    var selectedTip by remember { mutableStateOf(0.0) }
    var showServicePackages by remember { mutableStateOf(false) }
    
    val appliedPromoCode by viewModel.appliedPromoCode.collectAsState()
    val promoCodeError by viewModel.promoCodeError.collectAsState()
    
    val haptics = LocalHapticFeedback.current
    val dateFormatter = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    
    val basePrice = housekeeper.pricePerHour * hours
    val discount = viewModel.calculateDiscount(basePrice)
    val serviceFee = 5.00
    val total = basePrice - discount + serviceFee + selectedTip
    
    val tipOptions = listOf(0.0, 5.0, 10.0, 15.0, 20.0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Book Session",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null)
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Service Packages Section
        SectionHeader(
            title = "Service Packages",
            icon = Icons.Outlined.LocalOffer,
            trailing = {
                TextButton(onClick = { showServicePackages = !showServicePackages }) {
                    Text(if (showServicePackages) "Hide" else "View All")
                }
            }
        )
        
        AnimatedVisibility(visible = showServicePackages) {
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(MockData.servicePackages) { pkg ->
                    ServicePackageCard(
                        name = pkg.name,
                        description = pkg.description,
                        price = pkg.price,
                        originalPrice = pkg.originalPrice,
                        duration = pkg.durationHours,
                        isPopular = pkg.isPopular,
                        onSelect = { hours = pkg.durationHours }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Schedule Section
        SectionHeader(title = "Select Date", icon = Icons.Outlined.CalendarMonth)
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(MockData.nextSevenDays) { date ->
                val isSelected = selectedDate == date
                val isToday = Calendar.getInstance().time.date == date.date
                
                Surface(
                    onClick = { 
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedDate = date 
                    },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isToday) {
                            Text("Today", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        } else {
                            Text(dateFormatter.format(date).split(", ")[0], style = MaterialTheme.typography.labelSmall)
                        }
                        Text(
                            dateFormatter.format(date).split(", ")[1], 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Time Slots
        SectionHeader(title = "Select Time", icon = Icons.Outlined.Schedule)
        LazyRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(MockData.timeSlots) { slot ->
                val isSelected = selectedTime == slot
                FilterChip(
                    selected = isSelected,
                    onClick = { 
                        if (slot.isAvailable) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTime = slot 
                        }
                    },
                    label = { 
                        Text(
                            slot.time,
                            textDecoration = if (!slot.isAvailable) TextDecoration.LineThrough else null
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    enabled = slot.isAvailable,
                    colors = FilterChipDefaults.filterChipColors(
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Duration Section
        SectionHeader(title = "Duration", icon = Icons.Outlined.Timer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "How long do you need?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { 
                        if (hours > 1) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            hours-- 
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "$hours hrs",
                    modifier = Modifier.padding(horizontal = 20.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { 
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        hours++ 
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Payment Method
        SectionHeader(title = "Payment Method", icon = Icons.Outlined.Payment)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { /* Select payment */ },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.CreditCard, 
                        null, 
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(selectedPaymentMethod, fontWeight = FontWeight.SemiBold)
                    Text("Expires 12/26", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Promo Code with validation
        SectionHeader(title = "Promo Code", icon = Icons.Outlined.LocalOffer)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = promoCodeInput,
                    onValueChange = { 
                        promoCodeInput = it
                        viewModel.clearPromoCode()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter code (try CLEAN20)") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = promoCodeError != null,
                    supportingText = {
                        when {
                            promoCodeError != null -> Text(promoCodeError!!, color = ErrorRed)
                            appliedPromoCode != null -> Text(
                                "✓ ${appliedPromoCode!!.description}",
                                color = SuccessGreen
                            )
                        }
                    },
                    trailingIcon = {
                        if (appliedPromoCode != null) {
                            Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (appliedPromoCode != null) SuccessGreen else MaterialTheme.colorScheme.primary
                    )
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.validatePromoCode(promoCodeInput, basePrice)
                },
                enabled = promoCodeInput.isNotBlank() && appliedPromoCode == null,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Text("Apply")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tip Section
        SectionHeader(title = "Add a Tip", icon = Icons.Outlined.VolunteerActivism)
        Text(
            "Show appreciation for great service",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tipOptions) { tip ->
                val isSelected = selectedTip == tip
                FilterChip(
                    selected = isSelected,
                    onClick = { 
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTip = tip 
                    },
                    label = { 
                        Text(
                            if (tip == 0.0) "No tip" else "$${tip.toInt()}",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (tip > 0) AccentAmber else MaterialTheme.colorScheme.primary,
                        selectedLabelColor = if (tip > 0) Grey900 else Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Special Instructions
        SectionHeader(title = "Special Instructions", icon = Icons.Outlined.Notes)
        OutlinedTextField(
            value = specialNotes,
            onValueChange = { specialNotes = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("Any specific requests or access codes?") },
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Price Breakdown
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Price Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                
                PriceRow("Base Price ($hours hrs × $${housekeeper.pricePerHour.toInt()})", "$${String.format("%.2f", basePrice)}")
                
                AnimatedVisibility(visible = discount > 0) {
                    PriceRow(
                        "Promo Discount (${appliedPromoCode?.code ?: ""})", 
                        "-$${String.format("%.2f", discount)}", 
                        color = SuccessGreen
                    )
                }
                
                PriceRow("Service Fee", "$${String.format("%.2f", serviceFee)}")
                
                AnimatedVisibility(visible = selectedTip > 0) {
                    PriceRow("Tip", "$${String.format("%.2f", selectedTip)}", color = AccentAmber)
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Column(horizontalAlignment = Alignment.End) {
                        if (discount > 0) {
                            Text(
                                "$${String.format("%.2f", basePrice + serviceFee + selectedTip)}",
                                style = MaterialTheme.typography.bodyMedium,
                                textDecoration = TextDecoration.LineThrough,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "$${String.format("%.2f", total)}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { 
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                val dateStr = dateFormatter.format(selectedDate) + " at " + (selectedTime?.time ?: "10:00 AM")
                onConfirm(dateStr, housekeeper.id, hours) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text("Confirm & Pay $${String.format("%.2f", total)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon, 
                null, 
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        trailing?.invoke()
    }
}

@Composable
private fun ServicePackageCard(
    name: String,
    description: String,
    price: Double,
    originalPrice: Double,
    duration: Int,
    isPopular: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPopular) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPopular) 4.dp else 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isPopular) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "POPULAR",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
            Text(name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                description, 
                style = MaterialTheme.typography.bodySmall, 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$${originalPrice.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "$${price.toInt()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text("$duration hours", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PriceRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label, 
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value, 
            style = if (isBold) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
            fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (color != Color.Unspecified) color else if (isBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
