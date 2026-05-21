package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(viewModel: HabitViewModel) {
    var currentStep by remember { mutableStateOf(0) }
    
    // Step 1 States: Email & User Type
    var emailInput by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isNewUser by remember { mutableStateOf(true) } // true: New Recruit, false: Veteran Hero
    var isSimulatingSync by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("") }
    
    // Step 2 States: Class / Persona
    var selectedPersona by remember { mutableStateOf("Scholar of Learning") }
    
    // Step 3 States: Mascot Core Color
    var selectedMascotColor by remember { mutableStateOf("Cyan") }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val backgroundGradient = Brush.linearGradient(
        colors = listOf(DarkBackground, Color(0xFF160E2A))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .drawBehind {
                // Background soft light auras
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonPink.copy(alpha = 0.08f), Color.Transparent),
                        radius = 800f
                    ),
                    center = Offset(size.width * 0.1f, size.height * 0.2f)
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ElectricCyan.copy(alpha = 0.08f), Color.Transparent),
                        radius = 800f
                    ),
                    center = Offset(size.width * 0.9f, size.height * 0.8f)
                )
            }
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() with
                            slideOutHorizontally { width -> width } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "OnboardingStepTransition"
        ) { step ->
            when (step) {
                0 -> {
                    // STEP 1: WELCOME & EMAIL RECRUITMENT / RECALL
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Game Brand Seal Logo
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(NeonPink, SoftPurple)
                                    )
                                )
                                .shadow(24.dp, shape = RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "QuestLife Insignia",
                                tint = Color.White,
                                modifier = Modifier.size(52.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "A NEW QUEST AWAITS",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center,
                                letterSpacing = 2.sp
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Log in or choose your starting profile to join the epic guild of habit conquerors.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center,
                                color = Color.LightGray,
                                lineHeight = 20.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Email Text Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                emailError = null
                            },
                            label = { Text("Adventurer's Email") },
                            placeholder = { Text("e.g., hero@questlife.com") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = if (emailError != null) MaterialTheme.colorScheme.error else ElectricCyan
                                )
                            },
                            singleLine = true,
                            isError = emailError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                                focusedLabelColor = ElectricCyan,
                                cursorColor = ElectricCyan,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                errorContainerColor = DarkSurface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_email_input")
                        )

                        if (emailError != null) {
                            Text(
                                text = emailError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 12.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Toggle Selection (Segmented Control style)
                        Text(
                            text = "Adventure Status",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurfaceVariant)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isNewUser) NeonPink else Color.Transparent)
                                    .clickable { isNewUser = true }
                                    .testTag("onboarding_segment_recruit"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "New Recruit",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNewUser) Color.White else Color.Gray
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (!isNewUser) NeonPink else Color.Transparent)
                                    .clickable { isNewUser = false }
                                    .testTag("onboarding_segment_veteran"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Returning Hero",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isNewUser) Color.White else Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Help text for selected mode
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isNewUser) Icons.Default.Explore else Icons.Default.CloudDownload,
                                    contentDescription = "Status Help",
                                    tint = if (isNewUser) MintGreen else GoldLevel
                                )
                                Text(
                                    text = if (isNewUser) {
                                        "Welcome recruit! Prepare to customize your mascot companion and forge a brand new habit agenda."
                                    } else {
                                        "Welcome back! Log in to summon your historical accolades and maintain your heroic streak counters."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.LightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        if (isSimulatingSync) {
                            // Circular spacer loader with game feeling
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = ElectricCyan)
                                Text(
                                    text = syncMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                    color = ElectricCyan
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (emailInput.isBlank() || !emailInput.contains("@") || !emailInput.contains(".")) {
                                        emailError = "Please enter a valid guild email address."
                                    } else {
                                        focusManager.clearFocus()
                                        if (isNewUser) {
                                            // New User goes to step 2 directly (skipping archetype)
                                            currentStep = 2
                                        } else {
                                            // Returning user sync simulation
                                            scope.launch {
                                                isSimulatingSync = true
                                                val messages = listOf(
                                                    "Contacting cosmic indexer...",
                                                    "Finding historical record for $emailInput...",
                                                    "Downloading achievement scrolls...",
                                                    "Profile successfully synchronized!"
                                                )
                                                for (msg in messages) {
                                                    syncMessage = msg
                                                    delay(1000)
                                                }
                                                // Automatically onboard keeping existing seed/data
                                                viewModel.onboardUser(emailInput, "Veteran Master", "Gold", isNewUser = false)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("onboarding_next_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isNewUser) "Forging Destiny" else "Enlist & Synchronize",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Next"
                                    )
                                }
                            }
                        }
                    }
                }
                
                1 -> {
                    // STEP 2: CHOOSE WORKPERSON / HERO CLASS
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "ASSIGN YOUR ARCHETYPE",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Your daily habits will empower this primary character class.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Archtype selector cards
                        val archetypes = listOf(
                            Triple("Scholar of Learning", Icons.Default.MenuBook, "Gains wisdom energy from reading book pages, languages, or deep studies. Tied to Intellect."),
                            Triple("Warrior of Fitness", Icons.Default.FitnessCenter, "Amplifies physical might with daily active running, gym cycles, and steps. Tied to Movement."),
                            Triple("Monk of Mind", Icons.Default.SelfImprovement, "Draws level power from zen meditation, logs of gratitude, and deep sleep. Tied to Focus."),
                            Triple("Rogue of Routine", Icons.Default.Settings, "Conquers small chores, healthy dining boundaries, and clean habits. Tied to Consistency.")
                        )

                        archetypes.forEach { (name, icon, desc) ->
                            val isSelected = selectedPersona == name
                            val outlineColor = if (isSelected) NeonPink else Color.Transparent
                            val borderThickness = if (isSelected) 3.dp else 1.dp
                            val cardScale = if (isSelected) 1.02f else 1.0f

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .graphicsLayer(scaleX = cardScale, scaleY = cardScale)
                                    .clickable { selectedPersona = name }
                                    .testTag("onboarding_persona_${name.lowercase().replace(" ", "_")}"),
                                border = BorderStroke(
                                    borderThickness,
                                    if (isSelected) NeonPink else Color.Gray.copy(alpha = 0.2f)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) {
                                                    Brush.radialGradient(colors = listOf(NeonPink, SoftPurple))
                                                } else {
                                                    Brush.radialGradient(
                                                        colors = listOf(
                                                            Color.Gray.copy(alpha = 0.15f),
                                                            Color.Transparent
                                                        )
                                                    )
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = name,
                                            tint = if (isSelected) Color.White else Color.LightGray
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) NeonPink else Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 0 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Back", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = { currentStep = 2 },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(56.dp)
                                    .testTag("onboarding_step1_next_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Summon Mascot",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                )
                            }
                        }
                    }
                }
                
                2 -> {
                    // STEP 3: CONFIGURE MASCOT BOND
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "BOND YOUR GLO-MASCOT",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Select a core element glow color that matches your energy.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Preview of GloMascot Canvas! Let's render GloMascotCanvas
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(32.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            GloMascotCanvas(
                                progress = 0.85f,
                                modifier = Modifier.size(180.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Selection color Row
                        Text(
                            text = "Core Glow Element",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.LightGray,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val glowColors = listOf(
                            Triple("Cyan", ElectricCyan, "Aether Blue"),
                            Triple("Pink", NeonPink, "Flame Magenta"),
                            Triple("Gold", GoldLevel, "Solar Amber"),
                            Triple("Mint", MintGreen, "Polar Aurora")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            glowColors.forEach { (name, color, label) ->
                                val isSelected = selectedMascotColor == name
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMascotColor = name }
                                        .testTag("onboarding_color_${name.lowercase()}"),
                                    border = BorderStroke(
                                        if (isSelected) 3.dp else 1.dp,
                                        if (isSelected) color else Color.Gray.copy(alpha = 0.2f)
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) color.copy(alpha = 0.15f) else DarkSurface
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = if (isSelected) color else Color.LightGray,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.height(32.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 0 },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Back", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = {
                                    viewModel.onboardUser(emailInput, "Adventurer", selectedMascotColor, isNewUser = true)
                                },
                                modifier = Modifier
                                    .weight(2f)
                                    .height(56.dp)
                                    .testTag("onboarding_complete_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MintGreen,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Complete Rite",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
