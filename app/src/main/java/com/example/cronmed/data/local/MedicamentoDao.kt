package com.example.cronmed.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicamentoDao {
    @Query("SELECT * FROM medicamentos WHERE eliminado = 0 ORDER BY id DESC")
    fun getAllMedicamentos(): Flow<List<MedicamentoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicamento(medicamento: MedicamentoEntity): Long

    @Update
    suspend fun updateMedicamento(medicamento: MedicamentoEntity)

    @Delete
    suspend fun deleteMedicamento(medicamento: MedicamentoEntity)

    @Query("SELECT * FROM medicamentos WHERE id = :id")
    suspend fun getMedicamentoById(id: Int): MedicamentoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorial(historial: HistorialEntity)

    @Query("SELECT * FROM historial_medicamentos WHERE medicamentoId = :medicamentoId ORDER BY fechaHoraReal DESC")
    fun getHistorialByMedicamento(medicamentoId: Int): Flow<List<HistorialEntity>>

    @Query("""
        SELECT * FROM historial_medicamentos 
        WHERE date(fechaHoraReal / 1000, 'unixepoch', 'localtime') = :date 
        OR date(fechaHoraProgramada / 1000, 'unixepoch', 'localtime') = :date
    """)
    fun getHistorialByDate(date: String): Flow<List<HistorialEntity>>

    // Water Log Methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(waterLog: WaterLogEntity)

    @Query("SELECT * FROM water_log WHERE dateStr = :date ORDER BY timestamp DESC")
    fun getWaterLogsByDate(date: String): Flow<List<WaterLogEntity>>

    @Query("SELECT dateStr, SUM(amountMl) as totalAmount FROM water_log GROUP BY dateStr ORDER BY dateStr DESC LIMIT 7")
    fun getWeeklyWaterStats(): Flow<List<WaterTotal>>

    // Medication Weekly Stats
    @Query("""
        SELECT date(fechaHoraReal / 1000, 'unixepoch', 'localtime') as dateStr, COUNT(*) as count 
        FROM historial_medicamentos 
        WHERE medicamentoId = :medicamentoId AND estado LIKE '%TOMADO%'
        GROUP BY dateStr 
        ORDER BY dateStr DESC 
        LIMIT 7
    """)
    fun getWeeklyMedicationStats(medicamentoId: Int): Flow<List<MedicationDayStat>>
}

data class WaterTotal(
    val dateStr: String,
    val totalAmount: Int
)

data class MedicationDayStat(
    val dateStr: String,
    val count: Int
)
