package com.imnotesminimal.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AboutPrivacyScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.NoteEditorScreen
import com.example.ui.screens.NoteListScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TrashScreen
import com.example.ui.theme.MyApplicationTheme
import com.imnotesminimal.app.ui.NoteViewModel

class MainActivity : ComponentActivity() {
    private var pendingOAuthUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingOAuthUri = intent?.data
        enableEdgeToEdge()
        setContent {
            val viewModel: NoteViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            var oauthMessage by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(pendingOAuthUri) {
                val uri = pendingOAuthUri
                if (uri != null && uri.scheme == "com.imnotesminimal.app" && uri.host == "login-callback") {
                    viewModel.handleGoogleOAuthCallback(uri) { success, message ->
                        oauthMessage = message
                    }
                    pendingOAuthUri = null
                }
            }

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
                                oauthMessage = oauthMessage,
                                onOAuthMessageConsumed = { oauthMessage = null },
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingOAuthUri = intent.data
    }
}
