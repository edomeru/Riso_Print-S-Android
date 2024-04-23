/*
 * Copyright (c) 2024 RISO, Inc. All rights reserved.
 *
 * ContentPrintFile.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.model

import com.google.gson.annotations.SerializedName

/**
 * @class ContentPrintFile
 *
 * @brief The entity that holds file information from Content Distribution Service cloud.
 */
class ContentPrintFile(
    @SerializedName("file_id") var fileId: Int,
    @SerializedName("filename") var filename: String?,
    @SerializedName("print_settings") var printSettings: ContentPrintPrintSettings?
) {
    var thumbnailImagePath: String? = null
}