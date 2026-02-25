package com.example.myapplication.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    onApply: (ClosedFloatingPointRange<Float>, Float, List<String>) -> Unit
) {
    var priceRange by remember { mutableStateOf(15f..100f) }
    var minRating by remember { mutableStateOf(4.0f) }
    val selectedServices = remember { mutableStateListOf<String>() }
    
    val allServices = listOf("Standard", "Deep Clean", "Laundry", "Move-in/out", "Pet Friendly", "Eco-Friendly")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter Results",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, null)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Price Range
        Text("Price Range ($/hr)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        RangeSlider(
            value = priceRange,
            onValueChange = { priceRange = it },
            valueRange = 10f..150f,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("$${priceRange.start.toInt()}", style = MaterialTheme.typography.labelLarge)
            Text("$${priceRange.endInclusive.toInt()}", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Minimum Rating
        Text("Minimum Rating", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Slider(
            value = minRating,
            onValueChange = { minRating = it },
            valueRange = 1f..5f,
            steps = 3,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text("${"%.1f".format(minRating)} Stars or higher", style = MaterialTheme.typography.labelLarge)

        Spacer(modifier = Modifier.height(24.dp))

        // Service Types
        Text("Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OptInFlowRow(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allServices.forEach { service ->
                val isSelected = selectedServices.contains(service)
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) selectedServices.remove(service)
                        else selectedServices.add(service)
                    },
                    label = { Text(service) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { 
                onApply(priceRange, minRating, selectedServices.toList())
                onDismiss()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Show Results", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        
        TextButton(
            onClick = {
                priceRange = 15f..100f
                minRating = 4.0f
                selectedServices.clear()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset All", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptInFlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = { content() }
    )
}
