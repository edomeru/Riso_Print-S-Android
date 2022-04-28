/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * JniUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.common

import jp.co.riso.android.util.NetUtils
import jp.co.riso.smartdeviceapp.common.JniUtils

/**
 * @class JniUtils
 *
 * @brief Utility class using JNI to validate IP Address.
 */
object JniUtils {
    //https://code.google.com/p/android/issues/detail?id=181918
    @JvmStatic
    external fun validateIp(ipAddress: String?): String

    /**
     * @brief Validate IP Address
     *
     * @param ipAddress IP Address
     *
     * @return Validated IP Address
     * @retval null ipAddress is an invalid IP Address
     */
    @JvmStatic
    fun validateIpAddress(ipAddress: String?): String {
        var ipAddress = ipAddress
        ipAddress = NetUtils.validateIpAddress(ipAddress)
        return validateIp(ipAddress)
    }

    init {
        System.loadLibrary("common")
    }
}