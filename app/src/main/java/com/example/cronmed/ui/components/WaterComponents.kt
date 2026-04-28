package com.example.cronmed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.cronmed.ui.theme.*

@Composable
fun WaterSettingsDialog(
    enabled: Boolean,
    remindersPerDay: Int,
    amountPerGlass: Int,
    onDismiss: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onRemindersChange: (Int) -> Unit,
    onAmountChange: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CronMedDivider, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = CronMedSurface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Configuración de Hidratación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CronMedTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activar recordatorios", color = CronMedTextPrimary)
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(checkedTrackColor = CronMedBlue)
                    )
                }

                if (enabled) {
                    Text("Recordatorios por día: $remindersPerDay", color = CronMedTextSecondary)
                    Slider(
                        value = remindersPerDay.toFloat(),
                        onValueChange = { onRemindersChange(it.toInt()) },
                        valueRange = 1f..12f,
                        steps = 10,
                        colors = SliderDefaults.colors(thumbColor = CronMedBlue, activeTrackColor = CronMedBlue)
                    )

                    Text("Cantidad por vaso: ${amountPerGlass}ml", color = CronMedTextSecondary)
                    Slider(
                        value = amountPerGlass.toFloat(),
                        onValueChange = { onAmountChange(it.toInt()) },
                        valueRange = 100f..1000f,
                        steps = 8,
                        colors = SliderDefaults.colors(thumbColor = CronMedBlue, activeTrackColor = CronMedBlue)
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CronMedBlue)
                ) {
                    Text("Listo", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun WaterChart(weeklyStats: List<com.example.cronmed.data.local.WaterTotal>) {
    val maxAmount = weeklyStats.maxOfOrNull { it.totalAmount }?.coerceAtLeast(1000) ?: 2000
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CronMedDivider, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CronMedSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Historial Semanal (ml)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CronMedTextPrimary
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // Last 7 days
                val sortedStats = weeklyStats.take(7).reversed()
                
                sortedStats.forEach { stat ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(stat.totalAmount.toFloat() / maxAmount)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(CronMedBlue)
                        )
                        Text(
                            stat.dateStr.takeLast(2), // Just day
                            style = MaterialTheme.typography.labelSmall,
                            color = CronMedTextHint
                        )
                    }
                }
                
                if (sortedStats.isEmpty()) {
                    Text("Sin datos aún", color = CronMedTextHint)
                }
            }
        }
    }
}
