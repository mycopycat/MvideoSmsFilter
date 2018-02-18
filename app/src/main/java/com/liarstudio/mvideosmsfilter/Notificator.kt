package com.liarstudio.mvideosmsfilter

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.support.v4.app.NotificationCompat


public class Notificator(val context: Context) {

    companion object {
        const  val NOTIFICATION_SMS_ID = 1
    }
    public fun showNotification(title: String, message: String, icon: Int, isProgressRunning: Boolean) {
        /*Intent notifIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notifIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        */
        val notification = NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setProgress(0, 0, isProgressRunning)
                //.setContentIntent(contentIntent)
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round))//R.drawable.ic_meh_notif))
                .setOngoing(isProgressRunning)
                .build()
        if (!isProgressRunning)
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
        val notificationManager = context
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_SMS_ID, notification)
    }

}