package com.example.cronmed.ui.navigation

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cronmed.ui.screens.HistorialScreen
import com.example.cronmed.ui.screens.MedicamentoFormScreen
import com.example.cronmed.ui.screens.MedicamentoScreen
import com.example.cronmed.ui.theme.CronMedTextPrimary
import com.example.cronmed.ui.theme.CronMedTextSecondary
import com.example.cronmed.ui.viewmodel.MedicamentoViewModel

sealed class Screen(val route: String) {
    object Medicamentos : Screen("medicamentos")
    object MedicamentoForm : Screen("medicamento_form/{medicamentoId}") {
        fun createRoute(medicamentoId: Int) = "medicamento_form/$medicamentoId"
    }
    object Historial : Screen("historial/{medicamentoId}") {
        fun createRoute(medicamentoId: Int) = "historial/$medicamentoId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MedicamentoViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Medicamentos.route
    ) {
        composable(Screen.Medicamentos.route) {
            MedicamentoScreen(
                viewModel = viewModel,
                onAddMedicamento = {
                    navController.navigate(Screen.MedicamentoForm.createRoute(-1))
                },
                onEditMedicamento = { id ->
                    navController.navigate(Screen.MedicamentoForm.createRoute(id))
                },
                onViewHistorial = { id ->
                    navController.navigate(Screen.Historial.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.MedicamentoForm.route,
            arguments = listOf(navArgument("medicamentoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val medicamentoId = backStackEntry.arguments?.getInt("medicamentoId")
            MedicamentoFormScreen(
                viewModel = viewModel,
                medicamentoId = if (medicamentoId == -1) null else medicamentoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Historial.route,
            arguments = listOf(navArgument("medicamentoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val medicamentoId = backStackEntry.arguments?.getInt("medicamentoId") ?: return@composable
            HistorialScreen(
                viewModel = viewModel,
                medicamentoId = medicamentoId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

// Using system default (Roboto) — matches the clean medical app aesthetic.
val CronMedTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp,
        color = CronMedTextPrimary
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp,
        color = CronMedTextPrimary
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = CronMedTextPrimary
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = CronMedTextPrimary
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = CronMedTextPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = CronMedTextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp,
        color = CronMedTextPrimary
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = CronMedTextPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = CronMedTextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        color = CronMedTextSecondary
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
        color = CronMedTextSecondary
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
