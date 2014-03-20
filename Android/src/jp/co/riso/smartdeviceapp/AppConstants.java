/*
 * Copyright (c) 2014 All rights reserved.
 *
 * AppConstants.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp;

public class AppConstants {
    public static final long APP_SPLASH_DURATION = 3000;
    public static final String APP_FONT_FILE = "fonts/Raleway/Raleway-Regular.ttf";
    
    // Part of Memory to be allocated to Print Preview
    // Bit shift operator
    // Size = TOTAL MEMORY >> APP_BMP_CACHE_PART
    public static final int APP_BMP_CACHE_PART = 4;
}
