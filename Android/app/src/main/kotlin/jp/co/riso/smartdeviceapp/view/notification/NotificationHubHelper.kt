/*
 * Copyright (c) 2024 RISO, Inc. All rights reserved.
 *
 * NotificationHubHelper.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.notification

import android.content.Context
import com.microsoft.windowsazure.messaging.NotificationHub
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class NotificationHubHelper(
    context: Context,
    private val hubName: String,
    private val hubListenConnectionString: String
) {
    private val hub: NotificationHub

    init {
        hub = NotificationHub(hubName, hubListenConnectionString, context)
    }

    suspend fun registerWithNotificationHubs(fcmToken: String) {
        withContext(Dispatchers.IO) {
            try {
                hub.register(fcmToken)
            } catch (e: Exception) {
                // Handle registration error
                throw e
            }
        }
    }
}