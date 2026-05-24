package com.example.studentaccomodation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

object NotificationHelper {
    private const val CHANNEL_ID = "sn_alerts"
    private var notifId = 1000

    fun createChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(CHANNEL_ID, "Accommodation Alerts",
                NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alerts for matching listings and reservations"
                enableVibration(true)
            }
            (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(ch)
        }
    }

    fun sendMatchNotification(ctx: Context, title: String, price: Double, area: String) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🏠 New Match Found!")
            .setContentText("$title in $area — BWP ${price.toInt()}/month")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("A listing matching your preferences:\n\n$title\nLocation: $area\nPrice: BWP ${price.toInt()}/month\n\nTap to view details."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setColor(0xFF0E7C86.toInt())
            .build()
        try { NotificationManagerCompat.from(ctx).notify(notifId++, n) } catch (_: SecurityException) {}
    }

    fun sendReservationConfirmed(ctx: Context, ref: String, listingTitle: String) {
        val n = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("✅ Reservation Confirmed!")
            .setContentText("Ref: $ref  |  $listingTitle")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your reservation has been confirmed!\n\nReference: $ref\nProperty: $listingTitle"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(0xFF27AE60.toInt())
            .build()
        try { NotificationManagerCompat.from(ctx).notify(notifId++, n) } catch (_: SecurityException) {}
    }

    fun sendMessageNotification(ctx: Context, senderName: String, message: String) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            putExtra("navigate_to", "messages")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("💬 $senderName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setColor(0xFF1A3C5E.toInt())
            .build()
        try { NotificationManagerCompat.from(ctx).notify(notifId++, n) } catch (_: SecurityException) {}
    }
}

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body  = message.notification?.body  ?: message.data["body"]  ?: return
        val n = NotificationCompat.Builder(this, "sn_alerts")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        try { NotificationManagerCompat.from(this).notify(System.currentTimeMillis().toInt(), n) }
        catch (_: SecurityException) {}
    }

    override fun onNewToken(token: String) {
        // Token is stored to Firestore when user logs in (handled in AuthViewModel)
    }
}
