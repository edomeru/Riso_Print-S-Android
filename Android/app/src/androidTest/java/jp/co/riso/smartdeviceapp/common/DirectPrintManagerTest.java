
package jp.co.riso.smartdeviceapp.common;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.DirectPrintManager.DirectPrintCallback;
import jp.co.riso.smartdeviceapp.view.MainActivity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

public class DirectPrintManagerTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private DirectPrintManager mgr;

    public DirectPrintManagerTest() {
        super(MainActivity.class);
    }

    public DirectPrintManagerTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mgr = new DirectPrintManager();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    // ================================================================================
    // Tests - executeLPRPrint
    // ================================================================================
    
    public void testDirectPrint_ValidCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeLPRPrint("printerName", "appName", "appVersion" ,"userName", "jobName", "fileName", "orientation=0", "192.168.1.206", "hostname");
        
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertTrue(callback.mCalled);
    }
    
    public void testDirectPrint_NullParameterValidCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeLPRPrint(null, null, null, null, null, null, null, null, null);
        
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testDirectPrint_EmptyParametersValidCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeLPRPrint("","","","", "", "", "", "", "");
        
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testDirectPrint_NullCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(null);
        mgr.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "orientation=0", "192.168.1.206", "hostname");

        //wait for response
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testDirectPrint_NullParameterNullCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(null);
        mgr.executeLPRPrint(null, null, null, null, null, null, null, null, null);

        // wait for response
        while (mgr.isPrinting()) {
            // wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testDirectPrint_EmptyParametersNullCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(null);
        mgr.executeLPRPrint("", "", "", "", "", "", "", "", "");

        // wait for response
        while (mgr.isPrinting()) {
            // wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }

    public void testDirectPrint_JobIDIncrement() {
        // Set Job ID
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER);
        editor.commit();

        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "orientation=0", "192.168.1.206", "hostname");

        //wait for response
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        int newJobNum = preferences.getInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER);

        assertNotSame(newJobNum, AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER);
    }

    public void testDirectPrint_JobIDMaxNumber() {
        // Set Job ID
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, AppConstants.CONST_MAX_JOB_NUMBER);
        editor.commit();

        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeLPRPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "orientation=0", "192.168.1.206", "hostname");

        //wait for response
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        int newJobNum = preferences.getInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER);
        // Retry increment (Max + 2)
        assertEquals(1, newJobNum);
    }
    
    // ================================================================================
    // Tests - executeRAWPrint
    // ================================================================================
    
    public void testRawPrint_ValidCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeRAWPrint("printerName", "appName", "appVersion","userName", "jobName", "fileName", "orientation=0", "192.168.1.206", "hostname");
        
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertTrue(callback.mCalled);
    }
    
    public void testRawPrint_NullParameterValidCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeRAWPrint(null, null, null, null, null, null, null, null, null);
        
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testRawPrint_EmptyParametersValidCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeRAWPrint("", "", "", "", "", "", "", "", "");
        
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testRawPrint_NullCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(null);
        mgr.executeRAWPrint("printerName", "appName", "appVersion", "userName", "jobName", "fileName", "orientation=0", "192.168.1.206", "hostname");

        //wait for response
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testRawPrint_NullParameterNullCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(null);
        mgr.executeRAWPrint(null, null, null, null,null, null, null, null, null);

        // wait for response
        while (mgr.isPrinting()) {
            // wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testRawPrint_EmptyParametersNullCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(null);
        mgr.executeRAWPrint("", "", "", "", "", "", "", "", "");

        // wait for response
        while (mgr.isPrinting()) {
            // wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    // ================================================================================
    // Tests - sendCancelCommand
    // ================================================================================
    
    public void testSendCancelCommand() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeLPRPrint("printerName", "appName", "appVersion","userName", "jobName", "fileName", "orientation=0", "192.168.1.206", "hostname");

        assertFalse(callback.mCalled);
        mgr.sendCancelCommand();
        
        //wait for response
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void testSendCancelCommand_NullCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(null);
        mgr.executeLPRPrint("printerName", "appName", "appVersion","userName", "jobName", "fileName", "orientation=0", "192.168.1.206", "hostname");

        assertFalse(callback.mCalled);
        mgr.sendCancelCommand();
        
        //wait for response
        while (mgr.isPrinting()) {
            //wait for response
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail(e.toString());
            }
        }

        assertFalse(callback.mCalled);
    }
    
    public void test_SendCancelCommand_WithoutPrinting() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.sendCancelCommand();
    }

    //================================================================================
    // Internal Classes
    //================================================================================

    private class MockCallback implements DirectPrintCallback {
        
        public boolean mCalled = false;
        
        @Override
        public void onNotifyProgress(DirectPrintManager manager, int status, float progress) {
            mCalled = true;
        }
    }

}
