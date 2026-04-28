package com.example.cronmed.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cronmed.data.local.MedicamentoEntity
import com.example.cronmed.ui.components.BotonPrincipal
import com.example.cronmed.ui.theme.*
import com.example.cronmed.ui.viewmodel.MedicamentoViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MedicamentoFormScreen(
    viewModel: MedicamentoViewModel,
    medicamentoId: Int? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    var nombre by remember { mutableStateOf("") }
    var dosis by remember { mutableStateOf("") }
    var frecuencia by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(true) }
    var horaInicioMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImagePath by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> selectedImageUri = uri }

    LaunchedEffect(medicamentoId) {
        if (medicamentoId != null && medicamentoId != -1) {
            val medicamento = viewModel.getMedicamentoById(medicamentoId)
            medicamento?.let {
                nombre = it.nombre; dosis = it.dosis
                frecuencia = it.frecuenciaHoras.toString()
                observaciones = it.observaciones; activo = it.activo
                horaInicioMillis = it.horaInicio; existingImagePath = it.imagenPath
            }
        }
    }

    val timePickerDialog = TimePickerDialog(context, { _, h, m ->
        calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, m)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        // Eliminamos el add(DAY_OF_YEAR, 1) para permitir que el ViewModel ajuste según frecuencia
        horaInicioMillis = calendar.timeInMillis
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    val canSave = nombre.isNotBlank() && frecuencia.isNotBlank() && frecuencia.toIntOrNull() != null

    MedicamentoFormContent(
        medicamentoId = medicamentoId,
        nombre = nombre, onNombreChange = { nombre = it },
        dosis = dosis, onDosisChange = { dosis = it },
        frecuencia = frecuencia, onFrecuenciaChange = { frecuencia = it },
        observaciones = observaciones, onObservacionesChange = { observaciones = it },
        activo = activo, onActivoChange = { activo = it },
        horaInicioMillis = horaInicioMillis, onTimeClick = { timePickerDialog.show() },
        selectedImageUri = selectedImageUri, existingImagePath = existingImagePath,
        onImageClick = { galleryLauncher.launch("image/*") },
        onSaveClick = {
            if (canSave) {
                val med = MedicamentoEntity(
                    id = if (medicamentoId != null && medicamentoId != -1) medicamentoId else 0,
                    nombre = nombre, 
                    dosis = if (dosis.isBlank()) "Sin dosis" else dosis,
                    frecuenciaHoras = frecuencia.toIntOrNull() ?: 0,
                    horaInicio = horaInicioMillis, 
                    observaciones = observaciones,
                    activo = activo, 
                    imagenPath = existingImagePath
                )
                if (medicamentoId == null || medicamentoId == -1) viewModel.insert(med, selectedImageUri)
                else viewModel.update(med, selectedImageUri)
                onNavigateBack()
            }
        },
        onNavigateBack = onNavigateBack,
        canSave = canSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicamentoFormContent(
    medicamentoId: Int?,
    nombre: String, onNombreChange: (String) -> Unit,
    dosis: String, onDosisChange: (String) -> Unit,
    frecuencia: String, onFrecuenciaChange: (String) -> Unit,
    observaciones: String, onObservacionesChange: (String) -> Unit,
    activo: Boolean, onActivoChange: (Boolean) -> Unit,
    horaInicioMillis: Long, onTimeClick: () -> Unit,
    selectedImageUri: Uri?, existingImagePath: String?,
    onImageClick: () -> Unit, onSaveClick: () -> Unit, onNavigateBack: () -> Unit,
    canSave: Boolean
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val isEditing = medicamentoId != null && medicamentoId != -1

    val outlinedFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = CronMedBlue,
        unfocusedBorderColor = CronMedDivider,
        focusedLabelColor = CronMedBlue,
        unfocusedLabelColor = CronMedTextSecondary,
        cursorColor = CronMedBlue,
        focusedLeadingIconColor = CronMedBlue,
        unfocusedLeadingIconColor = CronMedTextHint,
        focusedContainerColor = CronMedSurface,
        unfocusedContainerColor = CronMedSurface
    )

    Scaffold(
        containerColor = CronMedBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Editar Medicamento" else "Nuevo Medicamento",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Selector de imagen ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (selectedImageUri == null && existingImagePath == null)
                            Brush.verticalGradient(listOf(CronMedCardBlue, CronMedBackground))
                        else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .border(1.dp, CronMedDivider, RoundedCornerShape(24.dp))
                    .clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null || existingImagePath != null) {
                    AsyncImage(
                        model = selectedImageUri ?: existingImagePath?.let { File(it) },
                        contentDescription = "Imagen del medicamento",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(24.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CronMedBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CronMedBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = CronMedBlue
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Añadir foto del medicamento",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CronMedBlue,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Toca para seleccionar",
                            style = MaterialTheme.typography.bodySmall,
                            color = CronMedTextHint
                        )
                    }
                }
            }

            // ── Nombre ──────────────────────────────────────────────────────
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre del medicamento *") },
                placeholder = { Text("Ej: Paracetamol") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = outlinedFieldColors,
                leadingIcon = {
                    Icon(Icons.Default.Medication, null, tint = CronMedBlue, modifier = Modifier.size(20.dp))
                }
            )

            // ── Dosis ────────────────────────────────────────────────────────
            OutlinedTextField(
                value = dosis,
                onValueChange = onDosisChange,
                label = { Text("Dosis (Opcional)") },
                placeholder = { Text("Ej: 500mg") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = outlinedFieldColors,
                leadingIcon = {
                    Icon(Icons.Default.MonitorWeight, null, tint = CronMedBlue, modifier = Modifier.size(20.dp))
                }
            )

            // ── Frecuencia ───────────────────────────────────────────────────
            OutlinedTextField(
                value = frecuencia,
                onValueChange = onFrecuenciaChange,
                label = { Text("Frecuencia (Horas) *") },
                placeholder = { Text("Cada cuántas horas") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = outlinedFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(Icons.Default.Update, null, tint = CronMedBlue, modifier = Modifier.size(20.dp))
                }
            )

            // ── Hora de inicio ───────────────────────────────────────────────
            Card(
                onClick = onTimeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CronMedDivider, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CronMedSurface),
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
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CronMedCardBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccessTime, null, tint = CronMedBlue, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Hora de inicio",
                            style = MaterialTheme.typography.labelMedium,
                            color = CronMedTextSecondary
                        )
                        Text(
                            text = timeFormatter.format(Date(horaInicioMillis)),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CronMedTextPrimary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CronMedCardBlue
                    ) {
                        Text(
                            "Cambiar",
                            color = CronMedBlue,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ── Observaciones ────────────────────────────────────────────────
            OutlinedTextField(
                value = observaciones,
                onValueChange = onObservacionesChange,
                label = { Text("Observaciones (Opcional)") },
                placeholder = { Text("Ej: Tomar después de las comidas...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = outlinedFieldColors,
                minLines = 3,
                leadingIcon = {
                    Icon(Icons.Default.Info, null, tint = CronMedBlue, modifier = Modifier.size(20.dp))
                }
            )

            // ── Switch Recordatorio ──────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CronMedDivider, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CronMedSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CronMedCardBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, tint = CronMedBlue, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Recordatorios",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = CronMedTextPrimary
                            )
                            Text(
                                "Recibir notificaciones en el móvil",
                                style = MaterialTheme.typography.bodySmall,
                                color = CronMedTextSecondary
                            )
                        }
                    }
                    Switch(
                        checked = activo,
                        onCheckedChange = onActivoChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CronMedBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = CronMedTextHint.copy(alpha = 0.3f)
                        )
                    )
                }
            }

            // ── Consejo de salud ──────────────────────────────────────────────
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
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CronMedTextPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💊", fontSize = 26.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Consejo de salud",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CronMedTextPrimary
                        )
                        Text(
                            "Mantén tus medicamentos en un lugar fresco y seco, lejos de la luz solar directa para preservar su efectividad.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CronMedTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            BotonPrincipal(
                texto = if (isEditing) "Actualizar Medicamento" else "Guardar Medicamento",
                onClick = onSaveClick,
                icono = Icons.Default.Save,
                enabled = canSave
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEEF4FF)
@Composable
fun MedicamentoFormPreview() {
    CronMedTheme {
        MedicamentoFormContent(
            medicamentoId = null,
            nombre = "Paracetamol", onNombreChange = {},
            dosis = "500mg", onDosisChange = {},
            frecuencia = "8", onFrecuenciaChange = {},
            observaciones = "Tomar después de los alimentos", onObservacionesChange = {},
            activo = true, onActivoChange = {},
            horaInicioMillis = System.currentTimeMillis(), onTimeClick = {},
            selectedImageUri = null, existingImagePath = null,
            onImageClick = {}, onSaveClick = {}, onNavigateBack = {},
            canSave = true
        )
    }
}
