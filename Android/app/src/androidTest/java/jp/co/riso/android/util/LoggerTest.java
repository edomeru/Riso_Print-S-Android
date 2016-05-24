package jp.co.riso.android.util;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

public class LoggerTest extends ActivityInstrumentationTestCase2<MainActivity> {
    
    public LoggerTest() {
        super(MainActivity.class);
    }
    
    public LoggerTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    //================================================================================
    // Test Logs
    //================================================================================
    
    public void testLogs() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);
        try {
            Logger.logVerbose(LoggerTest.class, "Log Message");
            Logger.logWarn(LoggerTest.class, "Log Message");
            Logger.logInfo(LoggerTest.class, "Log Message");
            Logger.logDebug(LoggerTest.class, "Log Message");
            Logger.logError(LoggerTest.class, "Log Message");
            Logger.logAssert(LoggerTest.class, "Log Message");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testLogs_Off() {
        Logger.initialize(Logger.LOGLEVEL_NONE, false, false);
        try {
            Logger.logVerbose(LoggerTest.class, "Log Message");
            Logger.logWarn(LoggerTest.class, "Log Message");
            Logger.logInfo(LoggerTest.class, "Log Message");
            Logger.logDebug(LoggerTest.class, "Log Message");
            Logger.logError(LoggerTest.class, "Log Message");
            Logger.logAssert(LoggerTest.class, "Log Message");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testLogs_NullClass() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);
        try {
            Logger.logVerbose(null, "Log Message");
            Logger.logWarn(null, "Log Message");
            Logger.logInfo(null, "Log Message");
            Logger.logDebug(null, "Log Message");
            Logger.logError(null, "Log Message");
            Logger.logAssert(null, "Log Message");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testLogs_WithParameters() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);
        try {
            Logger.logVerbose(LoggerTest.class, "Log Message %d", 123);
            Logger.logWarn(LoggerTest.class, "Log Message %d", 123);
            Logger.logInfo(LoggerTest.class, "Log Message %d", 123);
            Logger.logDebug(LoggerTest.class, "Log Message %d", 123);
            Logger.logError(LoggerTest.class, "Log Message %d", 123);
            Logger.logAssert(LoggerTest.class, "Log Message %d", 123);
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testLogs_WithInvalidParameters() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);
        try {
            Logger.logVerbose(LoggerTest.class, "Log Message %d", "asd");
            Logger.logWarn(LoggerTest.class, "Log Message %d", "asd");
            Logger.logInfo(LoggerTest.class, "Log Message %d", "asd");
            Logger.logDebug(LoggerTest.class, "Log Message %d", "asd");
            Logger.logError(LoggerTest.class, "Log Message %d", "asd");
            Logger.logAssert(LoggerTest.class, "Log Message %d", "asd");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testLogs_WithParamInvalidFormat() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);
        try {
            Logger.logVerbose(LoggerTest.class, "Log Message", "asd");
            Logger.logWarn(LoggerTest.class, "Log Message", "asd");
            Logger.logInfo(LoggerTest.class, "Log Message", "asd");
            Logger.logDebug(LoggerTest.class, "Log Message", "asd");
            Logger.logError(LoggerTest.class, "Log Message", "asd");
            Logger.logAssert(LoggerTest.class, "Log Message", "asd");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testLogs_WithIncompleteFormat() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);
        try {
            Logger.logVerbose(LoggerTest.class, "Log Message %d");
            Logger.logWarn(LoggerTest.class, "Log Message %d");
            Logger.logInfo(LoggerTest.class, "Log Message %d");
            Logger.logDebug(LoggerTest.class, "Log Message %d");
            Logger.logError(LoggerTest.class, "Log Message %d");
            Logger.logAssert(LoggerTest.class, "Log Message %d");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testLogs_WithMoreParam() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);
        try {
            Logger.logVerbose(LoggerTest.class, "Log Message %d", 123, "asd");
            Logger.logWarn(LoggerTest.class, "Log Message %d", 123, "asd");
            Logger.logInfo(LoggerTest.class, "Log Message %d", 123, "asd");
            Logger.logDebug(LoggerTest.class, "Log Message %d", 123, "asd");
            Logger.logError(LoggerTest.class, "Log Message %d", 123, "asd");
            Logger.logAssert(LoggerTest.class, "Log Message %d", 123, "asd");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testLogs_NullFormat() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);
        try {
            Logger.logVerbose(LoggerTest.class, null, 123, "asd");
            Logger.logWarn(LoggerTest.class, null, 123, "asd");
            Logger.logInfo(LoggerTest.class, null, 123, "asd");
            Logger.logDebug(LoggerTest.class, null, 123, "asd");
            Logger.logError(LoggerTest.class, null, 123, "asd");
            Logger.logAssert(LoggerTest.class, null, 123, "asd");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    //================================================================================
    // Test Get Log
    //================================================================================
    
    //================================================================================
    // Test Start Stop Time
    //================================================================================
    
    public void testStartStopTime() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, false);

        try {
            Logger.logStartTime(getActivity(), LoggerTest.class, "PROCESS1");
            Thread.sleep(500);
            Logger.logStopTime(getActivity(), LoggerTest.class, "PROCESS1");
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testStartStopTime_Off() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, false, false);

        try {
            Logger.logStartTime(getActivity(), LoggerTest.class, "PROCESS1");
            Thread.sleep(500);
            Logger.logStopTime(getActivity(), LoggerTest.class, "PROCESS1");
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testStartStopTime_invalidContext() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, false);

        try {
            Logger.logStartTime(null, LoggerTest.class, "PROCESS1");
            Thread.sleep(500);
            Logger.logStopTime(null, LoggerTest.class, "PROCESS1");
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testStartStopTime_InvalidClass() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, false);

        try {
            Logger.logStartTime(getActivity(), null, "PROCESS1");
            Thread.sleep(500);
            Logger.logStopTime(getActivity(), null, "PROCESS1");
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testStartStopTime_InvalidProcess() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, false);

        try {
            Logger.logStartTime(getActivity(), LoggerTest.class, null);
            Thread.sleep(500);
            Logger.logStopTime(getActivity(), LoggerTest.class, null);
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testStartStopTime_EmptyProcess() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, false);

        try {
            Logger.logStartTime(getActivity(), LoggerTest.class, "");
            Thread.sleep(500);
            
            Logger.logStopTime(getActivity(), LoggerTest.class, "");
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testStartStopTime_StartTwice() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, true);

        try {
            Logger.logStartTime(getActivity(), LoggerTest.class, "PROCESS1");
            Thread.sleep(500);
            Logger.logStartTime(getActivity(), LoggerTest.class, "PROCESS1");
            Thread.sleep(500);
            Logger.logStopTime(getActivity(), LoggerTest.class, "PROCESS1");
            
            assertTrue(Logger.getLogString(getActivity()) != null);
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testStartStopTime_StopWithoutStart() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, true);

        try {
            Logger.logStopTime(getActivity(), LoggerTest.class, "PROCESS1");
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    public void testStartStopTime_WriteToFile() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, true);

        try {
            Logger.logStartTime(getActivity(), LoggerTest.class, "PROCESS1");
            Thread.sleep(1000);
            Logger.logStopTime(getActivity(), LoggerTest.class, "PROCESS1");
            
            assertTrue(Logger.getLogString(getActivity()) != null);
        } catch (InterruptedException e) {
            
        } catch (Exception e) {
            fail("Should not throw any exception");
        }
    }
    
    @UiThreadTest
    public void testGetLogString() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, true);
        Logger.logStartTime(getActivity(), LoggerTest.class, "PROCESS1");
        assertTrue(Logger.getLogString(getActivity()) != null);
    }

    @UiThreadTest
    public void testGetLogString_NullContext() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, true);
        Logger.logStartTime(getActivity(), LoggerTest.class, "PROCESS1");
        assertTrue(Logger.getLogString(null) == null);
    }

    @UiThreadTest
    public void testWriteToFile() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, true);
        Logger.writeToFile(getActivity(), "Hello");
        assertTrue(Logger.getLogString(getActivity()) != null);
    }

    @UiThreadTest
    public void testWriteToFile_NullContext() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, true);
        Logger.writeToFile(null, "Hello");
        assertTrue(Logger.getLogString(getActivity()) == null);
    }

    @UiThreadTest
    public void testWriteToFile_NullMessage() {
        Logger.initialize(Logger.LOGLEVEL_VERBOSE, true, true);
        Logger.writeToFile(getActivity(), null);
        assertTrue(Logger.getLogString(getActivity()) != null);
    }
    
    public void testDeleteTask() {
        Logger.runDeleteTask(getActivity());
    }
    
    public void testDeleteTask_NullContext() {
        Logger.runDeleteTask(null);
    }
}
