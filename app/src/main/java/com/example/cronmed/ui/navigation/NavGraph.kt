package com.example.cronmed.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cronmed.data.local.MedicamentoEntity
import com.example.cronmed.ui.screens.HistorialScreen
import com.example.cronmed.ui.screens.HistorialScreenContent
import com.example.cronmed.ui.screens.MedicamentoFormContent
import com.example.cronmed.ui.screens.MedicamentoFormScreen
import com.example.cronmed.ui.screens.MedicamentoScreenContent
import com.example.cronmed.ui.theme.CronMedTheme
import com.example.cronmed.ui.viewmodel.MedicamentoViewModel

sealed class Screen(val route: String) {
    object List : Screen("list")
    object Form : Screen("form/{id}") {
        fun createRoute(id: Int) = "form/$id"
    }
    object Historial : Screen("historial/{id}") {
        fun createRoute(id: Int) = "historial/$id"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: MedicamentoViewModel = viewModel()
) {
    NavGraphContent(
        navController = navController,
        onListScreen = {
            val medicamentos by viewModel.allMedicamentos.collectAsState()
            MedicamentoScreenContent(
                medicamentos = medicamentos,
                onAddMedicamento = { navController.navigate(Screen.Form.createRoute(-1)) },
                onEditMedicamento = { id -> navController.navigate(Screen.Form.createRoute(id)) },
                onViewHistorial = { id -> navController.navigate(Screen.Historial.createRoute(id)) },
                onToggleMedicamento = { med, active ->
                    viewModel.update(med.copy(activo = active))
                },
                onDeleteMedicamento = { viewModel.delete(it) }
            )
        },
        onFormScreen = { id ->
            MedicamentoFormScreen(
                viewModel = viewModel,
                medicamentoId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        },
        onHistorialScreen = { id ->
            HistorialScreen(
                viewModel = viewModel,
                medicamentoId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    )
}

@Composable
fun NavGraphContent(
    navController: NavHostController,
    onListScreen: @Composable () -> Unit,
    onFormScreen: @Composable (Int?) -> Unit,
    onHistorialScreen: @Composable (Int) -> Unit
) {
    NavHost(navController = navController, startDestination = Screen.List.route) {
        composable(Screen.List.route) {
            onListScreen()
        }
        composable(
            route = Screen.Form.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id")
            onFormScreen(id)
        }
        composable(
            route = Screen.Historial.route,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: -1
            onHistorialScreen(id)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavGraphPreview() {
    val sampleMedicamento = MedicamentoEntity(
        id = 1,
        nombre = "Paracetamol",
        dosis = "500mg",
        frecuenciaHoras = 8,
        horaInicio = System.currentTimeMillis(),
        observaciones = "Después de comer",
        activo = true
    )

    CronMedTheme {
        val navController = rememberNavController()
        NavGraphContent(
            navController = navController,
            onListScreen = {
                MedicamentoScreenContent(
                    medicamentos = listOf(sampleMedicamento),
                    onAddMedicamento = {},
                    onEditMedicamento = {},
                    onViewHistorial = {},
                    onToggleMedicamento = { _, _ -> },
                    onDeleteMedicamento = {}
                )
            },
            onFormScreen = { _ ->
                MedicamentoFormContent(
                    medicamentoId = -1,
                    nombre = "",
                    onNombreChange = {},
                    dosis = "",
                    onDosisChange = {},
                    frecuencia = "",
                    onFrecuenciaChange = {},
                    observaciones = "",
                    onObservacionesChange = {},
                    activo = true,
                    onActivoChange = {},
                    horaInicioMillis = System.currentTimeMillis(),
                    onTimeClick = {},
                    selectedImageUri = null,
                    existingImagePath = null,
                    onImageClick = {},
                    onSaveClick = {},
                    onNavigateBack = {}
                )
            },
            onHistorialScreen = { _ ->
                HistorialScreenContent(
                    medicamento = sampleMedicamento,
                    historial = emptyList(),
                    onNavigateBack = {},
                    onRegistrarToma = {}
                )
            }
        )
    }
}
