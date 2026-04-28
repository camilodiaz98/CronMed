package com.example.cronmed.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.example.cronmed.MainActivity

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medicamentoId = intent.getIntExtra("MEDICAMENTO_ID", -1)
        val medicamentoNombre = intent.getStringExtra("MEDICAMENTO_NOMBRE") ?: "Medicamento"

        showNotification(context, medicamentoId, medicamentoNombre)
    }

    private fun showNotification(context: Context, id: Int, nombre: String) {
        val channelId = "cronmed_alerts_v2"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = Settings.System.DEFAULT_ALARM_ALERT_URI

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, 
                "Alertas de Medicamentos Críticas", 
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones con sonido de alarma para medicamentos"
                setSound(soundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Acciones
        val takeIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "TOMAR"
            putExtra("MEDICAMENTO_ID", id)
        }
        val takePendingIntent = PendingIntent.getBroadcast(
            context, id * 10 + 1, takeIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val postponeIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "POSPONER"
            putExtra("MEDICAMENTO_ID", id)
        }
        val postponePendingIntent = PendingIntent.getBroadcast(
            context, id * 10 + 2, postponeIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val omitirIntent = Intent(context, ActionReceiver::class.java).apply {
            action = "OMITIR"
            putExtra("MEDICAMENTO_ID", id)
        }
        val omitirPendingIntent = PendingIntent.getBroadcast(
            context, id * 10 + 3, omitirIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("💊 ¡Hora de tu medicamento!")
            .setContentText("Es momento de tomar: $nombre")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setFullScreenIntent(
                PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE),
                true
            )
            .setAutoCancel(true)
            .addAction(0, "Tomar", takePendingIntent)
            .addAction(0, "Posponer 1h", postponePendingIntent)
            .addAction(0, "Omitir", omitirPendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }
}
