/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PauseableHandlerCallback.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.os.pauseablehandler

import android.os.Message

/**
 * @interface PauseableHandlerCallback
 *
 * @brief Interface for PauseableHandler Events
 */
interface PauseableHandlerCallback {
    /**
     * @brief Notification that the message is about to be stored as the activity is paused. If not handled the message will be
     * saved and replayed when the activity resumes.
     *
     * @param message The message which optional can be handled
     *
     * @retval true The message is to be stored
     * @retval false The message will be discarded
     */
    fun storeMessage(message: Message?): Boolean

    /**
     * @brief Notification message to be processed. This will either be directly from handleMessage or played back from a saved
     * message when the activity was paused.
     *
     * @param message The message to be handled
     */
    fun processMessage(message: Message?)
}