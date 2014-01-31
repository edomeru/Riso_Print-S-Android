/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PauseableHandler.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.alinkgroup.android.os.pauseablehandler;

import java.lang.ref.WeakReference;
import java.util.Vector;

import android.os.Handler;
import android.os.Message;

/**
 * Message Handler class that supports buffering up of messages when the activity is paused i.e. in the background.
 * http://stackoverflow.com/questions/8040280/how-to-handle-handler-messages-when-activity-fragment-is-paused
 */
public class PauseableHandler extends Handler {
    
    /**
     * Message Queue Buffer
     */
    final Vector<Message> mMessageQueueBuffer = new Vector<Message>();
    
    /**
     * Flag indicating the pause state
     */
    private boolean mPaused = false;
    
    final WeakReference<PauseableHandlerCallback> mCallBack;
    
    public PauseableHandler(PauseableHandlerCallback callback) {
        mCallBack = new WeakReference<PauseableHandlerCallback>(callback);
    }
    
    /**
     * Resume the handler
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
     * Pause the handler
     */
    final public void pause() {
        mPaused = true;
    }
    
    /** {@inheritDoc} */
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
