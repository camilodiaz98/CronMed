package com.example.cronmed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cronmed.data.local.MedicamentoEntity
import com.example.cronmed.data.local.WaterLogEntity
import com.example.cronmed.data.local.WaterTotal
import com.example.cronmed.ui.components.*
import com.example.cronmed.ui.theme.*
import com.example.cronmed.ui.viewmodel.DoseStatus
import com.example.cronmed.ui.viewmodel.MedicamentoViewModel
import com.example.cronmed.ui.viewmodel.TodayDose
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicamentoScreen(
    viewModel: MedicamentoViewModel,
    onAddMedicamento: () -> Unit,
    onEditMedicamento: (Int) -> Unit,
    onViewHistorial: (Int) -> Unit
) {
    val medicamentos by viewModel.allMedicamentos.collectAsState()
    val userName by viewModel.userName.collectAsState()
    
    val waterRemindersEnabled by viewModel.waterRemindersEnabled.collectAsState()
    val waterRemindersPerDay by viewModel.waterRemindersPerDay.collectAsState()
    val waterAmountPerGlass by viewModel.waterAmountPerGlass.collectAsState()
    val todayWaterLogs by viewModel.todayWaterLogs.collectAsState()
    val weeklyWaterStats by viewModel.weeklyWaterStats.collectAsState()
    val todayDoses by viewModel.todayDoses.collectAsState()

    var showEditUserDialog by remember { mutableStateOf(false) }
    var showWaterSettingsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    if (showEditUserDialog) {
        EditUserDialog(
            currentName = userName,
            onDismiss = { showEditUserDialog = false },
            onConfirm = { newName ->
                viewModel.setUserName(newName)
                showEditUserDialog = false
            }
        )
    }

    if (showWaterSettingsDialog) {
        WaterSettingsDialog(
            enabled = waterRemindersEnabled,
            remindersPerDay = waterRemindersPerDay,
            amountPerGlass = waterAmountPerGlass,
            onDismiss = { showWaterSettingsDialog = false },
            onToggle = { viewModel.toggleWaterReminders(it) },
            onRemindersChange = { viewModel.setWaterRemindersPerDay(it) },
            onAmountChange = { viewModel.setWaterAmountPerGlass(it) }
        )
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }

    MedicamentoScreenContent(
        medicamentos = medicamentos,
        userName = userName,
        todayWaterLogs = todayWaterLogs,
        weeklyWaterStats = weeklyWaterStats,
        waterAmountPerGlass = waterAmountPerGlass,
        todayDoses = todayDoses,
        onAddMedicamento = onAddMedicamento,
        onEditMedicamento = onEditMedicamento,
        onViewHistorial = onViewHistorial,
        onToggleMedicamento = { medicamento, active ->
            viewModel.update(medicamento.copy(activo = active))
        },
        onDeleteMedicamento = { viewModel.delete(it) },
        onEditUser = { showEditUserDialog = true },
        onAddWater = { viewModel.addWater() },
        onOpenWaterSettings = { showWaterSettingsDialog = true },
        onOpenHelp = { showHelpDialog = true }
    )
}

