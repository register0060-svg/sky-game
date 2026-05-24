package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.models.GameDifficulty
import com.example.ui.Screens
import com.example.ui.theme.*
import com.example.viewmodel.GameEvent
import com.example.viewmodel.GameState
import com.example.viewmodel.GameViewModel
import kotlin.random.Random

@Composable
fun GameScreen(viewModel: GameViewModel, navController: NavController) {
    val state by viewModel.state.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()

    // Handle end game navigation
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) {
            navController.navigate(Screens.RESULT) {
                popUpTo(Screens.GAME) { inclusive = true }
            }
        }
    }

    BackHandler {
        navController.navigate(Screens.MENU) {
            popUpTo(Screens.MENU) { inclusive = true }
        }
    }

    // Shake animation for mistakes
    var shakeOffset by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(state.mistakes) {
        if (state.mistakes > 0) {
            val startTime = withFrameNanos { it }
            do {
                val playTimeNanos = withFrameNanos { it } - startTime
                val playTimeMs = playTimeNanos / 1_000_000L
                val progress = playTimeMs / 400f
                shakeOffset = java.lang.Math.sin(progress * java.lang.Math.PI * 4).toFloat() * 10f
            } while (playTimeNanos < 400_000_000L)
            shakeOffset = 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // TOP HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = {
                    navController.navigate(Screens.MENU) {
                        popUpTo(Screens.MENU) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("← Кері", color = TextSecondary, fontSize = 12.sp)
            }
            
            Text(
                text = "${state.mode.emoji} ${state.mode.title} · ${state.diff.emoji} ${state.diff.title}",
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Bold
            )
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.toggleSound() }) {
                    Text(if (soundEnabled) "🔈" else "🔇", fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Қате", fontSize = 10.sp, color = TextTertiary)
                    Text("${state.mistakes}", fontSize = 16.sp, color = ErrorRed, fontWeight = FontWeight.Black)
                }
            }
        }

        // TIME BAR
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val timeColor = if (state.timeLeft > 10) SuccessGreen else if (state.timeLeft > 5) WarningOrange else ErrorRed
            val timeProgress = if (state.totalTime > 0) state.timeLeft.toFloat() / state.totalTime else 0f
            
            val animatedProgress by animateFloatAsState(targetValue = timeProgress, label = "time")
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(3.dp))
                        .background(timeColor)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${state.timeLeft}",
                color = timeColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.width(30.dp)
            )
        }

        // TARGET ROW
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("ТАП", fontSize = 10.sp, color = TextTertiary, fontWeight = FontWeight.Bold)
                Text(
                    text = "${state.target}",
                    fontSize = 40.sp,
                    color = state.mode.color,
                    fontWeight = FontWeight.Black
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                if (state.combo >= 3) {
                    Text("🔥 ×${state.combo} КОМБО", fontSize = 12.sp, color = WarningOrange, fontWeight = FontWeight.Black)
                }
                Text("${state.found.size}/${state.mode.numbersCount}", fontSize = 16.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GAME AREA
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer { translationX = shakeAnim.value },
            contentAlignment = Alignment.Center
        ) {
            if (state.mode.isChaos) {
                ChaosField(state, viewModel)
            } else {
                GridField(state, viewModel)
            }
        }

        // PROGRESS BAR
        Spacer(modifier = Modifier.height(8.dp))
        val findProgress = if (state.mode.numbersCount > 0) state.found.size.toFloat() / state.mode.numbersCount else 0f
        val animFindProgress by animateFloatAsState(targetValue = findProgress, label = "found")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animFindProgress)
                    .clip(RoundedCornerShape(2.dp))
                    .background(state.mode.color)
            )
        }
    }
}

@Composable
fun GridField(state: GameState, viewModel: GameViewModel) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(state.mode.columns),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        itemsIndexed(state.numbers) { index, num ->
            CellItem(
                num = num,
                index = index,
                state = state,
                onClick = { viewModel.tapNumber(num, index) }
            )
        }
    }
}

