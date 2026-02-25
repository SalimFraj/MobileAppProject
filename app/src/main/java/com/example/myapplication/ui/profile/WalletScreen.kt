package com.example.myapplication.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.BookingStatus
import com.example.myapplication.ui.booking.BookingViewModel
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    profileViewModel: ProfileViewModel,
    bookingViewModel: BookingViewModel,
    onBack: () -> Unit
) {
    val userProfile by profileViewModel.userProfile.collectAsState()
    val bookings by bookingViewModel.myBookings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Derive wallet balance from loyalty points (1 point = $0.30 value)
    val walletBalance = (userProfile?.loyaltyPoints ?: 0) * 0.30

    // Derive recent transactions from completed/cancelled bookings
    val recentTransactions = remember(bookings) {
        bookings
            .filter { it.status == BookingStatus.COMPLETED || it.status == BookingStatus.CANCELLED }
            .sortedByDescending { it.dateTime }
            .take(10)
            .map { booking ->
                WalletTransaction(
                    id = booking.id,
                    title = booking.housekeeperName,
                    subtitle = booking.dateTime,
                    amount = if (booking.status == BookingStatus.COMPLETED) -booking.totalAmount else booking.totalAmount,
                    type = if (booking.status == BookingStatus.COMPLETED) TransactionType.PAYMENT else TransactionType.REFUND
                )
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Wallet Balance Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                Modifier.background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(PrimaryIndigo, DeepPurple)
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Available Balance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "$${"%,.2f".format(walletBalance)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    "${userProfile?.loyaltyPoints ?: 0} loyalty points",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                FilledTonalButton(
                                    onClick = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Top up coming soon")
                                        }
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Top Up")
                                }
                                FilledTonalButton(
                                    onClick = {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Send coming soon")
                                        }
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = Color.White.copy(alpha = 0.2f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Send")
                                }
                            }
                        }
                    }
                }
            }

            // Recent Transactions header
            item {
                Text(
                    "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.ReceiptLong,
                                null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No transactions yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Complete a booking to see activity here",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(recentTransactions, key = { it.id }) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }
    }
}

enum class TransactionType { PAYMENT, REFUND }

data class WalletTransaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val type: TransactionType
)

@Composable
private fun TransactionItem(transaction: WalletTransaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            color = when (transaction.type) {
                TransactionType.PAYMENT -> ErrorRedLight
                TransactionType.REFUND -> SuccessGreenLight
            },
            shape = CircleShape
        ) {
            Icon(
                imageVector = when (transaction.type) {
                    TransactionType.PAYMENT -> Icons.Default.ArrowUpward
                    TransactionType.REFUND -> Icons.Default.ArrowDownward
                },
                contentDescription = if (transaction.type == TransactionType.PAYMENT) "Payment" else "Refund",
                modifier = Modifier.padding(10.dp),
                tint = when (transaction.type) {
                    TransactionType.PAYMENT -> ErrorRed
                    TransactionType.REFUND -> SuccessGreen
                }
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                transaction.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = if (transaction.amount < 0) "-$${"%,.2f".format(-transaction.amount)}"
                   else "+$${"%,.2f".format(transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = when (transaction.type) {
                TransactionType.PAYMENT -> ErrorRed
                TransactionType.REFUND -> SuccessGreen
            }
        )
    }
}
