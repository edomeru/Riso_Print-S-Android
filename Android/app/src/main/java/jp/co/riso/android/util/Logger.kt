/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * Logger.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.util

import android.content.Context
import android.util.Log
import jp.co.riso.android.util.MemoryUtils
import jp.co.riso.android.util.Logger.DeleteTask
import jp.co.riso.smartdeviceapp.common.BaseTask
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * @class Logger
 *
 * @brief Utility class for logging operations
 */
object Logger {
    /// Log level to disable logs
    const val LOGLEVEL_NONE = Log.ASSERT + 1

    /// Log level to enable logs for assert
    const val LOGLEVEL_ASSERT = Log.ASSERT

    /// Log level to enable logs for error
    const val LOGLEVEL_ERROR = Log.ERROR

    /// Log level to enable logs for warning
    const val LOGLEVEL_WARN = Log.WARN

    /// Log level to enable logs for info
    const val LOGLEVEL_INFO = Log.INFO

    /// Log level to enable logs for debug
    const val LOGLEVEL_DEBUG = Log.DEBUG

    /// Log level to enable logs for verbose
    const val LOGLEVEL_VERBOSE = Log.VERBOSE

    /// Log directory
    const val CONST_LOGS_DIR = "logs"

    /// Log filename
    const val CONST_TXT_FILENAME = "log.txt"
    private var sLogLevel = LOGLEVEL_NONE
    private var sPerfLogs = false
    private var sPerfLogsToFile = false
    private var sStringFolder = ""
    private val sTimeLog = HashMap<String, Long>()
    private val sMemoryLog = HashMap<String, Float>()

    /**
     * @brief Initialize log.
     *
     * @param logLevel Log level
     * @param perfLogs Enable log to logger (logcat)
     * @param perfLogsToFile Enable log to file
     */
    @JvmStatic
    fun initialize(logLevel: Int, perfLogs: Boolean, perfLogsToFile: Boolean) {
        sLogLevel = logLevel
        sPerfLogs = perfLogs
        sPerfLogsToFile = perfLogsToFile
        sTimeLog.clear()
        sMemoryLog.clear()
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmssSS", Locale.getDefault())
        sStringFolder = dateFormat.format(Date())
    }