@Composable
fun CellItem(num: Int, index: Int, state: GameState, onClick: () -> Unit) {
    val isFound = state.found.contains(num)
    val isOk = state.okCell == index
    val isErr = state.errorCell == index
    val isHard = state.diff == GameDifficulty.HARD && !state.mode.isChaos
    val props = state.cellProps.getOrNull(index)

    val bgColor by animateColorAsState(
        targetValue = when {
            isOk -> SuccessGreen.copy(alpha = 0.4f)
            isErr -> ErrorRed.copy(alpha = 0.4f)
            isFound -> SuccessGreen.copy(alpha = 0.1f)
            else -> CardBg
        }, label = "bg"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isOk -> SuccessGreen
            isErr -> ErrorRed
            isFound -> SuccessGreen.copy(alpha = 0.3f)
            else -> CardBorder
        }, label = "border"
    )

    val txtColor by animateColorAsState(
        targetValue = when {
            isFound -> SuccessGreen
            else -> TextSecondary
        }, label = "txt"
    )

    val scale by animateFloatAsState(
        targetValue = when {
            isOk -> 1.2f
            isErr -> 0.8f
            else -> 1f
        }, label = "scale"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = !isFound, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer {
                if (isHard && props != null && !isFound) {
                    rotationZ = props.rotation
                    translationX = props.translateX
                    translationY = props.translateY
                    scaleX = props.sizeScale
                    scaleY = props.sizeScale
                    alpha = props.opacity
                }
            },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isFound) "✓" else num.toString(),
                color = txtColor,
                fontWeight = FontWeight.Black,
                fontSize = if (state.mode.columns <= 5) 20.sp else 16.sp
            )
        }
    }
}

@Composable
fun ChaosField(state: GameState, viewModel: GameViewModel) {
    val config = LocalConfiguration.current
    val screenWidth = config.screenWidthDp.dp
    val screenHeight = config.screenHeightDp.dp * 0.6f // Estimate available height

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.01f))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
    ) {
        state.numbers.forEachIndexed { index, num ->
            val pos = state.chaosPositions.getOrNull(index) ?: Pair(0.5f, 0.5f)
            val props = state.cellProps.getOrNull(index)
            
            // Jittered positioning based on grid approach
            val totalAspect = maxOf(0.1f, screenWidth.value) / maxOf(0.1f, screenHeight.value)
            val cols = Math.ceil(Math.sqrt(state.mode.numbersCount.toDouble() * totalAspect)).toInt().coerceAtLeast(1)
            val rows = Math.ceil(state.mode.numbersCount.toDouble() / cols).toInt().coerceAtLeast(1)
            
            val cw = screenWidth.value / cols
            val ch = screenHeight.value / rows
            
            // Just use the random uniform positions for simplicity in Compose but with margins
            val xPerc = pos.first * 0.8f + 0.1f // Keep away from edges
            val yPerc = pos.second * 0.8f + 0.1f

            val isFound = state.found.contains(num)
            val isOk = state.okCell == index
            val isErr = state.errorCell == index

            val bgColor by animateColorAsState(
                targetValue = when {
                    isOk -> SuccessGreen.copy(alpha = 0.4f)
                    isErr -> ErrorRed.copy(alpha = 0.4f)
                    isFound -> SuccessGreen.copy(alpha = 0.1f)
                    else -> CardBg.copy(alpha = 0.5f)
                }, label = "bg"
            )
            
            val borderColor by animateColorAsState(
                targetValue = when {
                    isOk -> SuccessGreen
                    isErr -> ErrorRed
                    isFound -> SuccessGreen.copy(alpha = 0.2f)
                    else -> CardBorder
                }, label = "border"
            )

            val txtColor by animateColorAsState(
                targetValue = when {
                    isFound -> SuccessGreen
                    else -> TextSecondary
                }, label = "txt"
            )

            val rootScale by animateFloatAsState(
                targetValue = when {
                    isOk -> 1.3f
                    isErr -> 0.8f
                    else -> 1f
                }, label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (screenWidth - 32.dp) * xPerc, y = (screenHeight - 32.dp) * yPerc)
                        .graphicsLayer {
                            scaleX = rootScale
                            scaleY = rootScale
                            if (props != null && !isFound) {
                                rotationZ = props.rotation
                                alpha = props.opacity
                            }
                            if (isFound) alpha = 0.3f
                        }
                        .size(if (props != null) (40 * props.sizeScale).dp else 40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .clickable(enabled = !isFound) { viewModel.tapNumber(num, index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isFound) "✓" else num.toString(),
                        color = txtColor,
                        fontWeight = FontWeight.Black,
                        fontSize = if (props != null) (20 * props.sizeScale).sp else 20.sp
                    )
                }
            }
        }
    }
}
