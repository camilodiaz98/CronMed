package com.example.cronmed.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class MedicationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val medicationName = inputData.getString("medication_name") ?: "Medicamento"
        
        // Aquí se implementaría la lógica de la notificación
        // showNotification(medicationName)
        
        return Result.success()
    }
}
