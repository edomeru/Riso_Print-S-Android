/*
 * Copyright (c) 2024 RISO, Inc. All rights reserved.
 *
 * NotificationHubListener.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.notification

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import com.microsoft.windowsazure.messaging.notificationhubs.NotificationListener
import jp.co.riso.android.util.AppUtils
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager
import jp.co.riso.smartdeviceapp.view.SplashActivity
import jp.co.riso.smartprint.R


/**
 * @class NotificationHubListener
 *
 * @brief A class that listens for notifications from the Azure Notification Hubs
 */
class NotificationHubListener : NotificationListener {
    override fun onPushNotificationReceived(context: Context?, message: RemoteMessage?) {
        val notification = message?.notification
        val title = notification?.title
        val body = notification?.body
        displayNotification(context, title, body)
    }

    companion object {
        private var channelId: String? = null
        private var notificationId: Int = 0

        // Get the private variable for testing
        fun getChannelId(): String? {
            return channelId
        }

        // Get the private variable for testing
        fun getNotificationId(): Int {
            return notificationId
        }

        fun createNotificationChannel(context: Context?, channelId: String?) {
            // Create the NotificationChannel (supported on API 26+)
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            // https://developer.android.com/develop/ui/views/notifications/channels#importance
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelId, importance)
            channel.setSound(soundUri, audioAttrs)

            // Register the channel with the system.
            val notificationManager: NotificationManager? =
                context?.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)

            // If device is Android 13, request permission for notifications
            if (context != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    val activity = context as Activity
                    ActivityCompat.requestPermissions(activity, arrayOf(POST_NOTIFICATIONS), 1)
                }
            }

            this.channelId = channelId
        }

        fun displayNotification(context: Context?, title: String?, body: String?) {
            // Increment the notification ID
            notificationId += 1

            // The channel ID should be created when the App is started
            if (channelId != null && context != null && title != null && body != null) {
                // https://developer.android.com/develop/ui/views/notifications/build-notification#lockscreenNotification
                val launchIntent =
                    AppUtils.createActivityIntent(context, SplashActivity::class.java)
                launchIntent!!.putExtra("body", body)
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

                // https://developer.android.com/about/versions/12/behavior-changes-12#pending-intent-mutability
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val bigTextStyle = NotificationCompat.BigTextStyle()
                    .bigText(body)
                    .setBigContentTitle(title)
                val builder = NotificationCompat.Builder(context, channelId!!)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(bigTextStyle)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)

                with(NotificationManagerCompat.from(context)) {
                    // Check if device is Android 13 for permissions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return@with
                        }
                    }

                    notify(notificationId, getNotification(builder))
                }
            }
        }

        fun getNotification(builder: NotificationCompat.Builder): Notification {
            return builder.build()
        }
    }
}
