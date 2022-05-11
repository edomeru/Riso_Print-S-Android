package jp.co.riso.smartdeviceapp.common

import androidx.preference.PreferenceManager
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp.Companion.appContext
import jp.co.riso.smartdeviceapp.common.DirectPrintManager.DirectPrintCallback
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test

class DirectPrintManagerTest {
    private var _mgr: DirectPrintManager? = null
    
    @Before
    fun setUp() {
        _mgr = DirectPrintManager()
    }
    
    // ================================================================================
    // Tests - executeLPRPrint
    // ================================================================================
    @Test
    fun testDirectPrint_ValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeLPRPrint(
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
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertTrue(callback.called)
    }

    @Test
    fun testDirectPrint_NullParameterValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeLPRPrint(null, null, null, null, null, null, null, null, null)
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testDirectPrint_EmptyParametersValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeLPRPrint("", "", "", "", "", "", "", "", "")
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testDirectPrint_NullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeLPRPrint(
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
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testDirectPrint_NullParameterNullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeLPRPrint(null, null, null, null, null, null, null, null, null)

        // wait for response
        while (_mgr!!.isPrinting) {
            // wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testDirectPrint_EmptyParametersNullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeLPRPrint("", "", "", "", "", "", "", "", "")

        // wait for response
        while (_mgr!!.isPrinting) {
            // wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
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
        _mgr!!.setCallback(callback)
        _mgr!!.executeLPRPrint(
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
        while (_mgr!!.isPrinting) {
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

    @Test
    fun testDirectPrint_JobIDMaxNumber() {
        // Set Job ID
        val preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val editor = preferences.edit()
        editor.putInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, AppConstants.CONST_MAX_JOB_NUMBER)
        editor.commit()
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeLPRPrint(
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
        while (_mgr!!.isPrinting) {
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
    @Test
    fun testRawPrint_ValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeRAWPrint(
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
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertTrue(callback.called)
    }

    @Test
    fun testRawPrint_NullParameterValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeRAWPrint(null, null, null, null, null, null, null, null, null)
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testRawPrint_EmptyParametersValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeRAWPrint("", "", "", "", "", "", "", "", "")
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testRawPrint_NullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeRAWPrint(
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
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testRawPrint_NullParameterNullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeRAWPrint(null, null, null, null, null, null, null, null, null)

        // wait for response
        while (_mgr!!.isPrinting) {
            // wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testRawPrint_EmptyParametersNullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeRAWPrint("", "", "", "", "", "", "", "", "")

        // wait for response
        while (_mgr!!.isPrinting) {
            // wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    // ================================================================================
    // Tests - sendCancelCommand
    // ================================================================================
    @Test
    fun testSendCancelCommand() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeLPRPrint(
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
        TestCase.assertFalse(callback.called)
        _mgr!!.sendCancelCommand()

        //wait for response
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun testSendCancelCommand_NullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeLPRPrint(
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
        TestCase.assertFalse(callback.called)
        _mgr!!.sendCancelCommand()

        //wait for response
        while (_mgr!!.isPrinting) {
            //wait for response
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                TestCase.fail(e.toString())
            }
        }
        TestCase.assertFalse(callback.called)
    }

    @Test
    fun test_SendCancelCommand_WithoutPrinting() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.sendCancelCommand()
    }

    //================================================================================
    // Internal Classes
    //================================================================================
    private class MockCallback : DirectPrintCallback {
        var called = false
        override fun onNotifyProgress(manager: DirectPrintManager?, status: Int, progress: Float) {
            called = true
        }
    }
}