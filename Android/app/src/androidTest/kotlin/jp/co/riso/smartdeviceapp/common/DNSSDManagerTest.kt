package jp.co.riso.smartdeviceapp.common

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import jp.co.riso.smartdeviceapp.view.BaseActivityTestUtil
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.net.InetAddress

class DNSSDManagerTest : BaseActivityTestUtil(), DNSSDManagerListener {
    private var _manager: DNSSDManager? = null
    private var _discoveryListener: DNSSDManager.DNSSDDiscoveryListener? = null
    private var _resolveListener: DNSSDManager.DNSSDResolveListener? = null
    private var _ipAddresses: MutableList<String>? = null

    @Before
    fun setUp() {
        _manager = DNSSDManager(mainActivity!!, this)
        _ipAddresses = mutableListOf()
    }

    @After
    fun cleanUp() {
        _manager = null
    }

    // ================================================================================
    // Tests - Device Discovery
    // ================================================================================
    @Test
    fun testDeviceDiscovery() {
        try {
            // Call discover twice and do not throw an exception
            _manager?.deviceDiscovery()
            _manager?.deviceDiscovery()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - Cancel Device Discovery
    // ================================================================================
    @Test
    fun testCancel() {
        try {
            _manager?.deviceDiscovery()
            // Call cancel twice and do not throw an exception
            _manager?.cancel()
            _manager?.cancel()
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - Get IPPS Capability
    // ================================================================================
    @Test
    fun testGetIPPSCapability_True() {
        val ipAddress = "192.168.1.200"
        // Add IP address
        _manager?.addHost(ipAddress)

        val result = _manager?.getIppsCapability(ipAddress)
        TestCase.assertTrue(_ipAddresses?.contains(ipAddress) == true)
        TestCase.assertTrue(result == true)
    }

    @Test
    fun testGetIPPSCapability_False() {
        // Add IP address
        val ipAddress = "192.168.1.200"
        val result = _manager?.getIppsCapability(ipAddress)
        TestCase.assertFalse(_ipAddresses?.contains(ipAddress) == true)
        TestCase.assertFalse(result == true)
    }

    // ================================================================================
    // Tests - DNSSDDiscoveryListener
    // ================================================================================
    @Test
    fun testDNSSDDiscoveryListener() {
        try {
            val nsdManager = mainActivity?.getSystemService(Context.NSD_SERVICE) as NsdManager
            val serviceType = "._tcp.local"
            val errorCode = 0
            val serviceInfo = NsdServiceInfo()
            serviceInfo.serviceName = "IPPS service"
            serviceInfo.serviceType = serviceType
            serviceInfo.host = InetAddress.getLocalHost()

            _discoveryListener = DNSSDManager.DNSSDDiscoveryListener(nsdManager, _manager!!)
            _discoveryListener?.onDiscoveryStarted(serviceType)
            _discoveryListener?.onDiscoveryStopped(serviceType)
            _discoveryListener?.onStartDiscoveryFailed(serviceType, errorCode)
            _discoveryListener?.onStopDiscoveryFailed(serviceType, errorCode)
            _discoveryListener?.onServiceFound(null)
            _discoveryListener?.onServiceLost(null)

            _discoveryListener?.onServiceFound(serviceInfo)
            _discoveryListener?.onServiceLost(serviceInfo)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - DNSSDResolveListener
    // ================================================================================
    @Test
    fun testDNSSDResolveListener() {
        try {
            val serviceInfo = NsdServiceInfo()
            serviceInfo.serviceName = "IPPS service"
            serviceInfo.serviceType = "._tcp.local"
            serviceInfo.host = InetAddress.getLocalHost()

            _resolveListener = DNSSDManager.DNSSDResolveListener(_manager!!)
            _resolveListener?.onResolveFailed(null, 0)
            _resolveListener?.onServiceResolved(null)

            _resolveListener?.onResolveFailed(serviceInfo, 0)
            _resolveListener?.onServiceResolved(serviceInfo)

            serviceInfo.host = null
            _resolveListener?.onServiceResolved(serviceInfo)
        } catch (e: Exception) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // DNSSDManagerListener Interface
    // ================================================================================
    override fun updatePrinterIPPSCapabilties(ipAddress: String) {
        _ipAddresses?.add(ipAddress)
    }
}