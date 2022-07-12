/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PauseableHandler.kt
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
    private val _messageQueueBuffer = Vector<Message>()

    /// Flag indicating the paused state
    private var _paused = false

    /// Callback reference
    private val _callBack: WeakReference<PauseableHandlerCallback?> = WeakReference(callback)

    /**
     * @brief Resumes the handler. Enables processing of messages.
     * @note Stored messages will be executed.
     */
    fun resume() {
        _paused = false
        while (_messageQueueBuffer.size > 0) {
            val msg = _messageQueueBuffer.elementAt(0)
            _messageQueueBuffer.removeElementAt(0)
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
            for (i in _messageQueueBuffer.indices) {
                if (_messageQueueBuffer[i].what == what) {
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
        if (_callBack.get() != null) {
            if (_paused) {
                if (_callBack.get()!!.storeMessage(msg)) {
                    val msgCopy = Message()
                    msgCopy.copyFrom(msg)
                    _messageQueueBuffer.add(msgCopy)
                }
            } else {
                _callBack.get()!!.processMessage(msg)
            }
        }
    }

}