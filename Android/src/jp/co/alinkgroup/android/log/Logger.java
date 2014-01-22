/*
 * Copyright (c) 2014 All rights reserved.
 *
 * Logger.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.alinkgroup.android.log;

import java.util.Date;
import java.util.HashMap;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.util.Log;

public class Logger {
    public final static int LOGLEVEL_NONE = Log.ASSERT + 1;
    public final static int LOGLEVEL_ASSERT = Log.ASSERT;
    public final static int LOGLEVEL_ERROR = Log.ERROR;
    public final static int LOGLEVEL_WARNING = Log.WARN;
    public final static int LOGLEVEL_INFO = Log.INFO;
    public final static int LOGLEVEL_DEBUG = Log.DEBUG;
    public final static int LOGLEVEL_VERBOSE = Log.VERBOSE;
    
    private static boolean mIsPerfLogs = false;
    private static int mLogLevel = LOGLEVEL_NONE;
    private static Context mContext = null;
    private static String mTag = "";
    private static HashMap<String, Date> mTimeLog = new HashMap<String, Date>();
    
    public static void init(Context appContext) {
        mContext = appContext;
        mTag = mContext.getClass().getSimpleName();
        
        if (Log.isLoggable(mTag, Log.ERROR)) {
            mLogLevel = LOGLEVEL_ERROR;
        }
        if (Log.isLoggable(mTag, Log.WARN)) {
            mLogLevel = LOGLEVEL_WARNING;
        }
        if (Log.isLoggable(mTag, Log.INFO)) {
            mLogLevel = LOGLEVEL_INFO;
        }
        if (Log.isLoggable(mTag, Log.DEBUG)) {
            mLogLevel = LOGLEVEL_DEBUG;
        }
        if (Log.isLoggable(mTag, Log.VERBOSE)) {
            mLogLevel = LOGLEVEL_VERBOSE;
        }
    }
    
    public static void logDebug(Object caller, String msg) {
        if (mLogLevel <= LOGLEVEL_DEBUG && caller != null) {
            Log.d(String.format("[%s]", caller.getClass().getSimpleName()), msg);
        }
    }
    
    public static void logInfo(Object caller, String msg) {
        if (mLogLevel <= LOGLEVEL_INFO && caller != null) {
            Log.i(String.format("[%s]", caller.getClass().getSimpleName()), msg);
        }
    }
    
    public static void logError(Object caller, String msg) {
        if (mLogLevel <= LOGLEVEL_ERROR && caller != null) {
            Log.e(String.format("[%s]", caller.getClass().getSimpleName()), msg);
        }
    }
    
    public static void logWarning(Object caller, String msg) {
        if (mLogLevel <= LOGLEVEL_WARNING && caller != null) {
            Log.w(String.format("[%s]", caller.getClass().getSimpleName()), msg);
        }
    }
    
    public static void logVerbose(Object caller, String msg) {
        if (mLogLevel == Log.VERBOSE && caller != null) {
            Log.v(String.format("[%s]", caller.getClass().getSimpleName()), msg);
        }
    }
    
    public static void setLogLevel(int logLevel) {
        mLogLevel = logLevel;
    }
    
    /* For perfomance test */
    public static void setPerformanceLogsOn(boolean isPerfLogs) {
        mIsPerfLogs = isPerfLogs;
    }
    
    public static void logAvailableMemory() {
        if (mIsPerfLogs) {
            float memory = getMemoryAvailable();
            if (Float.isInfinite(memory)) {
                Log.i(mTag, "Memory log not available");
                return;
            }
            Log.i(mTag, "Memory available: " + memory + "MB");
        }
    }
    
    public static void logTimeStart(Object caller, String processName) {
        if (mIsPerfLogs && caller != null) {
            String tag = String.format("[%s][%s]", caller.getClass().getSimpleName(), processName);
            Log.i(tag, "Time Start");
            mTimeLog.put(tag, new Date());
        }
    }
    
    public static void logTimeEnd(Object caller, String processName) {
        if (mIsPerfLogs && caller != null) {
            Date timeEnd = new Date();
            String tag = String.format("[%s][%s]", caller.getClass().getSimpleName(), processName);
            Date timeStart = mTimeLog.get(tag);
            if (timeStart != null) {
                long duration = timeEnd.getTime() - timeStart.getTime();
                Log.i(String.format("[%s][%s]", caller.getClass().getSimpleName(), processName), "Time End. Duration: " + duration + " ms");
            }
            mTimeLog.remove(tag);
        }
    }
    
    private static float getMemoryAvailable() {
        float consumedMegs = Float.NaN;
        if (mContext != null) {
            MemoryInfo mi = new MemoryInfo();
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            consumedMegs = (mi.availMem) / 1048576L;
        }
        
        return consumedMegs;
    }
    
}
