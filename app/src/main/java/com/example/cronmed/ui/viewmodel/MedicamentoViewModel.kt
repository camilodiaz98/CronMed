package com.example.cronmed.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cronmed.data.local.*
import com.example.cronmed.data.repository.MedicamentoRepository
import com.example.cronmed.util.AlarmScheduler
import com.example.cronmed.util.ImageUtils
import com.example.cronmed.util.WaterAlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class TodayDose(
    val time: Long,
    val name: String,
    val status: DoseStatus,
    val medId: Int
)

enum class DoseStatus { TAKEN, PENDING, UPCOMING, POSTPONED, OMITTED }

class MedicamentoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MedicamentoRepository
    private val alarmScheduler = AlarmScheduler(application)
    private val waterAlarmScheduler = WaterAlarmScheduler(application)
    
    val allMedicamentos: StateFlow<List<MedicamentoEntity>>

    private val sharedPrefs = application.getSharedPreferences("cronmed_prefs", Context.MODE_PRIVATE)
    
    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "Usuario CronMed") ?: "Usuario CronMed")
    val userName: StateFlow<String> = _userName.asStateFlow()

    // Water States
    private val _waterRemindersEnabled = MutableStateFlow(sharedPrefs.getBoolean("water_reminders_enabled", true))
    val waterRemindersEnabled = _waterRemindersEnabled.asStateFlow()

    private val _waterRemindersPerDay = MutableStateFlow(sharedPrefs.getInt("water_reminders_per_day", 4))
    val waterRemindersPerDay = _waterRemindersPerDay.asStateFlow()

    private val _waterAmountPerGlass = MutableStateFlow(sharedPrefs.getInt("water_amount_per_glass", 500))
    val waterAmountPerGlass = _waterAmountPerGlass.asStateFlow()

    private val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todayWaterLogs: StateFlow<List<WaterLogEntity>>
    val weeklyWaterStats: StateFlow<List<WaterTotal>>

    // Selected Medication Stats
    private val _selectedMedicationId = MutableStateFlow<Int?>(null)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedMedicationWeeklyStats: StateFlow<List<MedicationDayStat>> = _selectedMedicationId
        .flatMapLatest { id ->
            if (id != null) repository.getWeeklyMedicationStats(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Today's Doses for Chart and Regimen
    @OptIn(ExperimentalCoroutinesApi::class)
    val todayDoses: StateFlow<List<TodayDose>>

    init {
        val medicamentoDao = MedicamentoDatabase.getDatabase(application).medicamentoDao()
        repository = MedicamentoRepository(medicamentoDao)
        
        allMedicamentos = repository.allMedicamentos.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        todayWaterLogs = repository.getWaterLogsByDate(todayStr).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        weeklyWaterStats = repository.getWeeklyWaterStats().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        todayDoses = combine(
            repository.allMedicamentos,
            repository.getHistorialByDate(todayStr)
        ) { medicamentos, history ->
            val doses = mutableListOf<TodayDose>()
            val now = System.currentTimeMillis()
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfToday = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            val endOfToday = calendar.timeInMillis

            medicamentos.filter { it.activo }.forEach { med ->
                var projectedTime = med.horaInicio
                // Find first occurrence of today or before
                if (med.frecuenciaHoras > 0) {
                    while (projectedTime > startOfToday) {
                        projectedTime -= med.frecuenciaHoras * 3600000L
                    }
                }
                
                // Project forward through today
                while (projectedTime <= endOfToday) {
                    if (projectedTime >= startOfToday) {
                        val historyEntry = history.find { 
                            it.medicamentoId == med.id && 
                            Math.abs(it.fechaHoraProgramada - projectedTime) < 300000 // 5 mins tolerance
                        }
                        
                        val status = when {
                            historyEntry?.estado?.contains("TOMADO") == true -> DoseStatus.TAKEN
                            historyEntry?.estado?.contains("POSPUESTO") == true -> DoseStatus.POSTPONED
                            historyEntry?.estado?.contains("OMITIDO") == true -> DoseStatus.OMITTED
                            projectedTime < now - 1200000L -> DoseStatus.PENDING // 20 mins grace
                            else -> DoseStatus.UPCOMING
                        }
                        
                        doses.add(TodayDose(projectedTime, med.nombre, status, med.id))
                    }
                    if (med.frecuenciaHoras <= 0) break
                    projectedTime += med.frecuenciaHoras * 3600000L
                }
            }
            
            doses.sortedBy { it.time }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setSelectedMedicationId(id: Int?) {
        _selectedMedicationId.value = id
    }

    fun setUserName(name: String) {
        sharedPrefs.edit().putString("user_name", name).apply()
        _userName.value = name
    }

    fun addWater() = viewModelScope.launch {
        val log = WaterLogEntity(
            amountMl = _waterAmountPerGlass.value,
            dateStr = todayStr
        )
        repository.insertWaterLog(log)
    }

    fun toggleWaterReminders(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("water_reminders_enabled", enabled).apply()
        _waterRemindersEnabled.value = enabled
        if (enabled) {
            waterAlarmScheduler.scheduleWaterReminders(_waterRemindersPerDay.value)
        } else {
            waterAlarmScheduler.cancelAll()
        }
    }

    fun setWaterRemindersPerDay(count: Int) {
        sharedPrefs.edit().putInt("water_reminders_per_day", count).apply()
        _waterRemindersPerDay.value = count
        if (_waterRemindersEnabled.value) {
            waterAlarmScheduler.scheduleWaterReminders(count)
        }
    }

    fun setWaterAmountPerGlass(amount: Int) {
        sharedPrefs.edit().putInt("water_amount_per_glass", amount).apply()
        _waterAmountPerGlass.value = amount
    }

    fun insert(medicamento: MedicamentoEntity, imageUri: Uri? = null) = viewModelScope.launch {
        var finalMedicamento = medicamento
        imageUri?.let {
            val path = ImageUtils.saveImageToInternalStorage(getApplication(), it)
            finalMedicamento = finalMedicamento.copy(imagenPath = path)
        }
        
        val adjustedTime = adjustToNextOccurrence(finalMedicamento.horaInicio, finalMedicamento.frecuenciaHoras)
        finalMedicamento = finalMedicamento.copy(horaInicio = adjustedTime)

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
        
        val adjustedTime = adjustToNextOccurrence(finalMedicamento.horaInicio, finalMedicamento.frecuenciaHoras)
        finalMedicamento = finalMedicamento.copy(horaInicio = adjustedTime)

        repository.update(finalMedicamento)
        if (finalMedicamento.activo && !finalMedicamento.eliminado) {
            alarmScheduler.schedule(finalMedicamento)
        } else {
            alarmScheduler.cancel(finalMedicamento.id)
        }
    }

    private fun adjustToNextOccurrence(startTime: Long, frequencyHours: Int): Long {
        if (frequencyHours <= 0) return startTime
        val now = System.currentTimeMillis()
        var nextTime = startTime
        while (nextTime < now - 600000L) { // 10 mins grace
            nextTime += frequencyHours * 3600000L
        }
        return nextTime
    }

    fun softDelete(medicamento: MedicamentoEntity) = viewModelScope.launch {
        val updated = medicamento.copy(eliminado = true, activo = false)
        alarmScheduler.cancel(medicamento.id)
        repository.update(updated)
        
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
        softDelete(medicamento)
    }

    suspend fun getMedicamentoById(id: Int): MedicamentoEntity? {
        return repository.getMedicamentoById(id)
    }

    fun getHistorial(medicamentoId: Int): Flow<List<HistorialEntity>> {
        return repository.getHistorial(medicamentoId)
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
        
        // Mantener la coherencia del horario sumando la frecuencia a la hora programada
        val proximaToma = medicamento.horaInicio + (medicamento.frecuenciaHoras * 3600000L)
        val medicamentoActualizado = medicamento.copy(horaInicio = proximaToma)
        repository.update(medicamentoActualizado)
        
        if (medicamentoActualizado.activo && !medicamentoActualizado.eliminado) {
            alarmScheduler.schedule(medicamentoActualizado)
        }
    }
}

class MedicamentoViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicamentoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicamentoViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
