package com.example.myapplication.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                WalletCard()
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transaction History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { }) { Text("View All") }
                }
            }
            
            items(sampleTransactions) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
fun WalletCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                Text("Total Balance", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelLarge)
                Text("$1,240.50", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.ExtraBold)
            }
            
            Row(
                modifier = Modifier.align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WalletAction(Icons.Default.Add, "Top Up")
                WalletAction(Icons.Default.Send, "Send")
            }
            
            Icon(
                Icons.Default.Payments,
                null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(80.dp),
                tint = Color.White.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun WalletAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(44.dp),
            color = Color.White.copy(alpha = 0.2f),
            shape = CircleShape,
            onClick = {}
        ) {
            Icon(icon, null, modifier = Modifier.padding(10.dp), tint = Color.White)
        }
        Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(48.dp),
            color = if (transaction.isIncome) Color(0xFF4CAF50).copy(alpha = 0.1f) else Color(0xFFE91E63).copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                if (transaction.isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                null,
                modifier = Modifier.padding(12.dp),
                tint = if (transaction.isIncome) Color(0xFF4CAF50) else Color(0xFFE91E63)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(transaction.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Text(
            text = "${if (transaction.isIncome) "+" else "-"}$${transaction.amount}",
            fontWeight = FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium,
            color = if (transaction.isIncome) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface
        )
    }
}

data class Transaction(val title: String, val date: String, val amount: Double, val isIncome: Boolean)

val sampleTransactions = listOf(
    Transaction("Alice Johnson - Deep Clean", "Oct 12, 2023", 75.0, false),
    Transaction("Top Up - Credit Card", "Oct 10, 2023", 200.0, true),
    Transaction("Carol White - Standard Clean", "Oct 05, 2023", 45.0, false),
    Transaction("Refund - David Brown", "Oct 01, 2023", 18.0, true)
)
