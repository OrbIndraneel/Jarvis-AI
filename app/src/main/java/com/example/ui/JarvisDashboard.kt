package com.example.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CalendarEvent
import com.example.data.JarvisLog
import com.example.data.SmartDevice
import com.example.data.JarvisTask
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JarvisDashboard(viewModel: JarvisViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    // Permissions launchers
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceListening(context)
        } else {
            Toast.makeText(context, "Microphone calibration is required for voice commands, Sir.", Toast.LENGTH_SHORT).show()
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Toast.makeText(context, if (isGranted) "Storage access synchronized!" else "Defaulting to Stark encrypted secure directories.", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        containerColor = TechDarkBg,
        topBar = {
            JarvisMainHeader(viewModel)
        },
        bottomBar = {
            JarvisBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background futuristic grid canvas drawing
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridAlpha = 0.05f
                val step = 60.dp.toPx()
                
                // Draw vertical grid lines
                var x = 0f
                while (x < size.width) {
                    drawLine(
                        color = CyanGlow,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        alpha = gridAlpha,
                        strokeWidth = 1f
                    )
                    x += step
                }
                // Draw horizontal grid lines
                var y = 0f
                while (y < size.height) {
                    drawLine(
                        color = CyanGlow,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        alpha = gridAlpha,
                        strokeWidth = 1f
                    )
                    y += step
                }
            }

            // Cross-Platform Sync animation indicator
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(0.05f))
                    .border(1.dp, CyanGlow.copy(0.3f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = "Sync",
                    tint = CyanGlow,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SYNCED",
                    fontSize = 8.sp,
                    color = CyanGlow,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    0 -> ConsoleTabContent(viewModel, onMicClick = {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    })
                    1 -> SmartHomeTabContent(viewModel)
                    2 -> SchedulerTabContent(viewModel)
                    3 -> UtilitiesTabContent(viewModel, onGrantStorage = {
                        storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    })
                }
            }
        }
    }
}

// ---- Custom M3 Header (Geometric Balance Style) ----
@Composable
fun JarvisMainHeader(viewModel: JarvisViewModel) {
    val isOnline = !viewModel.offlineModeOnly
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color(0xFF1A1C1E))
            .drawBehind {
                drawLine(
                    color = Color(0xFF44474E).copy(0.4f),
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3F4759)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User profile",
                        tint = Color(0xFFD0E4FF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = if (isOnline) "SYSTEM ACTIVE" else "OFFLINE MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) TerminalGreen else WarningAmber,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Welcome back, Tony",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE2E2E6)
                    )
                }
            }

            // High-grade control button
            IconButton(
                onClick = { viewModel.toggleOverlayHUD() },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2F3033))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Overlay Control Center",
                    tint = Color(0xFFD0E4FF),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ---- Bottom Navigation Bar (Geometric Balance Style) ----
@Composable
fun JarvisBottomNavigation(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF1A1C1E),
        tonalElevation = 0.dp,
        modifier = Modifier
            .drawBehind {
                drawLine(
                    color = Color(0xFF44474E).copy(0.4f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text("Console", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Terminal, contentDescription = "Console", modifier = Modifier.size(20.dp)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0E4FF),
                selectedTextColor = Color(0xFFD0E4FF),
                indicatorColor = Color.Transparent, // Pure transparent clean active state
                unselectedIconColor = Color(0xFFC2C6CF).copy(0.5f),
                unselectedTextColor = Color(0xFFC2C6CF).copy(0.5f)
            ),
            modifier = Modifier.testTag("console_nav_tab")
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text("Automation", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.HomeWork, contentDescription = "Home", modifier = Modifier.size(20.dp)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0E4FF),
                selectedTextColor = Color(0xFFD0E4FF),
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color(0xFFC2C6CF).copy(0.5f),
                unselectedTextColor = Color(0xFFC2C6CF).copy(0.5f)
            ),
            modifier = Modifier.testTag("automation_nav_tab")
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            label = { Text("Scheduler", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule", modifier = Modifier.size(20.dp)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0E4FF),
                selectedTextColor = Color(0xFFD0E4FF),
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color(0xFFC2C6CF).copy(0.5f),
                unselectedTextColor = Color(0xFFC2C6CF).copy(0.5f)
            ),
            modifier = Modifier.testTag("schedule_nav_tab")
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            label = { Text("Sector Util", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Engineering, contentDescription = "Utilities", modifier = Modifier.size(20.dp)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFD0E4FF),
                selectedTextColor = Color(0xFFD0E4FF),
                indicatorColor = Color.Transparent,
                unselectedIconColor = Color(0xFFC2C6CF).copy(0.5f),
                unselectedTextColor = Color(0xFFC2C6CF).copy(0.5f)
            ),
            modifier = Modifier.testTag("utilities_nav_tab")
        )
    }
}

