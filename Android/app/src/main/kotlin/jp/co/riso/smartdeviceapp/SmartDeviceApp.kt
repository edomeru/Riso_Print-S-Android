/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * SmartDeviceApp.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp

import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.app.Activity
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Context
import jp.co.riso.android.util.Logger
import kotlin.jvm.Volatile

/**
 * @class SmartDeviceApp
 * 
 * @brief Application class.
 */
class SmartDeviceApp : Application(), ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        if (AppConstants.DEBUG) {
            Logger.initialize(
                Logger.LOGLEVEL_VERBOSE,
                AppConstants.FOR_PERF_LOGS,
                AppConstants.FOR_PERF_LOGS
            )
            Logger.runDeleteTask(applicationContext)
        }
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Companion.activity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        Companion.activity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        Companion.activity = activity
    }

    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        /**
         * @brief Retrieve the application context.
         *
         * @return Application shared context
         */
        @SuppressLint("StaticFieldLeak")
        @Volatile
        var appContext: Context? = null
            private set

        @SuppressLint("StaticFieldLeak")
        @Volatile
        var activity: Activity? = null
            private set
    }
}