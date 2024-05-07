package jp.co.riso.smartdeviceapp.view.notification

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import com.google.firebase.messaging.RemoteMessage
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NotificationHubListenerTest {
    private var _mockContextGranted: Context? = null
    private var _mockContextDenied: Context? = null

    @Before
    fun setup() {
        _mockContextGranted = mockContext(true)
        _mockContextDenied = mockContext(false)
    }

    @After
    fun tearDown() {
        _mockContextGranted = null
        _mockContextDenied = null
    }

    @Test
    fun testCreateNotificationChannel() {
        try {
            val channelId = "test"

            NotificationHubListener.createNotificationChannel(null, channelId)
            Assert.assertEquals(NotificationHubListener.getChannelId(), channelId)

            NotificationHubListener.createNotificationChannel(null, null)
            Assert.assertEquals(NotificationHubListener.getChannelId(), null)

            NotificationHubListener.createNotificationChannel(_mockContextGranted, channelId)
            Assert.assertEquals(NotificationHubListener.getChannelId(), channelId)

            NotificationHubListener.createNotificationChannel(_mockContextGranted, null)
            Assert.assertEquals(NotificationHubListener.getChannelId(), null)

            NotificationHubListener.createNotificationChannel(_mockContextDenied, channelId)
            Assert.assertEquals(NotificationHubListener.getChannelId(), channelId)

            NotificationHubListener.createNotificationChannel(_mockContextDenied, null)
            Assert.assertEquals(NotificationHubListener.getChannelId(), null)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_1() {
        try {
            val listener = NotificationHubListener()
            val message1 = mockRemoteMessage(null, null)
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(null, message1)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_2() {
        try {
            val listener = NotificationHubListener()
            val message2 = mockRemoteMessage(null, "test")
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(null, message2)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_3() {
        try {
            val listener = NotificationHubListener()
            val message3 = mockRemoteMessage("test", null)
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(null, message3)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_4() {
        try {
            val listener = NotificationHubListener()
            val message4 = mockRemoteMessage("test", "test")
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(null, message4)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_5() {
        try {
            val listener = NotificationHubListener()
            val message1 = mockRemoteMessage(null, null)
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(_mockContextGranted, message1)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_6() {
        try {
            val listener = NotificationHubListener()
            val message2 = mockRemoteMessage(null, "test")
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(_mockContextGranted, message2)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_7() {
        try {
            val listener = NotificationHubListener()
            val message3 = mockRemoteMessage("test", null)
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(_mockContextGranted, message3)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_8() {
        try {
            val listener = NotificationHubListener()
            val message4 = mockRemoteMessage("test", "test")
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(_mockContextGranted, message4)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_9() {
        try {
            val listener = NotificationHubListener()
            val message1 = mockRemoteMessage(null, null)
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(_mockContextDenied, message1)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_10() {
        try {
            val listener = NotificationHubListener()
            val message2 = mockRemoteMessage(null, "test")
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(_mockContextDenied, message2)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_11() {
        try {
            val listener = NotificationHubListener()
            val message3 = mockRemoteMessage("test", null)
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(_mockContextDenied, message3)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    @Test
    fun testOnPushNotificationReceived_12() {
        try {
            val listener = NotificationHubListener()
            val message4 = mockRemoteMessage("test", "test")
            val notificationId = NotificationHubListener.getNotificationId()

            listener.onPushNotificationReceived(_mockContextDenied, message4)
            Assert.assertTrue(NotificationHubListener.getNotificationId() > notificationId)
        } catch (e: Exception) {
            Assert.fail() // Error should not be thrown
        }
    }

    private fun mockContext(permission: Boolean = true): Activity {
        val mockContext = mockk<Activity>()
        val mockNotificationManager = mockk<NotificationManager>()
        every { mockNotificationManager.createNotificationChannel(any()) } returns Unit
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

    private fun mockRemoteMessage(title: String?, body: String?): RemoteMessage {
        val mockNotification = mockk<RemoteMessage.Notification>()
        every { mockNotification.title } returns title
        every { mockNotification.body } returns body
        val mockRemoteMessage = mockk<RemoteMessage>()
        every { mockRemoteMessage.notification } returns mockNotification
        return mockRemoteMessage
    }
}