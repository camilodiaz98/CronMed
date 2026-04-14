package com.example.cronmed.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historial_medicamentos")
data class HistorialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val medicamentoId: Int,
    val nombreMedicamento: String,
    val fechaHoraProgramada: Long,
    val fechaHoraReal: Long,
    val estado: String, // "TOMADO", "POSPUESTO", "OMITIDO"
    val observaciones: String = ""
)
