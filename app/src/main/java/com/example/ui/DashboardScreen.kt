package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Habit
import com.example.data.HabitHistory
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Confetti Particle representation
data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val r: Float,
    val vx: Float,
    val vy: Float,
    val rotation: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val history by viewModel.allHistory.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userPersona by viewModel.userPersona.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }

    // Particles/Confetti State
    var confettiParticles by remember { mutableStateOf<List<ConfettiParticle>>(emptyList()) }
    var isConfettiActive by remember { mutableStateOf(false) }

    // Trigger confetti splash
    val triggerConfetti = {
        scope.launch {
            val colors = listOf(NeonPink, ElectricCyan, GoldLevel, MintGreen, SoftPurple)
            val tempParticles = List(70) {
                val angle = Random.nextFloat() * 3.14159f * 2f
                val speed = Random.nextFloat() * 25f + 10f
                ConfettiParticle(
                    x = 0f,
                    y = 0f,
                    color = colors.random(),
                    r = Random.nextFloat() * 6f + 6f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 5f, // bias upwards
                    rotation = Random.nextFloat() * 360f
                )
            }
            confettiParticles = tempParticles
            isConfettiActive = true
            
            // simple physics simulation
            for (step in 1..25) {
                delay(30)
                confettiParticles = confettiParticles.map { p ->
                    p.copy(
                        x = p.x + p.vx,
                        y = p.y + p.vy,
                        vy = p.vy + 1.2f, // gravity
                        rotation = p.rotation + 15f
                    )
                }
            }
            isConfettiActive = false
            confettiParticles = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(NeonPink, SoftPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Logo Spark",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "QuestLife",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Quest",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            QuestBottomNavigation(
                currentTab = currentTab,
                onTabSelected = { viewModel.setCurrentTab(it) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            
            // Ambient Gradients in Background for modern gaming atmosphere
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Soft primary glow at top-right
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonPink.copy(alpha = 0.12f), Color.Transparent),
                                radius = 700f
                            ),
                            center = Offset(size.width * 0.9f, 100f)
                        )
                        // Soft cyan glow at bottom-left
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(ElectricCyan.copy(alpha = 0.08f), Color.Transparent),
                                radius = 900f
                            ),
                            center = Offset(size.width * 0.1f, size.height * 0.8f)
                        )
                    }
            )

            // Dynamic view swap based on active tab
            Crossfade(
                targetState = currentTab,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
                label = "TabCrossfade"
            ) { tab ->
                when (tab) {
                    "board" -> QuestBoardTab(
                        habits = habits,
                        history = history,
                        selectedDate = selectedDate,
                        activeFilter = activeFilter,
                        onDateSelected = { viewModel.setSelectedDate(it) },
                        onFilterSelected = { viewModel.setActiveFilter(it) },
                        onToggleHabit = { habit -> 
                            viewModel.toggleHabit(habit.id)
                            triggerConfetti()
                        },
                        onUpdateMetric = { habitId, valUpdate ->
                            viewModel.updateHabitValue(habitId, valUpdate)
                            val habit = habits.find { it.id == habitId }
                            if (habit != null && valUpdate >= habit.metricTarget) {
                                triggerConfetti()
                            }
                        },
                        onDeleteHabit = { viewModel.deleteHabit(it) }
                    )
                    "guild" -> GuildAnalyticsTab(
                        habits = habits,
                        history = history,
                        userEmail = userEmail,
                        userPersona = userPersona,
                        onLogout = { viewModel.logout() }
                    )
                    "mascot" -> MascotSanctuaryTab(
                        habits = habits,
                        history = history,
                        onTriggerBurst = { triggerConfetti() }
                    )
                }
            }

            // Confetti Splash Overlay Layer
            if (isConfettiActive) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center)
                ) {
                    val center = Offset(size.width / 2f, size.height * 0.4f)
                    confettiParticles.forEach { p ->
                        val transformOffset = Offset(center.x + p.x, center.y + p.y)
                        drawContext.canvas.save()
                        drawContext.canvas.rotate(p.rotation, transformOffset.x, transformOffset.y)
                        
                        // draw starry/rectangular flakes
                        drawRoundRect(
                            color = p.color,
                            topLeft = Offset(transformOffset.x - p.r, transformOffset.y - p.r),
                            size = Size(p.r * 2f, p.r),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawContext.canvas.restore()
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, desc, cat, metric, target ->
                viewModel.addHabit(name, desc, cat, metric, target)
                showAddDialog = false
            }
        )
    }
}

