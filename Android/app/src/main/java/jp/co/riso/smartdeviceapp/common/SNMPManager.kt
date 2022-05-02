/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SNMPManger.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.common

import jp.co.riso.smartdeviceapp.common.SNMPManager.SNMPManagerCallback
import jp.co.riso.smartdeviceapp.common.SNMPManager
import java.lang.ref.WeakReference

/**
 * @class SNMPManager
 *
 * @brief Manager responsible for SNMP operations.
 */
class SNMPManager {
    // Required for JNI
    var mContext: Long = 0
    private var mCallbackRef: WeakReference<SNMPManagerCallback?>? = null
    external fun initializeSNMPManager(communityName: String?)
    external fun finalizeSNMPManager()
    external fun deviceDiscovery()
    external fun manualDiscovery(ipAddress: String?)
    external fun cancel()

    /**
     * @brief Sets the callback function for the SNMP Manager.
     *
     * @param callback Callback function
     */
    fun setCallback(callback: SNMPManagerCallback?) {
        mCallbackRef = WeakReference(callback)
    }

    /**
     * @brief Callback called at the end of device discovery
     *
     * @param result Result of device discovery
     */
    private fun onEndDiscovery(result: Int) {
        if (mCallbackRef != null && mCallbackRef!!.get() != null) {
            mCallbackRef!!.get()!!.onEndDiscovery(this, result)
        }
    }

    /**
     * @brief Callback called when a device is found during device discovery
     *
     * @param ipAddress Device IP Address
     * @param name Device Name
     * @param capabilities Device capabilities
     */
    private fun onFoundDevice(ipAddress: String, name: String, capabilities: BooleanArray) {
        if (mCallbackRef != null && mCallbackRef!!.get() != null) {
            mCallbackRef!!.get()!!.onFoundDevice(this, ipAddress, name, capabilities)
        }
    }

    /**
     * @interface SNMPManagerCallback
     *
     * @brief SNMP Manager Interface
     */
    interface SNMPManagerCallback {
        /**
         * @brief Callback called at the end of device discovery
         *
         * @param manager SNMP Manager
         * @param result Result of device discovery
         */
        fun onEndDiscovery(manager: SNMPManager?, result: Int)

        /**
         * @brief Callback called when a device is found during device discovery
         *
         * @param manager SNMP Manager
         * @param ipAddress Device IP Address
         * @param name Device Name
         * @param capabilities Device capabilities
         */
        fun onFoundDevice(
            manager: SNMPManager?,
            ipAddress: String?,
            name: String?,
            capabilities: BooleanArray?
        )
    }

    companion object {
        /// Booklet finishing capability
        const val SNMP_CAPABILITY_BOOKLET_FINISHING = 0

        /// Staple capability
        const val SNMP_CAPABILITY_STAPLER = 1

        /// Punch 3 holes capability for Japan printers
        const val SNMP_CAPABILITY_FINISH_2_3 = 2

        /// Punch 4 holes capability
        const val SNMP_CAPABILITY_FINISH_2_4 = 3

        /// Punch 0 holes capability
        const val SNMP_CAPABILITY_FINISH_0 = 4

        /// Tray face down capability
        const val SNMP_CAPABILITY_TRAY_FACE_DOWN = 5

        /// Tray top capability
        const val SNMP_CAPABILITY_TRAY_TOP = 6

        /// Tray stack capability
        const val SNMP_CAPABILITY_TRAY_STACK = 7

        /// LPR print capability
        const val SNMP_CAPABILITY_LPR = 8

        /// Raw print capability 
        const val SNMP_CAPABILITY_RAW = 9

        /// External feeder capability
        const val SNMP_CAPABILITY_EXTERNAL_FEEDER = 10

        init {
            System.loadLibrary("common")
        }
    }
}