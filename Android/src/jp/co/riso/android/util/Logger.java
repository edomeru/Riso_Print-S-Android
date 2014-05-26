package jp.co.riso.android.util;

import android.util.Log;

public class Logger {
    public final static int LOGLEVEL_NONE = Log.ASSERT + 1;
    public final static int LOGLEVEL_ASSERT = Log.ASSERT;
    public final static int LOGLEVEL_ERROR = Log.ERROR;
    public final static int LOGLEVEL_WARN = Log.WARN;
    public final static int LOGLEVEL_INFO = Log.INFO;
    public final static int LOGLEVEL_DEBUG = Log.DEBUG;
    public final static int LOGLEVEL_VERBOSE= Log.VERBOSE;
    
    private static int sLogLevel = LOGLEVEL_NONE;
    
    public static void initialize(int logLevel) {
        sLogLevel = logLevel;
    }
    
    public static void logVerbose(Class<?> cls, String msg) {
        if (cls == null || sLogLevel > LOGLEVEL_VERBOSE) {
            return;
        }

        Log.v(cls.getName(), (msg == null) ? "" : msg);
    }
    
    public static void logDebug(Class<?> cls, String msg) {
        if (cls == null || sLogLevel > LOGLEVEL_DEBUG) {
            return;
        }
        
        Log.d(cls.getName(), (msg == null) ? "" : msg);
    }
    
    public static void logInfo(Class<?> cls, String msg) {
        if (cls == null || sLogLevel > LOGLEVEL_INFO) {
            return;
        }
        
        Log.i(cls.getName(), msg);
    }
    
    public static void logWarn(Class<?> cls, String msg) {
        if (cls == null || sLogLevel > LOGLEVEL_WARN) {
            return;
        }
        
        Log.w(cls.getName(), msg);
    }
    
    public static void logError(Class<?> cls, String msg) {
        if (cls == null || sLogLevel > LOGLEVEL_ERROR) {
            return;
        }
        
        Log.e(cls.getName(), msg);
    }
    
    public static void logAssert(Class<?> cls, String msg) {
        if (cls == null || sLogLevel > LOGLEVEL_ASSERT) {
            return;
        }
        
        Log.wtf(cls.getName(), msg);
    }
}
