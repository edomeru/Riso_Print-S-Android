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
            "08:00:27:93:79:5D",
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
        _mgr!!.executeLPRPrint(null, null, null, null, null, null, null, null, null, null)
        _mgr!!.executeLPRPrint("printerName", null, null, null, null, null, null, null, null, null)
        _mgr!!.executeLPRPrint("printerName", "appName", null, null, null, null, null, null, null, null)
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", null, null, null, null, null, null, null)
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", null, null, null, null, null, null)
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", null, null, null, null, null)
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", null, null, null, null)
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", null, null, null)
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", null, null)
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "macAddress", null)

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
        _mgr!!.executeLPRPrint("", "", "", "", "", "", "", "", "", "")
        _mgr!!.executeLPRPrint("printerName", "", "", "", "", "", "", "", "", "")
        _mgr!!.executeLPRPrint("printerName", "appName", "", "", "", "", "", "", "", "")
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "", "", "", "", "", "", "")
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "", "", "", "", "", "")
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "", "", "", "", "")
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "", "", "", "")
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "", "", "")
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "", "")
        _mgr!!.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "macAddress", "")
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
            "08:00:27:93:79:5D",
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
        _mgr!!.executeLPRPrint(null, null, null, null, null, null, null, null, null, null)

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
        _mgr!!.executeLPRPrint("", "", "", "", "", "", "", "", "", "")

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
        val preferences = PreferenceManager.getDefaultSharedPreferences(appContext!!)
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
            "08:00:27:93:79:5D",
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
        val preferences = PreferenceManager.getDefaultSharedPreferences(appContext!!)
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
            "08:00:27:93:79:5D",
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
            "08:00:27:93:79:5D",
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
        _mgr!!.executeRAWPrint(null, null, null, null, null, null, null, null, null, null)
        _mgr!!.executeRAWPrint("printerName", null, null, null, null, null, null, null, null, null)
        _mgr!!.executeRAWPrint("printerName", "appName", null, null, null, null, null, null, null, null)
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", null, null, null, null, null, null, null)
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", null, null, null, null, null, null)
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", null, null, null, null, null)
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", null, null, null, null)
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", null, null, null)
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", null, null)
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "08:00:27:93:79:5D", null)

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
        _mgr!!.executeRAWPrint("", "", "", "", "", "", "", "", "", "")
        _mgr!!.executeRAWPrint("printerName", "", "", "", "", "", "", "", "", "")
        _mgr!!.executeRAWPrint("printerName", "appName", "", "", "", "", "", "", "", "")
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "", "", "", "", "", "", "")
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "", "", "", "", "", "")
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "", "", "", "", "")
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "", "", "", "")
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "", "", "")
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "", "")
        _mgr!!.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "08:00:27:93:79:5D", "")
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
            "08:00:27:93:79:5D",
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
        _mgr!!.executeRAWPrint(null, null, null, null, null, null, null, null, null, null)

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
        _mgr!!.executeRAWPrint("", "", "", "", "", "", "", "", "", "")

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
    // Tests - executeIPPSPrint
    // ================================================================================
    @Test
    fun testIPPSPrint_ValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeIPPSPrint(
            1,
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "08:00:27:93:79:5D",
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
    fun testIPPSPrint_NullParameterValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeIPPSPrint(1,null, null, null, null, null, null, null, null, null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", null, null, null, null, null, null, null, null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", null, null, null, null, null, null, null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", null, null, null, null, null, null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", null, null, null, null, null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", null, null, null, null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "fileName", null, null, null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", null, null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", null, null)
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "08:00:27:93:79:5D", null)

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
    fun testIPPSPrint_EmptyParametersValidCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(callback)
        _mgr!!.executeIPPSPrint(1,"", "", "", "", "", "", "", "", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "", "", "", "", "", "", "", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "", "", "", "", "", "", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "", "", "", "", "", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "", "", "", "", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "", "", "", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "fileName", "", "", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "", "")
        _mgr!!.executeIPPSPrint(1,"printerName", "appName", "appVersion", "userName", "jobName", "fileName", "printSetting", "ipAddress", "08:00:27:93:79:5D", "")
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
    fun testIPPSPrint_NullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeIPPSPrint(
            1,
            "printerName",
            "appName",
            "appVersion",
            "userName",
            "jobName",
            "fileName",
            "orientation=0",
            "192.168.1.206",
            "08:00:27:93:79:5D",
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
    fun testIPPSPrint_NullParameterNullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeIPPSPrint(0,null, null, null, null, null, null, null, null, null, null)

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
    fun testIPPSPrint_EmptyParametersNullCallback() {
        val callback = MockCallback()
        _mgr!!.setCallback(null)
        _mgr!!.executeIPPSPrint(1,"", "", "", "", "", "", "", "", "", "")

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
            "08:00:27:93:79:5D",
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
            "08:00:27:93:79:5D",
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