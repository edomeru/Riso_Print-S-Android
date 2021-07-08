/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Logger.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import jp.co.riso.smartdeviceapp.common.BaseTask;

/**
 * @class Logger
 * 
 * @brief Utility class for logging operations
 */
public class Logger {
    /// Log level to disable logs
    public static final int LOGLEVEL_NONE = Log.ASSERT + 1;
    /// Log level to enable logs for assert
    public static final int LOGLEVEL_ASSERT = Log.ASSERT;
    /// Log level to enable logs for error
    public static final int LOGLEVEL_ERROR = Log.ERROR;
    /// Log level to enable logs for warning
    public static final int LOGLEVEL_WARN = Log.WARN;
    /// Log level to enable logs for info
    public static final int LOGLEVEL_INFO = Log.INFO;
    /// Log level to enable logs for debug
    public static final int LOGLEVEL_DEBUG = Log.DEBUG;
    /// Log level to enable logs for verbose
    public static final int LOGLEVEL_VERBOSE= Log.VERBOSE;
    
    /// Log directory
    public static final String CONST_LOGS_DIR = "logs";
    /// Log filename
    public static final String CONST_TXT_FILENAME = "log.txt";
    
    private static int sLogLevel = LOGLEVEL_NONE;
    private static boolean sPerfLogs = false;
    private static boolean sPerfLogsToFile = false;
    
    private static String sStringFolder = "";
    private static HashMap<String, Long> sTimeLog = new HashMap<String, Long>();
    private static HashMap<String, Float> sMemoryLog = new HashMap<String, Float>();
    
    /**
     * @brief Initialize log.
     * 
     * @param logLevel Log level
     * @param perfLogs Enable log to logger (logcat)
     * @param perfLogsToFile Enable log to file 
     */
    public static void initialize(int logLevel, boolean perfLogs, boolean perfLogsToFile) {
        sLogLevel = logLevel;
        sPerfLogs = perfLogs;
        sPerfLogsToFile = perfLogsToFile;
        sTimeLog.clear();
        sMemoryLog.clear();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmssSS", Locale.getDefault());
        sStringFolder = dateFormat.format(new Date());
    }
    
    /**
     * @brief Logs a VERBOSE message. <br>
     * 
     * Logs a formatted string, using the supplied format and arguments.
     * 
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    public static void logVerbose(Class<?> cls, String format, Object...args) {
        if (cls == null || sLogLevel > LOGLEVEL_VERBOSE) {
            return;
        }

        String msg = Logger.formatMessage(format, args);
        Log.v(cls.getSimpleName(), msg);
    }

    /**
     * @brief Logs a DEBUG message. <br>
     * 
     * Logs a formatted string, using the supplied format and arguments.
     * 
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    public static void logDebug(Class<?> cls, String format, Object...args) {
        if (cls == null || sLogLevel > LOGLEVEL_DEBUG) {
            return;
        }

        String msg = Logger.formatMessage(format, args);
        Log.d(cls.getSimpleName(), (msg == null) ? "" : msg);
    }

    /**
     * @brief Logs a INFO message. <br>
     * 
     * Logs a formatted string, using the supplied format and arguments.
     * 
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    public static void logInfo(Class<?> cls, String format, Object...args) {
        if (cls == null || sLogLevel > LOGLEVEL_INFO) {
            return;
        }

        String msg = Logger.formatMessage(format, args);
        Log.i(cls.getSimpleName(), msg);
    }

    /**
     * @brief Logs a WARN message. <br>
     * 
     * Logs a formatted string, using the supplied format and arguments.
     * 
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    public static void logWarn(Class<?> cls, String format, Object...args) {
        if (cls == null || sLogLevel > LOGLEVEL_WARN) {
            return;
        }

        String msg = Logger.formatMessage(format, args);
        Log.w(cls.getSimpleName(), msg);
    }

    /**
     * @brief Logs a ERROR message. <br>
     * 
     * Logs a formatted string, using the supplied format and arguments.
     * 
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    public static void logError(Class<?> cls, String format, Object...args) {
        if (cls == null || sLogLevel > LOGLEVEL_ERROR) {
            return;
        }

        String msg = Logger.formatMessage(format, args);
        Log.e(cls.getSimpleName(), msg);
    }

    /**
     * @brief Logs a WTF message. <br>
     * 
     * Logs a formatted string, using the supplied format and arguments.
     * 
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    public static void logAssert(Class<?> cls, String format, Object...args) {
        if (cls == null || sLogLevel > LOGLEVEL_ASSERT) {
            return;
        }

        String msg = Logger.formatMessage(format, args);
        Log.wtf(cls.getSimpleName(), msg);
    }
    
    /**
     * @brief Starts a Log Time sequence.
     * 
     * @param context Valid context
     * @param cls Calling class, will be used as identifier
     * @param processName Process Name, will be used as identifier
     */
    public static void logStartTime(Context context, Class<?> cls, String processName) {
        if (!sPerfLogs || context == null || cls == null || processName == null || processName.isEmpty()) {
            return;
        }

        String tag = String.format("[%s][%s]", cls.getSimpleName(), processName);
        
        if (sTimeLog.containsKey(tag)) {
            Logger.logWarn(Logger.class, "Unterminated key, removing");
            sTimeLog.remove(tag);
            sMemoryLog.remove(tag);
        }

        long currentTime = System.currentTimeMillis();
        sTimeLog.put(tag, currentTime);

        float availableMemory = MemoryUtils.getAvailableMemory(context);
        sMemoryLog.put(tag, availableMemory);
        
        String msg = String.format(Locale.getDefault(), "%s: Started(%d) Memory(%.2f)", tag, currentTime, availableMemory);
        Logger.logInfo(Logger.class, msg);
        
        if (sPerfLogsToFile) {
            writeToFile(context, msg);
        }
    }
    
