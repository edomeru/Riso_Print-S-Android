
package jp.co.riso.smartdeviceapp.common;

import jp.co.riso.smartdeviceapp.common.DirectPrintManager.DirectPrintCallback;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.test.ActivityInstrumentationTestCase2;


public class DirectPrintManagerTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private DirectPrintManager mgr;
    private boolean mCallbackCalled;

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
        mCallbackCalled = false;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetCallback() {
        mgr.setCallback(new MockCallback());
        mgr.initializeDirectPrint("jobName", "fileName", "orientation=0", "192.168.1.123");
        mgr.lprPrint();

        //wait for response
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }

        checkCallbackCalled();
        mgr.finalizeDirectPrint();
    }

    public void testSendCancelCommand() {
        mgr.setCallback(new MockCallback());
        mgr.initializeDirectPrint("jobName", "fileName", "orientation=0", "192.168.1.123");
        mgr.lprPrint();

        //wait for response
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }

        checkCallbackCalled();
        mgr.sendCancelCommand();
        //wait for response
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail(e.toString());
        }
        getInstrumentation().waitForIdleSync();
        mgr.finalizeDirectPrint();

        assertTrue(true); //nothing to test
        assertFalse(mCallbackCalled);
    }

    private void checkCallbackCalled() {
        assertTrue(mCallbackCalled);
        mCallbackCalled = false;
    }

    private class MockCallback implements DirectPrintCallback {
        @Override
        public void onNotifyProgress(DirectPrintManager manager, int status, float progress) {
            mCallbackCalled = true;
        }
    }

}
