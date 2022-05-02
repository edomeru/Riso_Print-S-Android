package jp.co.riso.smartdeviceapp.view

import android.view.View
import android.view.WindowManager
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.atomic.AtomicReference

open class BaseActivityTestUtil {
    @JvmField
    var mainActivity: MainActivity? = null

    @get:Rule
    var testRule = ActivityScenarioRule(
        MainActivity::class.java
    )

    @Before
    fun initMainActivity() {
        val activityRef = AtomicReference<MainActivity>()
        testRule.scenario.onActivity { newValue: MainActivity -> activityRef.set(newValue) }
        mainActivity = activityRef.get()
    }

    // wait some seconds so that you can see the change on emulator/device.
    fun waitFewSeconds() {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun testClick(id: Int) {
        val button = mainActivity!!.findViewById<View>(id)
        mainActivity!!.runOnUiThread { button.callOnClick() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }


    fun wakeUpScreen() {
        mainActivity!!.runOnUiThread {
            mainActivity!!.window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        waitFewSeconds()
    }

    @After
    fun tearDown() {
        if (NetUtils.isWifiAvailable) {
            NetUtils.unregisterWifiCallback(SmartDeviceApp.appContext!!)
        }
        testRule.scenario.close()
    }
}