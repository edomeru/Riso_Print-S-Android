/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * JniUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

import jp.co.riso.android.util.NetUtils;

/**
 * @class JniUtils
 * 
 * @brief Utility class using JNI to validate IP Address. 
 */
public class JniUtils {
    //https://code.google.com/p/android/issues/detail?id=181918
    @SuppressWarnings("JniMissingFunction")
    public static native String validateIp(String ipAddress);
    
    /**
     * @brief Validate IP Address
     * 
     * @param ipAddress IP Address
     * 
     * @return Validated IP Address
     * @retval null ipAddress is an invalid IP Address
     */
    public static String validateIpAddress(String ipAddress) {
        ipAddress = NetUtils.validateIpAddress(ipAddress);
        return validateIp(ipAddress);
    }
    
    static {
        System.loadLibrary("common");
    }
}