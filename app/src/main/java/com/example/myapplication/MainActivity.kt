package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.MockData
import com.example.myapplication.model.Chat
import com.example.myapplication.model.Housekeeper
import com.example.myapplication.ui.*
import com.example.myapplication.ui.theme.MyApplicationTheme

/**
 * Sealed class defining all navigation routes in the app.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Bookings : Screen("bookings")
    data object Messages : Screen("messages")
    data object Favorites : Screen("favorites")
    data object Profile : Screen("profile")
    data object HousekeeperDetail : Screen("housekeeper_detail")
    data object ChatDetail : Screen("chat_detail")
    data object Notifications : Screen("notifications")
    data object Wallet : Screen("wallet")
    data object Addresses : Screen("addresses")
    data object Subscription : Screen("subscription")
    data object Success : Screen("success")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var isLoading = true
        splashScreen.setKeepOnScreenCondition { isLoading }

        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel(factory = MainViewModel.Factory)
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(500)
                isLoading = false
            }

            val useDarkTheme = when (isDarkMode) {
                true -> true
                false -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()
                HouseKeepApp(viewModel, navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseKeepApp(viewModel: MainViewModel, navController: NavHostController) {
    var selectedHousekeeper by remember { mutableStateOf<Housekeeper?>(null) }
    var selectedChat by remember { mutableStateOf<Chat?>(null) }
    var hasCompletedOnboarding by rememberSaveable { mutableStateOf(false) }
    // Holds real data from the last completed booking
    data class BookingSummary(val name: String, val dateTime: String, val hours: Int, val service: String)
    var lastBooking by remember { mutableStateOf<BookingSummary?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainTabRoutes = listOf(
        Screen.Home.route, Screen.Bookings.route,
        Screen.Messages.route, Screen.Favorites.route, Screen.Profile.route
    )
    val isOnMainTab = currentRoute in mainTabRoutes || currentRoute == null

    if (!hasCompletedOnboarding) {
        OnboardingScreen(onFinished = { hasCompletedOnboarding = true })
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isOnMainTab) {
                NavigationBar {
                    AppDestinations.entries.forEach { dest ->
                        NavigationBarItem(
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                            selected = currentRoute == dest.route,
                            onClick = {
                                if (currentRoute != dest.route) {
                                    navController.navigate(dest.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        },
        topBar = {
            // HousekeeperListScreen manages its own collapsing header,
            // so no top bar is needed here for the Home route.
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // ── Main Tabs ──
            composable(Screen.Home.route) {
                HousekeeperListScreen(
                    viewModel = viewModel,
                    onHousekeeperClick = {
                        selectedHousekeeper = it
                        navController.navigate(Screen.HousekeeperDetail.route)
                    }
                )
            }
            composable(Screen.Bookings.route) {
                BookingScreen(
                    viewModel = viewModel,
                    onChat = { housekeeperId ->
                        // Match the housekeeper to a chat by housekeeperId (c1=housekeeper 1, etc.)
                        val chatId = "c$housekeeperId"
                        val matchingChat = MockData.chats.find { it.id == chatId }
                        if (matchingChat != null) {
                            selectedChat = matchingChat
                            navController.navigate(Screen.ChatDetail.route)
                        } else {
                            // Fallback: go to Messages tab
                            navController.navigate(Screen.Messages.route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            composable(Screen.Messages.route) {
                ChatScreen(
                    onChatClick = {
                        selectedChat = it
                        navController.navigate(Screen.ChatDetail.route)
                    }
                )
            }
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = viewModel,
                    onHousekeeperClick = {
                        selectedHousekeeper = it
                        navController.navigate(Screen.HousekeeperDetail.route)
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = viewModel,
                    onWalletClick = { navController.navigate(Screen.Wallet.route) },
                    onAddressesClick = { navController.navigate(Screen.Addresses.route) },
                    onSubscriptionClick = { navController.navigate(Screen.Subscription.route) }
                )
            }

            // ── Detail / Overlay Screens ──
            composable(Screen.HousekeeperDetail.route) {
                selectedHousekeeper?.let { housekeeper ->
                    HousekeeperDetailScreen(
                        housekeeper = housekeeper,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onHire = { dateTime, service, hours ->
                            lastBooking = BookingSummary(housekeeper.name, dateTime, hours, service)
                            navController.navigate(Screen.Success.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
            composable(Screen.ChatDetail.route) {
                selectedChat?.let { chat ->
                    ChatDetailScreen(
                        chat = chat,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
            composable(Screen.Notifications.route) {
                NotificationScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Wallet.route) {
                WalletScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Addresses.route) {
                AddressManagementScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Subscription.route) {
                SubscriptionScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.Success.route) {
                SuccessBookingScreen(
                    housekeeperName = lastBooking?.name ?: "",
                    dateTime = lastBooking?.dateTime ?: "",
                    durationHours = lastBooking?.hours ?: 0,
                    serviceName = lastBooking?.service ?: "",
                    onDismiss = {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                )
            }
        }
    }
}

/**
 * Bottom navigation destinations with their route, label, and icon.
 */
enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    HOME("Explore", Icons.Default.Home, Screen.Home.route),
    BOOKINGS("Bookings", Icons.AutoMirrored.Filled.ListAlt, Screen.Bookings.route),
    MESSAGES("Messages", Icons.Default.ChatBubbleOutline, Screen.Messages.route),
    FAVORITES("Saved", Icons.Default.FavoriteBorder, Screen.Favorites.route),
    PROFILE("Profile", Icons.Default.PersonOutline, Screen.Profile.route),
}
