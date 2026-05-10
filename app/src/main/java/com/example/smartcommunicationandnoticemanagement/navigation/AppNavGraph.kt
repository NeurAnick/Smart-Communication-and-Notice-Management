package com.example.smartcommunicationandnoticemanagement.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smartcommunicationandnoticemanagement.ui.admin.AdminDashboardScreen
import com.example.smartcommunicationandnoticemanagement.ui.admin.EditNoticeScreen
import com.example.smartcommunicationandnoticemanagement.ui.admin.SeenByScreen
import com.example.smartcommunicationandnoticemanagement.ui.auth.ForgotPasswordScreen
import com.example.smartcommunicationandnoticemanagement.ui.auth.LoginScreen
import com.example.smartcommunicationandnoticemanagement.ui.auth.RegisterScreen
import com.example.smartcommunicationandnoticemanagement.ui.auth.SplashScreen
import com.example.smartcommunicationandnoticemanagement.ui.chat.GroupChatScreen
import com.example.smartcommunicationandnoticemanagement.ui.faq.FAQScreen
import com.example.smartcommunicationandnoticemanagement.ui.notice.NoticeDetailScreen
import com.example.smartcommunicationandnoticemanagement.ui.profile.EditProfileScreen
import com.example.smartcommunicationandnoticemanagement.ui.student.StudentDashboardScreen
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthState
import com.example.smartcommunicationandnoticemanagement.viewmodel.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        val state = authState
        when (state) {
            is AuthState.Authenticated -> {
                val user = state.user
                val destination = if (user.role == "admin") NavRoute.AdminDashboard.route else NavRoute.StudentDashboard.route
                navController.navigate(destination) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.RegisterSuccess -> {
                navController.navigate(NavRoute.StudentDashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                // Only navigate to login if we are not already on an auth screen
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute != NavRoute.Login.route && 
                    currentRoute != NavRoute.Register.route && 
                    currentRoute != NavRoute.Splash.route &&
                    currentRoute != NavRoute.ForgotPassword.route) {
                    navController.navigate(NavRoute.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoute.Splash.route
    ) {
        composable(NavRoute.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(NavRoute.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(NavRoute.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(NavRoute.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(NavRoute.StudentDashboard.route) {
            StudentDashboardScreen(navController = navController)
        }
        composable(NavRoute.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
        composable(NavRoute.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        composable(NavRoute.FAQ.route) {
            FAQScreen(navController = navController)
        }
        composable(
            route = NavRoute.NoticeDetail.route,
            arguments = listOf(navArgument("noticeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noticeId = backStackEntry.arguments?.getString("noticeId") ?: ""
            NoticeDetailScreen(navController = navController, noticeId = noticeId)
        }
        composable(
            route = NavRoute.EditNotice.route,
            arguments = listOf(navArgument("noticeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noticeId = backStackEntry.arguments?.getString("noticeId") ?: ""
            EditNoticeScreen(navController = navController, noticeId = noticeId)
        }
        composable(
            route = NavRoute.GroupChat.route,
            arguments = listOf(navArgument("semester") { type = NavType.IntType })
        ) { backStackEntry ->
            val semester = backStackEntry.arguments?.getInt("semester") ?: 0
            GroupChatScreen(semester = semester, navController = navController)
        }
        composable(
            route = NavRoute.SeenBy.route,
            arguments = listOf(navArgument("noticeId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noticeId = backStackEntry.arguments?.getString("noticeId") ?: ""
            SeenByScreen(noticeId = noticeId, navController = navController)
        }
    }
}