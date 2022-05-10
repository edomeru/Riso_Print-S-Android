/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * JniUtils.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.common

import jp.co.riso.android.util.NetUtils

/**
 * @class JniUtils
 *
 * @brief Utility class using JNI to validate IP Address.
 */
object JniUtils {
    //https://code.google.com/p/android/issues/detail?id=181918
    @JvmStatic
    external fun validateIp(ipAddress: String?): String?

    /**
     * @brief Validate IP Address
     *
     * @param ipAddress IP Address
     *
     * @return Validated IP Address
     * @retval null ipAddress is an invalid IP Address
     */
    @JvmStatic
    fun validateIpAddress(ipAddress: String?): String? {
        var ip = ipAddress
        ip = NetUtils.validateIpAddress(ip)
        return validateIp(ip)
    }

    init {
        System.loadLibrary("common")
    }
}