// Visual category helpers
fun getCategoryIcon(cat: String): ImageVector {
    return when (cat.lowercase()) {
        "fitness" -> Icons.Default.FitnessCenter
        "mind" -> Icons.Default.SelfImprovement
        "learning" -> Icons.Default.MenuBook
        else -> Icons.Default.TaskAlt
    }
}

fun getCategoryColor(cat: String): Color {
    return when (cat.lowercase()) {
        "fitness" -> ElectricCyan
        "mind" -> SoftPurple
        "learning" -> GoldLevel
        else -> NeonPink
    }
}

// -------------------------------------------------------------
// TAB 1: QUEST BOARD (Habits list with tactile bubbly checks)
// -------------------------------------------------------------
@Composable
fun QuestBoardTab(
    habits: List<Habit>,
    history: List<HabitHistory>,
    selectedDate: LocalDate,
    activeFilter: String,
    onDateSelected: (LocalDate) -> Unit,
    onFilterSelected: (String) -> Unit,
    onToggleHabit: (Habit) -> Unit,
    onUpdateMetric: (Int, Float) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    val filteredHabits = remember(habits, activeFilter) {
        if (activeFilter == "ALL") habits else habits.filter { it.category.uppercase() == activeFilter.uppercase() }
    }

    // Determine completion rate for selected day
    val todayHistory = remember(history, selectedDate) {
        history.filter { it.dateString == selectedDate.toString() }
    }

    val dailyProgress = remember(habits, todayHistory) {
        if (habits.isEmpty()) 0f else {
            val completedCount = habits.count { h -> 
                val hist = todayHistory.find { it.habitId == h.id }
                hist != null && (h.metricName.isEmpty() || hist.metricValue >= h.metricTarget)
            }
            (completedCount.toFloat() / habits.size.toFloat()).coerceIn(0f, 1f)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        // 1. Playful Welcome with floating Glo Mascot Mini-Dashboard
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DailySummaryHeader(progress = dailyProgress, habitsCount = habits.size, completedCount = (dailyProgress * habits.size).toInt())
        }

        // 2. Weekly Calendar strip layout
        item {
            HorizontalCalendarStrip(selectedDate = selectedDate, onDateSelected = onDateSelected)
        }

        // 3. Category Filter pills
        item {
            FilterCategoryStrip(selectedFilter = activeFilter, onFilterSelected = onFilterSelected)
        }

        // Empty state instruction
        if (filteredHabits.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "No quests found in this category!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap '+' to forge a new daily quest!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 4. Habits list
        items(filteredHabits, key = { it.id }) { habit ->
            val habitHistRange = todayHistory.find { it.habitId == habit.id }
            val isCompleted = habitHistRange != null && (habit.metricName.isEmpty() || habitHistRange.metricValue >= habit.metricTarget)
            
            HabitQuestItemCard(
                habit = habit,
                isCompleted = isCompleted,
                historyEntry = habitHistRange,
                onToggle = { onToggleHabit(habit) },
                onMetricUpdate = { onUpdateMetric(habit.id, it) },
                onDelete = { onDeleteHabit(habit) }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun DailySummaryHeader(progress: Float, habitsCount: Int, completedCount: Int) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = NeonPink.copy(alpha = 0.2f)),
        border = BorderStroke(1.5.dp, Brush.linearGradient(colors = listOf(NeonPink.copy(alpha = 0.2f), ElectricCyan.copy(alpha = 0.2f))))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Mascot Glo preview side
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Draws cute canvas Glo based on completion progress
                GloMascotCanvas(progress = progress, modifier = Modifier.size(64.dp))
            }

            // Progress text details
            Column(modifier = Modifier.weight(1f)) {
                val greetingMsg = when {
                    progress >= 0.99f -> "LEGENDARY! All quests conquered!"
                    progress >= 0.7f -> "Awesome! Glo is super energized!"
                    progress >= 0.35f -> "Steady on! Quests are progressing bien!"
                    progress > 0.0f -> "Morning stretch! Glo has woken up!"
                    else -> "Cozy morning! Time to start the day's saga."
                }
                
                Text(
                    text = greetingMsg,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "$completedCount of $habitsCount quests completed today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                // Custom Duolingo-like thick round visual progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val animatedProgressWidth by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "ProgressBarProgress"
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgressWidth)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(NeonPink, SoftPurple, ElectricCyan)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun GloMascotCanvas(progress: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "FloatTransition")
    
    // Smooth idle breathing bobbing animation
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MascotBob"
    )

    Canvas(modifier = modifier.offset(y = bobbingOffset.dp)) {
        val width = size.width
        val height = size.height
        val cx = width / 2f
        val cy = height / 2f + 4f
        
        // 1. Draw Golden Aura Glow if highly completed
        if (progress >= 0.75f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GoldLevel.copy(alpha = 0.6f), Color.Transparent),
                    radius = width * 0.75f
                ),
                center = Offset(cx, cy)
            )
        }

        // 2. Draw Cloud Mascot Base (Playful overlapping puff layout)
        val cloudPaint = Paint().apply {
            color = if (progress >= 0.75f) Color(0xFFFFFDF0) else Color.White
            style = PaintingStyle.Fill
            isAntiAlias = true
        }

        val cloudPoints = listOf(
            Offset(cx, cy) to width * 0.35f, // center main bubble
            Offset(cx - width * 0.22f, cy + height * 0.05f) to width * 0.25f, // Left puff
            Offset(cx + width * 0.22f, cy + height * 0.05f) to width * 0.25f, // Right puff
            Offset(cx - width * 0.15f, cy - height * 0.12f) to width * 0.22f, // Upper left puff
            Offset(cx + width * 0.15f, cy - height * 0.12f) to width * 0.22f  // Upper right puff
        )

        cloudPoints.forEach { (offset, radius) ->
            drawCircle(color = if (progress >= 0.75f) Color(0xFFFFFDEF) else Color.White, radius = radius, center = offset)
        }
        
        // Shadow underneath puffs to create depth
        drawCircle(
            color = Color(0xFFE2DDFE).copy(alpha = 0.5f),
            radius = width * 0.32f,
            center = Offset(cx, cy + 4f)
        )
        cloudPoints.forEach { (offset, radius) ->
            if (offset.y > cy) {
                drawCircle(color = Color.White, radius = radius, center = offset)
            }
        }

        // 3. Draw Mascot Eyes and Mouth based on mood states
        val eyeColor = Color(0xFF1D1B2A)
        val cheekColor = Color(0xFFFF80AB)

        if (progress >= 0.75f) {
            // STARRY EYES MOOD (Heroic/Super Happy)
            // Starry eyes
            fun drawStarEye(x: Float, y: Float) {
                val path = Path().apply {
                    moveTo(x, y - 8f)
                    lineTo(x + 2.5f, y - 2.5f)
                    lineTo(x + 8f, y - 2f)
                    lineTo(x + 3.5f, y + 2f)
                    lineTo(x + 5f, y + 7.5f)
                    lineTo(x, y + 4.5f)
                    lineTo(x - 5f, y + 7.5f)
                    lineTo(x - 3.5f, y + 2f)
                    lineTo(x - 8f, y - 2f)
                    lineTo(x - 2.5f, y - 2.5f)
                    close()
                }
                drawPath(path = path, color = NeonPink)
            }

            drawStarEye(cx - width * 0.18f, cy - height * 0.05f)
            drawStarEye(cx + width * 0.18f, cy - height * 0.05f)

            // Dynamic smiling open mouth
            val mouthPath = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - 7f, cy + 1f, cx + 7f, cy + 11f),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
            }
            drawPath(path = mouthPath, color = eyeColor, style = Stroke(width = 4f, cap = StrokeCap.Round))
            
            // Star sparkles/Halo above
            drawCircle(color = GoldLevel, radius = 5f, center = Offset(cx - width * 0.35f, cy - height * 0.25f))
            drawCircle(color = GoldLevel, radius = 4f, center = Offset(cx + width * 0.32f, cy - height * 0.30f))

        } else if (progress >= 0.35f) {
            // PLAYFUL/WINKING MOOD (Content/Steady)
            // Left winking eye (curved arc)
            val leftEyePath = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - width * 0.24f, cy - height * 0.1f, cx - width * 0.12f, cy),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
            }
            drawPath(path = leftEyePath, color = eyeColor, style = Stroke(width = 4f, cap = StrokeCap.Round))

            // Right normal smiley eye (open dot or arc)
            drawCircle(color = eyeColor, radius = 4f, center = Offset(cx + width * 0.18f, cy - height * 0.05f))

            // Happy mouth arc
            val simpleMouth = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - 5f, cy + 2f, cx + 5f, cy + 7f),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
            }
            drawPath(path = simpleMouth, color = eyeColor, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

        } else {
            // SLEEPY/COZY MOOD (<35% completes)
            // Curved closed cozy eyes
            val leftEyePath = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx - width * 0.25f, cy - height * 0.06f, cx - width * 0.11f, cy + height * 0.02f),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = true
                )
            }
            val rightEyePath = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(cx + width * 0.11f, cy - height * 0.06f, cx + width * 0.25f, cy + height * 0.02f),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = true
                )
            }
            drawPath(path = leftEyePath, color = eyeColor.copy(alpha = 0.5f), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            drawPath(path = rightEyePath, color = eyeColor.copy(alpha = 0.5f), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

            // Tiny cozy round mouth o
            drawCircle(color = eyeColor.copy(alpha = 0.5f), radius = 3f, center = Offset(cx, cy + 5f))
        }

        // 4. Rosy pink cheeks for delight factor (always present)
        drawCircle(color = cheekColor.copy(alpha = 0.45f), radius = 6f, center = Offset(cx - width * 0.25f, cy + height * 0.04f))
        drawCircle(color = cheekColor.copy(alpha = 0.45f), radius = 6f, center = Offset(cx + width * 0.25f, cy + height * 0.04f))
    }
}