@Composable
fun MedicamentoScreenContent(
    medicamentos: List<MedicamentoEntity>,
    userName: String,
    todayWaterLogs: List<WaterLogEntity>,
    weeklyWaterStats: List<WaterTotal>,
    waterAmountPerGlass: Int,
    todayDoses: List<TodayDose>,
    onAddMedicamento: () -> Unit,
    onEditMedicamento: (Int) -> Unit,
    onViewHistorial: (Int) -> Unit,
    onToggleMedicamento: (MedicamentoEntity, Boolean) -> Unit,
    onDeleteMedicamento: (MedicamentoEntity) -> Unit,
    onEditUser: () -> Unit,
    onAddWater: () -> Unit,
    onOpenWaterSettings: () -> Unit,
    onOpenHelp: () -> Unit
) {
    val totalDosisHoy = todayDoses.size
    val completadasHoy = todayDoses.count { it.status == DoseStatus.TAKEN }
    val pospuestasHoy = todayDoses.count { it.status == DoseStatus.POSTPONED }
    val omitidasHoy = todayDoses.count { it.status == DoseStatus.OMITTED }
    val faltantesHoy = totalDosisHoy - completadasHoy - omitidasHoy
    
    val progresoHoy = if (totalDosisHoy > 0) completadasHoy.toFloat() / totalDosisHoy.toFloat() else 0f
    
    val totalWaterToday = todayWaterLogs.sumOf { it.amountMl }
    
    val proximaDosisStr = medicamentos
        .filter { it.activo }
        .minByOrNull { it.horaInicio }
        ?.let { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(it.horaInicio)) }
        ?: "--:--"

    Scaffold(
        containerColor = CronMedBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicamento,
                containerColor = CronMedBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar medicamento", modifier = Modifier.size(24.dp))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header
            item {
                HeaderUsuario(
                    nombre = userName,
                    subtitulo = "CronMed+",
                    onEditClick = onEditUser,
                    onHelpClick = onOpenHelp
                )
            }

            // 2. Progreso circular (Regimen)
            item {
                CircularProgressCard(
                    progreso = progresoHoy,
                    completadas = completadasHoy,
                    total = totalDosisHoy,
                    proximaHora = proximaDosisStr,
                    resumen = "Faltan $faltantesHoy por tomar" + (if (pospuestasHoy > 0) " ($pospuestasHoy pospuestas)" else "")
                )
            }

            // 3. Horario del Día
            item {
                DailyScheduleChart(todayDoses)
            }

            // 4. Mis Medicamentos Section Header
            item {
                SectionHeader(
                    titulo = "Mis Medicamentos",
                    accion = "Ver todos",
                    onAccionClick = {}
                )
            }

            // 5. Mis Medicamentos List
            if (medicamentos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💊", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No hay medicamentos registrados",
                                color = CronMedTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(medicamentos) { medicamento ->
                    MedicamentoItemCard(
                        medicamento = medicamento,
                        onClick = { onViewHistorial(medicamento.id) },
                        onEdit = { onEditMedicamento(medicamento.id) },
                        onToggle = { active -> onToggleMedicamento(medicamento, active) },
                        onDelete = { onDeleteMedicamento(medicamento) }
                    )
                }
            }

            // 6. Hidratación card (Registro rápido)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CronMedDivider, RoundedCornerShape(20.dp))
                        .clickable { onAddWater() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CronMedSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Registrar Hidratación",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = CronMedTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toca para registrar un vaso de ${waterAmountPerGlass}ml.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CronMedTextSecondary
                            )
                        }
                        IconButton(onClick = onOpenWaterSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Configurar", tint = CronMedTextHint)
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF64B5F6).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💧", fontSize = 24.sp)
                        }
                    }
                }
            }

            // 7. Título Hidratación
            item {
                Text(
                    "Estadísticas de Hidratación",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CronMedTextPrimary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // 8. Gráfica de Agua
            item {
                WaterChart(weeklyWaterStats)
            }

            // 9. Stats row: Racha + Hidratación (L)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        titulo = "Racha",
                        valor = "12 Días",
                        icono = Icons.Default.LocalActivity,
                        colorIcono = CronMedOrange,
                        subtitulo = "¡Excelente!",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        titulo = "Total Agua",
                        valor = "${totalWaterToday.toFloat() / 1000}L",
                        icono = Icons.Default.WaterDrop,
                        colorIcono = Color(0xFF64B5F6),
                        subtitulo = "Hoy",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun MedicamentoItemCard(
    medicamento: MedicamentoEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val proximaToma = timeFormatter.format(Date(medicamento.horaInicio))
    val isPast = medicamento.horaInicio < System.currentTimeMillis()

    val iconBg = when (medicamento.nombre.first().lowercaseChar()) {
        in 'a'..'g' -> CronMedCardBlue
        in 'h'..'n' -> CronMedPurpleLight
        else -> CronMedGreenLight
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CronMedDivider, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CronMedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (medicamento.activo) iconBg else CronMedBackground),
                contentAlignment = Alignment.Center
            ) {
                if (medicamento.imagenPath != null) {
                    AsyncImage(
                        model = File(medicamento.imagenPath),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("💊", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicamento.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (medicamento.activo) CronMedTextPrimary
                    else CronMedTextSecondary
                )
                Text(
                    text = "${medicamento.dosis} • Cada ${medicamento.frecuenciaHoras}h",
                    style = MaterialTheme.typography.bodySmall,
                    color = CronMedTextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (medicamento.activo) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isPast) "PENDIENTE" else "SIGUIENTE",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isPast) CronMedOrange else CronMedTextHint,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = proximaToma,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPast) CronMedOrange else CronMedBlue
                        )
                    }
                } else {
                    EstadoBadge(
                        texto = "Inactivo",
                        color = CronMedTextSecondary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Switch(
                    checked = medicamento.activo,
                    onCheckedChange = onToggle,
                    modifier = Modifier.height(24.dp),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CronMedBlue,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = CronMedTextHint.copy(alpha = 0.3f)
                    )
                )
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = CronMedBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = CronMedError.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEF4FF)
@Composable
fun MedicamentoScreenPreview() {
    CronMedTheme {
        MedicamentoScreenContent(
            medicamentos = emptyList(),
            userName = "Usuario CronMed",
            todayWaterLogs = emptyList(),
            weeklyWaterStats = emptyList(),
            waterAmountPerGlass = 500,
            todayDoses = emptyList(),
            onAddMedicamento = {},
            onEditMedicamento = {},
            onViewHistorial = {},
            onToggleMedicamento = { _, _ -> },
            onDeleteMedicamento = {},
            onEditUser = {},
            onAddWater = {},
            onOpenWaterSettings = {},
            onOpenHelp = {}
        )
    }
}
