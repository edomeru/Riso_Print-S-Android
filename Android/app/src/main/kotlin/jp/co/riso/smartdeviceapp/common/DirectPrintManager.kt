/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * DirectPrintManager.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.common

import androidx.preference.PreferenceManager
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import java.lang.ref.WeakReference

/**
 * @class DirectPrintManager
 *
 * @brief Helper class for printing PDF files with PJL settings.
 */
class DirectPrintManager {
    private val mJob: Long = 0
    private var _callbackRef: WeakReference<DirectPrintCallback?>? = null

    /**
     * @brief Initializes Direct Print.
     *
     * @param userName To be sent as OwnerName in the PJL command.
     * @param jobName Name of the Print Job.
     * @param fileName File name of the PDF.
     * @param printSetting Formatted string of the print settings.
     * @param ipAddress IP address of the printer.
     */
    // Ver.2.0.4.2 Start
    //private native void initializeDirectPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress);
    //private native void initializeDirectPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress, String hostName);
    // Ver.2.0.4.2 End
    // Ver.2.2.0.0 Start
    private external fun initializeDirectPrint(
        printerName: String,
        appName: String,
        appVersion: String,
        userName: String,
        jobName: String,
        fileName: String,
        printSetting: String,
        ipAddress: String,
        hostName: String,
        jobNumber: Int
    )
    // Ver.2.2.0.0 End
    /**
     * @brief Finalizes Direct Print.
     */
    private external fun finalizeDirectPrint()

    /**
     * @brief Executes an LPR Print.
     */
    private external fun lprPrint()

    /**
     * @brief Executes a RAW Print.
     */
    private external fun rawPrint()

    /**
     * @brief Cancels Direct Print.
     */
    private external fun cancel()

    /**
     * @brief Sets the callback for the DirectPrint Manager.
     *
     * @param callback Callback function
     */
    fun setCallback(callback: DirectPrintCallback?) {
        _callbackRef = WeakReference(callback)
    }

