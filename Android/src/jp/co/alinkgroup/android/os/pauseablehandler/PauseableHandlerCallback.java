/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PauseableHandlerCallback.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.alinkgroup.android.os.pauseablehandler;

import android.os.Message;

public interface PauseableHandlerCallback {
    
    /**
     * Notification that the message is about to be stored as the activity is paused. If not handled the message will be
     * saved and replayed when the activity resumes.
     * 
     * @param message
     *            the message which optional can be handled
     * @return true if the message is to be stored
     */
    abstract boolean storeMessage(Message message);
    
    /**
     * Notification message to be processed. This will either be directly from handleMessage or played back from a saved
     * message when the activity was paused.
     * 
     * @param message
     *            the message to be handled
     */
    abstract void processMessage(Message message);
}
