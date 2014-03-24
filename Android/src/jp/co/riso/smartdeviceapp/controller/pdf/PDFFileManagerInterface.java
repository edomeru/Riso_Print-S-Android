/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PDFFileManagerInterface.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.pdf;

public interface PDFFileManagerInterface {

    /**
     * Returns the status of the PDF initialization
     */
    public void onFileInitialized(int status);
}
