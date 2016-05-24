/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PDFFileManagerInterface.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

/**
 * @interface PDFFileManagerInterface
 * 
 * @brief Interface for PDFFileManager Events
 */
public interface PDFFileManagerInterface {

    /**
     * @brief Notifies the status of the PDF initialization
     * 
     * @param status Status of the PDF initialization
     * Values:
     * <ul>
     *      <li>PDFFileManager::PDF_OK</li>
     *      <li>PDFFileManager::PDF_ENCRYPTED</li>
     *      <li>PDFFileManager::PDF_PRINT_RESTRICTED</li>
     *      <li>PDFFileManager::PDF_OPEN_FAILED</li>
     * </ul>
     */
    public void onFileInitialized(int status);
}
