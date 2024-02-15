/*
 * Copyright (c) 2023 RISO, Inc. All rights reserved.
 *
 * DNSSDManager.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.common

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdManager.ResolveListener
import android.net.nsd.NsdServiceInfo
import jp.co.riso.android.util.Logger

interface DNSSDManagerListener {
    fun updatePrinterIPPSCapabilties(ipAddress: String)
}

interface DNSSDManagerHostListener {
    fun addHost(hostAddress: String)
    fun done()
}

class DNSSDManager(context: Context, private val listener: DNSSDManagerListener): DNSSDManagerHostListener {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = DNSSDDiscoveryListener(nsdManager, this)
    private val ipAddresses = mutableListOf<String>()
    private var isCurrentlyDiscovering = false

    private val SERVICE_TYPE = "_ipps._tcp."

    fun deviceDiscovery() {
        if (!isCurrentlyDiscovering) {
            isCurrentlyDiscovering = true

            nsdManager.discoverServices(
                SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
            )
        }
    }

    fun cancel() {
        if (isCurrentlyDiscovering) {
            nsdManager.stopServiceDiscovery(discoveryListener)
        }
    }

    fun getIppsCapability(ipAddress: String): Boolean {
        return ipAddresses.contains(ipAddress)
    }

    override fun addHost(hostAddress: String) {
        ipAddresses.add(hostAddress)
        listener.updatePrinterIPPSCapabilties(hostAddress)
        Logger.logDebug(DNSSDManager::class.java, "add host: $hostAddress")
    }


    override fun done() {
        isCurrentlyDiscovering = false
    }

    class DNSSDDiscoveryListener(private val nsdManager: NsdManager, private val listener: DNSSDManagerHostListener): NsdManager.DiscoveryListener {
        override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Logger.logError(
                DNSSDManager::class.java,
                "start DNS-SD for serviceType = $serviceType error = $errorCode failed"
            )
        }

        override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
            Logger.logError(
                DNSSDManager::class.java,
                "stop DNS-SD for serviceType = $serviceType error = $errorCode failed"
            )
        }

        override fun onDiscoveryStarted(serviceType: String?) {
            Logger.logDebug(DNSSDManager::class.java, "start DNS-SD")
        }

        override fun onDiscoveryStopped(serviceType: String?) {
            Logger.logDebug(DNSSDManager::class.java, "stop DNS-SD")
            listener.done()
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo?) {
            // Get the service IP address
            val resolveListener = DNSSDResolveListener(listener)
            serviceInfo?.let { nsdManager.resolveService(serviceInfo, resolveListener) }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo?) {
            Logger.logDebug(
                DNSSDManager::class.java,
                "DNS-SD" + serviceInfo?.serviceName + " lost")
        }
    }

    class DNSSDResolveListener(private val listener: DNSSDManagerHostListener): ResolveListener {
        override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {
            Logger.logError(
                DNSSDManager::class.java,
                "stop DNS-SD for serviceType = ${serviceInfo?.serviceName} error = $errorCode failed"
            )
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
            serviceInfo?.host?.hostAddress?.let { listener.addHost(it) }
        }
    }
}