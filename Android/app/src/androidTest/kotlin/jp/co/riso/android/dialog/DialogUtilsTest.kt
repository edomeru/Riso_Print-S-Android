package jp.co.riso.android.dialog

import androidx.fragment.app.DialogFragment
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import jp.co.riso.smartprint.R
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

class DialogUtilsTest : BaseActivityTestUtil() {

    @Before
    fun setUp() {
        wakeUpScreen()
    }

    @Test
    fun testConstructor() {
        TestCase.assertNotNull(DialogUtils)
    }

    @Test
    fun testDisplayDialog() {
        val d = InfoDialogFragment.newInstance(
            SmartDeviceApp.appContext!!.resources.getString(
                MSG
            ),
            SmartDeviceApp.appContext!!.resources.getString(BUTTON_TITLE)
        )
        DialogUtils.displayDialog(mainActivity!!, TAG, d)
        waitFewSeconds()
        val dialog = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        TestCase.assertTrue(dialog is DialogFragment)
        TestCase.assertTrue((dialog as DialogFragment?)!!.showsDialog)
        TestCase.assertTrue(
            dialog!!.dialog!!.isShowing
        )
    }

    @Test
    fun testDismissDialog() {
        val d = InfoDialogFragment.newInstance(
            SmartDeviceApp.appContext!!.resources.getString(
                MSG
            ),
            SmartDeviceApp.appContext!!.resources.getString(BUTTON_TITLE)
        )
        DialogUtils.displayDialog(mainActivity!!, TAG, d)
        waitFewSeconds()
        var dialog = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        TestCase.assertTrue(dialog is DialogFragment)
        TestCase.assertTrue((dialog as DialogFragment?)!!.showsDialog)
        TestCase.assertTrue(
            dialog!!.dialog!!.isShowing
        )
        DialogUtils.dismissDialog(mainActivity!!, TAG)
        waitFewSeconds()
        TestCase.assertNull(dialog.dialog)
        dialog = mainActivity!!.supportFragmentManager.findFragmentByTag(TAG)
        TestCase.assertNull(dialog)
    }

    companion object {
        private const val TAG = "DialogUtilsTest"
        private const val MSG = R.string.ids_app_name
        private const val BUTTON_TITLE = R.string.ids_lbl_ok
    }
}