    /**
     * @brief Stops a Log Time sequence.
     * 
     * @param context Valid context
     * @param cls Calling class, will be used as identifier
     * @param processName Process Name, will be used as identifier
     */
    public static void logStopTime(Context context, Class<?> cls, String processName) {
        if (!sPerfLogs || context == null || cls == null) {
            return;
        }

        String tag = String.format("[%s][%s]", cls.getSimpleName(), processName);

        if (!sTimeLog.containsKey(tag)) {
            Logger.logError(Logger.class, "Key not started");
            return;
        }
        long startTime = sTimeLog.get(tag);
        long stopTime = System.currentTimeMillis();
        
        float startMemory = sMemoryLog.get(tag);
        float endMemory = MemoryUtils.getAvailableMemory(context);
        
        sTimeLog.remove(tag);
        sMemoryLog.remove(tag);

        String msg = String.format(Locale.getDefault(), "%s: Stopped(%d) Memory(%.2f)",
                tag, stopTime, endMemory);
        String summary = String.format(Locale.getDefault(), "%s: Duration(%.2f) Memory change(%.2f)",
                tag, (stopTime - startTime) / 1000.0f, endMemory - startMemory);
        
        Logger.logInfo(Logger.class, msg);
        Logger.logInfo(Logger.class, summary);
        
        if (sPerfLogsToFile) {
            writeToFile(context, msg);
            writeToFile(context, summary);
        }
    }
    
    /**
     * @brief Gets the entire log string from file.
     * 
     * @param context Valid context
     * 
     * @return String containing the log
     */
    public static String getLogString(Context context) {
        if (context == null) {
            return null;
        }
        
        File path = new File(getFolderString(context), CONST_TXT_FILENAME);
        
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            StringBuffer buf = new StringBuffer();
            String str;
            while ((str = in.readLine()) != null) {
                buf.append(str);
            }
            in.close();
            return buf.toString();
        } catch (IOException e) {
            Log.e(Logger.class.getSimpleName(), "Error reading logs");
        }
        
        return null;
    }
    
    /**
     * @brief Writes the message to the log text file.
     * 
     * @param context Valid context
     * @param msg Message to be written
     */
    protected static void writeToFile(Context context, String msg) {
        if (context == null) {
            return;
        }
        
        File path = new File(getFolderString(context), CONST_TXT_FILENAME);

        Log.e(Logger.class.getSimpleName(), path.toString());
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(path, true));
            pw.append((msg == null) ? "" : msg);
            pw.append('\n');
            pw.close();
        } catch (FileNotFoundException e) { // openFileOutput
            Log.e(Logger.class.getSimpleName(), "Error writing to file");
        }
    }
    
    /**
     * @brief Get the current logger's folder string.
     * 
     * @param context Valid context
     * @return File containing the folder path.
     */
    protected static File getFolderString(Context context) {
        if (context == null) {
            return null;
        }
        
        return context.getExternalFilesDir(CONST_LOGS_DIR + "/" + sStringFolder);
    }

    /**
     * @brief Creates the log message, and throws unnecessary exceptions.
     * 
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    private static String formatMessage(String format, Object...args) {
        try {
            String msg = (format == null) ? "" : String.format(Locale.getDefault(), format, args);
            return msg;
        } catch(IllegalFormatException e) {
            Log.e(Logger.class.getSimpleName(), "IllegalFormatException, logging format directly");
            return format;
        }
    }

    /**
     * @brief Runs a background task which deletes the application logs.
     * 
     * @param context Application context
     */
    public static void runDeleteTask(Context context) {
        if (context == null) {
            return;
        }
        
        Logger logger = new Logger();
        DeleteTask task = (new DeleteTask());
        
        task.execute(context);
    }
    
    /**
     * @class DeleteTask
     * 
     * @brief Async Task for deleting a directory
     */
    public static class DeleteTask extends BaseTask<Context, Void> {
        int count = 0;
        double time = 0;
        
        /**
         * @brief Delete specified directory.
         * 
         * Based on: http://stackoverflow.com/questions/5701586/delete-a-folder-on-sd-card 
         * 
         * @param path File object of the Directory path to delete
         */
        public boolean deleteDirectory(File path) {
            if (path.exists()) {
                File[] files = path.listFiles();
                if (files == null) {
                    return true;
                }
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    files[i].delete();
                    count++;
                }
            }
            return true;
        }
        
        /**
         * @brief Deletes the sub folders inside the AppLogDirectory.
         * 
         * Based on: http://stackoverflow.com/questions/5701586/delete-a-folder-on-sd-card
         * 
         * @param path File object of the Directory path to delete
         */
        public boolean deleteAppLogDirectory(File path) {
            if (path.exists()) {
                File[] files = path.listFiles();
                if (files == null) {
                    return true;
                }
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isDirectory()) {
                        //deletes the whole sub-directory
                        if (deleteDirectory(files[i])) {
                            files[i].delete();
                        }
                    }
                }
            }
            return true;
        }
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            
            time = System.currentTimeMillis();
        }
        
        @Override
        protected Void doInBackground(Context... params) {
            deleteAppLogDirectory(params[0].getExternalFilesDir(CONST_LOGS_DIR));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            Log.d(DeleteTask.class.getSimpleName(), "Duration: " + (System.currentTimeMillis() - time));
            Log.d(DeleteTask.class.getSimpleName(), "Count: " + count);
        }
    }
    
    static {
        initialize(LOGLEVEL_NONE, false, false);
    }
}
