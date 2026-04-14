package com.example.cronmed.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.cronmed.data.local.MedicamentoEntity
import com.example.cronmed.ui.components.BotonPrincipal
import com.example.cronmed.ui.viewmodel.MedicamentoViewModel
import com.example.cronmed.ui.theme.CronMedTheme
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
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(medicamentoId) {
        if (medicamentoId != null && medicamentoId != -1) {
            val medicamento = viewModel.getMedicamentoById(medicamentoId)
            medicamento?.let {
                nombre = it.nombre
                dosis = it.dosis
                frecuencia = it.frecuenciaHoras.toString()
                observaciones = it.observaciones
                activo = it.activo
                horaInicioMillis = it.horaInicio
                existingImagePath = it.imagenPath
            }
        }
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            horaInicioMillis = calendar.timeInMillis
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    MedicamentoFormContent(
        medicamentoId = medicamentoId,
        nombre = nombre,
        onNombreChange = { nombre = it },
        dosis = dosis,
        onDosisChange = { dosis = it },
        frecuencia = frecuencia,
        onFrecuenciaChange = { frecuencia = it },
        observaciones = observaciones,
        onObservacionesChange = { observaciones = it },
        activo = activo,
        onActivoChange = { activo = it },
        horaInicioMillis = horaInicioMillis,
        onTimeClick = { timePickerDialog.show() },
        selectedImageUri = selectedImageUri,
        existingImagePath = existingImagePath,
        onImageClick = { galleryLauncher.launch("image/*") },
        onSaveClick = {
            if (nombre.isNotBlank() && dosis.isNotBlank() && frecuencia.isNotBlank()) {
                val newMedicamento = MedicamentoEntity(
                    id = if (medicamentoId != null && medicamentoId != -1) medicamentoId else 0,
                    nombre = nombre,
                    dosis = dosis,
                    frecuenciaHoras = frecuencia.toIntOrNull() ?: 0,
                    horaInicio = horaInicioMillis,
                    observaciones = observaciones,
                    activo = activo,
                    imagenPath = existingImagePath
                )
                if (medicamentoId == null || medicamentoId == -1) {
                    viewModel.insert(newMedicamento, selectedImageUri)
                } else {
                    viewModel.update(newMedicamento, selectedImageUri)
                }
                onNavigateBack()
            }
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicamentoFormContent(
    medicamentoId: Int?,
    nombre: String,
    onNombreChange: (String) -> Unit,
    dosis: String,
    onDosisChange: (String) -> Unit,
    frecuencia: String,
    onFrecuenciaChange: (String) -> Unit,
    observaciones: String,
    onObservacionesChange: (String) -> Unit,
    activo: Boolean,
    onActivoChange: (Boolean) -> Unit,
    horaInicioMillis: Long,
    onTimeClick: () -> Unit,
    selectedImageUri: Uri?,
    existingImagePath: String?,
    onImageClick: () -> Unit,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (medicamentoId == null || medicamentoId == -1) "Nuevo Medicamento" else "Editar Medicamento",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Selector de Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { onImageClick() },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null || existingImagePath != null) {
                    AsyncImage(
                        model = selectedImageUri ?: existingImagePath?.let { File(it) },
                        contentDescription = "Imagen del medicamento",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Añadir foto del medicamento", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Campo Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre del medicamento") },
                placeholder = { Text("Ej: Paracetamol") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.Medication, null, tint = MaterialTheme.colorScheme.primary) }
            )

            // Campo Dosis
            OutlinedTextField(
                value = dosis,
                onValueChange = onDosisChange,
                label = { Text("Dosis") },
                placeholder = { Text("Ej: 500mg") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                leadingIcon = { Icon(Icons.Default.MonitorWeight, null, tint = MaterialTheme.colorScheme.primary) }
            )

            // Frecuencia
            OutlinedTextField(
                value = frecuencia,
                onValueChange = onFrecuenciaChange,
                label = { Text("Frecuencia (Horas)") },
                placeholder = { Text("Cada cuantas horas") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Update, null, tint = MaterialTheme.colorScheme.primary) }
            )

            // Selector de Hora
            Card(
                onClick = onTimeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccessTime, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hora de inicio", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = timeFormatter.format(Date(horaInicioMillis)),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Cambiar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            // Observaciones
            OutlinedTextField(
                value = observaciones,
                onValueChange = onObservacionesChange,
                label = { Text("Observaciones adicionales") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                minLines = 3,
                leadingIcon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary) }
            )

            // Switch Activo
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Activar recordatorio", fontWeight = FontWeight.Medium)
                    Switch(checked = activo, onCheckedChange = onActivoChange)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            BotonPrincipal(
                texto = "Guardar Medicamento",
                onClick = onSaveClick
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MedicamentoFormPreview() {
    CronMedTheme {
        MedicamentoFormContent(
            medicamentoId = null,
            nombre = "Paracetamol",
            onNombreChange = {},
            dosis = "500mg",
            onDosisChange = {},
            frecuencia = "8",
            onFrecuenciaChange = {},
            observaciones = "Tomar después de los alimentos",
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
    }
}
