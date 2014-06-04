/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * JniUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

public class JniUtils {
    public static native String validateIp(String ipAddress, int len);

    static {
        System.loadLibrary("common");
    }
}
