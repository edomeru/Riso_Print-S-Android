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
    val mMessageQueueBuffer = Vector<Message>()

    /// Flag indicating the paused state
    private var mPaused = false

    /// Callback reference
    val mCallBack: WeakReference<PauseableHandlerCallback?>

    /**
     * @brief Resumes the handler. Enables processing of messages.
     * @note Stored messages will be executed.
     */
    fun resume() {
        mPaused = false
        while (mMessageQueueBuffer.size > 0) {
            val msg = mMessageQueueBuffer.elementAt(0)
            mMessageQueueBuffer.removeElementAt(0)
            sendMessage(msg)
        }
    }

    /**
     * @brief Pauses the handler. Messages processed during pause will be stored.
     */
    fun pause() {
        mPaused = true
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
            for (i in mMessageQueueBuffer.indices) {
                if (mMessageQueueBuffer[i].what == what) {
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
        if (mCallBack.get() != null) {
            if (mPaused) {
                if (mCallBack.get()!!.storeMessage(msg)) {
                    val msgCopy = Message()
                    msgCopy.copyFrom(msg)
                    mMessageQueueBuffer.add(msgCopy)
                }
            } else {
                mCallBack.get()!!.processMessage(msg)
            }
        }
    }

    /**
     * @brief Creates a PauseableHandler instance
     * @param callback Listener for PauseableHandler events
     */
    init {
        mCallBack = WeakReference(callback)
    }
}