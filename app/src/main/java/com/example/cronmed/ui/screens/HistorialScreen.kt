package com.example.cronmed.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cronmed.data.local.HistorialEntity
import com.example.cronmed.data.local.MedicamentoEntity
import com.example.cronmed.ui.components.BotonPrincipal
import com.example.cronmed.ui.theme.CronMedTheme
import com.example.cronmed.ui.viewmodel.MedicamentoViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistorialScreen(
    viewModel: MedicamentoViewModel,
    medicamentoId: Int,
    onNavigateBack: () -> Unit
) {
    val historial by viewModel.getHistorial(medicamentoId).collectAsState()
    var medicamento by remember { mutableStateOf<MedicamentoEntity?>(null) }

    LaunchedEffect(medicamentoId) {
        medicamento = viewModel.getMedicamentoById(medicamentoId)
    }

    HistorialScreenContent(
        medicamento = medicamento,
        historial = historial,
        onNavigateBack = onNavigateBack,
        onRegistrarToma = { viewModel.registrarTomaManual(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreenContent(
    medicamento: MedicamentoEntity?,
    historial: List<HistorialEntity>,
    onNavigateBack: () -> Unit,
    onRegistrarToma: (MedicamentoEntity) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(medicamento?.nombre ?: "Historial", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            medicamento?.let { med ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Detalles de la dosis", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${med.dosis} cada ${med.frecuenciaHoras} horas")
                        Spacer(modifier = Modifier.height(16.dp))
                        BotonPrincipal(
                            texto = "Registrar Toma Ahora",
                            onClick = { onRegistrarToma(med) }
                        )
                    }
                }
            }

            Text(
                text = "Registro de tomas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            if (historial.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                        Text("No hay registros aún", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(historial) { item ->
                        HistorialItem(item, dateFormat)
                    }
                }
            }
        }
    }
}

@Composable
fun HistorialItem(item: HistorialEntity, dateFormat: SimpleDateFormat) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (item.estado.contains("TOMADO")) Color(0xFF4CAF50) else Color(0xFFFF9800)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = item.estado,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = dateFormat.format(Date(item.fechaHoraReal)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.observaciones.isNotBlank()) {
                    Text(
                        text = item.observaciones,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistorialScreenPreview() {
    val sampleMedicamento = MedicamentoEntity(
        id = 1,
        nombre = "Ibuprofeno",
        dosis = "400mg",
        frecuenciaHoras = 8,
        horaInicio = System.currentTimeMillis(),
        observaciones = "Tomar con alimentos"
    )
    val sampleHistorial = listOf(
        HistorialEntity(
            id = 1,
            medicamentoId = 1,
            nombreMedicamento = "Ibuprofeno",
            fechaHoraProgramada = System.currentTimeMillis() - 3600000,
            fechaHoraReal = System.currentTimeMillis() - 3500000,
            estado = "TOMADO (Manual)"
        ),
        HistorialEntity(
            id = 2,
            medicamentoId = 1,
            nombreMedicamento = "Ibuprofeno",
            fechaHoraProgramada = System.currentTimeMillis() - 32400000,
            fechaHoraReal = System.currentTimeMillis() - 32300000,
            estado = "TOMADO"
        )
    )

    CronMedTheme {
        HistorialScreenContent(
            medicamento = sampleMedicamento,
            historial = sampleHistorial,
            onNavigateBack = {},
            onRegistrarToma = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HistorialItemPreview() {
    val sampleItem = HistorialEntity(
        id = 1,
        medicamentoId = 1,
        nombreMedicamento = "Ibuprofeno",
        fechaHoraProgramada = System.currentTimeMillis(),
        fechaHoraReal = System.currentTimeMillis(),
        estado = "TOMADO",
        observaciones = "Sin efectos secundarios"
    )
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())

    CronMedTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            HistorialItem(item = sampleItem, dateFormat = dateFormat)
        }
    }
}