    /**
     * @brief Executes an LPR Print.
     *
     * @param userName To be sent as OwnerName in the PJL command.
     * @param jobName Name of the Print Job.
     * @param fileName File name of the PDF.
     * @param printSetting Formatted string of the print settings.
     * @param ipAddress IP address of the printer.
     * @param hostName The name of the industrial design.
     *
     * @retval true Print execution is started
     * @retval false Print not executed
     */
    // Ver.2..0.4.2 Start
    /*
    public boolean executeLPRPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress) {
        if (printerName == null || appName == null || appVersion == null || userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null || jobName.isEmpty()
                || printerName.isEmpty() || appName.isEmpty() || appVersion.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty()) {
            return false;
        }
      initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress);
    */
    fun executeLPRPrint(
        printerName: String?,
        appName: String?,
        appVersion: String?,
        userName: String?,
        jobName: String?,
        fileName: String?,
        printSetting: String?,
        ipAddress: String?,
        hostName: String?
    ): Boolean {
        if (printerName == null || appName == null || appVersion == null || userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null || hostName == null || jobName.isEmpty()
            || printerName.isEmpty() || appName.isEmpty() || appVersion.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty() || hostName.isEmpty()
        ) {
            return false
        }
        //initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress, hostName);
        // Ver.2.0.4.2 End
        // Ver.2.2.0.0 Start

        // LPR Cancel Fix: Set a unique job number for print job (0-999)
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext)
        val jobNumber = preferences.getInt(
            AppConstants.PREF_KEY_JOB_NUMBER_COUNTER,
            AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER
        )
        updateJobNumber()
        initializeDirectPrint(
            printerName,
            appName,
            appVersion,
            userName,
            jobName,
            fileName,
            printSetting,
            ipAddress,
            hostName,
            jobNumber
        )
        // Ver.2.2.0.0 End
        if (isPrinting) {
            lprPrint()
            return true
        }
        return false
    }

    /**
     * @brief Executes a RAW Print.
     *
     * @param userName To be sent as OwnerName in the PJL command.
     * @param jobName Name of the Print Job.
     * @param fileName File name of the PDF.
     * @param printSetting Formatted string of the print settings.
     * @param ipAddress IP address of the printer.
     * @param hostName The name of the industrial design.
     *
     * @retval true Print execution is started
     * @retval false Print not executed
     */
    // Ver.2.0.4.2 Start
    /*
    public boolean executeRAWPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress) {
        if (printerName == null || appName == null || appVersion == null || userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null
                || printerName.isEmpty() || appName.isEmpty() || appVersion.isEmpty() || jobName.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty()) {
            return false;
        }

        initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress);
    */
    fun executeRAWPrint(
        printerName: String?,
        appName: String?,
        appVersion: String?,
        userName: String?,
        jobName: String?,
        fileName: String?,
        printSetting: String?,
        ipAddress: String?,
        hostName: String?
    ): Boolean {
        if (printerName == null || appName == null || appVersion == null || userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null || hostName == null || printerName.isEmpty() || appName.isEmpty() || appVersion.isEmpty() || jobName.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty() || hostName.isEmpty()) {
            return false
        }

        // initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress, hostName);
        // Ver.2.0.4.2 End
        // Ver.2.2.0.0 Start
        // Set job number to default (1)
        initializeDirectPrint(
            printerName,
            appName,
            appVersion,
            userName,
            jobName,
            fileName,
            printSetting,
            ipAddress,
            hostName,
            1
        )
        // Ver.2.2.0.0 End
        if (isPrinting) {
            rawPrint()
            return true
        }
        return false
    }

    /**
     * @brief Checks if print is ongoing.
     *
     * @retval true Print is ongoing
     * @retval false No ongoing print job
     */
    val isPrinting: Boolean
        get() = mJob != 0L

    /**
     * @brief Sends cancel command.
     *
     * Cancel Print task.
     */
    fun sendCancelCommand() {
        setCallback(null)
        DirectPrintCancelTask(this).execute()
    }

    companion object {
        const val PRINT_STATUS_ERROR_CONNECTING = -4 ///< Error while connecting to printer
        const val PRINT_STATUS_ERROR_SENDING = -3 ///< Error while sending file
        const val PRINT_STATUS_ERROR_FILE = -2 ///< Error while opening file
        const val PRINT_STATUS_ERROR = -1 ///< Error while starting print
        const val PRINT_STATUS_STARTED = 0 ///< Print has started
        const val PRINT_STATUS_CONNECTING = 1 ///< Connecting to the printer
        const val PRINT_STATUS_CONNECTED = 2 ///< Connected to the printer
        const val PRINT_STATUS_SENDING = 3 ///< Sending file to the printer
        const val PRINT_STATUS_SENT = 4 ///< File is successfully sent to the printer
        const val PRINT_STATUS_JOB_NUM_UPDATE = 5 ///< Update job number - LPR print retry

        init {
            System.loadLibrary("common")
        }
    }

    /**
     * @brief Notify progress. Called when printing status and progress is updated.
     *
     * @param status Printing status
     * @param progress Printing progress percentage
     */
    private fun onNotifyProgress(status: Int, progress: Float) {
        when (status) {
            PRINT_STATUS_ERROR_CONNECTING, PRINT_STATUS_ERROR_SENDING, PRINT_STATUS_ERROR_FILE, PRINT_STATUS_ERROR, PRINT_STATUS_SENT -> finalizeDirectPrint()
            PRINT_STATUS_JOB_NUM_UPDATE -> updateJobNumber()
        }
        if (_callbackRef != null && _callbackRef!!.get() != null) {
            _callbackRef!!.get()!!.onNotifyProgress(this, status, progress)
        }
    }

    /**
     * @brief Notify progress. Called when printing status and progress is updated.
     */
    private fun updateJobNumber() {
        val preferences =
            PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.appContext)
        val editor = preferences.edit()
        val jobNumber = preferences.getInt(
            AppConstants.PREF_KEY_JOB_NUMBER_COUNTER,
            AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER
        ) // current job number
        val nextJobNumber =
            (jobNumber + 1) % (AppConstants.CONST_MAX_JOB_NUMBER + 1) // increment job number (0-999)
        editor.putInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, nextJobNumber)
        editor.apply()
    }
    // ================================================================================
    // Internal classes
    // ================================================================================
    /**
     * @interface DirectPrintCallback
     *
     * @brief Interface for DirectPrintCallback Events
     */
    interface DirectPrintCallback {
        /**
         * @brief Notify progress callback. Called when printing status and progress is updated.
         *
         * @param manager DirectPrint Manager
         * @param status Print status
         * @param progress Printing progress percentage
         */
        fun onNotifyProgress(manager: DirectPrintManager?, status: Int, progress: Float)
    }

    /**
     * @class DirectPrintCancelTask
     *
     * @brief Async Task for Canceling Direct Print
     * @brief Creates DirectPrintCancelTask instance.
     *
     * @param _manager DirectPrint Manager
     */
    inner class DirectPrintCancelTask (private val _manager: DirectPrintManager) : BaseTask<Void?, Void?>() {
        override fun doInBackground(vararg params: Void?): Void? {
            _manager.cancel()
            _manager.finalizeDirectPrint()
            return null
        }

    }
}