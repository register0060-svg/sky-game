package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.models.ScoreEntry
import com.example.ui.theme.*
import com.example.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LeaderboardScreen(viewModel: GameViewModel, navController: NavController) {
    var selectedFilter by remember { mutableStateOf("all") }
    val scores by viewModel.getTopScoresByDiff(selectedFilter).collectAsState(initial = emptyList())
    val userName by viewModel.userName.collectAsState()

    val filters = listOf(
        Pair("all", "Барлығы"),
        Pair("easy", "🟢 Оңай"),
        Pair("medium", "🟡 Орташа"),
        Pair("hard", "🔴 Қиын")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("← Кері", color = TextSecondary, fontSize = 10.sp)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                "РЕЙТИНГ",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = S1Blue,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(50.dp)) // Balance back button
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { (id, label) ->
                val isSelected = selectedFilter == id
                Button(
                    onClick = { selectedFilter = id },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) S1Blue.copy(alpha = 0.15f) else CardBg
                    ),
                    border = BorderStroke(1.dp, if (isSelected) S1Blue.copy(alpha = 0.5f) else CardBorder),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 24.dp)
                ) {
                    Text(
                        label,
                        color = if (isSelected) S1Blue else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // My Rank Header (Optional)
        val myRankIndex = scores.indexOfFirst { it.name == userName }
        if (myRankIndex >= 0) {
            val myScore = scores[myRankIndex]
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(S1Blue.copy(alpha = 0.1f))
                    .border(1.dp, S1Blue.copy(alpha = 0.3f), RoundedCornerShape(11.dp))
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Менің орным: #${myRankIndex + 1} · ${myScore.score} ұпай",
                    color = S1Blue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Leaderboard List
        if (scores.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Рейтинг бос\nБірінші бол! 🏆",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(scores) { index, entry ->
                    LeaderboardRow(entry, index, userName)
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: ScoreEntry, index: Int, myName: String) {
    val isMe = myName.isNotBlank() && entry.name == myName
    val isGold = index == 0
    val medals = listOf("🥇", "🥈", "🥉")
    
    val bgColor = when {
        isGold -> WarningOrange.copy(alpha = 0.1f)
        isMe -> S1Blue.copy(alpha = 0.1f)
        else -> CardBg
    }
    
    val borderColor = when {
        isGold -> WarningOrange.copy(alpha = 0.3f)
        isMe -> S1Blue.copy(alpha = 0.3f)
        else -> CardBorder
    }

    val rankColor = when(index) {
        0 -> WarningOrange
        1 -> TextSecondary
        2 -> Color(0xFFCD7F32) // Bronze
        else -> TextTertiary
    }

    val rankText = if (index < 3) medals[index] else "${index + 1}"
    val dateFormat = SimpleDateFormat("dd MMM", Locale("kk", "KZ"))
    val dateStr = dateFormat.format(Date(entry.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            rankText,
            color = rankColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            if (isMe) "${entry.name} 👈" else entry.name,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(horizontalAlignment = Alignment.End) {
            Text("${entry.score}", color = S1Blue, fontSize = 14.sp, fontWeight = FontWeight.Black)
            Text(
                "${entry.modeName} · ${String.format("%.1f", entry.time)}с · $dateStr",
                color = TextTertiary,
                fontSize = 9.sp
            )
        }
    }
}
