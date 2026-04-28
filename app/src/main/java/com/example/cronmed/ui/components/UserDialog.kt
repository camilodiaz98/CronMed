package com.example.cronmed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.cronmed.ui.theme.*

@Composable
fun EditUserDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .border(1.dp, CronMedDivider, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CronMedSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Icon and Title
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(CronMedBlue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = CronMedBlue,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "Configurar Perfil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CronMedTextPrimary
                )

                Text(
                    text = "¿Cómo te gustaría que te llamemos?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CronMedTextSecondary,
                    textAlign = TextAlign.Center
                )

                // Input Field
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 20) name = it },
                    placeholder = { Text("Tu nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CronMedBlue,
                        unfocusedBorderColor = CronMedDivider,
                        focusedContainerColor = CronMedBackground,
                        unfocusedContainerColor = CronMedBackground
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            "Cancelar",
                            color = CronMedTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Save Button
                    Button(
                        onClick = { if (name.isNotBlank()) onConfirm(name) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CronMedBlue,
                            contentColor = Color.White
                        ),
                        enabled = name.isNotBlank()
                    ) {
                        Text(
                            "Guardar",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "camilodiaz desarrollo",
                    style = MaterialTheme.typography.labelSmall,
                    color = CronMedTextHint,
                    modifier = Modifier.alpha(0.7f)
                )
            }
        }
    }
}

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f)
                .border(1.dp, CronMedDivider, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CronMedSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cómo funciona CronMed+",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CronMedTextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HelpItem(
                        icon = Icons.Default.AddCircle,
                        title = "Registrar Medicamentos",
                        description = "Usa el botón '+' para agregar tus medicamentos. Define la dosis, frecuencia y hora de inicio."
                    )
                    HelpItem(
                        icon = Icons.Default.Alarm,
                        title = "Alarmas y Recordatorios",
                        description = "La app programará alarmas automáticamente según la frecuencia que elijas."
                    )
                    HelpItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Registrar Tomas",
                        description = "Desde el historial o el dashboard, puedes marcar cuando ya hayas tomado tu dosis."
                    )
                    HelpItem(
                        icon = Icons.Default.WaterDrop,
                        title = "Hidratación",
                        description = "Lleva un registro de cuánta agua bebes al día tocando el botón de hidratación rápida."
                    )
                    HelpItem(
                        icon = Icons.Default.BarChart,
                        title = "Estadísticas",
                        description = "Visualiza tu cumplimiento semanal en las gráficas para mejorar tu salud."
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CronMedBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text("Entendido", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "camilodiaz desarrollo",
                    style = MaterialTheme.typography.labelSmall,
                    color = CronMedTextHint,
                    modifier = Modifier.alpha(0.7f)
                )
            }
        }
    }
}

@Composable
private fun HelpItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CronMedBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = CronMedBlue, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CronMedTextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = CronMedTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
