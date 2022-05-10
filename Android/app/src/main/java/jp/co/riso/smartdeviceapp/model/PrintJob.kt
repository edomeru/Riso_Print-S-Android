/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintJob.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model

import java.util.*

/**
 * @class PrintJob
 *
 * @brief Class representing the Print Job data in Print Job History.
 */
class PrintJob {
    /**
     * @brief Retrieves the ID of the Print Job
     *
     * @return Print job ID
     */
    /**
     * @brief Sets print job ID
     *
     * @param mId Print job ID
     */
    var id = 0
    /**
     * @brief Retrieves the Printer ID of the Print Job
     *
     * @return Printer ID of the Print Job
     */
    /**
     * @brief Sets printer ID
     *
     * @param mPrinterId Printer ID of the Print Job
     */
    var printerId: Int
    /**
     * @brief Retrieves the name of the Print Job
     *
     * @return Print job name
     */
    /**
     * @brief Sets print job name
     *
     * @param mName Print job name
     */
    var name: String? = null
    /**
     * @brief Retrieves the Print Job date of execution
     *
     * @return Print job date
     */
    /**
     * @brief Set print job date of execution
     *
     * @param mDate Print job date
     */
    var date: Date? = null
    /**
     * @brief Retrieves the Print Job status (JobResult)
     *
     * @retval SUCCESSFUL Printing is successful
     * @retval ERROR Printing has failed
     */
    /**
     * @brief Sets Print Job status (JobResult)
     *
     * @param mResult Print job result
     */
    var result: JobResult? = null

    /**
     * @brief Creates a PrintJob instance
     *
     * @param mId Print job ID
     * @param mPrinterId Print job's printer ID
     * @param mName Print job name
     * @param mDate Print job's date of execution
     * @param mResult Print job result
     */
    constructor(mId: Int, mPrinterId: Int, mName: String, mDate: Date, mResult: JobResult) {
        id = mId
        printerId = mPrinterId
        name = mName
        date = mDate
        result = mResult
    }

    /**
     * @brief Creates a PrintJob instance
     *
     * @param mPrinterId Print job's printer ID
     * @param mName Print job name
     * @param mDate Print job's date of execution
     * @param mResult Print job result
     */
    constructor(mPrinterId: Int, mName: String?, mDate: Date?, mResult: JobResult?) {
        printerId = mPrinterId
        name = mName
        date = mDate
        result = mResult
    }

    /**
     * @brief Printing result status
     */
    enum class JobResult {
        /// Printing is successful
        SUCCESSFUL,  /// Printing has failed
        ERROR
    }
}