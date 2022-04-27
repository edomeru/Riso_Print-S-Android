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
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.android.dialog.ConfirmDialogFragment.ConfirmDialogListener
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import junit.framework.TestCase.*
import org.junit.Before
import org.junit.Test

class ConfirmDialogFragmentTest : BaseActivityTestUtil() {

    @Before
    fun setUp() {
        wakeUpScreen()
    }

    @Test
    fun testNewInstance_WithNull() {
        val c = ConfirmDialogFragment.newInstance(null, null, null)
        assertNotNull(c)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)
        c.dismissAllowingStateLoss()
    }

    @Test
    fun testNewInstance_WithMessage() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                MSG
            ),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)
        val msg = dialog.findViewById<View>(android.R.id.message)
        assertNotNull(msg)
        assertEquals(
            SmartDeviceApp.getAppContext().resources.getString(MSG),
            (msg as TextView).text
        )
        val pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        val neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        assertNotNull(pos)
        assertNotNull(neg)
        assertEquals(
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            pos.text
        )
        assertEquals(
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON),
            neg.text
        )
        c.dismissAllowingStateLoss()
    }

    @Test
    fun testNewInstance_WithTitle() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                TITLE
            ),
            SmartDeviceApp.getAppContext().resources.getString(MSG),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)
        val msg = dialog.findViewById<View>(android.R.id.message)
        assertNotNull(msg)
        assertEquals(
            SmartDeviceApp.getAppContext().resources.getString(MSG),
            (msg as TextView).text
        )
        val titleId = mainActivity!!.resources.getIdentifier("alertTitle", "id", "android")
        assertFalse(titleId == 0)
        val title = dialog.findViewById<View>(titleId)
        assertNotNull(title)
        assertEquals(
            SmartDeviceApp.getAppContext().resources.getString(TITLE),
            (title as TextView).text
        )
        val pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        val neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        assertNotNull(pos)
        assertNotNull(neg)
        assertEquals(
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            pos.text
        )
        assertEquals(
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON),
            neg.text
        )
        c.dismissAllowingStateLoss()
    }

    @Test
    fun testOnClick_Positive() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                MSG
            ),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)
        val pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        assertNotNull(pos)
        try {
            performClick(pos)
        } catch (e: Throwable) {
            Log.d(TAG, e.message!!)
        }
        assertNull(fragment.dialog)
        assertNull(mainActivity!!.fragmentManager.findFragmentByTag(TAG))
    }

    @Test
    fun testOnClick_Negative() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                MSG
            ),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)
        val neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        assertNotNull(neg)
        try {
            performClick(neg)
        } catch (e: Throwable) {
            Log.d(TAG, e.message!!)
        }
        assertNull(fragment.dialog)
        assertNull(mainActivity!!.fragmentManager.findFragmentByTag(TAG))
    }

    @Test
    fun testOnClick_PositiveListener() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                MSG
            ),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        val mockCallback = MockCallback()
        mainActivity!!.supportFragmentManager.beginTransaction().add(mockCallback, null).commit()
        c.setTargetFragment(mockCallback, 1)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)
        val pos = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        assertNotNull(pos)
        try {
            performClick(pos)
        } catch (e: Throwable) {
            Log.d(TAG, e.message!!)
        }
        assertNull(fragment.dialog)
        assertNull(mainActivity!!.supportFragmentManager.findFragmentByTag(TAG))
        assertTrue(mockCallback.isConfirmCalled)
    }

    @Test
    fun testOnClick_NegativeListener() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                MSG
            ),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        val mockCallback = MockCallback()
        mainActivity!!.supportFragmentManager.beginTransaction().add(mockCallback, null).commit()
        c.setTargetFragment(mockCallback, 1)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)
        val neg = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        assertNotNull(neg)
        try {
            performClick(neg)
        } catch (e: Throwable) {
            Log.d(TAG, e.message!!)
        }
        assertNull(fragment.dialog)
        assertNull(mainActivity!!.supportFragmentManager.findFragmentByTag(TAG))
        assertTrue(mockCallback.isCancelCalled)
    }

    @Test
    fun testOnCancel() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                TITLE
            ),
            SmartDeviceApp.getAppContext().resources.getString(MSG),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        val mockCallback = MockCallback()
        mainActivity!!.supportFragmentManager.beginTransaction().add(mockCallback, null).commit()
        c.setTargetFragment(mockCallback, 1)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        assertNotNull(dialog)
        assertTrue(dialog!!.isShowing)
        assertTrue(fragment.isCancelable)
        InstrumentationRegistry.getInstrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
        waitFewSeconds()
        assertNull(fragment.dialog)
        assertNull(mainActivity!!.supportFragmentManager.findFragmentByTag(TAG))
        assertTrue(mockCallback.isCancelCalled)
    }

    @Test
    fun testIsShowing_Null() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                MSG
            ),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        assertFalse(c.isShowing)
    }

    @Test
    fun testIsShowing_False() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                MSG
            ),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        waitFewSeconds()
        assertFalse(c.isShowing)
    }

    @Test
    fun testIsShowing_True() {
        val c = ConfirmDialogFragment.newInstance(
            SmartDeviceApp.getAppContext().resources.getString(
                MSG
            ),
            SmartDeviceApp.getAppContext().resources.getString(POSITIVE_BUTTON),
            SmartDeviceApp.getAppContext().resources.getString(NEGATIVE_BUTTON)
        )
        assertNotNull(c)
        c.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        assertTrue(fragment is DialogFragment)
        assertTrue((fragment as DialogFragment?)!!.showsDialog)
        assertTrue(c.isShowing)
    }

    //================================================================================
    // Private
    //================================================================================
    @Throws(Throwable::class)
    private fun performClick(button: Button) {
        mainActivity!!.runOnUiThread { button.callOnClick() }
        waitFewSeconds()
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    // for testing only
    @SuppressLint("ValidFragment")
    class MockCallback : Fragment(), ConfirmDialogListener {
        var isConfirmCalled = false
            private set
        var isCancelCalled = false
            private set

        override fun onConfirm() {
            isConfirmCalled = true
        }

        override fun onCancel() {
            isCancelCalled = true
        }
    }

    companion object {
        private const val TAG = "ConfirmDialogFragmentTest"
        private const val TITLE = R.string.ids_app_name
        private const val MSG = R.string.ids_app_name
        private const val POSITIVE_BUTTON = R.string.ids_lbl_ok
        private const val NEGATIVE_BUTTON = R.string.ids_lbl_cancel
    }
}