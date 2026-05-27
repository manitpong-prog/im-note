package com.imnotesminimal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.imnotesminimal.app.ui.NoteViewModel
import com.example.ui.screens.AboutPrivacyScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NoteListScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NoteViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "list"
                    ) {
                        composable("list") {
                            NoteListScreen(
                                viewModel = viewModel,
                                onNoteClick = { noteId ->
                                    if (noteId != null) {
                                        navController.navigate("editor?noteId=$noteId")
                                    } else {
                                        navController.navigate("editor")
                                    }
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToLogin = {
                                    navController.navigate("login")
                                },
                                onNavigateToTrash = {
                                    navController.navigate("trash")
                                },
                                onNavigateToAboutPrivacy = {
                                    navController.navigate("about_privacy")
                                }
                            )
                        }
                        composable("trash") {
                            TrashScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("about_privacy") {
                            AboutPrivacyScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onLoginSuccess = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onRegisterSuccess = {
                                    navController.popBackStack("settings", inclusive = false)
                                }
                            )
                        }
                        composable(
                            route = "editor?noteId={noteId}",
                            arguments = listOf(
                                navArgument("noteId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val noteIdStr = backStackEntry.arguments?.getString("noteId")
                            val noteId = noteIdStr?.toIntOrNull()

                            NoteEditorScreen(
                                noteId = noteId,
                                viewModel = viewModel,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
