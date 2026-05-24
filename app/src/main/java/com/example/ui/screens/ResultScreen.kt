package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.models.GameDifficulty
import com.example.models.GameMode
import com.example.ui.Screens
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(viewModel: GameViewModel, navController: NavController) {
    val state by viewModel.state.collectAsState()
    val userName by viewModel.userName.collectAsState()

    var nameInput by remember { mutableStateOf(userName) }
    var isSubmitted by remember { mutableStateOf(false) }

    BackHandler {
        navController.navigate(Screens.MENU) {
            popUpTo(Screens.MENU) { inclusive = true }
        }
    }

    val stars = when {
        !state.isWin -> 0
        state.mistakes == 0 || state.mistakes <= 3 -> 3
        else -> 2
    }

    val nextMode = getNextLevel(state.mode, state.diff)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Result Card
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (state.isWin) "🎉" else "⏰", fontSize = 48.sp)
            Text(
                if (state.isWin) "ЖЕҢДІҢ!" else "УАҚЫТ БІТТІ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary,
                letterSpacing = 2.sp
            )
            Text(
                if (state.isWin) "${state.mode.title} режимін аяқтадың" else "${state.found.size} / ${state.mode.numbersCount} санды таптың",
                fontSize = 11.sp,
                color = TextTertiary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Badge
            Text(
                "${state.diff.emoji} ${state.diff.title}",
                color = state.diff.color,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .background(state.diff.bg, RoundedCornerShape(20.dp))
                    .border(1.dp, state.diff.color.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 11.dp, vertical = 3.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stars
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 1..3) {
                    Text(
                        "⭐",
                        fontSize = 30.sp,
                        modifier = Modifier.graphicsLayer { alpha = if (i <= stars) 1f else 0.2f }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatBox(modifier = Modifier.weight(1f), label = "Уақыт", value = "${String.format("%.1f", state.elapsed)}с", color = state.mode.color)
                StatBox(modifier = Modifier.weight(1f), label = "Таптым", value = "${state.found.size}/${state.mode.numbersCount}", color = S1Blue)
                StatBox(modifier = Modifier.weight(1f), label = "Қателер", value = "${state.mistakes}", color = if (state.mistakes == 0) SuccessGreen else ErrorRed)
            }

            if (state.isWin && state.elapsed > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                val speed = state.mode.numbersCount / state.elapsed
                Text("Жылдамдық: ${String.format("%.1f", speed)} сан/с", fontSize = 10.sp, color = TextSecondary)
            }

            if (state.isNewBest) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "🏆 ЖАҢА РЕКОРД!",
                    color = WarningOrange,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .background(WarningOrange.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .border(1.dp, WarningOrange.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(9.dp)
                )
            }
            if (state.isWin) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ұпай: ${state.finalScore}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = S2Purple)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Submit LB
        if (state.isWin && !isSubmitted) {
            Column(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🏆 РЕЙТИНГКЕ КІРУ", color = TextTertiary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { if (it.length <= 10) nameInput = it.uppercase() },
                        placeholder = { Text("АТЫҢДЫ ЖАЗ", fontSize = 10.sp, color = TextTertiary) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = S1Blue,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = CardBg.copy(alpha = 0.2f),
                            unfocusedContainerColor = CardBg,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                viewModel.setUserName(nameInput)
                                viewModel.submitScore()
                                isSubmitted = true
                            }
                        },
                        enabled = nameInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = S1Blue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Text("ЖІБЕР", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        } else if (isSubmitted) {
            Text("✓ Рейтингке кірдің!", color = SuccessGreen, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Actions
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (nextMode != null && state.isWin) {
                Button(
                    onClick = {
                        viewModel.startGame(nextMode.first, nextMode.second)
                        navController.navigate(Screens.GAME) {
                            popUpTo(Screens.MENU)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = nextMode.first.color),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        "${nextMode.first.emoji} ${nextMode.first.title} · ${nextMode.second.emoji} ${nextMode.second.title} →",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.startGame(state.mode, state.diff)
                    navController.navigate(Screens.GAME) {
                        popUpTo(Screens.MENU)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("↺ Қайтадан", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }

            Button(
                onClick = {
                    navController.navigate(Screens.LEADERBOARD)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🏆 Рейтингті қарау", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
            }

            Button(
                onClick = {
                    navController.navigate(Screens.MENU) {
                        popUpTo(Screens.MENU) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                border = null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("← Мәзірге қайт", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun StatBox(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CardBg)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 8.sp, color = TextTertiary, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
    }
}

fun getNextLevel(currentMode: GameMode, currentDiff: GameDifficulty): Pair<GameMode, GameDifficulty>? {
    val diffs = GameDifficulty.entries
    val modes = GameMode.entries
    val dIndex = diffs.indexOf(currentDiff)
    val mIndex = modes.indexOf(currentMode)

    if (dIndex < diffs.size - 1) {
        return Pair(currentMode, diffs[dIndex + 1])
    }
    if (mIndex < modes.size - 1) {
        return Pair(modes[mIndex + 1], diffs[0])
    }
    return null
}
