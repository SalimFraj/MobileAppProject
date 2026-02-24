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
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.MockData
import com.example.myapplication.model.Housekeeper
import com.example.myapplication.ui.components.HousekeeperCard
import com.example.myapplication.ui.components.HousekeeperListSkeleton
import com.example.myapplication.ui.components.StaggeredAnimatedItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousekeeperListScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onHousekeeperClick: (Housekeeper) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val filteredList by viewModel.filteredHousekeepers.collectAsState(initial = emptyList())
    val favorites by viewModel.favorites.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var isMapView by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()

    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(1500)
            isRefreshing = false
        }
    }

    // Track scroll for collapsing header
    var isHeaderExpanded by remember { mutableStateOf(true) }
    val headerHeight by animateDpAsState(
        targetValue = if (isHeaderExpanded) 180.dp else 60.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "headerHeight"
    )
    val headerAlpha by animateFloatAsState(
        targetValue = if (isHeaderExpanded) 1f else 0f,
        animationSpec = tween(200),
        label = "headerAlpha"
    )
    
    // Nested scroll connection to detect scroll direction
    val nestedScrollConnection = remember {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(
                available: androidx.compose.ui.geometry.Offset,
                source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
            ): androidx.compose.ui.geometry.Offset {
                // Collapse when scrolling down, expand when scrolling up
                if (available.y < -10) {
                    isHeaderExpanded = false
                } else if (available.y > 10) {
                    isHeaderExpanded = true
                }
                return androidx.compose.ui.geometry.Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = if (isHeaderExpanded) 0.dp else 4.dp
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    // Collapsible header section
                    AnimatedVisibility(
                        visible = isHeaderExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "HouseKeep",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Downtown, Los Angeles",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = { 
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        isMapView = !isMapView 
                                    },
                                    modifier = Modifier.background(
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        CircleShape
                                    )
                                ) {
                                    Icon(
                                        if (isMapView) Icons.Default.List else Icons.Default.Map,
                                        contentDescription = "Toggle View",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    SearchBar(
                                        query = searchQuery,
                                        onQueryChange = viewModel::onSearchQueryChange
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                IconButton(
                                    onClick = { 
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showFilterSheet = true 
                                    },
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                        .size(48.dp)
                                ) {
                                    Icon(Icons.Default.Tune, null, tint = Color.White)
                                }
                            }
                        }
                    }
                    
                    // Always visible: Category tabs (compact when collapsed)
                    CategoryTabs(
                        selectedCategory = selectedCategory,
                        onCategorySelected = viewModel::onCategoryChange
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(targetState = isMapView, label = "ViewTransition") { mapping ->
            if (mapping) {
                SimulatedMapView(innerPadding, onHousekeeperClick)
            } else {
                PullToRefreshBox(
                    state = refreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    if (isLoading && !isRefreshing) {
                        HousekeeperListSkeleton(
                            itemCount = 3,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else if (filteredList.isEmpty()) {
                        EmptySearchResults()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            item {
                                PromoBanner()
                            }
                            itemsIndexed(filteredList, key = { _, h -> h.id }) { index, housekeeper ->
                                StaggeredAnimatedItem(index = index) {
                                    HousekeeperCard(
                                        housekeeper = housekeeper,
                                        isFavorite = favorites.contains(housekeeper.id),
                                        onFavoriteToggle = { 
                                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.toggleFavorite(housekeeper.id) 
                                        },
                                        onClick = { onHousekeeperClick(housekeeper) },
                                        index = index
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            FilterBottomSheet(
                onDismiss = { showFilterSheet = false },
                onApply = { price, rating, services ->
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.applyFilters(price, rating, services)
                }
            )
        }
    }
}

@Composable
fun EmptySearchResults() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))
            Text("No helpers found", style = MaterialTheme.typography.titleMedium)
            Text("Try adjusting your filters or search query.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PromoBanner() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "HOUSEKEEP PLUS",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Join the Club",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Save up to 30% on every booking with our premium membership.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                Icons.Default.AutoAwesome,
                null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SimulatedMapView(padding: PaddingValues, onHousekeeperClick: (Housekeeper) -> Unit) {
    var selectedInMap by remember { mutableStateOf<Housekeeper?>(null) }
    
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Explore,
                null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Interactive Map Experience",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Interactive Simulated Markers
        val offsets = listOf(-80 to -100, 60 to -40, -40 to 120, 100 to 100, -120 to 20)
        MockData.housekeepers.take(offsets.size).forEachIndexed { i, h ->
            val (x, y) = offsets[i]
            val isSelected = selectedInMap?.id == h.id
            
            Box(
                modifier = Modifier
                    .offset(x = x.dp, y = y.dp)
                    .size(if (isSelected) 64.dp else 48.dp)
                    .shadow(8.dp, CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White, CircleShape)
                    .padding(4.dp)
                    .clickable { selectedInMap = h }
            ) {
                AsyncImage(
                    model = h.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Mini Card for selection
        AnimatedVisibility(
            visible = selectedInMap != null,
            modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            selectedInMap?.let { h ->
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = h.imageUrl,
                            null,
                            modifier = Modifier.size(76.dp).clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(h.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("$${h.pricePerHour.toInt()}/hr • ${h.rating} ★", style = MaterialTheme.typography.bodySmall)
                            Button(
                                onClick = { onHousekeeperClick(h) },
                                modifier = Modifier.height(32.dp).padding(top = 4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text("View Profile", fontSize = 10.sp)
                            }
                        }
                        IconButton(onClick = { selectedInMap = null }) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Search for names...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    }
}

@Composable
fun CategoryTabs(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val haptics = LocalHapticFeedback.current
    LazyRow(
        modifier = Modifier.padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(MockData.categories) { category ->
            val isSelected = selectedCategory == category
            Surface(
                onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCategorySelected(category) 
                },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
