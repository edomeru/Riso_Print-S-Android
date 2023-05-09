package jp.co.riso.android.util

import android.content.Context
import android.net.wifi.WifiManager
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import junit.framework.TestCase
import org.junit.Test
import java.net.Inet6Address
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NetUtilsTest {

    private val _signal = CountDownLatch(1)

    // ================================================================================
    // Tests - constructors
    // ================================================================================
    @Test
    fun testConstructor_WifiOff() {
        turnWifi(false)
        val netUtils = NetUtils
        TestCase.assertNotNull(netUtils)
        turnWifiOn()
    }

    @Test
    fun testConstructor_WifiOn() {
        val netUtils = NetUtils
        TestCase.assertNotNull(netUtils)
    }

    // ================================================================================
    // Tests - isIPv4Address
    // ================================================================================
    @Test
    fun testIsIPv4Address_ValidIpv4Address() {
        var isIpV4Address: Boolean
        for (iPv4_valid_address in IPv4_VALID_ADDRESS) {
            isIpV4Address = NetUtils.isIPv4Address(iPv4_valid_address)
            TestCase.assertEquals(true, isIpV4Address)
        }
    }

    @Test
    fun testIsIPv4Address_InvalidIpv4Address() {
        var isIpV4Address: Boolean
        for (ipv4_invalid_address in IPv4_INVALID_ADDRESS) {
            isIpV4Address = NetUtils.isIPv4Address(ipv4_invalid_address)
            TestCase.assertEquals(false, isIpV4Address)
        }
        for (iPv6_valid_address in IPv6_VALID_ADDRESS) {
            isIpV4Address = NetUtils.isIPv4Address(iPv6_valid_address)
            TestCase.assertEquals(false, isIpV4Address)
        }
    }

    @Test
    fun testIsIPv4Address_NullIpAddress() {
        try {
            NetUtils.isIPv4Address(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isIPv4MulticastAddress
    // ================================================================================
    @Test
    fun testIsIPv4MulticastAddress_ValidIpv4MulticastAddress() {
        var isIpV4Address: Boolean
        for (iPv4_multicast_valid_address in IPv4_MULTICAST_VALID_ADDRESS) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(iPv4_multicast_valid_address)
            TestCase.assertEquals(true, isIpV4Address)
        }
    }

    @Test
    fun testIsIPv4MulticastAddress_InvalidIpv4MulticastAddress() {
        var isIpV4Address: Boolean
        for (iPv4_multicast_invalid_address in IPv4_MULTICAST_INVALID_ADDRESS) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(iPv4_multicast_invalid_address)
            TestCase.assertEquals(false, isIpV4Address)
        }
        for (ipv4_invalid_address in IPv4_INVALID_ADDRESS) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(ipv4_invalid_address)
            TestCase.assertEquals(false, isIpV4Address)
        }
        for (iPv6_valid_address in IPv6_VALID_ADDRESS) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(iPv6_valid_address)
            TestCase.assertEquals(false, isIpV4Address)
        }
    }

    @Test
    fun testIsIPv4MulticastAddress_NullIpAddress() {
        try {
            NetUtils.isIPv4MulticastAddress(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isIPv6Address
    // ================================================================================
    @Test
    fun testIsIPv6Address_ValidIpv6Address() {
        var isIpV6Address: Boolean
        for (iPv6_valid_address in IPv6_VALID_ADDRESS) {
            isIpV6Address = NetUtils.isIPv6Address(iPv6_valid_address)
            TestCase.assertEquals(true, isIpV6Address)
        }
    }

    @Test
    fun testIsIPv6Address_InvalidIpv6Address() {
        var isIpV6Address: Boolean
        for (iPv6_invalid_address in IPv6_INVALID_ADDRESS) {
            isIpV6Address = NetUtils.isIPv6Address(iPv6_invalid_address)
            TestCase.assertEquals(false, isIpV6Address)
        }
        for (iPv4_valid_address in IPv4_VALID_ADDRESS) {
            isIpV6Address = NetUtils.isIPv6Address(iPv4_valid_address)
            TestCase.assertEquals(false, isIpV6Address)
        }
    }

    @Test
    fun testIsIPv6Address_NullIpAddress() {
        try {
            NetUtils.isIPv6Address(null)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // *** This method is unused. isWifiAvailable() method is instead used for checking of connectivity.
    // ================================================================================
    // Tests - isNetworkAvailable
    // ================================================================================
    /*
    public void testIsNetworkAvailable_NullContext() {
        assertEquals(false, NetUtils.isNetworkAvailable(null));
    }

    public void testIsNetworkAvailable_WithoutConnection() {
        // permission CHANGE_WIFI_STATE in app's manifest file must be present
        turnWifi(false);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            // ignored
        }
        // wifi is OFF
        assertEquals(false, NetUtils.isNetworkAvailable(SmartDeviceApp.getAppContext()));
    }

    public void testIsNetworkAvailable_WithConnection() {
        // permission CHANGE_WIFI_STATE in app's manifest file must be present
        turnWifiOn();
        // wifi is ON
        assertEquals(true, NetUtils.isNetworkAvailable(SmartDeviceApp.getAppContext()));
    }
    */
    // ================================================================================
    // Tests - isWifiAvailable
    // ================================================================================
    @Test
    fun testIsNetworkAvailable_Null() {
        TestCase.assertEquals(false, NetUtils.isNetworkAvailable)
    }

    @Test
    fun testIsWifiAvailable_WithConnection() {
        NetUtils.registerNetworkCallback(SmartDeviceApp.appContext!!)
        // permission CHANGE_WIFI_STATE in app's manifest file must be present
        turnWifiOn()
        // wifi is ON
        TestCase.assertEquals(true, NetUtils.isNetworkAvailable)
        NetUtils.unregisterNetworkCallback(SmartDeviceApp.appContext!!)
        TestCase.assertEquals(false, NetUtils.isNetworkAvailable)
    }

    // ================================================================================
    // Tests - connectToIpv4Address
    // ================================================================================
    @Test
    fun testConnectToIpv4Address_OfflineIpv4Address() {
        try {
            val isReachable: Boolean = NetUtils.connectToIpv4Address(IPV4_OFFLINE_PRINTER_ADDRESS)
            TestCase.assertEquals(false, isReachable)
            _signal.await(500, TimeUnit.MILLISECONDS)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        } catch (e: InterruptedException) {
            TestCase.fail()
        }
    }

    @Test
    fun testConnectToIpv4Address_NullIpAddress() {
        try {
            NetUtils.connectToIpv4Address(null)
            _signal.await(500, TimeUnit.MILLISECONDS)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        } catch (e: InterruptedException) {
            TestCase.fail()
        }
    }

    @Test
    fun testConnectToIpv4Address_WifiDisabled() {
        try {
            turnWifi(false)
            val isReachable: Boolean = NetUtils.connectToIpv4Address(IPV4_OFFLINE_PRINTER_ADDRESS)
            TestCase.assertEquals(false, isReachable)
            turnWifiOn()
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - connectToIpv6Address
    // ================================================================================
    @Test
    fun testConnectToIpv6Address_OfflineIpv6Address() {
        try {
            var isReachable: Boolean = NetUtils.connectToIpv6Address(IPV6_OFFLINE_PRINTER_ADDRESS)
            TestCase.assertEquals(false, isReachable)
            _signal.await(500, TimeUnit.MILLISECONDS)
            isReachable = NetUtils.connectToIpv6Address(
                "$IPV6_STD_OFFLINE_PRINTER_ADDRESS%wlan0"
            )
            TestCase.assertEquals(false, isReachable)
            _signal.await(500, TimeUnit.MILLISECONDS)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        } catch (e: UnknownHostException) {
            TestCase.fail()
        } catch (e: InterruptedException) {
            TestCase.fail()
        }
    }

    /*
    public void testConnectToIpv6Address_OnlineIpv6Address() {
        try {
            int retry = 10;
            boolean isReachable = false;
            InetAddress inetIpAddress = InetAddress.getByName(IPV6_STD_PRINTER_ADDRESS);
            String ipv6Addr = IPV6_STD_PRINTER_ADDRESS;
            
            // Ipv6 Address
            ipv6Addr = getLocalIpv6Address();
            assertNotNull(ipv6Addr);

            while (retry > 0) {
                isReachable = NetUtils.connectToIpv6Address(ipv6Addr, inetIpAddress);
                if (isReachable) {
                    break;
                }
                mSignal.await(1, TimeUnit.SECONDS);
                retry--;
            }
            assertEquals(true, isReachable);

            mSignal.await(500, TimeUnit.MILLISECONDS);
            
            if (ipv6Addr.contains("%")) {
                String[] newIpString = ipv6Addr.split("%");
                if (newIpString != null) {
                    ipv6Addr = newIpString[0];
                }
            }
            // Reset retry
            retry = 10;
            while (retry > 0) {
                isReachable = NetUtils.connectToIpv6Address(ipv6Addr, inetIpAddress);
                if (isReachable) {
                    break;
                }
                mSignal.await(1, TimeUnit.SECONDS);
                retry--;
            }
            assertEquals(true, isReachable);
            
            mSignal.await(500, TimeUnit.MILLISECONDS);
            
            // Reset retry
            inetIpAddress = InetAddress.getByName(IPV6_STD_RISO_PRINTER_ADDRESS);
            ipv6Addr = IPV6_STD_RISO_PRINTER_ADDRESS;
            retry = 10;
            while (retry > 0) {
                isReachable = NetUtils.connectToIpv6Address(ipv6Addr, inetIpAddress);
                if (isReachable) {
                    break;
                }
                mSignal.await(1, TimeUnit.SECONDS);
                retry--;
            }
            assertEquals(true, isReachable);
            
            mSignal.await(500, TimeUnit.MILLISECONDS);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (InterruptedException e) {
            fail(); // Error should not be thrown
        } catch (UnknownHostException e) {
            fail(); // Error should not be thrown
        }
    }
    */
    @Test
    fun testConnectToIpv6Address_NullIpAddress() {
        try {
            NetUtils.connectToIpv6Address(null)
            _signal.await(500, TimeUnit.MILLISECONDS)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        } catch (e: InterruptedException) {
            TestCase.fail()
        } catch (e: UnknownHostException) {
            TestCase.fail()
        }
    }

    @Test
    fun testConnectToIpv6Address_NullInetAddressObject() {
        try {
            val ipv6Addr = _localIpv6Address
            TestCase.assertNotNull(ipv6Addr)
            NetUtils.connectToIpv6Address(ipv6Addr)
            _signal.await(500, TimeUnit.MILLISECONDS)
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        } catch (e: InterruptedException) {
            TestCase.fail()
        }
    }

    @Test
    fun testConnectToIpv6Address_WifiDisabled() {
        try {
            turnWifi(false)
            val isReachable: Boolean =
                NetUtils.connectToIpv6Address(IPV6_OFFLINE_PRINTER_ADDRESS)
            TestCase.assertEquals(false, isReachable)
            turnWifiOn()
        } catch (e: NullPointerException) {
            TestCase.fail() // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - trimZeroes
    // ================================================================================
    @Test
    fun testTrimZeroes_ipV6() {
        var ipV6Addr: String
        for (i in IPv6_TRIMMED_VALID_ADDRESS.indices) {
            ipV6Addr = IPv6_TRIMMED_VALID_ADDRESS[i]
            TestCase.assertEquals(ipV6Addr, NetUtils.trimZeroes(IPv6_VALID_ADDRESS[i]))
        }
    }

    @Test
    fun testTrimZeroes_ipV4() {
        var ipV4Addr: String
        for (i in IPv4_TRIMMED_VALID_ADDRESS.indices) {
            ipV4Addr = IPv4_TRIMMED_VALID_ADDRESS[i]
            TestCase.assertEquals(ipV4Addr, NetUtils.trimZeroes(IPv4_VALID_ADDRESS[i]))
        }
    }

    @Test
    fun testTrimZeroes_nullIpAddress() {
        TestCase.assertNotNull(NetUtils.trimZeroes(null))
    }

    // ================================================================================
    // Tests - validateIpAddress
    // ================================================================================
    @Test
    fun testValidateIpAddress_ipV6() {
        var ipV6Addr: String
        for (i in IPv6_TRIMMED_VALID_ADDRESS.indices) {
            ipV6Addr = IPv6_TRIMMED_VALID_ADDRESS[i]
            TestCase.assertEquals(
                ipV6Addr, NetUtils.validateIpAddress(
                    IPv6_VALID_ADDRESS[i]
                )
            )
        }
    }

    @Test
    fun testValidateIpAddress_ipV4() {
        var ipV4Addr: String
        for (i in IPv4_TRIMMED_VALID_ADDRESS.indices) {
            ipV4Addr = IPv4_TRIMMED_VALID_ADDRESS[i]
            TestCase.assertEquals(
                ipV4Addr, NetUtils.validateIpAddress(
                    IPv4_VALID_ADDRESS[i]
                )
            )
        }
    }

    @Test
    fun testValidateIpAddress_nullIpAddress() {
        TestCase.assertNull(NetUtils.validateIpAddress(null))
    }

    @Test
    fun testValidateIpAddress_invalid() {
        for (iPv6_invalid_address in IPv6_INVALID_ADDRESS) {
            TestCase.assertNull(NetUtils.validateIpAddress(iPv6_invalid_address))
        }
    }

    // ================================================================================
    // Private
    // ================================================================================
    private fun turnWifi(enabled: Boolean) {
        try {
            val wifiManager = InstrumentationRegistry.getInstrumentation()
                .targetContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiManager.isWifiEnabled = enabled
        } catch (ignored: Exception) {
        }
    }

    private fun turnWifiOn() {
        try {
            turnWifi(true)
            // Wait for connection to be established
            _signal.await(15, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            TestCase.fail()
        }
    }

    private val _localIpv6Address: String?
        get() {
            try {
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    val intf = en.nextElement()
                    val enumIpAddr = intf.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        val inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && inetAddress is Inet6Address) {
                            return inetAddress.getHostAddress()
                        }
                    }
                }
            } catch (ex: SocketException) {
            }
            return null
        }

    private val IPv4_INVALID_ADDRESS = arrayOf(
        "0.1.2.3.4",
        "x.x.x.x",
        "0.1.2.",
        "0.1.2",
        "0.1.",
        "0.1",
        "0.",
        "0",
        "1000.00001.000002.0000003"
    )
    private val IPv4_VALID_ADDRESS = arrayOf(
        "0.0.0.0",
        "224.0.0.1",
        "192.168.0.1",
        "255.255.255.255",
        "000.000.000.000",
        "0000.00000.000000.0000000",
        "0224.0.0.1",
        "192.00168.0.1",
        "255.255.00255.255",
        "255.255.255.000255"
    )
    private val IPv4_TRIMMED_VALID_ADDRESS = arrayOf(
        "0.0.0.0",
        "224.0.0.1",
        "192.168.0.1",
        "255.255.255.255",
        "0.0.0.0",
        "0.0.0.0",
        "224.0.0.1",
        "192.168.0.1",
        "255.255.255.255",
        "255.255.255.255"
    )
    private val IPv4_MULTICAST_VALID_ADDRESS = arrayOf(
        "224.0.0.1",
        "224.0.0.2",
        "224.0.0.3",
        "224.0.0.4",
        "224.0.0.5",
        "224.0.0.6",
        "224.0.0.9",
        "224.0.0.10",
        "224.0.0.13",
        "224.0.0.18",
        "224.0.0.19",
        "224.0.0.20",
        "224.0.0.21",
        "224.0.0.22",
        "224.0.0.102",
        "224.0.0.107",
        "224.0.0.251",
        "224.0.0.252",
        "224.0.0.253",
        "224.0.1.1",
        "224.0.1.39",
        "224.0.1.40",
        "224.0.1.41",
        "224.0.1.129",
        "224.0.1.130",
        "224.0.1.131",
        "224.0.1.132",
        "239.255.255.250",
        "255.255.255.255"
    )
    private val IPv4_MULTICAST_INVALID_ADDRESS = arrayOf(
        "0.0.0.0",
        "192.168.0.1"
    )
    private val IPv6_VALID_ADDRESS = arrayOf(
        "1:2:3:4:5:6:7:8",
        "1:2:3:4:5:6:7::",
        "1:2:3:4:5:6::8",
        "1:2:3:4:5::7:8",
        "1:2:3:4::6:7:8",
        "1:2:3::5:6:7:8",
        "1:2::4:5:6:7:8",
        "1::3:4:5:6:7:8",
        "::2:3:4:5:6:7:8",
        "1:2:3:4:5::8",
        "1:2:3:4::7:8",
        "1:2:3::6:7:8",
        "1:2::5:6:7:8",
        "1::4:5:6:7:8",
        "1:2:3:4::8",
        "1:2:3::7:8",
        "1:2::6:7:8",
        "1::5:6:7:8",
        "::4:5:6:7:8",
        "1:2:3::8",
        "1:2::7:8",
        "1::6:7:8",
        "1:2::8",
        "1::7:8",
        "1::8",
        "::8",
        "::",
        "a::",
        "A::",
        "::f",
        "::F",
        "00001:000002:0000003:00000004:000000005:0000000006:00000000007:000000000008",
        "00001:2:3:4:5:6:7::",
        "1:000002:3:4:5:6::8",
        "1:2:0000003:4:5::7:8",
        "1:2:3:00000004::6:7:8",
        "1:2:3::000000005:6:7:8",
        "1:2::4:5:0000000006:7:8",
        "1::3:4:5:6:00000000007:8",
        "::2:3:4:5:6:7:000000000008",
        "1:2:3:4:5::000000000009",
        "1:2:3:4::7:00000000000a",
        "1:2:3::6:7:00000000000b",
        "1:2::5:6:7:00000000000c",
        "1::4:5:6:7:00000000000d",
        "1:2:3:4::00000000000e",
        "1:2:3::7:00000000000f",
        "00ff::0192.00168.00001.0000206"
    )
    private val IPv6_TRIMMED_VALID_ADDRESS = arrayOf(
        "1:2:3:4:5:6:7:8",
        "1:2:3:4:5:6:7::",
        "1:2:3:4:5:6::8",
        "1:2:3:4:5::7:8",
        "1:2:3:4::6:7:8",
        "1:2:3::5:6:7:8",
        "1:2::4:5:6:7:8",
        "1::3:4:5:6:7:8",
        "::2:3:4:5:6:7:8",
        "1:2:3:4:5::8",
        "1:2:3:4::7:8",
        "1:2:3::6:7:8",
        "1:2::5:6:7:8",
        "1::4:5:6:7:8",
        "1:2:3:4::8",
        "1:2:3::7:8",
        "1:2::6:7:8",
        "1::5:6:7:8",
        "::4:5:6:7:8",
        "1:2:3::8",
        "1:2::7:8",
        "1::6:7:8",
        "1:2::8",
        "1::7:8",
        "1::8",
        "::8",
        "::",
        "a::",
        "a::",
        "::f",
        "::f",
        "1:2:3:4:5:6:7:8",
        "1:2:3:4:5:6:7::",
        "1:2:3:4:5:6::8",
        "1:2:3:4:5::7:8",
        "1:2:3:4::6:7:8",
        "1:2:3::5:6:7:8",
        "1:2::4:5:6:7:8",
        "1::3:4:5:6:7:8",
        "::2:3:4:5:6:7:8",
        "1:2:3:4:5::9",
        "1:2:3:4::7:a",
        "1:2:3::6:7:b",
        "1:2::5:6:7:c",
        "1::4:5:6:7:d",
        "1:2:3:4::e",
        "1:2:3::7:f",
        "ff::192.168.1.206"
    )
    private val IPv6_INVALID_ADDRESS = arrayOf(
        "z:2:3:4:5:6:7:8",
        "z:2:3:4:5:6:7::",
        "z:2:3:4:5:6::8",
        "z:2:3:4:5::7:8",
        "z:2:3:4::6:7:8",
        "z:2:3::5:6:7:8",
        "z:2::4:5:6:7:8",
        "z::3:4:5:6:7:8",
        "::z:3:4:5:6:7:8",
        "z:2:3:4:5::8",
        "z:2:3:4::7:8",
        "z:2:3::6:7:8",
        "z:2::5:6:7:8",
        "z::4:5:6:7:8",
        "z:2:3:4::8",
        "z:2:3::7:8",
        "z:2::6:7:8",
        "z::5:6:7:8",
        "::z:5:6:7:8",
        "z:2:3::8",
        "z:2::7:8",
        "z::6:7:8",
        "z:2::8",
        "z::7:8",
        "z::8",
        "::z"
    )

    companion object {
        private const val IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.x.x"
        private const val IPV6_OFFLINE_PRINTER_ADDRESS = "2001::4:216:97ff:fe1e:93e4%lo"

        private const val IPV6_STD_OFFLINE_PRINTER_ADDRESS = "fe80::2a0:deff:fe69:7fb3"
        // private const val IPV6_STD_RISO_PRINTER_ADDRESS = "2001::4:225:5cff:fe34:7c27"
    }
}