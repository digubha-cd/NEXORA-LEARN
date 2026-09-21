package com.example.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.core.model.StudentProfile
import com.example.core.repository.AuthRepository
import com.example.core.repository.AuthState
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainAppScreen
import com.example.ui.screens.SplashScreen
import kotlinx.coroutines.launch

object NexoraDestinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val MAIN = "main"
}

/**
 * Main Navigation Host managing flow between Splash, Login/Onboarding, and Home.
 */
@Composable
fun NexoraNavHost(
    authRepository: AuthRepository,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val authState by authRepository.authState.collectAsState()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = NexoraDestinations.SPLASH,
        modifier = modifier
    ) {
        // 1. Splash Destination
        composable(
            route = NexoraDestinations.SPLASH,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            SplashScreen(
                onSplashComplete = {
                    val nextRoute = if (authState is AuthState.Authenticated) {
                        NexoraDestinations.MAIN
                    } else {
                        NexoraDestinations.LOGIN
                    }
                    navController.navigate(nextRoute) {
                        popUpTo(NexoraDestinations.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Login / Onboarding Destination
        composable(
            route = NexoraDestinations.LOGIN,
            enterTransition = { fadeIn() + slideInHorizontally { it / 2 } },
            exitTransition = { fadeOut() + slideOutHorizontally { -it / 2 } }
        ) {
            LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = {
                    navController.navigate(NexoraDestinations.MAIN) {
                        popUpTo(NexoraDestinations.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 3. Main App Screen (Home, Subjects, Profile with Bottom Navigation)
        composable(
            route = NexoraDestinations.MAIN,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            val currentProfile = (authState as? AuthState.Authenticated)?.profile
                ?: StudentProfile(
                    userId = "default_user",
                    studentName = "Class 12 Student",
                    medium = "Gujarati Medium",
                    classLevel = "Class 12 Commerce",
                    targetMarks = "90+ Marks"
                )

            MainAppScreen(
                studentProfile = currentProfile,
                onSignOut = {
                    scope.launch {
                        authRepository.signOut()
                        navController.navigate(NexoraDestinations.LOGIN) {
                            popUpTo(NexoraDestinations.MAIN) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
