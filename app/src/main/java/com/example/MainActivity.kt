package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.Screens
import com.example.ui.components.StarsBackground
import com.example.ui.components.StarsBackground
import com.example.ui.screens.GameScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.theme.BgDark
import com.example.ui.theme.MyApplicationTheme
import com.example.utils.DeviceManager
import com.example.viewmodel.GameEvent
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private lateinit var deviceManager: DeviceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deviceManager = DeviceManager(this)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BgDark
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        val navController = rememberNavController()
                        val soundEnabled by viewModel.soundEnabled.collectAsState()
                        
                        // Background with stars
                        com.example.ui.components.StarsBackground()

                        LaunchedEffect(Unit) {
                            viewModel.events.collectLatest { event ->
                                when (event) {
                                    is GameEvent.Correct -> deviceManager.playOk(soundEnabled)
                                    is GameEvent.Wrong -> deviceManager.playErr(soundEnabled)
                                    is GameEvent.Win -> deviceManager.playWin(soundEnabled)
                                    is GameEvent.Fail -> deviceManager.playFail(soundEnabled)
                                }
                            }
                        }

                        NavHost(navController = navController, startDestination = Screens.MENU) {
                            composable(Screens.MENU) {
                                MenuScreen(viewModel, navController)
                            }
                            composable(Screens.GAME) {
                                GameScreen(viewModel, navController)
                            }
                            composable(Screens.RESULT) {
                                ResultScreen(viewModel, navController)
                            }
                            composable(Screens.LEADERBOARD) {
                                LeaderboardScreen(viewModel, navController)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deviceManager.release()
    }
}

