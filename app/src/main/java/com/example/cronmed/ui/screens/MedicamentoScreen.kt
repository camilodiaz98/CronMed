package com.example.cronmed.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.cronmed.ui.components.HeaderUsuario
import com.example.cronmed.ui.components.StatCard
import com.example.cronmed.ui.theme.CronMedTheme
import com.example.cronmed.ui.viewmodel.MedicamentoViewModel
import java.io.File

@Composable
fun MedicamentoScreen(
    viewModel: MedicamentoViewModel,
    onAddMedicamento: () -> Unit,
    onEditMedicamento: (Int) -> Unit,
    onViewHistorial: (Int) -> Unit
) {
    val medicamentos by viewModel.allMedicamentos.collectAsState()

    MedicamentoScreenContent(
        medicamentos = medicamentos,
        onAddMedicamento = onAddMedicamento,
        onEditMedicamento = onEditMedicamento,
        onViewHistorial = onViewHistorial,
        onToggleMedicamento = { medicamento, active ->
            viewModel.update(medicamento.copy(activo = active))
        },
        onDeleteMedicamento = { viewModel.delete(it) }
    )
}

@Composable
fun MedicamentoScreenContent(
    medicamentos: List<MedicamentoEntity>,
    onAddMedicamento: () -> Unit,
    onEditMedicamento: (Int) -> Unit,
    onViewHistorial: (Int) -> Unit,
    onToggleMedicamento: (MedicamentoEntity, Boolean) -> Unit,
    onDeleteMedicamento: (MedicamentoEntity) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddMedicamento,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, "Agregar") },
                text = { Text("Nuevo") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                HeaderUsuario(nombre = "Usuario CronMed", onEditClick = {})
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        titulo = "Racha",
                        valor = "5 Días",
                        icono = Icons.Default.LocalActivity,
                        colorIcono = Color(0xFFFFB74D),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        titulo = "Hidratación",
                        valor = "1.2L",
                        icono = Icons.Default.WaterDrop,
                        colorIcono = Color(0xFF64B5F6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    text = "Tus Medicamentos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (medicamentos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No hay medicamentos registrados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(medicamentos) { medicamento ->
                    MedicamentoItemCard(
                        medicamento = medicamento,
                        onClick = { onViewHistorial(medicamento.id) },
                        onEdit = { onEditMedicamento(medicamento.id) },
                        onToggle = { active -> 
                            onToggleMedicamento(medicamento, active)
                        },
                        onDelete = { onDeleteMedicamento(medicamento) }
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (medicamento.activo) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
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
                    Text(
                        text = "💊",
                        fontSize = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicamento.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (medicamento.activo) MaterialTheme.colorScheme.onSurface 
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = "${medicamento.dosis} • Cada ${medicamento.frecuenciaHoras}h",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Switch(
                checked = medicamento.activo,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Deshabilitar",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicamentoScreenPreview() {
    val sampleMedicamentos = listOf(
        MedicamentoEntity(
            id = 1,
            nombre = "Paracetamol",
            dosis = "500mg",
            frecuenciaHoras = 8,
            horaInicio = System.currentTimeMillis(),
            observaciones = "Después de comer",
            activo = true
        ),
        MedicamentoEntity(
            id = 2,
            nombre = "Ibuprofeno",
            dosis = "400mg",
            frecuenciaHoras = 6,
            horaInicio = System.currentTimeMillis(),
            observaciones = "Con abundante agua",
            activo = false
        )
    )
    CronMedTheme {
        MedicamentoScreenContent(
            medicamentos = sampleMedicamentos,
            onAddMedicamento = {},
            onEditMedicamento = {},
            onViewHistorial = {},
            onToggleMedicamento = { _, _ -> },
            onDeleteMedicamento = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MedicamentoItemPreview() {
    CronMedTheme {
        MedicamentoItemCard(
            medicamento = MedicamentoEntity(
                id = 1,
                nombre = "Paracetamol",
                dosis = "500mg",
                frecuenciaHoras = 8,
                horaInicio = System.currentTimeMillis(),
                observaciones = "Después de comer",
                activo = true
            ),
            onClick = {},
            onEdit = {},
            onToggle = {},
            onDelete = {}
        )
    }
}
