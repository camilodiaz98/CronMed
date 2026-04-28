package com.example.cronmed.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cronmed.ui.theme.*

// ─── Header del usuario ───────────────────────────────────────────────────────
@Composable
fun HeaderUsuario(
    nombre: String,
    subtitulo: String = "CronMed+",
    onEditClick: () -> Unit,
    onHelpClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onEditClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CronMedBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = nombre.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.labelMedium,
                    color = CronMedTextSecondary
                )
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CronMedTextPrimary
                )
            }
        }
        
        IconButton(
            onClick = onHelpClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(CronMedSurface)
                .border(1.dp, CronMedDivider, CircleShape)
        ) {
            Icon(
                Icons.Default.HelpOutline,
                contentDescription = "Ayuda",
                tint = CronMedBlue
            )
        }
    }
}

// ─── Tarjeta de estadística (Racha / Hidratación) ─────────────────────────────
@Composable
fun StatCard(
    titulo: String,
    valor: String,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier,
    subtitulo: String = ""
) {
    Card(
        modifier = modifier
            .shadow(elevation = 0.dp, shape = RoundedCornerShape(20.dp))
            .border(1.dp, CronMedDivider, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CronMedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorIcono.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = colorIcono,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = valor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = CronMedTextPrimary
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = CronMedTextSecondary
            )
            if (subtitulo.isNotBlank()) {
                Text(
                    text = subtitulo,
                    style = MaterialTheme.typography.labelSmall,
                    color = CronMedGreen,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── Progreso circular ────────────────────────────────────────────────────────
@Composable
fun CircularProgressCard(
    progreso: Float, // 0f..1f
    completadas: Int,
    total: Int,
    proximaHora: String,
    resumen: String = "",
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progreso,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CronMedDivider, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CronMedSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Régimen de hoy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CronMedTextPrimary
                    )
                    Text(
                        text = if (resumen.isBlank()) "Has completado $completadas de $total dosis hoy." else resumen,
                        style = MaterialTheme.typography.bodySmall,
                        color = CronMedTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Próxima badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CronMedBackground
            ) {
                Text(
                    text = "Próxima: $proximaHora",
                    style = MaterialTheme.typography.labelMedium,
                    color = CronMedBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Círculo de progreso
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    // Track
                    drawCircle(
                        color = CronMedBackground,
                        radius = radius,
                        style = Stroke(width = strokeWidth)
                    )
                    // Progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(CronMedBlueLight, CronMedBlue)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CronMedBlue
                    )
                }
            }
        }
    }
}

// ─── Tarjeta Recordatorio Hidratación ─────────────────────────────────────────
@Composable
fun HidratacionCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CronMedDivider, RoundedCornerShape(20.dp)),
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
                    text = "Recordatorio de Hidratación",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CronMedTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tomar agua con tus medicamentos ayuda a una mejor absorción y protege tu sistema digestivo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CronMedTextSecondary,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF64B5F6), Color(0xFF1565C0))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("💧", fontSize = 28.sp)
            }
        }
    }
}

// ─── Botón Principal ──────────────────────────────────────────────────────────
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icono: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CronMedBlue,
            contentColor = Color.White,
            disabledContainerColor = CronMedTextHint,
            disabledContentColor = Color.White
        ),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 4.dp
        )
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = texto,
            style = MaterialTheme.typography.titleSmall.copy(color = Color.White),
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Sección de título con acción ─────────────────────────────────────────────
@Composable
fun SectionHeader(
    titulo: String,
    accion: String = "",
    onAccionClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = CronMedTextPrimary
        )
        if (accion.isNotBlank()) {
            TextButton(onClick = onAccionClick) {
                Text(
                    text = accion,
                    style = MaterialTheme.typography.labelMedium,
                    color = CronMedBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Badge de estado ──────────────────────────────────────────────────────────
@Composable
fun EstadoBadge(
    texto: String,
    color: Color = CronMedGreen,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permisos necesarios", fontWeight = FontWeight.Bold) },
        text = { 
            Text("CronMed necesita permisos para enviarte notificaciones y alarmas para tus medicamentos. Por favor, acéptalos en la siguiente pantalla.") 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CronMedBlue)
            ) {
                Text("Continuar", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ahora no", color = CronMedTextSecondary)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = CronMedSurface
    )
}
