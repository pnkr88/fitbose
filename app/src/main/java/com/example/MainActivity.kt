package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CalorieLog
import com.example.data.model.WorkoutLog
import com.example.ui.DailyStat
import com.example.ui.FitnessViewModel
import com.example.ui.TodaySummaryState
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: FitnessViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FitnessApp(viewModel = viewModel)
            }
        }
    }
}

enum class FitnessTab(val title: String, val icon: String, val testTag: String) {
    DASHBOARD("Dashboard", "📊", "dashboard_tab"),
    CALORIES("Calories", "🍳", "calories_tab"),
    WORKOUTS("Workouts", "🏋️‍♂️", "workouts_tab"),
    GOALS("Goals", "🎯", "goals_tab")
}

@Composable
fun FitnessApp(viewModel: FitnessViewModel) {
    var currentTab by remember { mutableStateOf(FitnessTab.DASHBOARD) }
    
    val summaryState by viewModel.todaySummary.collectAsStateWithLifecycle()
    val todayCalorieLogs by viewModel.todayCalorieLogs.collectAsStateWithLifecycle()
    val todayWorkoutLogs by viewModel.todayWorkoutLogs.collectAsStateWithLifecycle()
    val calorieGoal by viewModel.calorieGoal.collectAsStateWithLifecycle()
    val workoutBurnGoal by viewModel.workoutBurnGoal.collectAsStateWithLifecycle()
    val proteinGoal by viewModel.proteinGoal.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            FitnessBottomBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
        ) {
            val isWide = maxWidth > 600.dp
            val modifier = if (isWide) {
                Modifier
                    .widthIn(max = 600.dp)
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp)
            } else {
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            }

            Column(modifier = modifier) {
                FitnessHeader()
                
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (currentTab) {
                        FitnessTab.DASHBOARD -> DashboardScreen(
                            summaryState = summaryState,
                            weeklyStats = viewModel.getWeeklyStats()
                        )
                        FitnessTab.CALORIES -> CaloriesScreen(
                            calorieLogs = todayCalorieLogs,
                            calorieGoal = calorieGoal,
                            onAddCalorie = { name, calories, protein, carbs, fats, type ->
                                viewModel.addCalorieLog(name, calories, protein, carbs, fats, type)
                            },
                            onDeleteCalorie = { log ->
                                viewModel.deleteCalorieLog(log)
                            }
                        )
                        FitnessTab.WORKOUTS -> WorkoutsScreen(
                            workoutLogs = todayWorkoutLogs,
                            workoutGoal = workoutBurnGoal,
                            onAddWorkout = { name, duration, burned, type, intensity ->
                                viewModel.addWorkoutLog(name, duration, burned, type, intensity)
                            },
                            onDeleteWorkout = { log ->
                                viewModel.deleteWorkoutLog(log)
                            }
                        )
                        FitnessTab.GOALS -> GoalsScreen(
                            currentCalorieGoal = calorieGoal,
                            currentWorkoutGoal = workoutBurnGoal,
                            currentProteinGoal = proteinGoal,
                            onGoalsUpdated = { cal, work, prot ->
                                viewModel.updateGoals(cal, work, prot)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FitnessHeader() {
    val currentDateStr = remember {
        val sdf = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        sdf.format(Date()).uppercase()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = currentDateStr,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                ),
                color = BentoTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hello, Alex",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = BentoTextPrimary
            )
        }
        
        // Polished avatar placeholder matching HTML template (bg-[#D7E8CD] border-2 border-[#BBCBB2])
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(BentoMossLight)
                .border(2.dp, BentoMossBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "👤",
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun FitnessBottomBar(
    selectedTab: FitnessTab,
    onTabSelected: (FitnessTab) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight),
        color = BentoSurfaceWhite,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FitnessTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(vertical = 6.dp)
                        .testTag(tab.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) BentoMossLight else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (tab) {
                                FitnessTab.DASHBOARD -> "📱"
                                FitnessTab.CALORIES -> "🥗"
                                FitnessTab.WORKOUTS -> "🏋️‍♂️"
                                FitnessTab.GOALS -> "⚙️"
                            },
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 10.sp
                        ),
                        color = if (isSelected) {
                            BentoMossPrimary
                        } else {
                            BentoTextSecondary.copy(alpha = 0.6f)
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ==========================================
// DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(
    summaryState: TodaySummaryState,
    weeklyStats: List<DailyStat>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Daily Calories Bento Card (Moss Light, rounded 32.dp, moss border)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = BentoMossLight
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoMossBorder)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Daily Calories",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoMossPrimary
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = String.format("%,d", summaryState.caloriesConsumed),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoTextPrimary
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = " / ${String.format("%,d", summaryState.calorieGoal)} kcal",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoTextSecondary
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }

                    // Translucent white icon wrapper
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                            .padding(12.dp)
                    ) {
                        Text(text = "🍳", fontSize = 24.sp)
                    }
                }

                // Bento progress indicator
                val calorieProgress = if (summaryState.calorieGoal > 0) {
                    summaryState.caloriesConsumed.toFloat() / summaryState.calorieGoal.toFloat()
                } else 0f
                val animatedCalorieProgress by animateFloatAsState(
                    targetValue = calorieProgress.coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 1000)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(BentoMossBorder.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedCalorieProgress)
                            .clip(CircleShape)
                            .background(BentoMossPrimary)
                    )
                }
            }
        }

        // Row of two columns for smaller Bento Cards (Strength Workout card & Hydration/Steps stats)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Next/Active Workout Card (White, rounded 32.dp, lavender highlight, border)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoLavender)
                            .size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🏋️‍♂️", fontSize = 20.sp)
                    }

                    Column {
                        Text(
                            text = "Workout Active",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = BentoTextSecondary
                            )
                        )
                        Text(
                            text = "${summaryState.caloriesBurned} kcal",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoTextPrimary
                            )
                        )
                        Text(
                            text = "${summaryState.activeMinutes} mins • Today",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoLavenderText
                            )
                        )
                    }
                }
            }

            // Right column having 2 stacked small bento cards
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Net balance Card (GrayGreen background, rounded 24.dp, border)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoGrayGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoGrayGreenBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoBg)
                                .padding(6.dp)
                        ) {
                            Text(text = "⚡", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "NET BALANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = BentoTextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = "${summaryState.netCalories} kcal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = BentoTextPrimary
                            )
                        }
                    }
                }

                // Hydration Card / Goal Progress Card (Warm Sand Yellow, rounded 24.dp, border)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSandYellow),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoSandYellowBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BentoSandYellowBorder)
                                .padding(6.dp)
                        ) {
                            Text(text = "🎯", fontSize = 16.sp)
                        }
                        Column {
                            Text(
                                text = "GOAL PROGRESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 8.sp,
                                    color = BentoTextSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            )
                            val progressPct = if (summaryState.calorieGoal > 0) {
                                (summaryState.caloriesConsumed.toFloat() / summaryState.calorieGoal.toFloat() * 100).toInt()
                            } else 0
                            Text(
                                text = "$progressPct% Met",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = BentoTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Active Macros Bento Block
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Nutrition Macros Breakdown",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = BentoTextPrimary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Protein
                    MacroCard(
                        modifier = Modifier.weight(1f),
                        label = "Protein",
                        value = "${summaryState.totalProtein}g",
                        target = "${summaryState.proteinGoal}g",
                        color = BentoMossPrimary,
                        icon = "🥩"
                    )
                    // Carbs
                    MacroCard(
                        modifier = Modifier.weight(1f),
                        label = "Carbs",
                        value = "${summaryState.totalCarbs}g",
                        target = "250g",
                        color = BentoClaySecondary,
                        icon = "🍞"
                    )
                    // Fats
                    MacroCard(
                        modifier = Modifier.weight(1f),
                        label = "Fats",
                        value = "${summaryState.totalFats}g",
                        target = "70g",
                        color = BentoTextSecondary,
                        icon = "🥑"
                    )
                }
            }
        }

        // Trend Chart Bento Box (Solid CharcoalDark background, rounded 32.dp, trend highlights)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = BentoCharcoalDark)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activity Trend",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoBg
                        )
                    )
                    Text(
                        text = "Last 7 days",
                        style = MaterialTheme.typography.bodySmall.copy(color = BentoMossLight.copy(alpha = 0.7f))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // High contrast Bento custom chart rendering
                WeeklyStatsChart(weeklyStats = weeklyStats)
            }
        }
    }
}

