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

// http://stackoverflow.com/questions/8040280/how-to-handle-handler-messages-when-activity-fragment-is-paused
/**
 * Message Handler class that supports buffering up of messages when the activity is paused i.e. in the background.
 */
public class PauseableHandler extends Handler {
    /**
     * Message Queue Buffer
     */
    final Vector<Message> messageQueueBuffer = new Vector<Message>();
    
    final WeakReference<PauseableHandlerCallback> mCallBack;
    
    public PauseableHandler(PauseableHandlerCallback callback) {
        mCallBack = new WeakReference<PauseableHandlerCallback>(callback);
    }
    
    /**
     * Flag indicating the pause state
     */
    private boolean paused;
    
    /**
     * Resume the handler
     */
    final public void resume() {
        paused = false;
        
        while (messageQueueBuffer.size() > 0) {
            final Message msg = messageQueueBuffer.elementAt(0);
            messageQueueBuffer.removeElementAt(0);
            sendMessage(msg);
        }
    }
    
    /**
     * Pause the handler
     */
    final public void pause() {
        paused = true;
    }
    
    /** {@inheritDoc} */
    @Override
    final public void handleMessage(Message msg) {
        if (mCallBack.get() != null) {
            if (paused) {
                if (mCallBack.get().storeMessage(msg)) {
                    Message msgCopy = new Message();
                    msgCopy.copyFrom(msg);
                    messageQueueBuffer.add(msgCopy);
                }
            } else {
                mCallBack.get().processMessage(msg);
            }
        }
    }
}
