/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PauseableHandlerCallback.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.os.pauseablehandler;

import android.os.Message;

/**
 * @interface PauseableHandlerCallback
 * 
 * @brief Interface for PauseableHandler Events
 */
public interface PauseableHandlerCallback {
    
    /**
     * @brief Notification that the message is about to be stored as the activity is paused. If not handled the message will be
     * saved and replayed when the activity resumes.
     * 
     * @param message The message which optional can be handled
     * 
     * @retval true The message is to be stored
     * @retval false The message will be discarded
     */
    boolean storeMessage(Message message);
    
    /**
     * @brief Notification message to be processed. This will either be directly from handleMessage or played back from a saved
     * message when the activity was paused.
     * 
     * @param message The message to be handled
     */
    void processMessage(Message message);
}
