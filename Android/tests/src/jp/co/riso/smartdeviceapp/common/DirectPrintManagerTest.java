
package jp.co.riso.smartdeviceapp.common;

import jp.co.riso.smartdeviceapp.common.DirectPrintManager.DirectPrintCallback;
import jp.co.riso.smartdeviceapp.view.MainActivity;
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
    
    public void testDirectPrint_ValidCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeLPRPrint("jobName", "fileName", "orientation=0", "192.168.1.206");
        
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
    
    public void testDirectPrint_NullCallback() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(null);
        mgr.executeLPRPrint("jobName", "fileName", "orientation=0", "192.168.1.206");

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
    
    public void testSendCancelCommand() {
        MockCallback callback = new MockCallback();
        mgr.setCallback(callback);
        mgr.executeLPRPrint("jobName", "fileName", "orientation=0", "192.168.1.206");

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
        mgr.executeLPRPrint("jobName", "fileName", "orientation=0", "192.168.1.206");

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
