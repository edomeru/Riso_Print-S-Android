package jp.co.riso.android.os.pauseablehandler

import android.os.Looper
import android.os.Message
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.intent.IntentStubberRegistry
import junit.framework.TestCase
import org.junit.Test

class PauseableHandlerTest {
    private var _handler: PauseableHandler? = null

    @Test
    fun testConstructor_NullCallback() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val handler = PauseableHandler(Looper.myLooper(), null)
            TestCase.assertNotNull(handler)
        }
    }

    @Test
    fun testHandleMessage() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), null)
            val msg = Message.obtain(_handler, MESSAGE)
            _handler!!.sendMessage(msg) //will call handleMessage
            TestCase.assertTrue(_handler!!.hasMessages(MESSAGE))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertFalse(_handler!!.hasMessages(MESSAGE))
    }

    @Test
    fun testHasStoredMessage() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), null)
            val msg = Message.obtain(_handler, MESSAGE)
            _handler!!.sendMessage(msg) //will call handleMessage
            TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE))
    }

    @Test
    fun testHasNoStoredMessage() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), null)
            val msg = Message.obtain(_handler, 0)
            _handler!!.sendMessage(msg)
            TestCase.assertTrue(_handler!!.hasStoredMessage(0))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertFalse(_handler!!.hasStoredMessage(0))
    }

    @Test
    fun testHandleMessage_PauseResume() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), null)
            val msg = Message.obtain(_handler, MESSAGE)
            _handler!!.pause()
            _handler!!.sendMessage(msg) //will call handleMessage
            TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE))
            TestCase.assertTrue(_handler!!.hasMessages(MESSAGE))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        // message is already processed
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE))
        TestCase.assertFalse(_handler!!.hasMessages(MESSAGE))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { _handler!!.resume() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()

        // no effect since message is already processed
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE))
        TestCase.assertFalse(_handler!!.hasMessages(MESSAGE))
    }

    companion object {
        private const val MESSAGE = 0x10001
    }
}