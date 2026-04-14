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
}
