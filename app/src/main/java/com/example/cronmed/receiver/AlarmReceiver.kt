package com.example.cronmed.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.cronmed.MainActivity
import com.example.cronmed.R

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicamentoId = intent.getIntExtra("MEDICAMENTO_ID", -1)
        val medicamentoNombre = intent.getStringExtra("MEDICAMENTO_NOMBRE") ?: "Medicamento"

        showNotification(context, medicamentoId, medicamentoNombre)
    }

    private fun showNotification(context: Context, id: Int, nombre: String) {
        val channelId = "cronmed_alerts"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertas de Medicamentos", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // Acciones
        val takeIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "TOMAR"
            putExtra("MEDICAMENTO_ID", id)
        }
        val takePendingIntent = PendingIntent.getBroadcast(context, id * 10, takeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val postponeIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "POSPONER"
            putExtra("MEDICAMENTO_ID", id)
        }
        val postponePendingIntent = PendingIntent.getBroadcast(context, id * 20, postponeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Hora de tu medicamento")
            .setContentText("Es momento de tomar: $nombre")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .addAction(0, "Tomar ahora", takePendingIntent)
            .addAction(0, "Posponer 1h", postponePendingIntent)
            .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE))
            .build()

        notificationManager.notify(id, notification)
    }
}