@Composable
fun HorizontalCalendarStrip(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val today = LocalDate.now()
    val days = remember {
        (-3..3).map { today.plusDays(it.toLong()) }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { date ->
            val isSelected = date == selectedDate
            val isDateToday = date == today
            
            val scale by animateFloatAsState(if (isSelected) 1.12f else 1.0f, label = "DateScale")

            Card(
                shape = RoundedCornerShape(16.dp),
                border = if (isSelected) BorderStroke(2.dp, NeonPink) else if (isDateToday) BorderStroke(1.dp, SoftPurple.copy(alpha = 0.5f)) else null,
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) NeonPink.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 3.dp)
                    .clickable { onDateSelected(date) }
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = if (isSelected) NeonPink else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 16.sp),
                        color = if (isSelected) NeonPink else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun FilterCategoryStrip(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    val filters = listOf("ALL", "fitness", "mind", "learning", "routine")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter.uppercase() == selectedFilter.uppercase()
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) {
                            getCategoryColor(filter).copy(alpha = 0.25f)
                        } else {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        }
                    )
                    .border(
                        BorderStroke(
                            1.5.dp,
                            if (isSelected) getCategoryColor(filter) else Color.Transparent
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (filter != "ALL") {
                        Icon(
                            imageVector = getCategoryIcon(filter),
                            contentDescription = filter,
                            tint = if (isSelected) getCategoryColor(filter) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = filter.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// QUEST CARD DECORATION & TACTILE bubble toggle
// -------------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitQuestItemCard(
    habit: Habit,
    isCompleted: Boolean,
    historyEntry: HabitHistory?,
    onToggle: () -> Unit,
    onMetricUpdate: (Float) -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = getCategoryColor(habit.category)
    var showExpandSlider by remember { mutableStateOf(false) }

    // Spring bouncy animations on completion
    val backgroundOffset by animateDpAsState(if (isCompleted) 1.dp else 4.dp, label = "CardPressBorder")
    val cardBackground = if (isCompleted) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        
        // 3D tactile bottom offset block (gives high depth buttons/cards like Duolingo!)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .offset(y = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(categoryColor.copy(alpha = 0.25f))
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBackground),
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = backgroundOffset)
                .combinedClickable(
                    onClick = {
                        if (habit.metricName.isNotEmpty()) {
                            showExpandSlider = !showExpandSlider
                        } else {
                            onToggle()
                        }
                    },
                    onLongClick = { onDelete() }
                ),
            border = BorderStroke(
                1.5.dp,
                if (isCompleted) categoryColor.copy(alpha = 0.8f) else categoryColor.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    
                    // Left Text Column
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom styled category circle icon
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(categoryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(habit.category),
                                contentDescription = habit.category,
                                tint = categoryColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(2.dp))
                            
                            Text(
                                text = habit.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 2
                            )
                        }
                    }

                    // Metric text or simple bubbles
                    if (habit.metricName.isNotEmpty()) {
                        val currentVal = historyEntry?.metricValue ?: 0f
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Text(
                                text = "${currentVal.toInt()}/${habit.metricTarget.toInt()} ${habit.metricName}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (isCompleted) MintGreen else categoryColor
                            )
                            Text(
                                text = if (isCompleted) "Completed!" else "Tapping logs val",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // TACTILE TOGGLE CHECK BUBBLE
                    TactileCheckBubble(
                        isCompleted = isCompleted,
                        color = categoryColor,
                        onToggle = onToggle
                    )
                }

                // Subtitle details showing current streak levels
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Small level flame capsule icon
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = NeonPink,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${habit.streak}d streak",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = NeonPink
                        )
                    }

                    // Records info
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Best Record",
                            tint = GoldLevel,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Best: ${habit.bestStreak}d",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = GoldLevel
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDelete() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("delete_habit_button_${habit.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Habit",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }

                // Dropdown Slider for metrics
                AnimatedVisibility(visible = showExpandSlider) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Set progress value for today:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        
                        val initialVal = historyEntry?.metricValue ?: 0f
                        var tempSliderVal by remember(initialVal) { mutableFloatStateOf(initialVal) }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Slider(
                                value = tempSliderVal,
                                onValueChange = { tempSliderVal = it },
                                valueRange = 0f..(habit.metricTarget * 1.5f),
                                colors = SliderDefaults.colors(
                                    thumbColor = categoryColor,
                                    activeTrackColor = categoryColor,
                                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${tempSliderVal.toInt()} ${habit.metricName}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = categoryColor
                            )
                        }

                        Button(
                            onClick = {
                                onMetricUpdate(tempSliderVal)
                                showExpandSlider = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = categoryColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .height(38.dp)
                        ) {
                            Text("Confirm Quest Score", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TactileCheckBubble(
    isCompleted: Boolean,
    color: Color,
    onToggle: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.2f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "BubbleScale"
    )

    IconButton(
        onClick = onToggle,
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .clip(CircleShape)
            .background(if (isCompleted) color else color.copy(alpha = 0.15f))
            .border(2.dp, color, CircleShape)
    ) {
        AnimatedContent(
            targetState = isCompleted,
            transitionSpec = {
                scaleIn() togetherWith scaleOut()
            },
            label = "CheckAnimation"
        ) { completed ->
            if (completed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Complete",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color.copy(alpha = 0.40f)))
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: GUILD ANALYTICS (Contribution Grids + Glowing charts)
// -------------------------------------------------------------
@Composable
fun GuildAnalyticsTab(
    habits: List<Habit>,
    history: List<HabitHistory>,
    userEmail: String?,
    userPersona: String?,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            
            // Elegant user profile card with logout feature
            Card(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Adventurer Avatar",
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = userPersona ?: "Adventurer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = userEmail ?: "recruit@questlife.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .testTag("onboarding_logout_button")
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Text(
                text = "Guild Chronicles",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Observe your historic campaigns and level achievements.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 1. Contribution Grid Calendar card with round gradient tiles
        item {
            ContributionGridCard(history = history, habitsCount = habits.size)
        }

        // 2. Beautiful level milestone game bars
        item {
            MilestonesProgressList(habits = habits)
        }

        // 3. Glowing Neon Trend Line Chart
        item {
            GlowingTrendLineCard(history = history)
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ContributionGridCard(history: List<HabitHistory>, habitsCount: Int) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Campaign Grid",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                    )
                    Text(
                        text = "15-Week completions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Color Key
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Less", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp))
                    listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        SoftPurple.copy(alpha = 0.4f),
                        ElectricCyan.copy(alpha = 0.7f),
                        NeonPink
                    ).forEach { col ->
                        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(col))
                    }
                    Text("More", style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Renders standard Contribution grid mapping 15 weeks
            val columns = 15
            val rows = 7
            val totalDays = columns * rows
            
            val today = LocalDate.now()
            val gridDates = remember {
                (0 until totalDays).map { index ->
                    val offset = (totalDays - 1) - index
                    today.minusDays(offset.toLong())
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                
                // Weekday Labels on Left
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.height(115.dp).padding(end = 4.dp)
                ) {
                    listOf("M", "W", "F", "S").forEach { l ->
                        Text(
                            text = l, 
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }

                // Scrollable Grid column pack
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    for (weekIndex in 0 until columns) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            for (dayIndex in 0 until rows) {
                                val cellNum = weekIndex * rows + dayIndex
                                if (cellNum < gridDates.size) {
                                    val cellDate = gridDates[cellNum]
                                    val cellDateStr = cellDate.toString()
                                    
                                    val completedCountOnDate = remember(history, cellDateStr) {
                                        history.count { it.dateString == cellDateStr && it.metricValue > 0f }
                                    }

                                    // Map completion size to visual gradient intensity
                                    val tileColor = when {
                                        completedCountOnDate == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        completedCountOnDate <= 2 -> SoftPurple.copy(alpha = 0.5f)
                                        completedCountOnDate <= 4 -> ElectricCyan.copy(alpha = 0.8f)
                                        else -> NeonPink
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(13.dp)
                                            .clip(RoundedCornerShape(3.5.dp))
                                            .background(tileColor)
                                            .clickable { /* Tap to view day info */ }
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

@Composable
fun MilestonesProgressList(habits: List<Habit>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quest Level Milestones",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )

            habits.forEach { habit ->
                val categoryColor = getCategoryColor(habit.category)
                
                // Fancy gamified RPG titles
                val rpgLevel = when {
                    habit.bestStreak >= 25 -> "Level 5: Archmage 🧙"
                    habit.bestStreak >= 15 -> "Level 4: Fire Knight ⚔️"
                    habit.bestStreak >= 10 -> "Level 3: Scholar 📜"
                    habit.bestStreak >= 5 -> "Level 2: Novice 🛡️"
                    else -> "Level 1: Adventurer 🌲"
                }

                // Calculation to beating record progress percentage
                val recordMilestoneProgress = if (habit.bestStreak == 0) 1.0f else habit.streak.toFloat() / habit.bestStreak.toFloat()

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = rpgLevel,
                                style = MaterialTheme.typography.bodySmall,
                                color = categoryColor
                            )
                        }
                        
                        Text(
                            text = "${habit.streak} / ${habit.bestStreak}d Record",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Level Up progression Bar with texture
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(recordMilestoneProgress.coerceIn(0f, 1f))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(categoryColor.copy(alpha = 0.7f), categoryColor)
                                    )
                                )
                        )
                    }

                    // Empathetic gamified broken-streak encourager copy
                    val diff = habit.bestStreak - habit.streak
                    val promptText = when {
                        diff <= 0 && habit.bestStreak > 0 -> "🏆 Legendary! You set an all-time quest high score!"
                        diff in 1..3 -> "🔥 Keep going! You're only $diff days from leveling up this skill!"
                        else -> "⚔️ Onward! Level up this class by hitting daily quests."
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = promptText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun GlowingTrendLineCard(history: List<HabitHistory>) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Guild Energy Trends",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = "Glowing score index over the past 14 days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dynamic calculation of percentage completes for last 14 days
            val last14Days = remember {
                (0 until 14).map { LocalDate.now().minusDays(it.toLong()) }.reversed()
            }
            val linePoints = remember(history) {
                last14Days.map { date ->
                    val dateStr = date.toString()
                    val entries = history.filter { it.dateString == dateStr }
                    if (entries.isEmpty()) 0f else {
                        val completesValue = entries.count { it.metricValue > 0f }
                        // Mock normalization out of 8 maximum seeded habits
                        completesValue.toFloat() / 8f
                    }
                }
            }

            // Beautiful glowing canvas-drawn graph curve
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val cw = size.width
                val ch = size.height
                val pointCount = linePoints.size
                val stepX = cw / (pointCount - 1)

                val points = linePoints.mapIndexed { index, score ->
                    // invert graph coords
                    val px = index * stepX
                    val py = ch - (score * ch * 0.8f) - 10f // keep padding
                    Offset(px, py)
                }

                // Draw solid glowing underbrush gradient
                val fillPath = Path().apply {
                    moveTo(0f, ch)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(cw, ch)
                    close()
                }
                
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonPink.copy(alpha = 0.35f), Color.Transparent),
                        startY = 0f,
                        endY = ch
                    )
                )

                // Smooth trend curve strokes
                val strokePath = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                }

                drawPath(
                    path = strokePath,
                    color = NeonPink,
                    style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw neon circle nodes
                points.forEach { point ->
                    // Outermost pulse aura
                    drawCircle(
                        color = NeonPink.copy(alpha = 0.25f),
                        radius = 12f,
                        center = point
                    )
                    // Inner bright bead
                    drawCircle(
                        color = Color.White,
                        radius = 5f,
                        center = point
                    )
                    drawCircle(
                        color = ElectricCyan,
                        radius = 2.5f,
                        center = point
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // X Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    last14Days.first().dayOfMonth.toString() + " " + last14Days.first().month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    last14Days[7].dayOfMonth.toString() + " " + last14Days[7].month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    last14Days.last().dayOfMonth.toString() + " " + last14Days.last().month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                ).forEach { dateLabel ->
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: MASCOT SANCTUARY / INTERACTIVE WIDGET PREVIEW
// -------------------------------------------------------------
@Composable
fun MascotSanctuaryTab(habits: List<Habit>, history: List<HabitHistory>, onTriggerBurst: () -> Unit) {
    var mascotMoodValue by remember { mutableFloatStateOf(0.75f) }
    
    // Simulate current completions percentage for Glo
    val completionPercent = if (habits.isEmpty()) 0f else {
        (history.filter { it.dateString == LocalDate.now().toString() && it.metricValue > 0f }.size.toFloat() / habits.size.toFloat()).coerceIn(0f, 1f)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        
        // Tab description
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Mascot Sanctuary",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Interact with Glo, feed them daily completions, and place living home widgets!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Giant interactive sanctuary mascot module
        item {
            InteractiveMascotSanctuaryCard(
                completionPercent = completionPercent,
                onMascotTap = {
                    onTriggerBurst()
                }
            )
        }

        // Simulated Living mobile home widget (Refining Widget Image 5!)
        item {
            LivingWidgetSimulator(
                currentPercent = completionPercent,
                onInstantToggle = {
                    onTriggerBurst()
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun InteractiveMascotSanctuaryCard(completionPercent: Float, onMascotTap: () -> Unit) {
    val haptic = LocalContext.current
    var isShaking by remember { mutableStateOf(false) }
    
    // Dynamic breathing/scale state
    val infiniteTransition = rememberInfiniteTransition(label = "IdleFloat")
    val idleMascotScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Breathing"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.5.dp, Brush.linearGradient(colors = listOf(NeonPink.copy(alpha = 0.2f), ElectricCyan.copy(alpha = 0.2f)))),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp), ambientColor = ElectricCyan.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            
            Text(
                text = "Glo the Atmospheric Mascot",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Tap to pet Glo and release atmospheric energy sparks!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Giant Mascot Arena Area
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                getCategoryColor("fitness").copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .clickable {
                        onMascotTap()
                        isShaking = true
                    }
                    .graphicsLayer(scaleX = idleMascotScale, scaleY = idleMascotScale),
                contentAlignment = Alignment.Center
            ) {
                GloMascotCanvas(
                    progress = completionPercent,
                    modifier = Modifier.size(150.dp)
                )
            }
            
            LaunchedEffect(isShaking) {
                if (isShaking) {
                    delay(400)
                    isShaking = false
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Conversational Speeches
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubble,
                        contentDescription = "Speaks",
                        tint = ElectricCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    val speakText = when {
                        completionPercent >= 0.75f -> "星星! Star sparkles! You made my power level grow so bright today! You are legendary!"
                        completionPercent >= 0.35f -> "Breathe in, breathe out... I can feel our atmospheric bond. We are half-way into our sagas!"
                        else -> "Yawn... I'm feeling a bit misty. Complete your quests today so we can wake up fully!"
                    }

                    Text(
                        text = valSpeakRegex(speakText),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontStyle = FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// Regex mock helper
fun valSpeakRegex(text: String): String = text

@Composable
fun LivingWidgetSimulator(currentPercent: Float, onInstantToggle: () -> Unit) {
    var isSimCompleted by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Living Widget Preview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
            )
            Text(
                text = "Interactive preview of the QuestLife home screen widgets.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Dynamic phone screen emulator frame!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF07040D)) // Deep phone dark space wallpaper
                    .border(3.dp, Color(0xFF2C283F), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                // Background stars wallpaper
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val r = Random(12345)
                    for (i in 1..25) {
                        drawCircle(
                            color = Color.White.copy(alpha = r.nextFloat() * 0.4f),
                            radius = r.nextFloat() * 2f + 1f,
                            center = Offset(r.nextFloat() * size.width, r.nextFloat() * size.height)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    
                    // Widget Frame Placed on Home Wall paper
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, if (isSimCompleted) MintGreen else SoftPurple.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(115.dp)
                            .shadow(8.dp, RoundedCornerShape(18.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            
                            // Widget Left side: Cloud Mascot reaction
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Dynamic character reaction directly inside widget! Sleeps until complete!
                                GloMascotCanvas(
                                    progress = if (isSimCompleted) 1.0f else 0.1f,
                                    modifier = Modifier.size(52.dp)
                                )
                            }

                            // Widget Middle: copy info
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Wake Up Early",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 14.sp),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isSimCompleted) "Complete! Glo wakes up and smiles!" else "Glo is sleeping. Complete directly to energize!",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = Color.LightGray.copy(alpha = 0.8f)
                                )
                            }

                            // Widget Right side: COMPLETE BUTTON!
                            Button(
                                onClick = {
                                    isSimCompleted = !isSimCompleted
                                    onInstantToggle()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSimCompleted) MintGreen else NeonPink
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .minimumInteractiveComponentSize()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSimCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                                        contentDescription = "Complete",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (isSimCompleted) "Done" else "Tap",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom: Simulated home launcher icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            Icons.Default.Phone to Color(0xFF4CAF50),
                            Icons.Default.Mail to Color(0xFF2196F3),
                            Icons.Default.Settings to Color(0xFF757575),
                            Icons.Default.Palette to Color(0xFFFF9800)
                        ).forEach { (icon, color) ->
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = "Home App",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// QUEST BOTTOM NAVIGATION BAR (M3 styled with consistent pills)
// -------------------------------------------------------------
@Composable
fun QuestBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        NavigationBarItem(
            selected = currentTab == "board",
            onClick = { onTabSelected("board") },
            icon = {
                Icon(
                    imageVector = if (currentTab == "board") Icons.Default.SportsEsports else Icons.Outlined.SportsEsports,
                    contentDescription = "Quests"
                )
            },
            label = { Text("Quests Board") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonPink,
                selectedTextColor = NeonPink,
                indicatorColor = NeonPink.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentTab == "guild",
            onClick = { onTabSelected("guild") },
            icon = {
                Icon(
                    imageVector = if (currentTab == "guild") Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents,
                    contentDescription = "Guild Stats"
                )
            },
            label = { Text("Guild Analytics") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricCyan,
                selectedTextColor = ElectricCyan,
                indicatorColor = ElectricCyan.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = currentTab == "mascot",
            onClick = { onTabSelected("mascot") },
            icon = {
                Icon(
                    imageVector = if (currentTab == "mascot") Icons.Default.WbCloudy else Icons.Outlined.WbCloudy,
                    contentDescription = "Mascot Sanctuary"
                )
            },
            label = { Text("Sanctuary") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = GoldLevel,
                selectedTextColor = GoldLevel,
                indicatorColor = GoldLevel.copy(alpha = 0.15f)
            )
        )
    }
}

// -------------------------------------------------------------
// ADD HABIT FORGING DIALOG
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Float) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("routine") }
    var useMetric by remember { mutableStateOf(false) }
    var metricName by remember { mutableStateOf("pages") }
    var targetVal by remember { mutableStateOf("30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Heading
                Text(
                    text = "Forge New Daily Quest",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Quest Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        focusedLabelColor = NeonPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Conversational Encourager Copy") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        focusedLabelColor = NeonPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ready for a breath of fresh air? Let's getThose 10 mins.", fontSize = 11.sp, color = Color.Gray) }
                )

                // Category selector
                Text(
                    text = "Select Quest Guild Class:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.LightGray
                )
                
                val categories = listOf("fitness", "mind", "learning", "routine")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == selectedCategory
                        val color = getCategoryColor(cat)
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) color else color.copy(alpha = 0.1f))
                                .border(1.dp, if (isSelected) Color.White else color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.uppercase(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                                color = if (isSelected) Color.White else Color.LightGray
                            )
                        }
                    }
                }

                // Metric Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Enable Numerical Progress Tracking", color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = useMetric,
                        onCheckedChange = { useMetric = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonPink, checkedTrackColor = NeonPink.copy(alpha = 0.5f))
                    )
                }

                if (useMetric) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = metricName,
                            onValueChange = { metricName = it },
                            label = { Text("Metric Name (e.g. miles)") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan, focusedLabelColor = ElectricCyan,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetVal,
                            onValueChange = { targetVal = it },
                            label = { Text("Target Value") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCyan, focusedLabelColor = ElectricCyan,
                                focusedTextColor = Color.White, unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.LightGray)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty()) {
                                onConfirm(
                                    name,
                                    desc.ifEmpty { "Forge ahead to complete your Saga!" },
                                    selectedCategory,
                                    if (useMetric) metricName else "",
                                    if (useMetric) targetVal.toFloatOrNull() ?: 1f else 0f
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Forge Quest", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
