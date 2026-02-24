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
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.MockData
import com.example.myapplication.model.UserProfileEntity
import com.example.myapplication.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel,
    onWalletClick: () -> Unit,
    onAddressesClick: () -> Unit,
    onSubscriptionClick: () -> Unit
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    var showEditProfileSheet by remember { mutableStateOf(false) }
    var showReferralDialog by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current
    
    val loyaltyPoints = userProfile?.loyaltyPoints ?: 150

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Header with Edit Button
            Box(
                modifier = Modifier
                    .clickable { showEditProfileSheet = true }
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = userProfile?.profileImageUrl?.ifEmpty { "https://i.pravatar.cc/300?u=user123" },
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp),
                    shadowElevation = 4.dp
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.padding(8.dp),
                        tint = Color.White
                    )
                }
            }
            
            Text(
                text = userProfile?.name ?: "Salim Al-Harthy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = userProfile?.email ?: "salim@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loyalty Points Card
            LoyaltyPointsCard(
                points = loyaltyPoints,
                onViewRewards = { /* Navigate to rewards */ }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProfileStat("12", "Cleanings", Icons.Outlined.CleaningServices)
                    ProfileStat("4.9", "Avg Rating", Icons.Outlined.Star)
                    ProfileStat("Gold", "Member", Icons.Outlined.WorkspacePremium)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Referral Card
            ReferralCard(
                referralCode = userProfile?.referralCode ?: "SALIM25",
                onShare = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    clipboardManager.setText(AnnotatedString(userProfile?.referralCode ?: "SALIM25"))
                    showReferralDialog = true
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Premium Membership Banner
            PremiumBanner(onSubscriptionClick = onSubscriptionClick)

            Spacer(modifier = Modifier.height(20.dp))

            // Settings Sections
            ProfileSection("Preferences") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isDarkMode == true) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "Dark Mode", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = if (isDarkMode == true) "On" else "Off",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isDarkMode == true,
                        onCheckedChange = { 
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            viewModel.toggleDarkMode() 
                        }
                    )
                }
                
                ProfileMenuItem(
                    Icons.Outlined.Notifications, 
                    "Notifications",
                    subtitle = "Push, email, SMS"
                )
                ProfileMenuItem(
                    Icons.Outlined.Language, 
                    "Language",
                    subtitle = "English"
                )
            }

            ProfileSection("Account Settings") {
                ProfileMenuItem(
                    Icons.Outlined.Person, 
                    "Personal Information",
                    subtitle = "Name, phone, email",
                    onClick = { showEditProfileSheet = true }
                )
                ProfileMenuItem(
                    Icons.Outlined.AccountBalanceWallet, 
                    "My Wallet",
                    subtitle = "$45.00 balance",
                    onClick = onWalletClick
                )
                ProfileMenuItem(
                    Icons.Outlined.LocationOn, 
                    "Saved Addresses",
                    subtitle = "2 addresses saved",
                    onClick = onAddressesClick
                )
                ProfileMenuItem(
                    Icons.Outlined.CreditCard, 
                    "Payment Methods",
                    subtitle = "Visa, PayPal"
                )
            }

            ProfileSection("Rewards & Referrals") {
                ProfileMenuItem(
                    Icons.Outlined.Star, 
                    "Loyalty Rewards",
                    subtitle = "$loyaltyPoints points available"
                )
                ProfileMenuItem(
                    Icons.Outlined.PersonAdd, 
                    "Refer a Friend",
                    subtitle = "Earn \$25 credit"
                )
                ProfileMenuItem(
                    Icons.Outlined.CardGiftcard, 
                    "Gift Cards",
                    subtitle = "Buy & redeem"
                )
            }

            ProfileSection("Support & Legal") {
                ProfileMenuItem(Icons.AutoMirrored.Filled.Help, "Help Center")
                ProfileMenuItem(Icons.Outlined.Chat, "Chat with Support")
                ProfileMenuItem(Icons.Outlined.PrivacyTip, "Privacy Policy")
                ProfileMenuItem(Icons.Outlined.Description, "Terms of Service")
                ProfileMenuItem(Icons.Outlined.Info, "About HouseKeep", subtitle = "Version 2.0.1")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Logout */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRedLight,
                    contentColor = ErrorRed
                )
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Referral code copied dialog
    if (showReferralDialog) {
        AlertDialog(
            onDismissRequest = { showReferralDialog = false },
            icon = { Icon(Icons.Default.ContentCopy, null, tint = SuccessGreen) },
            title = { Text("Copied!", textAlign = TextAlign.Center) },
            text = { Text("Referral code copied to clipboard. Share it with friends to earn \$25 credit!") },
            confirmButton = {
                Button(onClick = { showReferralDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    // Edit profile bottom sheet
    if (showEditProfileSheet) {
        EditProfileSheet(
            profile = userProfile,
            onDismiss = { showEditProfileSheet = false },
            onSave = { /* Save profile */ showEditProfileSheet = false }
        )
    }
}

@Composable
fun LoyaltyPointsCard(points: Int, onViewRewards: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onViewRewards() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PrimaryIndigo, DeepPurple)
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Loyalty Points",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "$points",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "pts",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        "Earn 25 pts with every booking",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Rewards",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReferralCard(referralCode: String, onShare: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = AccentAmberLight.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CardGiftcard,
                null,
                tint = AccentAmber,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Refer & Earn \$25",
                    fontWeight = FontWeight.Bold,
                    color = Grey900
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Your code: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Grey700
                    )
                    Surface(
                        color = AccentAmber.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            referralCode,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Grey900
                        )
                    }
                }
            }
            FilledTonalButton(
                onClick = onShare,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Share")
            }
        }
    }
}

@Composable
fun PremiumBanner(onSubscriptionClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onSubscriptionClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.WorkspacePremium, 
                null, 
                tint = MaterialTheme.colorScheme.primary, 
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "HouseKeep Plus",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Save 30% on all services • Priority booking",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                null, 
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    profile: UserProfileEntity?,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var email by remember { mutableStateOf(profile?.email ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(Modifier.height(24.dp))
            
            // Profile image edit
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { /* Pick image */ },
                contentAlignment = Alignment.BottomEnd
            ) {
                AsyncImage(
                    model = profile?.profileImageUrl?.ifEmpty { "https://i.pravatar.cc/300?u=user123" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        null,
                        modifier = Modifier.padding(6.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Person, null) }
            )
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Email, null) }
            )
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Phone, null) }
            )
            
            Spacer(Modifier.height(32.dp))
            
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ProfileStat(value: String, label: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(
            text = label, 
            style = MaterialTheme.typography.bodySmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector, 
    title: String, 
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, 
            contentDescription = null, 
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
