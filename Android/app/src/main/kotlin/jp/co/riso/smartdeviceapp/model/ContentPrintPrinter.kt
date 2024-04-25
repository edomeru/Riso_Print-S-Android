/*
 * Copyright (c) 2024 RISO, Inc. All rights reserved.
 *
 * ContentPrintBoxPrinter.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model

import com.google.gson.annotations.SerializedName
import jp.co.riso.smartdeviceapp.AppConstants

/**
 * @class ContentPrintPrinter
 *
 * @brief The entity that holds printer information from Content Distribution Service cloud.
 */
class ContentPrintPrinter {
    @SerializedName("serial_no")
    var serialNo: String? = null

    @SerializedName("printer_name")
    var printerName: String? = null

    @SerializedName("model")
    var model: String? = null

    @SerializedName("printer_capabilities")
    var printerCapabilities: ContentPrintPrinterCapabilities? = null

    /**
     * @brief Determines if the printer is of the FT or CEREZONA S series
     *
     * @return True if the printers if of the FT or CEREZONA S series, false otherwise
     */
    val isPrinterFTorCEREZONA_S: Boolean
        get() = model?.contains(AppConstants.PRINTER_MODEL_FT) == true // Classify CEREZONA S as FT model

    /**
     * @brief Determines if the printer is of the GL or OGA series
     *
     * @return True if the printers if of the GL or OGA series, false otherwise
     */
    val isPrinterGLorOGA: Boolean
        get() = model?.contains(AppConstants.PRINTER_MODEL_GL) == true // Classify OGA as GL model
}