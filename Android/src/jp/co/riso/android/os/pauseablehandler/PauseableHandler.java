/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PauseableHandler.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.os.pauseablehandler;

import java.lang.ref.WeakReference;
import java.util.Vector;

import android.os.Handler;
import android.os.Message;

/**
 * @class PauseableHandler
 * 
 * @brief Message Handler class that supports buffering up of messages when the activity is paused
 * (i.e. in the background)
 * <br>
 * Based on: http://stackoverflow.com/questions/8040280/how-to-handle-handler-messages-when-activity-fragment-is-paused
 */
public class PauseableHandler extends Handler {
    
    /// Message Queue Buffer
    final Vector<Message> mMessageQueueBuffer = new Vector<Message>();
    
    /// Flag indicating the paused state
    private boolean mPaused = false;
    
    /// Callback reference
    final WeakReference<PauseableHandlerCallback> mCallBack;
    
    /**
     * @brief Creates a PausableHander instance
     * @param callback Listener for PauseableHandler events
     */
    public PauseableHandler(PauseableHandlerCallback callback) {
        mCallBack = new WeakReference<PauseableHandlerCallback>(callback);
    }
    
    /**
     * @brief Resumes the handler. Enables processing of messages.
     * @note Stored messages will be executed.
     */
    final public void resume() {
        mPaused = false;
        
        while (mMessageQueueBuffer.size() > 0) {
            final Message msg = mMessageQueueBuffer.elementAt(0);
            mMessageQueueBuffer.removeElementAt(0);
            sendMessage(msg);
        }
    }

    /**
     * @brief Pauses the handler. Messages processed during pause will be stored.
     */
    final public void pause() {
        mPaused = true;
    }
    
    /**
     * @brief Determines whether a message will be stored or discarded if processed while paused 
     * 
     * @param what ID of the processed message.
     * @retval true Message will be stored
     * @retval false Message will be discarded
     */
    final public boolean hasStoredMessage(int what) {
        boolean contains = hasMessages(what);
        
        if (!contains) {
            for (int i = 0; i < mMessageQueueBuffer.size(); i++) {
                if (mMessageQueueBuffer.get(i).what == what) {
                    return true;
                }
            }
        }
        
        return contains;
    }
    
    // ================================================================================
    // INTERFACE - Handler
    // ================================================================================
    
    @Override
    final public void handleMessage(Message msg) {
        if (mCallBack.get() != null) {
            if (mPaused) {
                if (mCallBack.get().storeMessage(msg)) {
                    Message msgCopy = new Message();
                    msgCopy.copyFrom(msg);
                    mMessageQueueBuffer.add(msgCopy);
                }
            } else {
                mCallBack.get().processMessage(msg);
            }
        }
    }
}
