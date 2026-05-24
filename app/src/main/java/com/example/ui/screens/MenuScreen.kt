package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
fun MenuScreen(viewModel: GameViewModel, navController: NavController) {
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    
    var selectedMode by remember { mutableStateOf<GameMode?>(null) }
    var showDiffDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logo
        Text("🌌", fontSize = 52.sp)
        Text(
            text = "SKY GAME",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = S1Blue,
            letterSpacing = 6.sp
        )
        Text(
            text = "САНДАР ОЙЫНЫ · СЫНАҚ УАҚЫТЫ",
            fontSize = 10.sp,
            color = TextTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // Top actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { navController.navigate(Screens.LEADERBOARD) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("🏆 Рейтинг", color = TextSecondary, fontSize = 12.sp)
            }
            
            Button(
                onClick = { viewModel.toggleSound() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (soundEnabled) S1Blue.copy(alpha = 0.1f) else CardBg
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (soundEnabled) "🔈 Дыбыс" else "🔇 Дыбыс",
                    color = if (soundEnabled) S1Blue else TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "— ДЕҢГЕЙЛЕР —",
            color = TextTertiary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Modes List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(GameMode.entries) { mode ->
                ModeCard(mode, viewModel.prefs) {
                    selectedMode = mode
                    showDiffDialog = true
                }
            }
        }
    }

    if (showDiffDialog && selectedMode != null) {
        ModalBottomSheet(
            onDismissRequest = { showDiffDialog = false },
            containerColor = BgDark,
            dragHandle = { BottomSheetDefaults.DragHandle(color = CardBorder) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(selectedMode!!.emoji, fontSize = 32.sp)
                Text(
                    selectedMode!!.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "${selectedMode!!.numbersCount} сан · ${selectedMode!!.columns} баған",
                    fontSize = 12.sp,
                    color = TextTertiary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                GameDifficulty.entries.forEach { diff ->
                    DifficultyCard(mode = selectedMode!!, diff = diff, prefs = viewModel.prefs) {
                        showDiffDialog = false
                        viewModel.startGame(selectedMode!!, diff)
                        navController.navigate(Screens.GAME) {
                            popUpTo(Screens.MENU)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ModeCard(mode: GameMode, prefs: com.example.data.PreferencesManager, onClick: () -> Unit) {
    val bests = GameDifficulty.entries.mapNotNull { prefs.getBestTime(mode.id, it.id) }.minOrNull()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBg)
            .border(1.dp, mode.color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mode.id.toString().padStart(2, '0'),
            fontSize = 12.sp,
            color = TextTertiary,
            modifier = Modifier.width(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(mode.emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(mode.title, color = mode.color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (bests != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "🏆 ${bests}с",
                        color = mode.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(mode.color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                "${mode.numbersCount} сан · ${mode.baseTimeSeconds}с · ${if (mode.isChaos) "6–10" else "1–5"}",
                color = TextTertiary,
                fontSize = 11.sp
            )
        }
        
        val tagColor = if (mode.isChaos) ErrorRed else WarningOrange
        Text(
            if (mode.isChaos) "🔴ХАОС" else "🟡HARD",
            color = tagColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(tagColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                .border(1.dp, tagColor.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DifficultyCard(mode: GameMode, diff: GameDifficulty, prefs: com.example.data.PreferencesManager, onClick: () -> Unit) {
    val best = prefs.getBestTime(mode.id, diff.id)
    val totalTime = mode.baseTimeSeconds + diff.timeMod
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(diff.bg)
            .border(1.dp, diff.color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(diff.emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(diff.title, color = diff.color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                "${diff.description}${if (best != null) " · 🏆 ${best}с" else ""}",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        Text("${totalTime}с", color = diff.color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
