package jp.co.riso.android.os.pauseablehandler

import android.os.Looper
import android.os.Message
import android.test.ActivityInstrumentationTestCase2
import android.util.Log
import jp.co.riso.smartdeviceapp.view.MainActivity
import junit.framework.TestCase

class PauseableHandlerCallbackTest : ActivityInstrumentationTestCase2<MainActivity> {
    private var mCallback: MockCallback? = null
    private var mMessageProcessed = false
    private var mMessageStored = false
    private var mHandler: PauseableHandler? = null

    constructor() : super(MainActivity::class.java) {}
    constructor(activityClass: Class<MainActivity?>?) : super(activityClass) {}

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mCallback = null
        mMessageProcessed = false
        mMessageStored = false
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    fun testConstructor_WithCallback() {
        mCallback = MockCallback()
        instrumentation.runOnMainSync {
            val handler = PauseableHandler(Looper.myLooper(), mCallback)
            TestCase.assertNotNull(handler)
        }
    }

    fun testHasStoredMessage() {
        mCallback = MockCallback()
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), mCallback)
            val msg = Message.obtain(mHandler, MESSAGE_FORSTORING)
            mHandler!!.sendMessage(msg)
            TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        }
    }

    fun testHasStoredMessage_Pause() {
        mCallback = MockCallback()
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), mCallback)
            var msg = Message.obtain(mHandler, MESSAGE_FORSTORING)
            mHandler!!.pause()
            mHandler!!.sendMessage(msg!!)
            TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
            msg = Message.obtain(mHandler, MESSAGE_FORSTORING_2)
            mHandler!!.sendMessage(msg)
            TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING_2))
        }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageStored)
        TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING_2))
        instrumentation.runOnMainSync { mHandler!!.resume() }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageProcessed)
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING_2))
    }

    fun testHandleMessage() {
        mCallback = MockCallback()
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), mCallback)
            val msg = Message.obtain(mHandler, MESSAGE_DONOTSTORE)
            mHandler!!.sendMessage(msg) //will call handleMessage
            TestCase.assertTrue(mHandler!!.hasMessages(MESSAGE_DONOTSTORE))
        }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageProcessed)
        TestCase.assertFalse(mHandler!!.hasMessages(MESSAGE_DONOTSTORE))
    }

    fun testHandleMessage_WithDelay() {
        mCallback = MockCallback()
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), mCallback)
            val msg = Message.obtain(mHandler, MESSAGE_DONOTSTORE)
            mHandler!!.sendMessageDelayed(msg, 1000) //will call handleMessage
            TestCase.assertTrue(mHandler!!.hasMessages(MESSAGE_DONOTSTORE))
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Log.d("PauseableHandlerCallbackTest", e.message!!)
        }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageProcessed)
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE_DONOTSTORE))
        TestCase.assertFalse(mHandler!!.hasMessages(MESSAGE_DONOTSTORE))
    }

    fun testHandleMessage_PauseResume() {
        mCallback = MockCallback()
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), mCallback)
            val msg = Message.obtain(mHandler, MESSAGE_FORSTORING)
            mHandler!!.pause()
            mHandler!!.sendMessage(msg)
            TestCase.assertTrue(mHandler!!.hasMessages(MESSAGE_FORSTORING))
        }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageStored)
        TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        instrumentation.runOnMainSync { mHandler!!.resume() }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageProcessed)
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertFalse(mHandler!!.hasMessages(MESSAGE_FORSTORING))
    }

    fun testHandleMessage_PauseWithDelay() {
        mCallback = MockCallback()
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), mCallback)
            val msg = Message.obtain(mHandler, MESSAGE_FORSTORING)
            mHandler!!.pause()
            mHandler!!.sendMessageDelayed(msg, 1000)
            TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
            TestCase.assertTrue(mHandler!!.hasMessages(MESSAGE_FORSTORING))
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Log.d(TAG, e.message!!)
        }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageStored)
        TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        instrumentation.runOnMainSync { mHandler!!.resume() }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageProcessed)
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertFalse(mHandler!!.hasMessages(MESSAGE_FORSTORING))
    }

    fun testHandleMessage_WithDelayBeforePause() {
        mCallback = MockCallback()
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), mCallback)
            val msg = Message.obtain(mHandler, MESSAGE_FORSTORING)
            mHandler!!.sendMessageDelayed(msg, 1000)
            TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
            TestCase.assertTrue(mHandler!!.hasMessages(MESSAGE_FORSTORING))
            mHandler!!.pause()
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Log.d("PauseableHandlerCallbackTest", e.message!!)
        }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageStored)
        TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        instrumentation.runOnMainSync { mHandler!!.resume() }
        instrumentation.waitForIdleSync()
        TestCase.assertTrue(mMessageProcessed)
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertFalse(mHandler!!.hasMessages(MESSAGE_FORSTORING))
    }

    //================================================================================
    // Internal Class
    //================================================================================
    private inner class MockCallback : PauseableHandlerCallback {
        override fun storeMessage(message: Message?): Boolean {
            mMessageStored = true
            return true
        }

        override fun processMessage(message: Message?) {
            mMessageProcessed = true
        }
    }

    companion object {
        private const val TAG = "PauseableHandlerCallbackTest"
        private const val MESSAGE_FORSTORING = 0x10001
        private const val MESSAGE_DONOTSTORE = 0x10002
        private const val MESSAGE_FORSTORING_2 = 0x10003
    }
}