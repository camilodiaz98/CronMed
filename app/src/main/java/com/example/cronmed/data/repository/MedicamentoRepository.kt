package com.example.cronmed.data.repository

import com.example.cronmed.data.local.HistorialEntity
import com.example.cronmed.data.local.MedicamentoDao
import com.example.cronmed.data.local.MedicamentoEntity
import com.example.cronmed.data.local.WaterLogEntity
import com.example.cronmed.data.local.WaterTotal
import com.example.cronmed.data.local.MedicationDayStat
import kotlinx.coroutines.flow.Flow

class MedicamentoRepository(private val medicamentoDao: MedicamentoDao) {
    val allMedicamentos: Flow<List<MedicamentoEntity>> = medicamentoDao.getAllMedicamentos()

    suspend fun insert(medicamento: MedicamentoEntity): Long {
        return medicamentoDao.insertMedicamento(medicamento)
    }

    suspend fun update(medicamento: MedicamentoEntity) {
        medicamentoDao.updateMedicamento(medicamento)
    }

    suspend fun delete(medicamento: MedicamentoEntity) {
        medicamentoDao.deleteMedicamento(medicamento)
    }

    suspend fun getMedicamentoById(id: Int): MedicamentoEntity? {
        return medicamentoDao.getMedicamentoById(id)
    }

    fun getHistorial(medicamentoId: Int): Flow<List<HistorialEntity>> {
        return medicamentoDao.getHistorialByMedicamento(medicamentoId)
    }

    fun getHistorialByDate(date: String): Flow<List<HistorialEntity>> {
        return medicamentoDao.getHistorialByDate(date)
    }

    suspend fun insertHistorial(historial: HistorialEntity) {
        medicamentoDao.insertHistorial(historial)
    }

    // Water Log Methods
    suspend fun insertWaterLog(waterLog: WaterLogEntity) {
        medicamentoDao.insertWaterLog(waterLog)
    }

    fun getWaterLogsByDate(date: String): Flow<List<WaterLogEntity>> {
        return medicamentoDao.getWaterLogsByDate(date)
    }

    fun getWeeklyWaterStats(): Flow<List<WaterTotal>> {
        return medicamentoDao.getWeeklyWaterStats()
    }

    // Medication Stats
    fun getWeeklyMedicationStats(medicamentoId: Int): Flow<List<MedicationDayStat>> {
        return medicamentoDao.getWeeklyMedicationStats(medicamentoId)
    }
}
