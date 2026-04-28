package com.example.cronmed.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cronmed.ui.theme.*
import com.example.cronmed.ui.viewmodel.DoseStatus
import com.example.cronmed.ui.viewmodel.TodayDose
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyScheduleChart(doses: List<TodayDose>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CronMedDivider, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CronMedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Horario de Hoy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CronMedTextPrimary
                )
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CronMedCardBlue
                ) {
                    val completed = doses.count { it.status == DoseStatus.TAKEN }
                    Text(
                        "$completed/${doses.size} completadas",
                        style = MaterialTheme.typography.labelSmall,
                        color = CronMedBlue,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (doses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Sin tomas para hoy",
                        style = MaterialTheme.typography.bodySmall,
                        color = CronMedTextHint
                    )
                }
            } else {
                // Group doses by medication name
                val groupedDoses = doses.groupBy { it.name }
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    groupedDoses.forEach { (name, medicationDoses) ->
                        MedicationRow(name, medicationDoses)
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicationRow(name: String, doses: List<TodayDose>) {
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Medication Name Column
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = CronMedTextPrimary,
            modifier = Modifier.width(90.dp),
            maxLines = 1
        )
        
        // Doses Row (Scrollable if many)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(doses) { dose ->
                val statusColor = when (dose.status) {
                    DoseStatus.TAKEN -> CronMedGreen
                    DoseStatus.PENDING -> CronMedOrange
                    DoseStatus.UPCOMING -> CronMedBlue
                    DoseStatus.POSTPONED -> CronMedPurple
                    DoseStatus.OMITTED -> CronMedError
                }
                
                val statusIcon = when (dose.status) {
                    DoseStatus.TAKEN -> Icons.Default.CheckCircle
                    DoseStatus.PENDING -> Icons.Default.Schedule
                    DoseStatus.UPCOMING -> Icons.Default.RadioButtonUnchecked
                    DoseStatus.POSTPONED -> Icons.Default.History
                    DoseStatus.OMITTED -> Icons.Default.Cancel
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = timeFormatter.format(Date(dose.time)),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (dose.status == DoseStatus.PENDING) CronMedOrange else CronMedTextSecondary
                    )
                }
            }
        }
    }
}
