package com.example.cronmed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cronmed.data.local.HistorialEntity
import com.example.cronmed.data.local.MedicamentoEntity
import com.example.cronmed.data.local.MedicationDayStat
import com.example.cronmed.ui.components.BotonPrincipal
import com.example.cronmed.ui.theme.*
import com.example.cronmed.ui.viewmodel.MedicamentoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorialScreen(
    viewModel: MedicamentoViewModel,
    medicamentoId: Int,
    onNavigateBack: () -> Unit
) {
    val historial by viewModel.getHistorial(medicamentoId).collectAsState(initial = emptyList())
    val weeklyStats by viewModel.selectedMedicationWeeklyStats.collectAsState()
    var medicamento by remember { mutableStateOf<MedicamentoEntity?>(null) }

    LaunchedEffect(medicamentoId) {
        viewModel.setSelectedMedicationId(medicamentoId)
        medicamento = viewModel.getMedicamentoById(medicamentoId)
    }

    // Cleanup when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.setSelectedMedicationId(null)
        }
    }

    HistorialScreenContent(
        medicamento = medicamento,
        historial = historial,
        weeklyStats = weeklyStats,
        onNavigateBack = onNavigateBack,
        onRegistrarToma = { viewModel.registrarTomaManual(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreenContent(
    medicamento: MedicamentoEntity?,
    historial: List<HistorialEntity>,
    weeklyStats: List<MedicationDayStat>,
    onNavigateBack: () -> Unit,
    onRegistrarToma: (MedicamentoEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Scaffold(
        containerColor = CronMedBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = medicamento?.nombre ?: "Historial",
                        fontWeight = FontWeight.Bold,
                        color = CronMedTextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CronMedSurface)
                                .border(1.dp, CronMedDivider, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = CronMedTextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = CronMedBackground
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Gráfica Semanal ─────────────────────────────────────────────
            item {
                MedicationWeeklyChart(weeklyStats)
            }

            // ── Imagen del medicamento (si la tiene) ────────────────────────
            medicamento?.imagenPath?.let { path ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, CronMedDivider, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        AsyncImage(
                            model = File(path),
                            contentDescription = "Imagen del medicamento",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // ── Detalles del medicamento ────────────────────────────────────
            medicamento?.let { med ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CronMedDivider, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CronMedSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CronMedCardBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Medication,
                                        null,
                                        tint = CronMedBlue,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = med.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = CronMedTextPrimary
                                    )
                                    Text(
                                        text = "${med.dosis} cada ${med.frecuenciaHoras} horas",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CronMedTextSecondary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stats de tomas
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MiniStatCard(
                                    label = "Total tomas",
                                    valor = "${historial.size}",
                                    color = CronMedBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatCard(
                                    label = "Tomadas",
                                    valor = "${historial.count { it.estado.contains("TOMADO") }}",
                                    color = CronMedGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                MiniStatCard(
                                    label = "Omitidas",
                                    valor = "${historial.count { !it.estado.contains("TOMADO") }}",
                                    color = CronMedWarning,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            BotonPrincipal(
                                texto = "Registrar Toma Ahora",
                                onClick = { onRegistrarToma(med) },
                                icono = Icons.Default.CheckCircle
                            )
                        }
                    }
                }

                // ── Título sección historial ────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Registro de tomas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CronMedTextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CronMedCardBlue
                        ) {
                            Text(
                                "${historial.size} registros",
                                style = MaterialTheme.typography.labelSmall,
                                color = CronMedBlue,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // ── Lista vacía ─────────────────────────────────────────────────
            if (historial.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📋", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No hay registros aún",
                                style = MaterialTheme.typography.bodyMedium,
                                color = CronMedTextSecondary
                            )
                            Text(
                                "Registra tu primera toma",
                                style = MaterialTheme.typography.bodySmall,
                                color = CronMedTextHint
                            )
                        }
                    }
                }
            } else {
                items(historial) { item ->
                    HistorialItem(item, dateFormat)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun MedicationWeeklyChart(stats: List<MedicationDayStat>) {
    val maxCount = stats.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CronMedDivider, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CronMedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Tomas esta semana",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CronMedTextPrimary
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Last 7 days reversed to show oldest to newest
                val sortedStats = stats.take(7).reversed()
                
                sortedStats.forEach { stat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stat.count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = CronMedBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .fillMaxHeight(stat.count.toFloat() / maxCount.toFloat() * 0.8f)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(CronMedBlueLight, CronMedBlue)
                                    )
                                )
                        )
                        Text(
                            stat.dateStr.takeLast(2),
                            style = MaterialTheme.typography.labelSmall,
                            color = CronMedTextHint
                        )
                    }
                }
                
                if (sortedStats.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin tomas registradas", color = CronMedTextHint)
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    valor: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CronMedTextSecondary
            )
        }
    }
}

@Composable
fun HistorialItem(item: HistorialEntity, dateFormat: SimpleDateFormat) {
    val isTomado = item.estado.contains("TOMADO")
    val isManual = item.estado.contains("Manual")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CronMedDivider, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CronMedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isTomado) CronMedGreen.copy(alpha = 0.1f)
                        else CronMedWarning.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isTomado) Icons.Default.CheckCircle else Icons.Default.Schedule,
                    contentDescription = null,
                    tint = if (isTomado) CronMedGreen else CronMedWarning,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isTomado) "Tomado" else "Omitido",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = CronMedTextPrimary
                    )
                    if (isManual) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CronMedBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "Manual",
                                style = MaterialTheme.typography.labelSmall,
                                color = CronMedBlue,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    text = dateFormat.format(Date(item.fechaHoraReal)),
                    style = MaterialTheme.typography.bodySmall,
                    color = CronMedTextSecondary
                )
                if (item.observaciones.isNotBlank()) {
                    Text(
                        text = item.observaciones,
                        style = MaterialTheme.typography.bodySmall,
                        color = CronMedBlue.copy(alpha = 0.8f)
                    )
                }
            }

            // Indicador visual
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTomado) CronMedGreen else CronMedWarning
                    )
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEF4FF)
@Composable
fun HistorialScreenPreview() {
    val sampleMedicamento = MedicamentoEntity(
        id = 1, nombre = "Ibuprofeno", dosis = "400mg", frecuenciaHoras = 8,
        horaInicio = System.currentTimeMillis(), observaciones = "Tomar con alimentos"
    )
    val sampleHistorial = listOf(
        HistorialEntity(id = 1, medicamentoId = 1, nombreMedicamento = "Ibuprofeno",
            fechaHoraProgramada = System.currentTimeMillis() - 3600000,
            fechaHoraReal = System.currentTimeMillis() - 3500000, estado = "TOMADO (Manual)"),
        HistorialEntity(id = 2, medicamentoId = 1, nombreMedicamento = "Ibuprofeno",
            fechaHoraProgramada = System.currentTimeMillis() - 32400000,
            fechaHoraReal = System.currentTimeMillis() - 32300000, estado = "TOMADO"),
        HistorialEntity(id = 3, medicamentoId = 1, nombreMedicamento = "Ibuprofeno",
            fechaHoraProgramada = System.currentTimeMillis() - 72000000,
            fechaHoraReal = System.currentTimeMillis() - 71000000, estado = "OMITIDO")
    )
    CronMedTheme {
        HistorialScreenContent(
            medicamento = sampleMedicamento,
            historial = sampleHistorial,
            weeklyStats = emptyList(),
            onNavigateBack = {},
            onRegistrarToma = {}
        )
    }
}
