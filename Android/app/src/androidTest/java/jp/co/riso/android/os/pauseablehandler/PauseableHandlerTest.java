
package jp.co.riso.android.os.pauseablehandler;

import jp.co.riso.smartdeviceapp.view.MainActivity;

import android.os.Looper;
import android.os.Message;
import android.test.ActivityInstrumentationTestCase2;

public class PauseableHandlerTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final int MESSAGE = 0x10001;

    private PauseableHandler mHandler ;

    public PauseableHandlerTest() {
        super(MainActivity.class);
    }

    public PauseableHandlerTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor_NullCallback() {
        PauseableHandler handler = new PauseableHandler(Looper.myLooper(), null);
        assertNotNull(handler);
    }

    public void testHandleMessage() {
        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(Looper.myLooper(), null);

                Message msg = Message.obtain(mHandler, MESSAGE);
                mHandler.sendMessage(msg); //will call handleMessage
                assertTrue(mHandler.hasMessages(MESSAGE));
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(mHandler.hasMessages(MESSAGE));
    }

    public void testHasStoredMessage() {
        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(Looper.myLooper(), null);

                Message msg = Message.obtain(mHandler, MESSAGE);
                mHandler.sendMessage(msg); //will call handleMessage
                assertTrue(mHandler.hasStoredMessage(MESSAGE));
            }
        });
        getInstrumentation().waitForIdleSync();
        assertFalse(mHandler.hasStoredMessage(MESSAGE));
    }

    public void testHandleMessage_PauseResume() {

        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(Looper.myLooper(), null);

                Message msg = Message.obtain(mHandler, MESSAGE);
                mHandler.pause();
                mHandler.sendMessage(msg); //will call handleMessage
                assertTrue(mHandler.hasStoredMessage(MESSAGE));

                assertTrue(mHandler.hasMessages(MESSAGE));
            }
        });

        getInstrumentation().waitForIdleSync();
        // message is already processed
        assertFalse(mHandler.hasStoredMessage(MESSAGE));
        assertFalse(mHandler.hasMessages(MESSAGE));

        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler.resume();
            }
        });
        getInstrumentation().waitForIdleSync();

        // no effect since message is already processed
        assertFalse(mHandler.hasStoredMessage(MESSAGE));
        assertFalse(mHandler.hasMessages(MESSAGE));
    }
}
