/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PauseableHandler.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.os.pauseablehandler

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference
import java.util.*

/**
 * @class PauseableHandler
 *
 * @brief Message Handler class that supports buffering up of messages when the activity is paused
 * (i.e. in the background)
 * <br></br>
 * Based on: http://stackoverflow.com/questions/8040280/how-to-handle-handler-messages-when-activity-fragment-is-paused
 */
class PauseableHandler(looper: Looper?, callback: PauseableHandlerCallback?) : Handler(
    looper!!, null
) {
    /// Message Queue Buffer
    val messageQueueBuffer = Vector<Message>()

    /// Flag indicating the paused state
    private var _paused = false

    /// Callback reference
    val callBack: WeakReference<PauseableHandlerCallback?> = WeakReference(callback)

    /**
     * @brief Resumes the handler. Enables processing of messages.
     * @note Stored messages will be executed.
     */
    fun resume() {
        _paused = false
        while (messageQueueBuffer.size > 0) {
            val msg = messageQueueBuffer.elementAt(0)
            messageQueueBuffer.removeElementAt(0)
            sendMessage(msg)
        }
    }

    /**
     * @brief Pauses the handler. Messages processed during pause will be stored.
     */
    fun pause() {
        _paused = true
    }

    /**
     * @brief Determines whether a message will be stored or discarded if processed while paused
     *
     * @param what ID of the processed message.
     * @retval true Message will be stored
     * @retval false Message will be discarded
     */
    fun hasStoredMessage(what: Int): Boolean {
        val contains = hasMessages(what)
        if (!contains) {
            for (i in messageQueueBuffer.indices) {
                if (messageQueueBuffer[i].what == what) {
                    return true
                }
            }
        }
        return contains
    }

    // ================================================================================
    // INTERFACE - Handler
    // ================================================================================
    override fun handleMessage(msg: Message) {
        if (callBack.get() != null) {
            if (_paused) {
                if (callBack.get()!!.storeMessage(msg)) {
                    val msgCopy = Message()
                    msgCopy.copyFrom(msg)
                    messageQueueBuffer.add(msgCopy)
                }
            } else {
                callBack.get()!!.processMessage(msg)
            }
        }
    }

}