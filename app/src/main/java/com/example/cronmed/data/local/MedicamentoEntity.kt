package com.example.cronmed.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos")
data class MedicamentoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val dosis: String,
    val frecuenciaHoras: Int,
    val horaInicio: Long,
    val observaciones: String,
    val activo: Boolean = true,
    val imagenPath: String? = null,
    val eliminado: Boolean = false // Para ocultarlo pero mantener la trazabilidad
)
