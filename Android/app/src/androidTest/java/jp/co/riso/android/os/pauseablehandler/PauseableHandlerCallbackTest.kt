package jp.co.riso.android.os.pauseablehandler

import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.TestCase
import org.junit.Test

class PauseableHandlerCallbackTest {
    private var _callback: MockCallback? = null
    private var _messageProcessed = false
    private var _messageStored = false
    private var _handler: PauseableHandler? = null

    @Test
    fun testConstructor_WithCallback() {
        _callback = MockCallback()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val handler = PauseableHandler(Looper.myLooper(), _callback)
            TestCase.assertNotNull(handler)
        }
    }

    @Test
    fun testHasStoredMessage() {
        _callback = MockCallback()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), _callback)
            val msg = Message.obtain(_handler, MESSAGE_FORSTORING)
            _handler!!.sendMessage(msg)
            TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        }
    }

    @Test
    fun testHasStoredMessage_Pause() {
        _callback = MockCallback()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), _callback)
            var msg = Message.obtain(_handler, MESSAGE_FORSTORING)
            _handler!!.pause()
            _handler!!.sendMessage(msg!!)
            TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
            msg = Message.obtain(_handler, MESSAGE_FORSTORING_2)
            _handler!!.sendMessage(msg)
            TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING_2))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageStored)
        TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING_2))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { _handler!!.resume() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageProcessed)
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE_FORSTORING_2))
    }

    @Test
    fun testHandleMessage() {
        _callback = MockCallback()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), _callback)
            val msg = Message.obtain(_handler, MESSAGE_DONOTSTORE)
            _handler!!.sendMessage(msg) //will call handleMessage
            TestCase.assertTrue(_handler!!.hasMessages(MESSAGE_DONOTSTORE))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageProcessed)
        TestCase.assertFalse(_handler!!.hasMessages(MESSAGE_DONOTSTORE))
    }

    @Test
    fun testHandleMessage_WithDelay() {
        _callback = MockCallback()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), _callback)
            val msg = Message.obtain(_handler, MESSAGE_DONOTSTORE)
            _handler!!.sendMessageDelayed(msg, 1000) //will call handleMessage
            TestCase.assertTrue(_handler!!.hasMessages(MESSAGE_DONOTSTORE))
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Log.d("PauseableHandlerCallbackTest", e.message!!)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageProcessed)
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE_DONOTSTORE))
        TestCase.assertFalse(_handler!!.hasMessages(MESSAGE_DONOTSTORE))
    }

    @Test
    fun testHandleMessage_PauseResume() {
        _callback = MockCallback()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), _callback)
            val msg = Message.obtain(_handler, MESSAGE_FORSTORING)
            _handler!!.pause()
            _handler!!.sendMessage(msg)
            TestCase.assertTrue(_handler!!.hasMessages(MESSAGE_FORSTORING))
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageStored)
        TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { _handler!!.resume() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageProcessed)
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertFalse(_handler!!.hasMessages(MESSAGE_FORSTORING))
    }

    @Test
    fun testHandleMessage_PauseWithDelay() {
        _callback = MockCallback()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), _callback)
            val msg = Message.obtain(_handler, MESSAGE_FORSTORING)
            _handler!!.pause()
            _handler!!.sendMessageDelayed(msg, 1000)
            TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
            TestCase.assertTrue(_handler!!.hasMessages(MESSAGE_FORSTORING))
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Log.d(TAG, e.message!!)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageStored)
        TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { _handler!!.resume() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageProcessed)
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertFalse(_handler!!.hasMessages(MESSAGE_FORSTORING))
    }

    @Test
    fun testHandleMessage_WithDelayBeforePause() {
        _callback = MockCallback()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            _handler = PauseableHandler(Looper.myLooper(), _callback)
            val msg = Message.obtain(_handler, MESSAGE_FORSTORING)
            _handler!!.sendMessageDelayed(msg, 1000)
            TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
            TestCase.assertTrue(_handler!!.hasMessages(MESSAGE_FORSTORING))
            _handler!!.pause()
        }
        try {
            Thread.sleep(1000)
        } catch (e: InterruptedException) {
            Log.d("PauseableHandlerCallbackTest", e.message!!)
        }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageStored)
        TestCase.assertTrue(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        InstrumentationRegistry.getInstrumentation().runOnMainSync { _handler!!.resume() }
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        TestCase.assertTrue(_messageProcessed)
        TestCase.assertFalse(_handler!!.hasStoredMessage(MESSAGE_FORSTORING))
        TestCase.assertFalse(_handler!!.hasMessages(MESSAGE_FORSTORING))
    }

    //================================================================================
    // Internal Class
    //================================================================================
    private inner class MockCallback : PauseableHandlerCallback {
        override fun storeMessage(message: Message?): Boolean {
            _messageStored = true
            return true
        }

        override fun processMessage(message: Message?) {
            _messageProcessed = true
        }
    }

    companion object {
        private const val TAG = "PauseableHandlerCallbackTest"
        private const val MESSAGE_FORSTORING = 0x10001
        private const val MESSAGE_DONOTSTORE = 0x10002
        private const val MESSAGE_FORSTORING_2 = 0x10003
    }
}