    /**
     * @brief Logs a VERBOSE message. <br></br>
     *
     * Logs a formatted string, using the supplied format and arguments.
     *
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    @JvmStatic
    fun logVerbose(cls: Class<*>?, format: String?, vararg args: Any) {
        if (cls == null || sLogLevel > LOGLEVEL_VERBOSE) {
            return
        }
        val msg = formatMessage(format, *args)
        Log.v(cls.simpleName, msg!!)
    }

    /**
     * @brief Logs a DEBUG message. <br></br>
     *
     * Logs a formatted string, using the supplied format and arguments.
     *
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    @JvmStatic
    fun logDebug(cls: Class<*>?, format: String?, vararg args: Any) {
        if (cls == null || sLogLevel > LOGLEVEL_DEBUG) {
            return
        }
        val msg = formatMessage(format, *args)
        Log.d(cls.simpleName, msg ?: "")
    }

    /**
     * @brief Logs a INFO message. <br></br>
     *
     * Logs a formatted string, using the supplied format and arguments.
     *
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    @JvmStatic
    fun logInfo(cls: Class<*>?, format: String?, vararg args: Any) {
        if (cls == null || sLogLevel > LOGLEVEL_INFO) {
            return
        }
        val msg = formatMessage(format, *args)
        Log.i(cls.simpleName, msg!!)
    }

    /**
     * @brief Logs a WARN message. <br></br>
     *
     * Logs a formatted string, using the supplied format and arguments.
     *
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    @JvmStatic
    fun logWarn(cls: Class<*>?, format: String?, vararg args: Any) {
        if (cls == null || sLogLevel > LOGLEVEL_WARN) {
            return
        }
        val msg = formatMessage(format, *args)
        Log.w(cls.simpleName, msg!!)
    }

    /**
     * @brief Logs a ERROR message. <br></br>
     *
     * Logs a formatted string, using the supplied format and arguments.
     *
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    @JvmStatic
    fun logError(cls: Class<*>?, format: String?, vararg args: Any) {
        if (cls == null || sLogLevel > LOGLEVEL_ERROR) {
            return
        }
        val msg = formatMessage(format, *args)
        Log.e(cls.simpleName, msg!!)
    }

    /**
     * @brief Logs a WTF message. <br></br>
     *
     * Logs a formatted string, using the supplied format and arguments.
     *
     * @param cls Calling class. Will be used as Log's TAG
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    @JvmStatic
    fun logAssert(cls: Class<*>?, format: String?, vararg args: Any) {
        if (cls == null || sLogLevel > LOGLEVEL_ASSERT) {
            return
        }
        val msg = formatMessage(format, *args)
        Log.wtf(cls.simpleName, msg)
    }

    /**
     * @brief Starts a Log Time sequence.
     *
     * @param context Valid context
     * @param cls Calling class, will be used as identifier
     * @param processName Process Name, will be used as identifier
     */
    @JvmStatic
    fun logStartTime(context: Context?, cls: Class<*>?, processName: String?) {
        if (!sPerfLogs || context == null || cls == null || processName == null || processName.isEmpty()) {
            return
        }
        val tag = String.format("[%s][%s]", cls.simpleName, processName)
        if (sTimeLog.containsKey(tag)) {
            logWarn(Logger::class.java, "Unterminated key, removing")
            sTimeLog.remove(tag)
            sMemoryLog.remove(tag)
        }
        val currentTime = System.currentTimeMillis()
        sTimeLog[tag] = currentTime
        val availableMemory = MemoryUtils.getAvailableMemory(context)
        sMemoryLog[tag] = availableMemory
        val msg = String.format(
            Locale.getDefault(),
            "%s: Started(%d) Memory(%.2f)",
            tag,
            currentTime,
            availableMemory
        )
        logInfo(Logger::class.java, msg)
        if (sPerfLogsToFile) {
            writeToFile(context, msg)
        }
    }

    /**
     * @brief Stops a Log Time sequence.
     *
     * @param context Valid context
     * @param cls Calling class, will be used as identifier
     * @param processName Process Name, will be used as identifier
     */
    @JvmStatic
    fun logStopTime(context: Context?, cls: Class<*>?, processName: String?) {
        if (!sPerfLogs || context == null || cls == null) {
            return
        }
        val tag = String.format("[%s][%s]", cls.simpleName, processName)
        if (!sTimeLog.containsKey(tag)) {
            logError(Logger::class.java, "Key not started")
            return
        }
        val startTime = sTimeLog[tag]!!
        val stopTime = System.currentTimeMillis()
        val startMemory = sMemoryLog[tag]!!
        val endMemory = MemoryUtils.getAvailableMemory(context)
        sTimeLog.remove(tag)
        sMemoryLog.remove(tag)
        val msg = String.format(
            Locale.getDefault(), "%s: Stopped(%d) Memory(%.2f)",
            tag, stopTime, endMemory
        )
        val summary = String.format(
            Locale.getDefault(), "%s: Duration(%.2f) Memory change(%.2f)",
            tag, (stopTime - startTime) / 1000.0f, endMemory - startMemory
        )
        logInfo(Logger::class.java, msg)
        logInfo(Logger::class.java, summary)
        if (sPerfLogsToFile) {
            writeToFile(context, msg)
            writeToFile(context, summary)
        }
    }

    /**
     * @brief Gets the entire log string from file.
     *
     * @param context Valid context
     *
     * @return String containing the log
     */
    @JvmStatic
    fun getLogString(context: Context?): String? {
        if (context == null) {
            return null
        }
        val path = File(getFolderString(context), CONST_TXT_FILENAME)
        try {
            val `in` = BufferedReader(FileReader(path))
            val buf = StringBuffer()
            var str: String?
            while (`in`.readLine().also { str = it } != null) {
                buf.append(str)
            }
            `in`.close()
            return buf.toString()
        } catch (e: IOException) {
            Log.e(Logger::class.java.simpleName, "Error reading logs")
        }
        return null
    }

