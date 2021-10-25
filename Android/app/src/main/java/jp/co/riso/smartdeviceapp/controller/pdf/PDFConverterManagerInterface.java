/*
 * Copyright (c) 2018 RISO, Inc. All rights reserved.
 *
 * PDFConverterManagerInterface.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;


/**
 * @interface PDFConverterManagerInterface
 *
 * @brief Interface for PDFConverterManager Events
 */
public interface PDFConverterManagerInterface {

    /**
     * @brief Notifies the status of the PDF initialization
     *
     * @param status Status of the PDF Conversion
     * Values:
     * <ul>
     *      <li>PDFFileManager::CONVERSION_OK</li>
     *      <li>PDFFileManager::CONVERSION_FAILED</li>
     * </ul>
     */
    void onFileConverted(int status);

    /**
     * @brief Set message in progress dialog.
     *
     * @param current progress.
     * @param total progress limit.
     * @param isPercentage flag whether to use and compute for percentage
     */
    void onNotifyProgress(int current, int total, boolean isPercentage);

    /**
     * @brief Set message in progress dialog.
     *
     * @param message message.
     */
    void onNotifyProgress(String message);
}
