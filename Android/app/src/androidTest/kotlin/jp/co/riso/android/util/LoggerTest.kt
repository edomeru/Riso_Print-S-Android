package jp.co.riso.android.util

import jp.co.riso.android.util.Logger.getLogString
import jp.co.riso.android.util.Logger.initialize
import jp.co.riso.android.util.Logger.logAssert
import jp.co.riso.android.util.Logger.logDebug
import jp.co.riso.android.util.Logger.logError
import jp.co.riso.android.util.Logger.logInfo
import jp.co.riso.android.util.Logger.logStartTime
import jp.co.riso.android.util.Logger.logStopTime
import jp.co.riso.android.util.Logger.logVerbose
import jp.co.riso.android.util.Logger.logWarn
import jp.co.riso.android.util.Logger.runDeleteTask
import jp.co.riso.android.util.Logger.writeToFile
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import junit.framework.TestCase
import org.junit.Test

class LoggerTest : BaseActivityTestUtil() {

    //================================================================================
    // Test Logs
    //================================================================================
    @Test
    fun testLogs() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(LoggerTest::class.java, "Log Message")
            logWarn(LoggerTest::class.java, "Log Message")
            logInfo(LoggerTest::class.java, "Log Message")
            logDebug(LoggerTest::class.java, "Log Message")
            logError(LoggerTest::class.java, "Log Message")
            logAssert(LoggerTest::class.java, "Log Message")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testLogs_Off() {
        initialize(Logger.LOGLEVEL_NONE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(LoggerTest::class.java, "Log Message")
            logWarn(LoggerTest::class.java, "Log Message")
            logInfo(LoggerTest::class.java, "Log Message")
            logDebug(LoggerTest::class.java, "Log Message")
            logError(LoggerTest::class.java, "Log Message")
            logAssert(LoggerTest::class.java, "Log Message")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testLogs_NullClass() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(null, "Log Message")
            logWarn(null, "Log Message")
            logInfo(null, "Log Message")
            logDebug(null, "Log Message")
            logError(null, "Log Message")
            logAssert(null, "Log Message")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testLogs_WithParameters() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(LoggerTest::class.java, "Log Message %d", 123)
            logWarn(LoggerTest::class.java, "Log Message %d", 123)
            logInfo(LoggerTest::class.java, "Log Message %d", 123)
            logDebug(LoggerTest::class.java, "Log Message %d", 123)
            logError(LoggerTest::class.java, "Log Message %d", 123)
            logAssert(LoggerTest::class.java, "Log Message %d", 123)
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testLogs_WithInvalidParameters() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(LoggerTest::class.java, "Log Message %d", "asd")
            logWarn(LoggerTest::class.java, "Log Message %d", "asd")
            logInfo(LoggerTest::class.java, "Log Message %d", "asd")
            logDebug(LoggerTest::class.java, "Log Message %d", "asd")
            logError(LoggerTest::class.java, "Log Message %d", "asd")
            logAssert(LoggerTest::class.java, "Log Message %d", "asd")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testLogs_WithParamInvalidFormat() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(LoggerTest::class.java, "Log Message", "asd")
            logWarn(LoggerTest::class.java, "Log Message", "asd")
            logInfo(LoggerTest::class.java, "Log Message", "asd")
            logDebug(LoggerTest::class.java, "Log Message", "asd")
            logError(LoggerTest::class.java, "Log Message", "asd")
            logAssert(LoggerTest::class.java, "Log Message", "asd")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testLogs_WithIncompleteFormat() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(LoggerTest::class.java, "Log Message %d")
            logWarn(LoggerTest::class.java, "Log Message %d")
            logInfo(LoggerTest::class.java, "Log Message %d")
            logDebug(LoggerTest::class.java, "Log Message %d")
            logError(LoggerTest::class.java, "Log Message %d")
            logAssert(LoggerTest::class.java, "Log Message %d")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testLogs_WithMoreParam() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(LoggerTest::class.java, "Log Message %d", 123, "asd")
            logWarn(LoggerTest::class.java, "Log Message %d", 123, "asd")
            logInfo(LoggerTest::class.java, "Log Message %d", 123, "asd")
            logDebug(LoggerTest::class.java, "Log Message %d", 123, "asd")
            logError(LoggerTest::class.java, "Log Message %d", 123, "asd")
            logAssert(LoggerTest::class.java, "Log Message %d", 123, "asd")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testLogs_NullFormat() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logVerbose(LoggerTest::class.java, null, 123, "asd")
            logWarn(LoggerTest::class.java, null, 123, "asd")
            logInfo(LoggerTest::class.java, null, 123, "asd")
            logDebug(LoggerTest::class.java, null, 123, "asd")
            logError(LoggerTest::class.java, null, 123, "asd")
            logAssert(LoggerTest::class.java, null, 123, "asd")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    //================================================================================
    // Test Get Log
    //================================================================================
    //================================================================================
    // Test Start Stop Time
    //================================================================================
    @Test
    fun testStartStopTime() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = false)
        try {
            logStartTime(mainActivity, LoggerTest::class.java, "PROCESS1")
            Thread.sleep(500)
            logStopTime(mainActivity, LoggerTest::class.java, "PROCESS1")
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testStartStopTime_Off() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = false, perfLogsToFile = false)
        try {
            logStartTime(mainActivity, LoggerTest::class.java, "PROCESS1")
            Thread.sleep(500)
            logStopTime(mainActivity, LoggerTest::class.java, "PROCESS1")
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testStartStopTime_invalidContext() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = false)
        try {
            logStartTime(null, LoggerTest::class.java, "PROCESS1")
            Thread.sleep(500)
            logStopTime(null, LoggerTest::class.java, "PROCESS1")
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testStartStopTime_InvalidClass() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = false)
        try {
            logStartTime(mainActivity, null, "PROCESS1")
            Thread.sleep(500)
            logStopTime(mainActivity, null, "PROCESS1")
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testStartStopTime_InvalidProcess() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = false)
        try {
            logStartTime(mainActivity, LoggerTest::class.java, null)
            Thread.sleep(500)
            logStopTime(mainActivity, LoggerTest::class.java, null)
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testStartStopTime_EmptyProcess() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = false)
        try {
            logStartTime(mainActivity, LoggerTest::class.java, "")
            Thread.sleep(500)
            logStopTime(mainActivity, LoggerTest::class.java, "")
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testStartStopTime_StartTwice() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = true)
        try {
            logStartTime(mainActivity, LoggerTest::class.java, "PROCESS1")
            Thread.sleep(500)
            logStartTime(mainActivity, LoggerTest::class.java, "PROCESS1")
            Thread.sleep(500)
            logStopTime(mainActivity, LoggerTest::class.java, "PROCESS1")
            TestCase.assertTrue(getLogString(mainActivity) != null)
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testStartStopTime_StopWithoutStart() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = true)
        try {
            logStopTime(mainActivity, LoggerTest::class.java, "PROCESS1")
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testStartStopTime_WriteToFile() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = true)
        try {
            logStartTime(mainActivity, LoggerTest::class.java, "PROCESS1")
            Thread.sleep(1000)
            logStopTime(mainActivity, LoggerTest::class.java, "PROCESS1")
            TestCase.assertTrue(getLogString(mainActivity) != null)
        } catch (e: InterruptedException) {
        } catch (e: Exception) {
            TestCase.fail("Should not throw any exception")
        }
    }

    @Test
    fun testGetLogString() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = true)
        logStartTime(mainActivity, LoggerTest::class.java, "PROCESS1")
        TestCase.assertTrue(getLogString(mainActivity) != null)
    }

    @Test
    fun testGetLogString_NullContext() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = true)
        logStartTime(mainActivity, LoggerTest::class.java, "PROCESS1")
        TestCase.assertTrue(getLogString(null) == null)
    }

    @Test
    fun testWriteToFile() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = true)
        writeToFile(mainActivity, "Hello")
        TestCase.assertTrue(getLogString(mainActivity) != null)
    }

    @Test
    fun testWriteToFile_NullContext() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = true)
        writeToFile(null, "Hello")
        TestCase.assertTrue(getLogString(mainActivity) == null)
    }

    @Test
    fun testWriteToFile_NullMessage() {
        initialize(Logger.LOGLEVEL_VERBOSE, perfLogs = true, perfLogsToFile = true)
        writeToFile(mainActivity, null)
        TestCase.assertTrue(getLogString(mainActivity) != null)
    }

    @Test
    fun testDeleteTask() {
        runDeleteTask(mainActivity)
    }

    @Test
    fun testDeleteTask_NullContext() {
        runDeleteTask(null)
    }
}