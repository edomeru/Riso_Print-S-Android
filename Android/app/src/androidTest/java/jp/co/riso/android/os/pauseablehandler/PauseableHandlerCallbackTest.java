
package jp.co.riso.android.os.pauseablehandler;

import jp.co.riso.smartdeviceapp.view.MainActivity;

import android.os.Looper;
import android.os.Message;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

public class PauseableHandlerCallbackTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private static final String TAG = "PauseableHandlerCallbackTest";
    private static final int MESSAGE_FORSTORING = 0x10001;
    private static final int MESSAGE_DONOTSTORE = 0x10002;
    private static final int MESSAGE_FORSTORING_2 = 0x10003;

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
        mCallback = null;
        mMessageProcessed = false;
        mMessageStored = false;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConstructor_WithCallback() {
        mCallback = new MockCallback();
        PauseableHandler handler = new PauseableHandler(Looper.myLooper(), mCallback);
        assertNotNull(handler);
    }

    public void testHasStoredMessage() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync((Runnable) () -> {
            mHandler = new PauseableHandler(Looper.myLooper(), mCallback);
            Message msg = Message.obtain(mHandler, MESSAGE_FORSTORING);

            mHandler.sendMessage(msg);
            assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
        });
    }

    public void testHasStoredMessage_Pause() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync((Runnable) () -> {
            mHandler = new PauseableHandler(Looper.myLooper(), mCallback);
            Message msg = Message.obtain(mHandler, MESSAGE_FORSTORING);
            mHandler.pause();
            mHandler.sendMessage(msg);
            assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

            msg = Message.obtain(mHandler, MESSAGE_FORSTORING_2);
            mHandler.sendMessage(msg);
            assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING_2));
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageStored);
        assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
        assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING_2));

        getInstrumentation().runOnMainSync((Runnable) () -> mHandler.resume());
        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
        assertFalse(mHandler.hasStoredMessage(MESSAGE_FORSTORING_2));
    }

    public void testHandleMessage() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync((Runnable) () -> {
            mHandler = new PauseableHandler(Looper.myLooper(), mCallback);
            Message msg = Message.obtain(mHandler, MESSAGE_DONOTSTORE);
            mHandler.sendMessage(msg); //will call handleMessage
            assertTrue(mHandler.hasMessages(MESSAGE_DONOTSTORE));
        });
        getInstrumentation().waitForIdleSync();

        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasMessages(MESSAGE_DONOTSTORE));
    }

    public void testHandleMessage_WithDelay() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync((Runnable) () -> {
            mHandler = new PauseableHandler(Looper.myLooper(), mCallback);
            Message msg = Message.obtain(mHandler, MESSAGE_DONOTSTORE);
            mHandler.sendMessageDelayed(msg, 1000); //will call handleMessage
            assertTrue(mHandler.hasMessages(MESSAGE_DONOTSTORE));
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d("PauseableHandlerCallbackTest", e.getMessage());
        }

        getInstrumentation().waitForIdleSync();

        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(MESSAGE_DONOTSTORE));
        assertFalse(mHandler.hasMessages(MESSAGE_DONOTSTORE));
    }

    public void testHandleMessage_PauseResume() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync((Runnable) () -> {
            mHandler = new PauseableHandler(Looper.myLooper(), mCallback);

            Message msg = Message.obtain(mHandler, MESSAGE_FORSTORING);
            mHandler.pause();
            mHandler.sendMessage(msg);
            assertTrue(mHandler.hasMessages(MESSAGE_FORSTORING));
        });

        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageStored);
        assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

        getInstrumentation().runOnMainSync((Runnable) () -> mHandler.resume());
        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
        assertFalse(mHandler.hasMessages(MESSAGE_FORSTORING));
    }

    public void testHandleMessage_PauseWithDelay() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync((Runnable) () -> {
            mHandler = new PauseableHandler(Looper.myLooper(), mCallback);

            Message msg = Message.obtain(mHandler, MESSAGE_FORSTORING);
            mHandler.pause();
            mHandler.sendMessageDelayed(msg, 1000);
            assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
            assertTrue(mHandler.hasMessages(MESSAGE_FORSTORING));
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d(TAG, e.getMessage());
        }

        getInstrumentation().waitForIdleSync();

        assertTrue(mMessageStored);
        assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

        getInstrumentation().runOnMainSync((Runnable) () -> mHandler.resume());
        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

        assertFalse(mHandler.hasMessages(MESSAGE_FORSTORING));

    }

    public void testHandleMessage_WithDelayBeforePause() {
        mCallback = new MockCallback();
        getInstrumentation().runOnMainSync((Runnable) () -> {
            mHandler = new PauseableHandler(Looper.myLooper(), mCallback);

            Message msg = Message.obtain(mHandler, MESSAGE_FORSTORING);
            mHandler.sendMessageDelayed(msg, 1000);
            assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
            assertTrue(mHandler.hasMessages(MESSAGE_FORSTORING));
            mHandler.pause();
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Log.d("PauseableHandlerCallbackTest", e.getMessage());
        }

        getInstrumentation().waitForIdleSync();

        assertTrue(mMessageStored);
        assertTrue(mHandler.hasStoredMessage(MESSAGE_FORSTORING));

        getInstrumentation().runOnMainSync((Runnable) () -> mHandler.resume());
        getInstrumentation().waitForIdleSync();
        assertTrue(mMessageProcessed);
        assertFalse(mHandler.hasStoredMessage(MESSAGE_FORSTORING));
        assertFalse(mHandler.hasMessages(MESSAGE_FORSTORING));
    }

    //================================================================================
    // Internal Class
    //================================================================================

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
