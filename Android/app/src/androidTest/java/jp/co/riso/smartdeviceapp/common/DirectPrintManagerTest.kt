package jp.co.riso.smartdeviceapp.common

import android.test.ActivityInstrumentationTestCase2
import androidx.preference.PreferenceManager
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.appContext
import jp.co.riso.smartdeviceapp.common.DirectPrintManager.DirectPrintCallback
import jp.co.riso.smartdeviceapp.view.MainActivity
import junit.framework.TestCase

class DirectPrintManagerTest : ActivityInstrumentationTestCase2<MainActivity> {
    private var mgr: DirectPrintManager? = null

    constructor() : super(MainActivity::class.java) {}
    constructor(activityClass: Class<MainActivity?>?) : super(activityClass) {}

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mgr = DirectPrintManager()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    // ================================================================================
    // Tests - executeLPRPrint
    // ================================================================================
    fun testDirectPrint_ValidCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeLPRPrint(
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "hostname"
        )
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertTrue(callback.mCalled)
    }

    fun testDirectPrint_NullParameterValidCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeLPRPrint(null, null, null, null, null, null, null, null, null)
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testDirectPrint_EmptyParametersValidCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeLPRPrint("", "", "", "", "", "", "", "", "")
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testDirectPrint_NullCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(null)
        mgr!!.executeLPRPrint(
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "hostname"
        )

        //wait for response
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testDirectPrint_NullParameterNullCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(null)
        mgr!!.executeLPRPrint(null, null, null, null, null, null, null, null, null)

        // wait for response
        while (mgr!!.isPrinting) {
            // wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testDirectPrint_EmptyParametersNullCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(null)
        mgr!!.executeLPRPrint("", "", "", "", "", "", "", "", "")

        // wait for response
        while (mgr!!.isPrinting) {
            // wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testDirectPrint_JobIDIncrement() {
        // Set Job ID
        val preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val editor = preferences.edit()
        editor.putInt(
            AppConstants.PREF_KEY_JOB_NUMBER_COUNTER,
            AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER
        )
        editor.commit()
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeLPRPrint(
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "hostname"
        )

        //wait for response
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        val newJobNum = preferences.getInt(
            AppConstants.PREF_KEY_JOB_NUMBER_COUNTER,
            AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER
        )
        TestCase.assertNotSame(newJobNum, AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER)
    }

    fun testDirectPrint_JobIDMaxNumber() {
        // Set Job ID
        val preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val editor = preferences.edit()
        editor.putInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, AppConstants.CONST_MAX_JOB_NUMBER)
        editor.commit()
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeLPRPrint(
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "hostname"
        )

        //wait for response
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        val newJobNum = preferences.getInt(
            AppConstants.PREF_KEY_JOB_NUMBER_COUNTER,
            AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER
        )
        // Retry increment (Max + 2)
        TestCase.assertEquals(1, newJobNum)
    }

    // ================================================================================
    // Tests - executeRAWPrint
    // ================================================================================
    fun testRawPrint_ValidCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeRAWPrint(
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "hostname"
        )
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertTrue(callback.mCalled)
    }

    fun testRawPrint_NullParameterValidCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeRAWPrint(null, null, null, null, null, null, null, null, null)
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testRawPrint_EmptyParametersValidCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeRAWPrint("", "", "", "", "", "", "", "", "")
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testRawPrint_NullCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(null)
        mgr!!.executeRAWPrint(
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "hostname"
        )

        //wait for response
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testRawPrint_NullParameterNullCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(null)
        mgr!!.executeRAWPrint(null, null, null, null, null, null, null, null, null)

        // wait for response
        while (mgr!!.isPrinting) {
            // wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testRawPrint_EmptyParametersNullCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(null)
        mgr!!.executeRAWPrint("", "", "", "", "", "", "", "", "")

        // wait for response
        while (mgr!!.isPrinting) {
            // wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    // ================================================================================
    // Tests - sendCancelCommand
    // ================================================================================
    fun testSendCancelCommand() {
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.executeLPRPrint(
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "hostname"
        )
        TestCase.assertFalse(callback.mCalled)
        mgr!!.sendCancelCommand()

        //wait for response
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun testSendCancelCommand_NullCallback() {
        val callback = MockCallback()
        mgr!!.setCallback(null)
        mgr!!.executeLPRPrint(
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "hostname"
        )
        TestCase.assertFalse(callback.mCalled)
        mgr!!.sendCancelCommand()

        //wait for response
        while (mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.mCalled)
    }

    fun test_SendCancelCommand_WithoutPrinting() {
        val callback = MockCallback()
        mgr!!.setCallback(callback)
        mgr!!.sendCancelCommand()
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    private class MockCallback : DirectPrintCallback {
        var mCalled = false
        override fun onNotifyProgress(manager: DirectPrintManager?, status: Int, progress: Float) {
            mCalled = true
        }
    }
}