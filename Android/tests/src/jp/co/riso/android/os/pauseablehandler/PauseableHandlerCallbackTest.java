
package jp.co.riso.android.os.pauseablehandler;

import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.os.Message;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class PauseableHandlerCallbackTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String TAG = "PauseableHandlerCallbackTest";
    private static final int MESSAGE_FORSTORING = 0x10001;
    private static final int MESSAGE_DONOTSTORE = 0x10002;

    private MockCallback mCallback;
    private boolean mMessageProcessed = false;
    private boolean mMessageStored = false;
    private PauseableHandler mHandler ;

    public PauseableHandlerCallbackTest() {
        super(MainActivity.class);
    }

    public PauseableHandlerCallbackTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mMessageProcessed = false;
        mMessageStored = false;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor_NullCallback() {
        PauseableHandler handler = new PauseableHandler(null);
        assertNotNull(handler);
    }

    public void testHasStoredMessage() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(mCallback);
                Message msg = Message.obtain(mHandler,5);

                mHandler.sendMessage(msg);
                assertTrue(mHandler.hasStoredMessage(5));
                assertTrue(mHandler.hasMessages(5));
            }
        });
    }

    public void testSendMessageCallback() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(mCallback);
                Message msg = Message.obtain(mHandler, MESSAGE_DONOTSTORE);
                mHandler.sendMessage(msg);
                assertTrue(mHandler.hasMessages(MESSAGE_DONOTSTORE));
            }
        });
        getInstrumentation().waitForIdleSync();

        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasMessages(MESSAGE_DONOTSTORE));
    }

    public void testSendMessageCallbackDelayed() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(mCallback);
                Message msg = Message.obtain(mHandler, MESSAGE_DONOTSTORE);
                mHandler.sendMessageDelayed(msg, 1000);
                assertTrue(mHandler.hasMessages(MESSAGE_DONOTSTORE));
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d("PauseableHandlerCallbackTest", e.getMessage());
        }

        getInstrumentation().waitForIdleSync();

        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(5));
        assertFalse(mHandler.hasMessages(5));
    }

    public void testSendMessageCallbackPause() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(mCallback);

                Message msg = Message.obtain(mHandler, MESSAGE_FORSTORING);
                mHandler.pause();
                mHandler.sendMessage(msg);
                assertTrue(mHandler.hasMessages(MESSAGE_FORSTORING));
            }
        });

        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageStored);
        assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler.resume();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
        assertFalse(mHandler.hasMessages(MESSAGE_FORSTORING));
    }

    public void testSendMessageCallbackPauseDelayed() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(mCallback);

                Message msg = Message.obtain(mHandler, MESSAGE_FORSTORING);
                mHandler.pause();
                mHandler.sendMessageDelayed(msg, 1000);
                assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
                assertTrue(mHandler.hasMessages(MESSAGE_FORSTORING));
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        }

        getInstrumentation().waitForIdleSync();

        assertTrue(mMessageStored);
        assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler.resume();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

        assertFalse(mHandler.hasMessages(MESSAGE_FORSTORING));

    }

    public void testSendMessageCallbackDelayedBeforePause() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler = new PauseableHandler(mCallback);

                Message msg = Message.obtain(mHandler, MESSAGE_FORSTORING);
                mHandler.sendMessageDelayed(msg, 1000);
                assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
                assertTrue(mHandler.hasMessages(MESSAGE_FORSTORING));
                mHandler.pause();
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d("PauseableHandlerCallbackTest", e.getMessage());
        }

        getInstrumentation().waitForIdleSync();

        assertTrue(mMessageStored);
        assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

        getInstrumentation().runOnMainSync(new Runnable(){
            @Override
            public void run(){
                mHandler.resume();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
        assertFalse(mHandler.hasMessages(MESSAGE_FORSTORING));
    }

    private class MockCallback implements PauseableHandlerCallback {
        @Override
        public boolean storeMessage(Message message) {
            mMessageStored = true;
            return true;
        }

        @Override
        public void processMessage(Message message) {
            mMessageProcessed = true;
        }
    }
}
