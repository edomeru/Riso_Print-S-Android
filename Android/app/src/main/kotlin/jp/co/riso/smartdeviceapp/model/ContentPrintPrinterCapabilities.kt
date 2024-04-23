package jp.co.riso.smartdeviceapp.model

import com.google.gson.annotations.SerializedName

/**
 * @class ContentPrintPrinterCapabilities
 *
 * @brief The entity that holds printer capabilities from Content Distribution Service cloud.
 */
class ContentPrintPrinterCapabilities {
    @SerializedName("paperType_lightweight")
    var paperTypeLightweight: Boolean = false

    @SerializedName("inputTray_standard")
    var inputTrayStandard: Boolean = false

    @SerializedName("inputTray_tray1")
    var inputTrayTray1: Boolean = false

    @SerializedName("inputTray_tray2")
    var inputTrayTray2: Boolean = false

    @SerializedName("inputTray_tray3")
    var inputTrayTray3: Boolean = false

    @SerializedName("inputTray_external")
    var inputTrayExternal: Boolean = false

    @SerializedName("booklet")
    var booklet: Boolean = false

    @SerializedName("finisher_0holes")
    var finisher0Holes: Boolean = false

    @SerializedName("finisher_23holes")
    var finisher23Holes: Boolean = false

    @SerializedName("finisher_24holes")
    var finisher24Holes: Boolean = false

    @SerializedName("staple")
    var staple: Boolean = false

    @SerializedName("outputTray_face_down")
    var outputTrayFacedown: Boolean = false

    @SerializedName("outputTray_top")
    var outputTrayTop: Boolean = false

    @SerializedName("outputTray_stacking")
    var outputTrayStacking: Boolean = false
}