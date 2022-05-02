package jp.co.riso.android.dialog

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

class InfoDialogFragmentTest : BaseActivityTestUtil() {

    @Before
    fun setUp() {
        wakeUpScreen()
    }

    @Test
    fun testNewInstance_WithNull() {
        val info = InfoDialogFragment.newInstance(null, null)
        TestCase.assertNotNull(info)
        info.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        info.dismissAllowingStateLoss()
    }

    @Test
    fun testNewInstance_WithMessage() {
        val info = InfoDialogFragment.newInstance(
            SmartDeviceApp.appContext!!.resources.getString(
                MSG
            ),
            SmartDeviceApp.appContext!!.resources.getString(BUTTON_TITLE)
        )
        TestCase.assertNotNull(info)
        info.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertTrue(fragment.isCancelable)
        val msg = dialog.findViewById<View>(android.R.id.message)
        TestCase.assertNotNull(msg)
        TestCase.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(MSG),
            (msg as TextView).text
        )
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        TestCase.assertNotNull(b)
        TestCase.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(BUTTON_TITLE),
            b.text
        )
        info.dismissAllowingStateLoss()
    }

    @Test
    fun testNewInstance_WithTitle() {
        val info = InfoDialogFragment.newInstance(
            SmartDeviceApp.appContext!!.resources.getString(
                TITLE
            ),
            SmartDeviceApp.appContext!!.resources.getString(MSG),
            SmartDeviceApp.appContext!!.resources.getString(BUTTON_TITLE)
        )
        TestCase.assertNotNull(info)
        info.show(mainActivity!!.supportFragmentManager, TAG)
        waitFewSeconds()
        val fragment = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        TestCase.assertTrue(fragment is DialogFragment)
        TestCase.assertTrue((fragment as DialogFragment?)!!.showsDialog)
        val dialog = fragment!!.dialog as AlertDialog?
        TestCase.assertNotNull(dialog)
        TestCase.assertTrue(dialog!!.isShowing)
        TestCase.assertTrue(fragment.isCancelable)
        val msg = dialog.findViewById<View>(android.R.id.message)
        TestCase.assertNotNull(msg)
        TestCase.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(MSG),
            (msg as TextView).text
        )
        val titleId = mainActivity!!.resources.getIdentifier("alertTitle", "id", "android")
        TestCase.assertFalse(titleId == 0)
        val title = dialog.findViewById<View>(titleId)
        TestCase.assertNotNull(title)
        TestCase.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(TITLE),
            (title as TextView).text
        )
        val b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        TestCase.assertNotNull(b)
        TestCase.assertEquals(
            SmartDeviceApp.appContext!!.resources.getString(BUTTON_TITLE),
            b.text
        )
        info.dismissAllowingStateLoss()
    }

    companion object {
        private const val TAG = "InfoDialogFragmentTest"
        private const val TITLE = R.string.ids_app_name
        private const val MSG = R.string.ids_app_name
        private const val BUTTON_TITLE = R.string.ids_lbl_ok
    }
}