@Composable
fun MacroCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    target: String,
    color: Color,
    icon: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = BentoTextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = "Target: $target",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = BentoTextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun WeeklyStatsChart(weeklyStats: List<DailyStat>) {
    // Determine maximum scale for drawing bounds
    val maxVal = remember(weeklyStats) {
        val maxLog = weeklyStats.maxOfOrNull { maxOf(it.caloriesConsumed, it.caloriesBurned) } ?: 2000
        maxOf(maxLog, 2500).toFloat()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val itemCount = weeklyStats.size
            
            val spacing = 20.dp.toPx()
            val usableWidth = canvasWidth - (spacing * (itemCount - 1))
            val barWidth = (usableWidth / itemCount) / 2.5f

            weeklyStats.forEachIndexed { index, stat ->
                val xBase = (index * (canvasWidth / itemCount)) + (canvasWidth / itemCount / 2f)

                // Consumed Calorie Bar (Teal -> BentoMossPrimary)
                val consumedHeight = (stat.caloriesConsumed.toFloat() / maxVal) * canvasHeight
                val xConsumed = xBase - barWidth - 2.dp.toPx()
                drawRoundRect(
                    color = BentoMossPrimary,
                    topLeft = androidx.compose.ui.geometry.Offset(xConsumed, canvasHeight - consumedHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, consumedHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )

                // Burned Calorie Bar (Orange -> BentoTrendHighlight / BentoClaySecondary)
                val burnedHeight = (stat.caloriesBurned.toFloat() / maxVal) * canvasHeight
                val xBurned = xBase + 2.dp.toPx()
                drawRoundRect(
                    color = BentoTrendHighlight,
                    topLeft = androidx.compose.ui.geometry.Offset(xBurned, canvasHeight - burnedHeight),
                    size = androidx.compose.ui.geometry.Size(barWidth, burnedHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Chart labels (Days)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weeklyStats.forEach { stat ->
                Text(
                    text = stat.dayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = BentoBg.copy(alpha = 0.8f),
                    modifier = Modifier.width(44.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ==========================================
// CALORIES TRACKING SCREEN
// ==========================================
@Composable
fun CaloriesScreen(
    calorieLogs: List<CalorieLog>,
    calorieGoal: Int,
    onAddCalorie: (String, Int, Int, Int, Int, String) -> Unit,
    onDeleteCalorie: (CalorieLog) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var caloriesStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var carbsStr by remember { mutableStateOf("") }
    var fatsStr by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf("Lunch") }
    var isExpandedForm by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        
        // Progress Summary at Top (Bento-styled budget tracker)
        val consumed = calorieLogs.sumOf { it.calories }
        val remaining = (calorieGoal - consumed).coerceAtLeast(0)
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BentoGrayGreen),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoGrayGreenBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Calorie Budget",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "$consumed / $calorieGoal kcal",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = BentoTextPrimary
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoMossLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoMossBorder)
                ) {
                    Text(
                        text = "$remaining left",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = BentoMossPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Form Expand/Collapse Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpandedForm = !isExpandedForm },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoMossLight)
                            .padding(8.dp)
                    ) {
                        Text(text = "🥗", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isExpandedForm) "Close Food Logger" else "Log Daily Meal / Calorie Intake",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextPrimary
                    )
                }
                Text(
                    text = if (isExpandedForm) "▲" else "▼",
                    fontSize = 12.sp,
                    color = BentoMossPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = isExpandedForm,
            modifier = Modifier.animateContentSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Presets quick selection chips
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextSecondary
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(label = "🍳 Oatmeal (300 Kcal)") {
                            foodName = "Oatmeal"
                            caloriesStr = "300"
                            proteinStr = "12"
                            carbsStr = "50"
                            fatsStr = "5"
                            mealType = "Breakfast"
                        }
                        PresetChip(label = "🥩 Chicken & Rice (550 Kcal)") {
                            foodName = "Chicken Breast with Rice"
                            caloriesStr = "550"
                            proteinStr = "45"
                            carbsStr = "65"
                            fatsStr = "8"
                            mealType = "Lunch"
                        }
                        PresetChip(label = "🥛 Shake (250 Kcal)") {
                            foodName = "Whey Protein Shake"
                            caloriesStr = "250"
                            proteinStr = "30"
                            carbsStr = "5"
                            fatsStr = "3"
                            mealType = "Snack"
                        }
                    }

                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Food or Meal Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("food_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoMossPrimary,
                            unfocusedBorderColor = BentoBorderLight,
                            focusedLabelColor = BentoMossPrimary,
                            unfocusedLabelColor = BentoTextSecondary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = caloriesStr,
                            onValueChange = { caloriesStr = it },
                            label = { Text("Kcal") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("food_calories_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoMossPrimary,
                                unfocusedBorderColor = BentoBorderLight,
                                focusedLabelColor = BentoMossPrimary,
                                unfocusedLabelColor = BentoTextSecondary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = proteinStr,
                            onValueChange = { proteinStr = it },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoMossPrimary,
                                unfocusedBorderColor = BentoBorderLight,
                                focusedLabelColor = BentoMossPrimary,
                                unfocusedLabelColor = BentoTextSecondary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = carbsStr,
                            onValueChange = { carbsStr = it },
                            label = { Text("Carbs (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoMossPrimary,
                                unfocusedBorderColor = BentoBorderLight,
                                focusedLabelColor = BentoMossPrimary,
                                unfocusedLabelColor = BentoTextSecondary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = fatsStr,
                            onValueChange = { fatsStr = it },
                            label = { Text("Fats (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoMossPrimary,
                                unfocusedBorderColor = BentoBorderLight,
                                focusedLabelColor = BentoMossPrimary,
                                unfocusedLabelColor = BentoTextSecondary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    // Meal type selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                            val isSelected = mealType == type
                            Button(
                                onClick = { mealType = type },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) BentoMossPrimary else BentoGrayGreen,
                                    contentColor = if (isSelected) Color.White else BentoTextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = type, fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (foodName.isNotBlank() && caloriesStr.isNotBlank()) {
                                onAddCalorie(
                                    foodName,
                                    caloriesStr.toIntOrNull() ?: 0,
                                    proteinStr.toIntOrNull() ?: 0,
                                    carbsStr.toIntOrNull() ?: 0,
                                    fatsStr.toIntOrNull() ?: 0,
                                    mealType
                                )
                                // Clear inputs
                                foodName = ""
                                caloriesStr = ""
                                proteinStr = ""
                                carbsStr = ""
                                fatsStr = ""
                                isExpandedForm = false
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_calorie_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoMossPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Log Food")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Add to Tracker", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Meal Logs List
        Text(
            text = "Today's Intake logs",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = BentoTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (calorieLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🥗", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No calorie logs recorded today.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextSecondary
                    )
                    Text(
                        text = "Tap log daily meal above to record food intake.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(calorieLogs) { log ->
                    CalorieLogItem(log = log, onDelete = { onDeleteCalorie(log) })
                }
            }
        }
    }
}

@Composable
fun PresetChip(label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .padding(2.dp),
        shape = RoundedCornerShape(12.dp),
        color = BentoGrayGreen,
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoGrayGreenBorder)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = BentoTextPrimary
        )
    }
}

@Composable
fun CalorieLogItem(
    log: CalorieLog,
    onDelete: () -> Unit
) {
    val mealEmoji = when (log.mealType) {
        "Breakfast" -> "🍳"
        "Lunch" -> "🥗"
        "Dinner" -> "🥩"
        else -> "🍎"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BentoMossLight)
                        .border(1.dp, BentoMossBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = mealEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = log.foodName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row {
                        Text(
                            text = "${log.mealType} • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextSecondary
                        )
                        Text(
                            text = "P: ${log.proteinGrams}g C: ${log.carbsGrams}g F: ${log.fatsGrams}g",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = BentoMossPrimary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${log.calories} kcal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = BentoTextPrimary
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_calorie_${log.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete food",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ==========================================
// WORKOUTS SCREEN
// ==========================================
@Composable
fun WorkoutsScreen(
    workoutLogs: List<WorkoutLog>,
    workoutGoal: Int,
    onAddWorkout: (String, Int, Int, String, String) -> Unit,
    onDeleteWorkout: (WorkoutLog) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("") }
    var caloriesBurnedStr by remember { mutableStateOf("") }
    var workoutType by remember { mutableStateOf("Strength") }
    var intensity by remember { mutableStateOf("Medium") }
    var isExpandedForm by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {

        // Workout Summary Card (Bento Lavender styled)
        val burned = workoutLogs.sumOf { it.caloriesBurned }
        val duration = workoutLogs.sumOf { it.durationMinutes }
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BentoLavender),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoLavenderText.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Burn Goal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BentoLavenderText
                    )
                    Text(
                        text = "$burned / $workoutGoal kcal",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = BentoTextPrimary
                    )
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
                ) {
                    Text(
                        text = "$duration mins",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = BentoLavenderText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Workout Logger header trigger
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpandedForm = !isExpandedForm },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(BentoLavender)
                            .padding(8.dp)
                    ) {
                        Text(text = "🏃‍♂️", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isExpandedForm) "Close Logger" else "Log Fitness Activity / Workout",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextPrimary
                    )
                }
                Text(
                    text = if (isExpandedForm) "▲" else "▼",
                    fontSize = 12.sp,
                    color = BentoLavenderText,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(
            visible = isExpandedForm,
            modifier = Modifier.animateContentSize()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextSecondary
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(label = "🏃‍♂️ Run 5K (400 Kcal)") {
                            workoutName = "5K Run"
                            durationStr = "30"
                            caloriesBurnedStr = "400"
                            workoutType = "Cardio"
                            intensity = "High"
                        }
                        PresetChip(label = "🏋️‍♂️ Lift Weights (250)") {
                            workoutName = "Strength Lifting"
                            durationStr = "45"
                            caloriesBurnedStr = "250"
                            workoutType = "Strength"
                            intensity = "Medium"
                        }
                        PresetChip(label = "🧘‍♂️ Yoga (120 Kcal)") {
                            workoutName = "Power Yoga"
                            durationStr = "40"
                            caloriesBurnedStr = "120"
                            workoutType = "Flexibility"
                            intensity = "Low"
                        }
                    }

                    OutlinedTextField(
                        value = workoutName,
                        onValueChange = { workoutName = it },
                        label = { Text("Workout / Exercise Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("workout_name_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoLavenderText,
                            unfocusedBorderColor = BentoBorderLight,
                            focusedLabelColor = BentoLavenderText,
                            unfocusedLabelColor = BentoTextSecondary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = durationStr,
                            onValueChange = { durationStr = it },
                            label = { Text("Duration (mins)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("workout_duration_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoLavenderText,
                                unfocusedBorderColor = BentoBorderLight,
                                focusedLabelColor = BentoLavenderText,
                                unfocusedLabelColor = BentoTextSecondary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )

                        OutlinedTextField(
                            value = caloriesBurnedStr,
                            onValueChange = { caloriesBurnedStr = it },
                            label = { Text("Burned Kcal") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("workout_burned_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoLavenderText,
                                unfocusedBorderColor = BentoBorderLight,
                                focusedLabelColor = BentoLavenderText,
                                unfocusedLabelColor = BentoTextSecondary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    // Workout Category type selection
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Cardio", "Strength", "HIIT", "Flexibility").forEach { type ->
                            val isSelected = workoutType == type
                            Button(
                                onClick = { workoutType = type },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) BentoLavenderText else BentoBg,
                                    contentColor = if (isSelected) Color.White else BentoTextPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = type, fontSize = 11.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Intensity Selector
                    Text(
                        text = "Intensity Level",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            val isSelected = intensity == level
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { intensity = level },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        BentoLavender.copy(alpha = 0.5f)
                                    } else {
                                        BentoBg
                                    }
                                ),
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(1.5.dp, BentoLavenderText)
                                } else androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
                            ) {
                                Text(
                                    text = level,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) BentoLavenderText else BentoTextSecondary
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (workoutName.isNotBlank() && durationStr.isNotBlank() && caloriesBurnedStr.isNotBlank()) {
                                onAddWorkout(
                                    workoutName,
                                    durationStr.toIntOrNull() ?: 0,
                                    caloriesBurnedStr.toIntOrNull() ?: 0,
                                    workoutType,
                                    intensity
                                )
                                workoutName = ""
                                durationStr = ""
                                caloriesBurnedStr = ""
                                isExpandedForm = false
                                focusManager.clearFocus()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("add_workout_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BentoLavenderText,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Log Workout")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Log Workout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Workouts log list
        Text(
            text = "Today's Workouts logs",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = BentoTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (workoutLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🏋️‍♂️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No workouts logged today.",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextSecondary
                    )
                    Text(
                        text = "Tap log fitness activity above to track calorie burns.",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(workoutLogs) { log ->
                    WorkoutLogItem(log = log, onDelete = { onDeleteWorkout(log) })
                }
            }
        }
    }
}

@Composable
fun WorkoutLogItem(
    log: WorkoutLog,
    onDelete: () -> Unit
) {
    val typeEmoji = when (log.workoutType) {
        "Cardio" -> "🏃‍♂️"
        "Strength" -> "🏋️‍♂️"
        "HIIT" -> "⚡"
        else -> "🧘‍♂️"
    }

    val (badgeBg, badgeText) = when (log.intensity) {
        "High" -> Pair(BentoClaySecondary.copy(alpha = 0.2f), BentoClaySecondary)
        "Medium" -> Pair(BentoSandYellow, BentoTextPrimary)
        else -> Pair(BentoGrayGreen, BentoTextPrimary)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BentoLavender)
                        .border(1.dp, BentoLavenderText.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = typeEmoji, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = log.workoutName,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = BentoTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${log.durationMinutes} mins • ${log.workoutType}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextSecondary
                        )
                        
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = badgeBg)
                        ) {
                            Text(
                                text = log.intensity,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = badgeText,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${log.caloriesBurned} kcal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = BentoTrendHighlight
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_workout_${log.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete workout",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ==========================================
// GOALS / SETTINGS SCREEN
// ==========================================
@Composable
fun GoalsScreen(
    currentCalorieGoal: Int,
    currentWorkoutGoal: Int,
    currentProteinGoal: Int,
    onGoalsUpdated: (Int, Int, Int) -> Unit
) {
    var caloriesInput by remember { mutableStateOf(currentCalorieGoal.toString()) }
    var workoutInput by remember { mutableStateOf(currentWorkoutGoal.toString()) }
    var proteinInput by remember { mutableStateOf(currentProteinGoal.toString()) }
    var successMessage by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Configure Target Goals",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = BentoTextPrimary
        )
        Text(
            text = "Fine-tune daily calorie intake, calorie burn limits, and macro nutrients to align with your personal fitness strategy.",
            style = MaterialTheme.typography.bodyMedium,
            color = BentoTextSecondary
        )

        AnimatedVisibility(visible = successMessage) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BentoMossLight),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoMossBorder)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "✅", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Target limits calibrated successfully!",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = BentoMossPrimary
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = BentoSurfaceWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderLight)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = caloriesInput,
                    onValueChange = { caloriesInput = it },
                    label = { Text("Daily Calorie Intake Target (Kcal)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_calories_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoMossPrimary,
                        unfocusedBorderColor = BentoBorderLight,
                        focusedLabelColor = BentoMossPrimary,
                        unfocusedLabelColor = BentoTextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = workoutInput,
                    onValueChange = { workoutInput = it },
                    label = { Text("Daily Workout Burn Target (Kcal)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_workouts_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoMossPrimary,
                        unfocusedBorderColor = BentoBorderLight,
                        focusedLabelColor = BentoMossPrimary,
                        unfocusedLabelColor = BentoTextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                OutlinedTextField(
                    value = proteinInput,
                    onValueChange = { proteinInput = it },
                    label = { Text("Daily Protein Intake Target (g)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("target_protein_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoMossPrimary,
                        unfocusedBorderColor = BentoBorderLight,
                        focusedLabelColor = BentoMossPrimary,
                        unfocusedLabelColor = BentoTextSecondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Button(
                    onClick = {
                        val cal = caloriesInput.toIntOrNull() ?: currentCalorieGoal
                        val work = workoutInput.toIntOrNull() ?: currentWorkoutGoal
                        val prot = proteinInput.toIntOrNull() ?: currentProteinGoal
                        
                        onGoalsUpdated(cal, work, prot)
                        successMessage = true
                        focusManager.clearFocus()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_goals_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BentoMossPrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "Save & Apply Targets",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
