/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SmartDeviceApp.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.smartdeviceapp;

import jp.co.alinkgroup.android.log.Logger;
import android.app.Application;

public class SmartDeviceApp extends Application {
    
    @Override
    public void onCreate() {
        Logger.init(getApplicationContext());
        Logger.setLogLevel(Logger.LOGLEVEL_DEBUG);
    }
}
