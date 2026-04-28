package com.example.cronmed.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.cronmed.data.local.HistorialEntity
import com.example.cronmed.data.local.MedicamentoDatabase
import com.example.cronmed.util.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("MEDICAMENTO_ID", -1)
        val action = intent.action
        val database = MedicamentoDatabase.getDatabase(context)
        val dao = database.medicamentoDao()
        val scheduler = AlarmScheduler(context)

        CoroutineScope(Dispatchers.IO).launch {
            val medicamento = dao.getMedicamentoById(id) ?: return@launch

            when (action) {
                "TOMAR" -> {
                    dao.insertHistorial(
                        HistorialEntity(
                            medicamentoId = id,
                            nombreMedicamento = medicamento.nombre,
                            fechaHoraProgramada = medicamento.horaInicio,
                            fechaHoraReal = System.currentTimeMillis(),
                            estado = "TOMADO"
                        )
                    )
                    // Calcular próxima toma relativa al momento actual (Flexible)
                    val siguienteHora = System.currentTimeMillis() + (medicamento.frecuenciaHoras * 3600000L)
                    val actualizado = medicamento.copy(horaInicio = siguienteHora)
                    dao.updateMedicamento(actualizado)
                    scheduler.schedule(actualizado)
                }
                "POSPONER" -> {
                    dao.insertHistorial(
                        HistorialEntity(
                            medicamentoId = id,
                            nombreMedicamento = medicamento.nombre,
                            fechaHoraProgramada = medicamento.horaInicio,
                            fechaHoraReal = System.currentTimeMillis(),
                            estado = "POSPUESTO",
                            observaciones = "Pospuesto 1 hora"
                        )
                    )
                    // Reprogramar en 1 hora desde el momento actual
                    val nuevaHora = System.currentTimeMillis() + 3600000L
                    val actualizado = medicamento.copy(horaInicio = nuevaHora)
                    dao.updateMedicamento(actualizado) // Actualizar DB para que la UI lo refleje
                    scheduler.schedule(actualizado)
                }
                "OMITIR" -> {
                    dao.insertHistorial(
                        HistorialEntity(
                            medicamentoId = id,
                            nombreMedicamento = medicamento.nombre,
                            fechaHoraProgramada = medicamento.horaInicio,
                            fechaHoraReal = System.currentTimeMillis(),
                            estado = "OMITIDO"
                        )
                    )
                    // Saltar esta dosis y programar la siguiente según frecuencia (Relativa a ahora)
                    val siguienteHora = System.currentTimeMillis() + (medicamento.frecuenciaHoras * 3600000L)
                    val actualizado = medicamento.copy(horaInicio = siguienteHora)
                    dao.updateMedicamento(actualizado)
                    scheduler.schedule(actualizado)
                }
            }

            // Cerrar notificación
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id)
        }
    }
}