// ==========================================
//   TAB 0: CONSOLE & VOICE CONTROLLER
// ==========================================
@Composable
fun ConsoleTabContent(viewModel: JarvisViewModel, onMicClick: () -> Unit) {
    val logsList by viewModel.logs.collectAsStateWithLifecycle()
    var rawTextQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    // Email drafting expansion variables
    var showDraftSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Upper Segment: Breathing Canvas ARC reactor (Geometric)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.44f),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                JarvisArcReactor(
                    isSpeaking = viewModel.isSpeakingActive,
                    isListening = viewModel.isListening,
                    onClick = onMicClick
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Text(
                    text = if (viewModel.isListening) "SPEECH PROTOCOL ENGAGED" else "TAP REACTOR TO COMMUNICATE",
                    color = if (viewModel.isListening) TerminalGreen else Color(0xFFC2C6CF).copy(0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp
                )
                if (viewModel.isListening) {
                    Text(
                        text = viewModel.recognizedTextState,
                        color = Color.White,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Light,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                    )
                }
            }
        }

        // Middle Segment: Terminal Logs Window Console (Geometric Balance Slate Panel)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f)
                .border(BorderStroke(1.dp, Color(0xFF44474E).copy(0.4f)), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2F3033))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Console bar header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CyanGlow)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DIAGNOSTIC TERMINAL LOG",
                            color = Color(0xFFD0E4FF),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Row {
                        // Email Compositor Shortcut
                        IconButton(
                            onClick = { showDraftSheet = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MarkEmailRead,
                                contentDescription = "Draft Email",
                                tint = Color(0xFFD0E4FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.quickClearConsole() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Console",
                                tint = Color(0xFFFF5252).copy(0.81f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Divider(color = Color(0xFF44474E).copy(0.3f))
                Spacer(modifier = Modifier.height(6.dp))

                if (logsList.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Terminal is idle, Sir. Standing by for command uploads.",
                            color = Color(0xFFC2C6CF).copy(0.4f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        reverseLayout = true
                    ) {
                        items(logsList) { log ->
                            TerminalRowItem(log)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Interaction Suggestion Chips Row (Geometric Balance suggestion pills)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            if (viewModel.autocompleteSuggestion.isNotEmpty()) {
                item {
                    val combinedText = rawTextQuery + viewModel.autocompleteSuggestion
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CyanGlow.copy(0.12f))
                            .border(1.dp, CyanGlow, CircleShape)
                            .clickable {
                                rawTextQuery = combinedText
                                viewModel.onQueryChanged(combinedText)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("pill_autocomplete")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FlashOn, contentDescription = "Auto", tint = CyanGlow, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "TAB TO COMPLETE: \"$combinedText\"",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = CyanGlow
                            )
                        }
                    }
                }
            }
            items(listOf("Draft email", "Home scene: Movie", "System status")) { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF44474E))
                        .clickable {
                            if (suggestion == "Draft email") {
                                showDraftSheet = true
                            } else {
                                viewModel.processCommand(suggestion)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFD0E4FF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Lower Segment: Integrated Capsule Console Controller Bar (Geometric Balance Pill Capsule)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(CircleShape)
                .background(Color(0xFF2F3033))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Integrated Voice Micro Button
            IconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (viewModel.isListening) Color(0xFFFF5252) else Color(0xFFD0E4FF))
            ) {
                Icon(
                    imageVector = if (viewModel.isListening) Icons.Default.Hearing else Icons.Default.Mic,
                    contentDescription = "Tap status indicator microphone",
                    tint = Color(0xFF1A1C1E),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Integrated borderless terminal input
            TextField(
                value = rawTextQuery,
                onValueChange = { 
                    rawTextQuery = it
                    viewModel.onQueryChanged(it)
                },
                placeholder = { Text("Instruct Jarvis...", fontSize = 13.sp, color = Color(0xFFC2C6CF).copy(0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color(0xFFE2E2E6),
                    unfocusedTextColor = Color(0xFFE2E2E6)
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                modifier = Modifier
                    .weight(1f)
                    .testTag("cmd_input_shell"),
                trailingIcon = {
                    if (rawTextQuery.isNotEmpty()) {
                        IconButton(onClick = { rawTextQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "clear field", tint = Color(0xFFC2C6CF).copy(0.6f), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.width(4.dp))

            // Right Send button
            IconButton(
                onClick = {
                    if (rawTextQuery.trim().isNotEmpty()) {
                        viewModel.processCommand(rawTextQuery)
                        rawTextQuery = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF44474E))
                    .testTag("cmd_btn_launch")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send Command",
                    tint = Color(0xFFD0E4FF),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // --- Dialog Sheet: Holographic Email Drafter ---
    if (showDraftSheet) {
        var emailTopic by remember { mutableStateOf("") }
        var emailTone by remember { mutableStateOf("Respectful Butler") } // Respectful Butler, Sarcastic Companion, Tactical HUD
        var recipientUser by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDraftSheet = false },
            containerColor = Color(0xFF0D121B),
            title = {
                Text(
                    "EMAIL TACTICAL TRANSMISSION COMPOSER",
                    color = CyanGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Leverage J.A.R.V.I.S. cognitive systems to auto-compile high-grade written matrices.",
                        color = Color.White.copy(0.6f),
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = recipientUser,
                        onValueChange = { recipientUser = it },
                        label = { Text("Recipient (e.g. Pepper Potts)", color = Color.White.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = CyanGlow.copy(0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emailTopic,
                        onValueChange = { emailTopic = it },
                        label = { Text("Core Directive / Topic of Email", color = Color.White.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = CyanGlow.copy(0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tone selector
                    Text("BEHAVIORAL FREQUENCY TONE", color = CyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Respectful Butler", "Snarky Companion", "Tactical HUD").forEach { toneOption ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (emailTone == toneOption) CyanGlow else Color.White.copy(0.04f))
                                    .border(1.dp, CyanGlow.copy(0.3f), RoundedCornerShape(8.dp))
                                    .clickable { emailTone = toneOption }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = toneOption.split(" ").first(),
                                    color = if (emailTone == toneOption) TechDarkBg else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailTopic.isNotEmpty()) {
                            val prompt = "Compose a beautiful structural email draft to $recipientUser about: $emailTopic. Style: $emailTone."
                            viewModel.processCommand(prompt)
                            showDraftSheet = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow)
                ) {
                    Text("GENERATE TRANSMISSION", color = TechDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDraftSheet = false }) {
                    Text("CANCEL", color = Color.White.copy(0.5f), fontSize = 11.sp)
                }
            }
        )
    }
}

// ---- Glowing pulsing rotation ARC Reactor (Geometric Balance Style) ----
@Composable
fun JarvisArcReactor(isSpeaking: Boolean, isListening: Boolean, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "reactor_anim")
    
    // Constant slow rotate
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Dynamic speak breathing
    val pulseStrength by infiniteTransition.animateFloat(
        initialValue = if (isSpeaking) 0.88f else if (isListening) 0.94f else 0.97f,
        targetValue = if (isSpeaking) 1.15f else if (isListening) 1.08f else 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isSpeaking) 450 else if (isListening) 750 else 2200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreColor = if (isListening) Color(0xFFFF5252) else Color(0xFFD0E4FF)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(190.dp)
            .clickable(onClick = onClick)
    ) {
        // 1. Outer Ring (dashed edge lines)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.dp.toPx()
            drawCircle(
                color = Color(0xFF44474E),
                radius = (size.width - strokeWidth) / 2f,
                style = Stroke(
                    width = strokeWidth,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(12f, 12f), 0f
                    )
                )
            )
        }

        // 2. Mid Ring (80% size circle with horizontal/vertical hair-crosshairs)
        Box(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .clip(CircleShape)
                .border(1.dp, coreColor.copy(0.18f), CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val midX = size.width / 2f
                val midY = size.height / 2f
                // Horizontal crossline
                drawLine(
                    color = coreColor.copy(0.1f),
                    start = Offset(0f, midY),
                    end = Offset(size.width, midY),
                    strokeWidth = 1.dp.toPx()
                )
                // Vertical crossline
                drawLine(
                    color = coreColor.copy(0.1f),
                    start = Offset(midX, 0f),
                    end = Offset(midX, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // 3. Inner Glowing Core (filled with ambient radial aura gradient and white-hot spark)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(105.dp)
                .scale(pulseStrength)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            coreColor,
                            coreColor.copy(0.35f),
                            Color.Transparent
                        )
                    )
                )
                .background(coreColor) // Pure flat filled solid core overlay background
        ) {
            // Concentric dark container styled with border highlights
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1A1C1E))
                    .border(2.dp, coreColor, CircleShape)
                    .rotate(if (isSpeaking || isListening) rotationAngle else 0f)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Hearing else Icons.Default.BlurOn,
                    contentDescription = "Core Active Unit",
                    tint = coreColor,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

// ---- Terminal Log Row Item ----
@Composable
fun TerminalRowItem(log: JarvisLog) {
    val dateText = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (log.isUser) "[USER COMMAND @ $dateText]" else "[JARVIS LOG @ $dateText]",
                color = if (log.isUser) WarningAmber.copy(0.7f) else CyanGlow.copy(0.7f),
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = log.query,
            color = Color.White.copy(0.9f),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 6.dp, top = 1.dp)
        )

        Text(
            text = log.reply,
            color = if (log.reply.startsWith("Core synchroni") || log.reply.startsWith("Excuse me")) Color(0xFFFF5252) else TerminalGreen,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 4.dp)
        )
        Divider(color = Color.White.copy(0.04f), thickness = 0.5.dp)
    }
}

private val SineToLinearEasing = Easing { fraction ->
    fraction
}

// ==========================================
//   TAB 1: SMART HOME AUTOMATION
// ==========================================
@Composable
fun SmartHomeTabContent(viewModel: JarvisViewModel) {
    val devicesList by viewModel.devices.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "AUXILIARY SMART ENVIRONMENT TELEMETRY",
            color = CyanGlow,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (devicesList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CyanGlow)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(devicesList) { device ->
                    SmartDeviceRowItem(device = device, onToggle = {
                        viewModel.toggleDevice(device.deviceId)
                    })
                }
            }
        }
    }
}

@Composable
fun SmartDeviceRowItem(device: SmartDevice, onToggle: () -> Unit) {
    val isActive = device.status == "ON" || device.status == "UNLOCKED"
    val progressColor = if (isActive) TerminalGreen else Color.White.copy(0.4f)
    
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2F3033)),
        border = BorderStroke(1.dp, if (isActive) Color(0xFFD0E4FF).copy(0.4f) else Color(0xFF44474E).copy(0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.7f)
            ) {
                // Device specific icons
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) Color(0xFFD0E4FF).copy(0.15f) else Color(0xFF44474E).copy(0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    val icon = when (device.deviceType) {
                        "LIGHT" -> Icons.Default.Lightbulb
                        "LOCK" -> if (isActive) Icons.Default.LockOpen else Icons.Default.Lock
                        "THERMOSTAT" -> Icons.Default.Thermostat
                        else -> Icons.Default.Tv
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = "Device icon",
                        tint = if (isActive) Color(0xFFD0E4FF) else Color(0xFFC2C6CF).copy(0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = device.deviceName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Sector: ${device.room}  |  Type: ${device.deviceType}",
                        color = Color(0xFFC2C6CF).copy(0.6f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Stateful switches toggles
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (device.deviceType == "THERMOSTAT" && isActive) {
                    Text(
                        text = "${device.value}°F",
                        color = WarningAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TechDarkBg,
                        checkedTrackColor = Color(0xFFD0E4FF),
                        uncheckedThumbColor = Color.White.copy(0.3f),
                        uncheckedTrackColor = Color.White.copy(0.06f)
                    ),
                    modifier = Modifier.testTag("device_toggle_${device.deviceId}")
                )
            }
        }
    }
}


// ==========================================
//   TAB 2: CALENDAR SCHEDULER & MISSIONS
// ==========================================
@Composable
fun SchedulerTabContent(viewModel: JarvisViewModel) {
    val eventsList by viewModel.events.collectAsStateWithLifecycle()
    val tasksList by viewModel.tasks.collectAsStateWithLifecycle()

    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Permission launcher for syncNativeCalendarEvents
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.syncNativeCalendarEvents()
        } else {
            Toast.makeText(context, "Sir, authorization is required to fetch device agenda pools.", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("scheduler_panel"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // --- SECTION 1: MASTER REPLICATION GATEWAY & CONTROLS ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CyanGlow.copy(0.3f)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2129)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "INTELLIGENCE ENGINE & CHRONOS GRID CONTROLS",
                        color = CyanGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Turbo Model Mode (Flash-Lite)",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Uses gemini-3.1-flash-lite-preview for instant replies",
                                color = Color.White.copy(0.6f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Switch(
                            checked = viewModel.useFlashLite,
                            onCheckedChange = { viewModel.useFlashLite = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyanGlow,
                                checkedTrackColor = CyanGlow.copy(0.2f),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.Gray.copy(0.2f)
                            ),
                            modifier = Modifier.testTag("sw_model_turbo")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.White.copy(0.08f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2F3D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp).border(1.dp, CyanGlow.copy(0.2f), RoundedCornerShape(8.dp)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Scan Google Calendar", tint = CyanGlow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SCAN CALENDARS", color = CyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Button(
                            onClick = {
                                viewModel.syncCrossPlatform()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B2F3D)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(36.dp).border(1.dp, CyanGlow.copy(0.2f), RoundedCornerShape(8.dp)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = "Sync Nodes", tint = CyanGlow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CLOUD REPLICATE", color = CyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // --- SECTION 2: TACTICAL EVENT SCHEDULE (Google / Outlook / local) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Today, contentDescription = "Timeline", tint = CyanGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CHRONO TIMELINE ACTIVE DIRECTIVES",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { showAddEventDialog = true },
                    modifier = Modifier.size(28.dp).background(CyanGlow.copy(0.15f), RoundedCornerShape(6.dp)).testTag("btn_trigger_add_event")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add directive", tint = CyanGlow, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (eventsList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                        .background(Color.White.copy(0.02f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tactical timeline operations scheduled, Sir.",
                        color = Color.White.copy(0.35f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            items(eventsList) { item ->
                CalendarEventRowItem(event = item, onDelete = {
                    viewModel.deleteMeeting(item.id, item.title)
                })
            }
        }

        // --- SECTION 3: PLANETARY MISSION OBJECTIVES (Tasks / Todo) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Tasks", tint = CyanGlow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE MISSION OBJECTIVES (TASKS)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                IconButton(
                    onClick = { showAddTaskDialog = true },
                    modifier = Modifier.size(28.dp).background(CyanGlow.copy(0.15f), RoundedCornerShape(6.dp)).testTag("btn_trigger_add_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add mission objective", tint = CyanGlow, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (tasksList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(1.dp, Color.White.copy(0.05f), RoundedCornerShape(12.dp))
                        .background(Color.White.copy(0.02f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mission roster cleared. Standing by for objective inputs, Sir.",
                        color = Color.White.copy(0.35f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            items(tasksList) { task ->
                TaskObjectiveRowItem(task = task, onToggle = {
                    viewModel.toggleTaskCompletion(task)
                }, onDelete = {
                    viewModel.deleteTask(task.id, task.title)
                })
            }
        }

        // --- SECTION 4: QUANTUM REPLICATION CONSOLE LOGS ---
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                    .background(Color(0xFF14171E))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Hub, contentDescription = "Nodes", tint = CyanGlow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "STARK GATEWAY CROSS-SYNC MATRIX",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    val statusColor = when(viewModel.syncStatus) {
                        "SYNCING" -> Color(0xFFFFB300)
                        "COMPLETED" -> TerminalGreen
                        "FAILED" -> Color(0xFFFF5252)
                        else -> CyanGlow
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = viewModel.syncStatus,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Print small scrolling sync feed
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(0.3f))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    viewModel.syncLogsList.takeLast(4).forEach { logLine ->
                        Text(
                            text = logLine,
                            color = TerminalGreen.copy(0.85f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val lastSyncTimeText = remember(viewModel.lastSyncTimestamp) {
                    val sdf = SimpleDateFormat("HH:mm:ss 'UTC'", Locale.getDefault())
                    sdf.format(Date(viewModel.lastSyncTimestamp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LAST MUTATION PACKET REPLICATED: $lastSyncTimeText",
                        color = Color.White.copy(0.4f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "ACTIVE NODES LISTED: 4 (DESK, TAB, HUD, MOB)",
                        color = Color.White.copy(0.4f),
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }

    // --- Dialog to create offline and online meetings ---
    if (showAddEventDialog) {
        var eventTitle by remember { mutableStateOf("") }
        var eventDesc by remember { mutableStateOf("") }
        var eventLengthStr by remember { mutableStateOf("1") } // hours default

        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            containerColor = Color(0xFF0D121B),
            title = {
                Text(
                    "INDEX NEW CALENDAR DIRECTIVE",
                    color = CyanGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Directive Title (e.g. Arc Calibration)", color = Color.White.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = CyanGlow.copy(0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = eventDesc,
                        onValueChange = { eventDesc = it },
                        label = { Text("Telemetry Notes", color = Color.White.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = CyanGlow.copy(0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = eventLengthStr,
                        onValueChange = { eventLengthStr = it },
                        label = { Text("Directive Duration (Hours)", color = Color.White.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = CyanGlow.copy(0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = eventLengthStr.toIntOrNull() ?: 1
                        if (eventTitle.isNotEmpty()) {
                            viewModel.createMeeting(eventTitle, eventDesc, hours)
                            showAddEventDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow)
                ) {
                    Text("UPLOAD INDEX", color = TechDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) {
                    Text("CANCEL", color = Color.White.copy(0.5f), fontSize = 11.sp)
                }
            }
        )
    }

    // --- Dialog to create offline and online tasks (Mission Objectives) ---
    if (showAddTaskDialog) {
        var taskTitle by remember { mutableStateOf("") }
        var taskPriority by remember { mutableStateOf("MEDIUM") } // "HIGH", "MEDIUM", "LOW"
        var taskCategory by remember { mutableStateOf("Core") } // "Stark Labs", "System", "Avengers", "Personal"
        var taskDueHours by remember { mutableStateOf("24") }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = Color(0xFF0D121B),
            title = {
                Text(
                    "CONSTRUCT MISSION OBJECTIVE",
                    color = CyanGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Objective Title (e.g. Armor Polishing)", color = Color.White.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = CyanGlow.copy(0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Priority Selector
                    Text("OBJECTIVE PRIORITY TARGET:", color = Color.White.copy(0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("LOW", "MEDIUM", "HIGH").forEach { p ->
                            val isSelected = taskPriority == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) CyanGlow.copy(0.2f) else Color.White.copy(0.04f))
                                    .border(1.dp, if (isSelected) CyanGlow else Color.White.copy(0.1f), RoundedCornerShape(6.dp))
                                    .clickable { taskPriority = p }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p, color = if (isSelected) CyanGlow else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }

                    // Category Input
                    OutlinedTextField(
                        value = taskCategory,
                        onValueChange = { taskCategory = it },
                        label = { Text("Objective Category Deployment", color = Color.White.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = CyanGlow.copy(0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = taskDueHours,
                        onValueChange = { taskDueHours = it },
                        label = { Text("Completion Deadline (Hours)", color = Color.White.copy(0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyanGlow,
                            unfocusedBorderColor = CyanGlow.copy(0.3f)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hours = taskDueHours.toIntOrNull() ?: 24
                        if (taskTitle.isNotEmpty()) {
                            viewModel.createTask(taskTitle, taskPriority, taskCategory, hours)
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanGlow)
                ) {
                    Text("ENGAGE MISSION", color = TechDarkBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("ABORT", color = Color.White.copy(0.5f), fontSize = 11.sp)
                }
            }
        )
    }
}

@Composable
fun TaskObjectiveRowItem(task: JarvisTask, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2F3033)),
        border = BorderStroke(1.dp, Color(0xFF44474E).copy(0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(0.8f)
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(36.dp).testTag("check_task_${task.id}")
                ) {
                    Icon(
                        imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Complete",
                        tint = if (task.completed) TerminalGreen else Color.White.copy(0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column {
                    Text(
                        text = task.title,
                        color = if (task.completed) Color.White.copy(0.5f) else Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        style = if (task.completed) androidx.compose.ui.text.TextStyle(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                        ) else androidx.compose.ui.text.TextStyle.Default
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Category Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(0.08f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.category.uppercase(Locale.getDefault()),
                                color = Color.White.copy(0.6f),
                                fontSize = 8.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Priority Badge
                        val (pColor, pText) = when(task.priority.uppercase(Locale.getDefault())) {
                            "HIGH" -> Color(0xFFFF5252) to "HIGH PRIORITY"
                            "LOW" -> Color.LightGray to "LOW PRIORITY"
                            else -> CyanGlow to "MEDIUM PRIORITY"
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(pColor.copy(0.12f))
                                .border(1.dp, pColor.copy(0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = pText,
                                color = pColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("btn_delete_task_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete Objective",
                    tint = Color(0xFFFF5252).copy(0.8f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun CalendarEventRowItem(event: CalendarEvent, onDelete: () -> Unit) {
    val formatter = SimpleDateFormat("EEEE, MMMM dd | hh:mm a", Locale.getDefault())
    val dateText = formatter.format(Date(event.startTime))

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2F3033)),
        border = BorderStroke(1.dp, Color(0xFF44474E).copy(0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Event",
                        tint = Color(0xFFFF5252).copy(0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "Chronometer: $dateText",
                color = Color(0xFFD0E4FF),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 2.dp)
            )

            Text(
                text = event.description,
                color = Color(0xFFE2E2E6).copy(0.85f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 6.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Room,
                    contentDescription = "Location",
                    tint = Color(0xFFC2C6CF).copy(0.5f),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.location,
                    color = Color(0xFFC2C6CF).copy(0.5f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}


// ==========================================
//   TAB 3: UTILITIES, STORAGE SCANNY & OVERLAY
// ==========================================
@Composable
fun UtilitiesTabContent(viewModel: JarvisViewModel, onGrantStorage: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    viewModel.checkOverlayStatus()

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // A. SYSTEM OVERLAY CONSOLE PANEL (Geometric Balance Styled)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F3033)),
                border = BorderStroke(1.dp, Color(0xFF44474E).copy(0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "HOVER HUD SYSTEM OVERLAY",
                        color = Color(0xFFD0E4FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Enables drawing a miniature glowing ARC Reactor always overlaying external applications for quick communication pipelines.",
                        color = Color(0xFFC2C6CF).copy(0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OVERLAY PERMISSION STATUS:",
                                fontSize = 9.sp,
                                color = Color(0xFFC2C6CF).copy(0.5f),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (viewModel.hasOverlayPermission) "AUTHORIZED" else "MISSING LINK",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (viewModel.hasOverlayPermission) TerminalGreen else WarningAmber,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (!viewModel.hasOverlayPermission) {
                            Button(
                                onClick = {
                                    val intent = Intent(
                                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                    context.startActivity(intent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                            ) {
                                Text("GRANT ACCESS", color = TechDarkBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.toggleOverlayHUD() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.isOverlayActive) Color(0xFFFF5252).copy(0.8f) else Color(0xFFD0E4FF)
                                )
                            ) {
                                Text(
                                    text = if (viewModel.isOverlayActive) "STANDBY HUD" else "ENGAGE HUD OVERLAY",
                                    color = TechDarkBg,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // B. VOCAL FREQUENCY TONALITY MODULATION (Geometric Balance Styled)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F3033)),
                border = BorderStroke(1.dp, Color(0xFF44474E).copy(0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "VIRTUAL VOCAL CALIBRATOR",
                        color = Color(0xFFD0E4FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))

                    // Pitch slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Pitch frequency: ${String.format("%.1f", viewModel.ttsPitch)}Hz", color = Color(0xFFC2C6CF), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = viewModel.ttsPitch,
                        onValueChange = { viewModel.updateSpeechPreferences(it, viewModel.ttsSpeed, viewModel.customVoiceTone) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFD0E4FF), activeTrackColor = Color(0xFFD0E4FF))
                    )

                    // Rate speed slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vocal transmission speed: ${String.format("%.1f", viewModel.ttsSpeed)}x", color = Color(0xFFC2C6CF), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Slider(
                        value = viewModel.ttsSpeed,
                        onValueChange = { viewModel.updateSpeechPreferences(viewModel.ttsPitch, it, viewModel.customVoiceTone) },
                        valueRange = 0.5f..2.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFD0E4FF), activeTrackColor = Color(0xFFD0E4FF))
                    )

                    // Behavioral Matrix Selection (Tones)
                    Text(
                        text = "COGNITIVE BEHAVIORAL INTERFACE:",
                        fontSize = 9.sp,
                        color = Color(0xFFC2C6CF).copy(0.5f),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Respectful Butler", "Snarky Companion", "Tactical HUD").forEach { tone ->
                            val isSel = viewModel.customVoiceTone == tone
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) Color(0xFFD0E4FF) else Color(0xFF44474E).copy(0.3f))
                                    .border(1.dp, Color(0xFF44474E).copy(0.4f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.updateSpeechPreferences(viewModel.ttsPitch, viewModel.ttsSpeed, tone)
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tone.split(" ").first(),
                                    color = if (isSel) TechDarkBg else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // C. Storage Scanny & Offline Overrides (Geometric Balance Styled)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F3033)),
                border = BorderStroke(1.dp, Color(0xFF44474E).copy(0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "OFFLINE LINK OVERRIDES",
                        color = Color(0xFFD0E4FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Manually isolate J.A.R.V.I.S from Stark Web mainframes to enforce secure offline operation parameters.",
                        color = Color(0xFFC2C6CF).copy(0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("FORCE ISOLATION:", color = Color(0xFFC2C6CF).copy(0.5f), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                text = if (viewModel.offlineModeOnly) "OFFLINE MATRIX" else "WEB INTEL LINK ACTIVE",
                                color = if (viewModel.offlineModeOnly) WarningAmber else TerminalGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Switch(
                            checked = viewModel.offlineModeOnly,
                            onCheckedChange = { viewModel.offlineModeOnly = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TechDarkBg,
                                checkedTrackColor = Color(0xFFD0E4FF)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color(0xFF44474E).copy(0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "DIAGNOSTIC SYSTEM DIRECTORIES",
                        color = Color(0xFFD0E4FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Request storage indexing permissions to scan files, catalog media tracks and diagnostics on your phone device sectors.",
                        color = Color(0xFFC2C6CF).copy(0.8f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Button(
                        onClick = onGrantStorage,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD0E4FF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("SCAN SYSTEM DIRECTORIES", color = TechDarkBg, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
