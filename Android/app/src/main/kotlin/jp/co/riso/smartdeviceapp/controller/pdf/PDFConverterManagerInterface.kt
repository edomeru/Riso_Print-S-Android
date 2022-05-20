/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PDFConverterManagerInterface.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.pdf

/**
 * @interface PDFConverterManagerInterface
 *
 * @brief Interface for PDFConverterManager Events
 */
interface PDFConverterManagerInterface {
    /**
     * @brief Notifies the status of the PDF initialization
     *
     * @param status Status of the PDF Conversion
     * Values:
     *
     *  * PDFFileManager::CONVERSION_OK
     *  * PDFFileManager::CONVERSION_FAILED
     *
     */
    fun onFileConverted(status: Int)

    /**
     * @brief Set message in progress dialog.
     *
     * @param current progress.
     * @param total progress limit.
     * @param isPercentage flag whether to use and compute for percentage
     */
    fun onNotifyProgress(current: Int, total: Int, isPercentage: Boolean)

    /**
     * @brief Set message in progress dialog.
     *
     * @param message message.
     */
    fun onNotifyProgress(message: String?)
}