package jp.co.riso.android.dialog

import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.commitNow
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.co.riso.android.dialog.DialogUtils.dismissDialog
import jp.co.riso.android.dialog.DialogUtils.displayDialog
import jp.co.riso.android.dialog.InfoDialogFragment.Companion.newInstance
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.appContext
import jp.co.riso.smartdeviceapp.view.MainActivity
import jp.co.riso.smartprint.R
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DialogUtilsTest {

    private var _activity: MainActivity? = null

    @Before
    fun setUp() {
       //wakeUpScreen()
    }

    @Test
    fun testConstructor() {
        TestCase.assertNotNull(DialogUtils)
    }

    @Test
    fun testDisplayDialog() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity: MainActivity ->

                // wake up screen
                activity.runOnUiThread {
                    activity.window.addFlags(
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    )
                }
                waitFewSeconds()

                val d: InfoDialogFragment = newInstance(
                    appContext!!.resources.getString(MSG),
                    appContext!!.resources.getString(BUTTON_TITLE)
                )
                displayDialog(activity, TAG, d)
                waitFewSeconds()
                val dialog = activity.supportFragmentManager.findFragmentByTag(TAG)
                TestCase.assertTrue(dialog is DialogFragment)
                TestCase.assertTrue((dialog as DialogFragment?)!!.showsDialog)
                TestCase.assertTrue(
                    dialog!!.dialog!!.isShowing
                )
            }
            scenario.close()
        }
    }

    @Test
    fun testDismissDialog() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity: MainActivity ->

                // wake up screen
                activity.runOnUiThread {
                    activity.window.addFlags(
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    )
                }
                waitFewSeconds()

                val d: InfoDialogFragment = newInstance(
                    appContext!!.resources.getString(MSG),
                    appContext!!.resources.getString(BUTTON_TITLE)
                )
                displayDialog(activity, TAG, d)
                waitFewSeconds()
                var dialog = activity.supportFragmentManager.findFragmentByTag(TAG)
                TestCase.assertTrue(dialog is DialogFragment)
                TestCase.assertTrue((dialog as DialogFragment?)!!.showsDialog)
                TestCase.assertTrue(
                    dialog!!.dialog!!.isShowing
                )
                dismissDialog(activity, TAG)
                waitFewSeconds()
                TestCase.assertNull(dialog?.dialog)
                dialog = activity.supportFragmentManager.findFragmentByTag(TAG)
                TestCase.assertNull(dialog)
            }
            scenario.close()
        }
    }

    // ================================================================================
    // Private methods
    // ================================================================================
    // wait some seconds so that you can see the change on emulator/device.
    private fun waitFewSeconds() {
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun wakeUpScreen() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity: MainActivity ->
                activity.runOnUiThread {
                    activity.window.addFlags(
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    )
                }
                waitFewSeconds()
            }
        }
    }

    companion object {
        private const val TAG = "DialogUtilsTest"
        private const val MSG = R.string.ids_app_name
        private const val BUTTON_TITLE = R.string.ids_lbl_ok
    }
}