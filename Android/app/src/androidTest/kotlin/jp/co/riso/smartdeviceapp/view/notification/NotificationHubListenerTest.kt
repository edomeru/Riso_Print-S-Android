package jp.co.riso.smartdeviceapp.view.notification

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import okhttp3.internal.notify
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

class NotificationHubListenerTest {
    @Test
    fun testCreateNotificationChannel() {
        try {
            val mockContextGranted = mockContext(true)
            val mockContextDenied = mockContext(false)

            NotificationHubListener.createNotificationChannel(null, TEST_CHANNEL_ID)
            Assert.assertEquals(NotificationHubListener.getChannelId(), TEST_CHANNEL_ID)

            NotificationHubListener.createNotificationChannel(null, null)
            Assert.assertEquals(NotificationHubListener.getChannelId(), null)

            NotificationHubListener.createNotificationChannel(mockContextGranted, TEST_CHANNEL_ID)
            Assert.assertEquals(NotificationHubListener.getChannelId(), TEST_CHANNEL_ID)

            NotificationHubListener.createNotificationChannel(mockContextGranted, null)
            Assert.assertEquals(NotificationHubListener.getChannelId(), null)

            NotificationHubListener.createNotificationChannel(mockContextDenied, TEST_CHANNEL_ID)
            Assert.assertEquals(NotificationHubListener.getChannelId(), TEST_CHANNEL_ID)

            NotificationHubListener.createNotificationChannel(mockContextDenied, null)
            Assert.assertEquals(NotificationHubListener.getChannelId(), null)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_NullChannel_NullContext() {
        val listener = NotificationHubListener()
        val message1 = mockRemoteMessage(null, null)
        val message2 = mockRemoteMessage(TEST_TITLE, TEST_BODY)
        val notificationId = NotificationHubListener.getNotificationId()

        listener.onPushNotificationReceived(null, message1)
        listener.onPushNotificationReceived(null, message2)
        Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
    }

    @Test
    fun testOnPushNotificationReceived_PermissionGranted() {
        val listener = spyk<NotificationHubListener>()
        val mockContext = mockContext(true)
        val message1 = mockRemoteMessage(null, null)
        val message2 = mockRemoteMessage(TEST_TITLE, TEST_BODY)
        val notificationId = NotificationHubListener.getNotificationId()
        NotificationHubListener.createNotificationChannel(mockContext, TEST_CHANNEL_ID)

        // mockkConstructor on the Notification.Builder() does not work
        // as a workaround, create a function to return builder.build() and then mock it
        val mockNotification = mockk<Notification>(relaxed = true)
        mockkObject(NotificationHubListener)
        every { NotificationHubListener.getNotification(any()) } returns mockNotification
        listener.onPushNotificationReceived(mockContext, message1)
        listener.onPushNotificationReceived(mockContext, message2)
        unmockkObject(NotificationHubListener)
        Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)

        // Call the getNotification() function for coverage
        val mockBuilder = mockk<NotificationCompat.Builder>(relaxed = true)
        NotificationHubListener.getNotification(mockBuilder)
    }

    @Test
    fun testOnPushNotificationReceived_PermissionDenied() {
        val listener = spyk<NotificationHubListener>()
        val mockContext = mockContext(false)
        val message1 = mockRemoteMessage(null, null)
        val message2 = mockRemoteMessage(TEST_TITLE, TEST_BODY)
        val notificationId = NotificationHubListener.getNotificationId()
        NotificationHubListener.createNotificationChannel(mockContext, TEST_CHANNEL_ID)

        listener.onPushNotificationReceived(mockContext, message1)
        listener.onPushNotificationReceived(mockContext, message2)
        Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
    }

    // ================================================================================
    // Private Functions
    // ================================================================================
    private fun mockRemoteMessage(title: String?, body: String?): RemoteMessage {
        val mockNotification = mockk<RemoteMessage.Notification>()
        every { mockNotification.title } returns title
        every { mockNotification.body } returns body
        val mockRemoteMessage = mockk<RemoteMessage>()
        every { mockRemoteMessage.notification } returns mockNotification
        return mockRemoteMessage
    }

    companion object {
        private const val TEST_CHANNEL_ID = "test"
        private const val TEST_TITLE = "test notification message title"
        private const val TEST_BODY = "test notification message body"

        private fun mockContext(permission: Boolean = true): Activity {
            val mockContext = mockk<Activity>(relaxed = true)
            val mockNotificationManager = mockk<NotificationManager>()
            every { mockNotificationManager.createNotificationChannel(any()) } just Runs
            every { mockNotificationManager.notify(any(), any(), any()) } just Runs
            every { mockContext.getSystemService(Context.NOTIFICATION_SERVICE) } returns mockNotificationManager
            every { mockContext.requestPermissions(any(), any()) } returns Unit

            if (permission) {
                every {
                    mockContext.checkSelfPermission(POST_NOTIFICATIONS)
                    mockContext.checkPermission(POST_NOTIFICATIONS, any(), any())
                } returns PackageManager.PERMISSION_GRANTED
            } else {
                every {
                    mockContext.checkSelfPermission(POST_NOTIFICATIONS)
                    mockContext.checkPermission(POST_NOTIFICATIONS, any(), any())
                } returns PackageManager.PERMISSION_DENIED
            }
            return mockContext
        }

        @JvmStatic
        @BeforeClass
        fun set() {
            mockkStatic(PendingIntent::class)
            val mockIntent = mockk<PendingIntent>(relaxed = true)
            every { PendingIntent.getActivity(any(), any(), any(), any()) } returns mockIntent
        }

        @JvmStatic
        @AfterClass
        fun tearDown() {
            unmockkStatic(PendingIntent::class)
        }
    }
}