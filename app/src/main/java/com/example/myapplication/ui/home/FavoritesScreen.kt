package com.example.myapplication.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.MockData
import com.example.myapplication.model.Housekeeper
import com.example.myapplication.ui.components.HousekeeperCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onHousekeeperClick: (Housekeeper) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()
    val favoriteHousekeepers = MockData.housekeepers.filter { favorites.contains(it.id) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Favorites", fontWeight = FontWeight.Bold) })
        },
        modifier = modifier
    ) { innerPadding ->
        if (favoriteHousekeepers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No favorites yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(favoriteHousekeepers, key = { it.id }) { housekeeper ->
                    HousekeeperCard(
                        housekeeper = housekeeper,
                        isFavorite = true,
                        onFavoriteToggle = { viewModel.toggleFavorite(housekeeper.id) },
                        onClick = { onHousekeeperClick(housekeeper) },
                        index = 0
                    )
                }
            }
        }
    }
}
