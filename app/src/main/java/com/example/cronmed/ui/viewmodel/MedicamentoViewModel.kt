package com.example.cronmed.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cronmed.data.local.HistorialEntity
import com.example.cronmed.data.local.MedicamentoDatabase
import com.example.cronmed.data.local.MedicamentoEntity
import com.example.cronmed.data.repository.MedicamentoRepository
import com.example.cronmed.util.AlarmScheduler
import com.example.cronmed.util.ImageUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicamentoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MedicamentoRepository
    private val alarmScheduler = AlarmScheduler(application)
    val allMedicamentos: StateFlow<List<MedicamentoEntity>>

    init {
        val medicamentoDao = MedicamentoDatabase.getDatabase(application).medicamentoDao()
        repository = MedicamentoRepository(medicamentoDao)
        allMedicamentos = repository.allMedicamentos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun insert(medicamento: MedicamentoEntity, imageUri: Uri? = null) = viewModelScope.launch {
        var finalMedicamento = medicamento
        imageUri?.let {
            val path = ImageUtils.saveImageToInternalStorage(getApplication(), it)
            finalMedicamento = finalMedicamento.copy(imagenPath = path)
        }
        val id = repository.insert(finalMedicamento)
        val insertedMedicamento = finalMedicamento.copy(id = id.toInt())
        if (insertedMedicamento.activo) {
            alarmScheduler.schedule(insertedMedicamento)
        }
    }

    fun update(medicamento: MedicamentoEntity, imageUri: Uri? = null) = viewModelScope.launch {
        var finalMedicamento = medicamento
        imageUri?.let {
            val path = ImageUtils.saveImageToInternalStorage(getApplication(), it)
            finalMedicamento = finalMedicamento.copy(imagenPath = path)
        }
        repository.update(finalMedicamento)
        if (finalMedicamento.activo && !finalMedicamento.eliminado) {
            alarmScheduler.schedule(finalMedicamento)
        } else {
            alarmScheduler.cancel(finalMedicamento.id)
        }
    }

    fun softDelete(medicamento: MedicamentoEntity) = viewModelScope.launch {
        val updated = medicamento.copy(eliminado = true, activo = false)
        alarmScheduler.cancel(medicamento.id)
        repository.update(updated)
        
        // Registrar en historial que se eliminó
        repository.insertHistorial(
            HistorialEntity(
                medicamentoId = medicamento.id,
                nombreMedicamento = medicamento.nombre,
                fechaHoraProgramada = System.currentTimeMillis(),
                fechaHoraReal = System.currentTimeMillis(),
                estado = "ELIMINADO (Deshabilitado)",
                observaciones = "El medicamento fue retirado del dashboard"
            )
        )
    }

    fun delete(medicamento: MedicamentoEntity) = viewModelScope.launch {
        // Mantenemos esta función pero ahora llama a softDelete para cumplir el requerimiento
        softDelete(medicamento)
    }

    suspend fun getMedicamentoById(id: Int): MedicamentoEntity? {
        return repository.getMedicamentoById(id)
    }

    fun getHistorial(medicamentoId: Int): StateFlow<List<HistorialEntity>> {
        return repository.getHistorial(medicamentoId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    fun registrarTomaManual(medicamento: MedicamentoEntity) = viewModelScope.launch {
        repository.insertHistorial(
            HistorialEntity(
                medicamentoId = medicamento.id,
                nombreMedicamento = medicamento.nombre,
                fechaHoraProgramada = medicamento.horaInicio,
                fechaHoraReal = System.currentTimeMillis(),
                estado = "TOMADO (Manual)"
            )
        )
        
        val proximaToma = System.currentTimeMillis() + (medicamento.frecuenciaHoras * 3600000L)
        val medicamentoActualizado = medicamento.copy(horaInicio = proximaToma)
        repository.update(medicamentoActualizado)
        
        if (medicamentoActualizado.activo && !medicamentoActualizado.eliminado) {
            alarmScheduler.schedule(medicamentoActualizado)
        }
    }
}
