package jp.co.riso.android.os.pauseablehandler

import android.os.Looper
import android.os.Message
import android.test.ActivityInstrumentationTestCase2
import jp.co.riso.smartdeviceapp.view.MainActivity
import junit.framework.TestCase

class PauseableHandlerTest : ActivityInstrumentationTestCase2<MainActivity> {
    private var mHandler: PauseableHandler? = null

    constructor() : super(MainActivity::class.java) {}
    constructor(activityClass: Class<MainActivity?>?) : super(activityClass) {}

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    fun testConstructor_NullCallback() {
        instrumentation.runOnMainSync {
            val handler = PauseableHandler(Looper.myLooper(), null)
            TestCase.assertNotNull(handler)
        }
    }

    fun testHandleMessage() {
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), null)
            val msg = Message.obtain(mHandler, MESSAGE)
            mHandler!!.sendMessage(msg) //will call handleMessage
            TestCase.assertTrue(mHandler!!.hasMessages(MESSAGE))
        }
        instrumentation.waitForIdleSync()
        TestCase.assertFalse(mHandler!!.hasMessages(MESSAGE))
    }

    fun testHasStoredMessage() {
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), null)
            val msg = Message.obtain(mHandler, MESSAGE)
            mHandler!!.sendMessage(msg) //will call handleMessage
            TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE))
        }
        instrumentation.waitForIdleSync()
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE))
    }

    fun testHandleMessage_PauseResume() {
        instrumentation.runOnMainSync {
            mHandler = PauseableHandler(Looper.myLooper(), null)
            val msg = Message.obtain(mHandler, MESSAGE)
            mHandler!!.pause()
            mHandler!!.sendMessage(msg) //will call handleMessage
            TestCase.assertTrue(mHandler!!.hasStoredMessage(MESSAGE))
            TestCase.assertTrue(mHandler!!.hasMessages(MESSAGE))
        }
        instrumentation.waitForIdleSync()
        // message is already processed
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE))
        TestCase.assertFalse(mHandler!!.hasMessages(MESSAGE))
        instrumentation.runOnMainSync { mHandler!!.resume() }
        instrumentation.waitForIdleSync()

        // no effect since message is already processed
        TestCase.assertFalse(mHandler!!.hasStoredMessage(MESSAGE))
        TestCase.assertFalse(mHandler!!.hasMessages(MESSAGE))
    }

    companion object {
        private const val MESSAGE = 0x10001
    }
}