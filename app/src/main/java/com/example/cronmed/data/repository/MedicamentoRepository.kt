package com.example.cronmed.data.repository

import com.example.cronmed.data.local.HistorialEntity
import com.example.cronmed.data.local.MedicamentoDao
import com.example.cronmed.data.local.MedicamentoEntity
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

    suspend fun insertHistorial(historial: HistorialEntity) {
        medicamentoDao.insertHistorial(historial)
    }
}