    /**
     * @brief Writes the message to the log text file.
     *
     * @param context Valid context
     * @param msg Message to be written
     */
    @JvmStatic
    fun writeToFile(context: Context?, msg: String?) {
        if (context == null) {
            return
        }
        val path = File(getFolderString(context), CONST_TXT_FILENAME)
        Log.e(Logger::class.java.simpleName, path.toString())
        try {
            val pw = PrintWriter(FileOutputStream(path, true))
            pw.append(msg ?: "")
            pw.append('\n')
            pw.close()
        } catch (e: FileNotFoundException) { // openFileOutput
            Log.e(Logger::class.java.simpleName, "Error writing to file")
        }
    }

    /**
     * @brief Get the current logger's folder string.
     *
     * @param context Valid context
     * @return File containing the folder path.
     */
    internal fun getFolderString(context: Context?): File? {
        return context?.getExternalFilesDir(CONST_LOGS_DIR + "/" + sStringFolder)
    }

    /**
     * @brief Creates the log message, and throws unnecessary exceptions.
     *
     * @param format The format string
     * @param args The list of arguments passed to the formatter. If there are more arguments than required by format, additional arguments are ignored.
     */
    private fun formatMessage(format: String?, vararg args: Any): String? {
        return try {
            if (format == null) "" else String.format(Locale.getDefault(), format, *args)
        } catch (e: IllegalFormatException) {
            Log.e(Logger::class.java.simpleName, "IllegalFormatException, logging format directly")
            format
        }
    }

    /**
     * @brief Runs a background task which deletes the application logs.
     *
     * @param context Application context
     */
    @JvmStatic
    fun runDeleteTask(context: Context?) {
        if (context == null) {
            return
        }
        val task = DeleteTask()
        task.execute(context)
    }

    /**
     * @class DeleteTask
     *
     * @brief Async Task for deleting a directory
     */
    class DeleteTask : BaseTask<Context?, Void?>() {
        var count = 0
        var time = 0.0

        /**
         * @brief Delete specified directory.
         *
         * Based on: http://stackoverflow.com/questions/5701586/delete-a-folder-on-sd-card
         *
         * @param path File object of the Directory path to delete
         */
        fun deleteDirectory(path: File): Boolean {
            if (path.exists()) {
                val files = path.listFiles() ?: return true
                for (file in files) {
                    if (file.isDirectory) {
                        deleteDirectory(file)
                    }
                    file.delete()
                    count++
                }
            }
            return true
        }

        /**
         * @brief Deletes the sub folders inside the AppLogDirectory.
         *
         * Based on: http://stackoverflow.com/questions/5701586/delete-a-folder-on-sd-card
         *
         * @param path File object of the Directory path to delete
         */
        fun deleteAppLogDirectory(path: File?): Boolean {
            if (path!!.exists()) {
                val files = path.listFiles() ?: return true
                for (file in files) {
                    if (file.isDirectory) {
                        //deletes the whole sub-directory
                        if (deleteDirectory(file)) {
                            file.delete()
                        }
                    }
                }
            }
            return true
        }

        override fun onPreExecute() {
            super.onPreExecute()
            time = System.currentTimeMillis().toDouble()
        }

        protected override fun doInBackground(vararg params: Context?): Void? {
            deleteAppLogDirectory(params[0]?.getExternalFilesDir(CONST_LOGS_DIR))
            return null
        }

        protected override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            Log.d(
                DeleteTask::class.java.simpleName,
                "Duration: " + (System.currentTimeMillis() - time)
            )
            Log.d(DeleteTask::class.java.simpleName, "Count: $count")
        }

    }

    init {
        initialize(LOGLEVEL_NONE, false, false)
    }
}