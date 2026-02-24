package com.example.myapplication.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.data.MockData
import com.example.myapplication.model.Chat
import com.example.myapplication.model.Message
import com.example.myapplication.model.MessageType
import com.example.myapplication.ui.components.TypingIndicator
import com.example.myapplication.ui.theme.*
import kotlinx.coroutines.delay

data class MessageGroup(
    val date: String,
    val messages: List<Message>
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatDetailScreen(
    chat: Chat,
    onBack: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var showTypingIndicator by remember { mutableStateOf(false) }
    var selectedMessageId by remember { mutableStateOf<String?>(null) }
    var showReactionPicker by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    
    // Simulate typing indicator
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            showTypingIndicator = true
            delay(3000)
            showTypingIndicator = false
            delay(10000)
        }
    }
    
    // Group messages by date
    val messageGroups = remember(MockData.messages) {
        listOf(
            MessageGroup("Today", MockData.messages)
        )
    }

    val reactionEmojis = listOf("❤️", "😂", "😮", "😢", "👍", "👎")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* View profile */ }
                    ) {
                        Box {
                            AsyncImage(
                                model = chat.participantImageUrl,
                                contentDescription = chat.participantName,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            // Online indicator
                            if (chat.isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.BottomEnd)
                                        .background(SuccessGreen, CircleShape)
                                        .padding(2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                chat.participantName, 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold
                            )
                            AnimatedContent(
                                targetState = showTypingIndicator,
                                label = "statusAnimation"
                            ) { isTyping ->
                                if (isTyping) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "typing",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        TypingIndicator(
                                            dotSize = 4.dp,
                                            dotColor = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                } else if (chat.isOnline) {
                                    Text(
                                        "Online",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SuccessGreen
                                    )
                                } else {
                                    Text(
                                        "Last seen 2h ago",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Call, contentDescription = "Call")
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column {
                    // Reaction picker
                    AnimatedVisibility(
                        visible = showReactionPicker && selectedMessageId != null,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                reactionEmojis.forEach { emoji ->
                                    Text(
                                        emoji,
                                        modifier = Modifier
                                            .clickable {
                                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                // Add reaction logic here
                                                showReactionPicker = false
                                                selectedMessageId = null
                                            }
                                            .padding(8.dp),
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    // Input area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .navigationBarsPadding()
                            .imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Attachment button
                        IconButton(
                            onClick = { /* Add attachment */ },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Outlined.AttachFile,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Image button
                        IconButton(
                            onClick = { /* Add image */ },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Image,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Text input
                        TextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...") },
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            maxLines = 4
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Send button with animation
                        val sendScale by animateFloatAsState(
                            targetValue = if (messageText.isNotBlank()) 1f else 0.8f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "sendScale"
                        )
                        
                        FloatingActionButton(
                            onClick = { 
                                if (messageText.isNotBlank()) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    messageText = "" 
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .scale(sendScale),
                            shape = CircleShape,
                            containerColor = if (messageText.isNotBlank()) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (messageText.isNotBlank()) 
                                MaterialTheme.colorScheme.onPrimary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = if (messageText.isNotBlank()) 4.dp else 0.dp
                            )
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send, 
                                contentDescription = "Send", 
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            state = listState,
            reverseLayout = false
        ) {
            messageGroups.forEach { group ->
                // Date header
                item {
                    DateDivider(date = group.date)
                }
                
                items(group.messages, key = { it.id }) { message ->
                    EnhancedMessageBubble(
                        message = message,
                        isSelected = selectedMessageId == message.id,
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedMessageId = message.id
                            showReactionPicker = true
                        },
                        onClick = {
                            if (selectedMessageId != null) {
                                selectedMessageId = null
                                showReactionPicker = false
                            }
                        }
                    )
                }
            }
            
            // Typing indicator at bottom
            if (showTypingIndicator) {
                item {
                    TypingBubble()
                }
            }
        }
    }
}

@Composable
fun DateDivider(date: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                date,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedMessageBubble(
    message: Message,
    isSelected: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    val bubbleScale by animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "bubbleScale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .scale(bubbleScale),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        // Message reactions display
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(
                    start = if (message.isFromMe) 0.dp else 8.dp,
                    end = if (message.isFromMe) 8.dp else 0.dp,
                    bottom = 2.dp
                )
            ) {
                message.reactions.forEach { reaction ->
                    Text(reaction, fontSize = 12.sp)
                }
            }
        }
        
        Surface(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            color = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                message.isFromMe -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (message.isFromMe) 18.dp else 4.dp,
                bottomEnd = if (message.isFromMe) 4.dp else 18.dp
            ),
            shadowElevation = if (isSelected) 4.dp else 0.dp
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = message.text,
                    color = when {
                        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                        message.isFromMe -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(Modifier.height(4.dp))
                
                // Timestamp and read receipt
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = message.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            message.isFromMe -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                    
                    // Read receipt for sent messages
                    if (message.isFromMe) {
                        Icon(
                            if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = if (message.isRead) 
                                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else InfoBlue
                            else 
                                if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f) 
                                else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypingBubble() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TypingIndicator(
                    dotSize = 8.dp,
                    dotColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
