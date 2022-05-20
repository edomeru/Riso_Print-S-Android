/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PDFFileManagerInterface.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.pdf

/**
 * @interface PDFFileManagerInterface
 *
 * @brief Interface for PDFFileManager Events
 */
interface PDFFileManagerInterface {
    /**
     * @brief Notifies the status of the PDF initialization
     *
     * @param status Status of the PDF initialization
     * Values:
     *
     *  * PDFFileManager::PDF_OK
     *  * PDFFileManager::PDF_ENCRYPTED
     *  * PDFFileManager::PDF_PRINT_RESTRICTED
     *  * PDFFileManager::PDF_OPEN_FAILED
     *
     */
    fun onFileInitialized(status: Int)
}