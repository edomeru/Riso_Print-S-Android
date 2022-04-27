package jp.co.riso.android.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.dialog.WaitingDialogFragment.WaitingDialogListener
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

class WaitingDialogFragmentTest : BaseActivityTestUtil() {

    private var fm: FragmentManager? = null

    @Before
    fun setUp() {
        fm = mainActivity!!.supportFragmentManager
        wakeUpScreen()
    }

    @Test
    fun testNewInstance_WithNull() {
        val w = WaitingDialogFragment.newInstance(null, null, true, null)
        TestCase.assertNotNull(w)
        w.show(fm!!, TAG)

        // wait some seconds so that you can see the change on emulator/device.
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val fragment = fm!!.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertTrue(fragment.isCancelable)
        w.dismissAllowingStateLoss()
    }

    @Test
    fun testNewInstance_NotCancelable() {
        val w = WaitingDialogFragment.newInstance(TITLE, MSG, false, BUTTON_TITLE)
        TestCase.assertNotNull(w)
        w.retainInstance = true
        w.show(fm!!, TAG)
        waitFewSeconds()
        val fragment = fm!!.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertFalse(fragment.isCancelable)
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        TestCase.assertEquals("", b.text)
        TestCase.assertFalse(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).isShown)
        w.dismissAllowingStateLoss()
    }

    @Test
    fun testOnClick() {
        val w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE)
        TestCase.assertNotNull(w)
        val mockCallback = MockCallback()
        fm!!.beginTransaction().add(mockCallback, null).commit()
        w.setTargetFragment(mockCallback, 1)
        w.show(fm!!, TAG)
        waitFewSeconds()
        val fragment = fm!!.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertTrue(fragment.isCancelable)
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        TestCase.assertNotNull(b)
        TestCase.assertEquals(BUTTON_TITLE, b.text)
        try {
            performClick(b)
        } catch (e: Throwable) {
            Log.d(TAG, e.message!!)
        }
        TestCase.assertNull(fragment.dialog)
        TestCase.assertNull(fm!!.findFragmentByTag(TAG))
        TestCase.assertTrue(mockCallback.isCancelCalled)
    }

    @Test
    fun testOnCancel() {
        val w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE)
        TestCase.assertNotNull(w)
        val mockCallback = MockCallback()
        fm!!.beginTransaction().add(mockCallback, null).commit()
        w.setTargetFragment(mockCallback, 1)
        w.show(fm!!, TAG)
        waitFewSeconds()
        val fragment = fm!!.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertTrue(fragment.isCancelable)

        // back button
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        waitFewSeconds()
        TestCase.assertNull(fragment.dialog)
        TestCase.assertNull(fm!!.findFragmentByTag(TAG))
        TestCase.assertTrue(mockCallback.isCancelCalled)
    }

    @Test
    fun testSetMessage() {
        val w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE)
        TestCase.assertNotNull(w)
        w.show(fm!!, TAG)
        waitFewSeconds()
        val fragment = fm!!.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertTrue(fragment.isCancelable)
        w.setMessage("new message")
        waitFewSeconds()
        TestCase.assertNotNull(fragment.dialog)
        TestCase.assertNotNull(fm!!.findFragmentByTag(TAG))
        val msg = dialog.findViewById<View>(R.id.progressText)
        TestCase.assertNotNull(msg)
        TestCase.assertEquals("new message", (msg as TextView).text)
        w.dismissAllowingStateLoss()
    }

    @Test
    fun testSetMessage_NullDialog() {
        val w = WaitingDialogFragment.newInstance(TITLE, MSG, true, BUTTON_TITLE)
        TestCase.assertNotNull(w)
        try {
            w.setMessage("New message before displaying the dialog")
        } catch (e: NullPointerException) {
            TestCase.fail("setMessage null")
        }
        waitFewSeconds()
        w.show(fm!!, TAG)
        waitFewSeconds()
        val fragment = fm!!.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertTrue(fragment.isCancelable)
        val msg = dialog.findViewById<View>(R.id.progressText)
        TestCase.assertNotNull(msg)
        TestCase.assertEquals(MSG, (msg as TextView).text)
        dialog.dismiss()
        waitFewSeconds()
        TestCase.assertNull(fragment.dialog)
        TestCase.assertNull(fm!!.findFragmentByTag(TAG))
        try {
            w.setMessage("New message after closing the dialog")
        } catch (e: NullPointerException) {
            TestCase.fail("setMessage null")
        }
        w.dismissAllowingStateLoss()
    }

    @Test
    fun testOnKey_NotBack() {
        val w = WaitingDialogFragment.newInstance(TITLE, MSG, false, BUTTON_TITLE)
        TestCase.assertNotNull(w)
        w.show(fm!!, TAG)
        waitFewSeconds()
        val fragment = fm!!.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertFalse(fragment.isCancelable)
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_VOLUME_DOWN)
        waitFewSeconds()
        w.dismissAllowingStateLoss()
    }

    // ================================================================================
    // Internal Class
    // ================================================================================
    // for testing only
    @SuppressLint("ValidFragment")
    class MockCallback : Fragment(), WaitingDialogListener {
        var isCancelCalled = false
            private set

        override fun onCancel() {
            isCancelCalled = true
        }
    }

    @Throws(Throwable::class)
    private fun performClick(button: Button) {
        mainActivity!!.runOnUiThread { button.performClick() }
        waitFewSeconds()
    }

    companion object {
        private const val TAG = "WaitingDialogFragmentTest"
        private const val TITLE = "title"
        private const val MSG = "message"
        private const val BUTTON_TITLE = "OK"
    }
}