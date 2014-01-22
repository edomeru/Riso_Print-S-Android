/*
 * Copyright (c) 2014 All rights reserved.
 *
 * InputCheckerUtil.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.android.util;

public class InputCheckerUtil {
    
    private InputCheckerUtil() {
        // Avoid initialization
    }
    
    public static boolean isWithinValidRange(int min, int max, int value) {
        return min <= value && max >= value;
    }
    
    public static boolean isValid(String string) {
        return string != null && !string.trim().isEmpty();
    }
    
}
