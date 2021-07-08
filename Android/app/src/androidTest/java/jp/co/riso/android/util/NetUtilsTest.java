
package jp.co.riso.android.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.test.ActivityInstrumentationTestCase2;

public class NetUtilsTest extends ActivityInstrumentationTestCase2<MainActivity>  {
    final CountDownLatch mSignal = new CountDownLatch(1);

    private final String[] IPV4_INVALID_ADDRESS = {
            "0.1.2.3.4",
            "x.x.x.x",
            "0.1.2.",
            "0.1.2",
            "0.1.",
            "0.1",
            "0.",
            "0",
            "1000.00001.000002.0000003"
    };
    private final String[] IPv4_VALID_ADDRESS = {
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
    };
    private final String[] IPv4_TRIMMED_VALID_ADDRESS = {
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
    };
    private final String[] IPv4_MULTICAST_VALID_ADDRESS = {
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
    };
    private final String[] IPv4_MULTICAST_INVALID_ADDRESS = {
            "0.0.0.0",
            "192.168.0.1"
    };

    private static final String IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.x.x";

    private final String[] IPv6_VALID_ADDRESS = {
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
    };
    
    private final String[] IPv6_TRIMMED_VALID_ADDRESS = {
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
    };
    
    private final String[] IPv6_INVALID_ADDRESS = {
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
    };
    private static final String IPV6_OFFLINE_PRINTER_ADDRESS = "2001::4:216:97ff:fe1e:93e4%lo";
    private static final String IPV6_STD_PRINTER_ADDRESS = "fe80::2a0:deff:fe69:7fb2";
    private static final String IPV6_STD_OFFLINE_PRINTER_ADDRESS = "fe80::2a0:deff:fe69:7fb3";
    private static final String IPV6_STD_RISO_PRINTER_ADDRESS = "2001::4:225:5cff:fe34:7c27";

    public NetUtilsTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ================================================================================
    // Tests - constructors
    // ================================================================================

    public void testConstructor_WifiOff() {
        turnWifi(false);
        NetUtils netUtils = new NetUtils();
        assertNotNull(netUtils);
        turnWifiOn();
    }

    public void testConstructor_WifiOn() {
        NetUtils netUtils = new NetUtils();
        assertNotNull(netUtils);
    }

    // ================================================================================
    // Tests - isIPv4Address
    // ================================================================================

    public void testIsIPv4Address_ValidIpv4Address() {
        boolean isIpV4Address;

        for (int i = 0; i < IPv4_VALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4Address(IPv4_VALID_ADDRESS[i]);
            assertEquals(true, isIpV4Address);
        }
    }

    public void testIsIPv4Address_InvalidIpv4Address() {
        boolean isIpV4Address;

        for (int i = 0; i < IPV4_INVALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4Address(IPV4_INVALID_ADDRESS[i]);
            assertEquals(false, isIpV4Address);
        }

        for (int i = 0; i < IPv6_VALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4Address(IPv6_VALID_ADDRESS[i]);
            assertEquals(false, isIpV4Address);
        }
    }

    public void testIsIPv4Address_NullIpAddress() {
        try {
            NetUtils.isIPv4Address(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isIPv4MulticastAddress
    // ================================================================================

    public void testIsIPv4MulticastAddress_ValidIpv4MulticastAddress() {
        boolean isIpV4Address;

        for (int i = 0; i < IPv4_MULTICAST_VALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(IPv4_MULTICAST_VALID_ADDRESS[i]);
            assertEquals(true, isIpV4Address);
        }
    }

    public void testIsIPv4MulticastAddress_InvalidIpv4MulticastAddress() {
        boolean isIpV4Address;

        for (int i = 0; i < IPv4_MULTICAST_INVALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(IPv4_MULTICAST_INVALID_ADDRESS[i]);
            assertEquals(false, isIpV4Address);
        }
        for (int i = 0; i < IPV4_INVALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(IPV4_INVALID_ADDRESS[i]);
            assertEquals(false, isIpV4Address);
        }
        for (int i = 0; i < IPv6_VALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(IPv6_VALID_ADDRESS[i]);
            assertEquals(false, isIpV4Address);
        }
    }

    public void testIsIPv4MulticastAddress_NullIpAddress() {
        try {
            NetUtils.isIPv4MulticastAddress(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - isIPv6Address
    // ================================================================================

    public void testIsIPv6Address_ValidIpv6Address() {
        boolean isIpV6Address;

        for (int i = 0; i < IPv6_VALID_ADDRESS.length; i++) {
            isIpV6Address = NetUtils.isIPv6Address(IPv6_VALID_ADDRESS[i]);
            assertEquals(true, isIpV6Address);
        }
    }

    public void testIsIPv6Address_InvalidIpv6Address() {
        boolean isIpV6Address;

        for (int i = 0; i < IPv6_INVALID_ADDRESS.length; i++) {
            isIpV6Address = NetUtils.isIPv6Address(IPv6_INVALID_ADDRESS[i]);
            assertEquals(false, isIpV6Address);
        }

        for (int i = 0; i < IPv4_VALID_ADDRESS.length; i++) {
            isIpV6Address = NetUtils.isIPv6Address(IPv4_VALID_ADDRESS[i]);
            assertEquals(false, isIpV6Address);
        }
    }

    public void testIsIPv6Address_NullIpAddress() {
        try {
            NetUtils.isIPv6Address(null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
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
    
    public void testIsWifiAvailable_Null() {
        assertEquals(false, NetUtils.isWifiAvailable(null));
    }
    
    public void testIsWifiAvailable_WithConnection() {
        // permission CHANGE_WIFI_STATE in app's manifest file must be present
        turnWifiOn();
        // wifi is ON
        assertEquals(true, NetUtils.isWifiAvailable(SmartDeviceApp.getAppContext()));
    }

    // ================================================================================
    // Tests - connectToIpv4Address
    // ================================================================================

    public void testConnectToIpv4Address_OfflineIpv4Address() {
        try {
            boolean isReachable;

            isReachable = NetUtils.connectToIpv4Address(IPV4_OFFLINE_PRINTER_ADDRESS);
            assertEquals(false, isReachable);

            mSignal.await(500, TimeUnit.MILLISECONDS);

        } catch (NullPointerException | InterruptedException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testConnectToIpv4Address_NullIpAddress() {
        try {
            NetUtils.connectToIpv4Address(null);

            mSignal.await(500, TimeUnit.MILLISECONDS);
        } catch (NullPointerException | InterruptedException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testConnectToIpv4Address_WifiDisabled() {
        try {
            turnWifi(false);
            boolean isReachable;

            isReachable = NetUtils.connectToIpv4Address(IPV4_OFFLINE_PRINTER_ADDRESS);
            assertEquals(false, isReachable);

            turnWifiOn();
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    // ================================================================================
    // Tests - connectToIpv6Address
    // ================================================================================

    public void testConnectToIpv6Address_OfflineIpv6Address() {
        try {
            boolean isReachable;
            InetAddress inetIpAddress = InetAddress.getByName(IPV6_STD_PRINTER_ADDRESS);

            isReachable = NetUtils.connectToIpv6Address(IPV6_OFFLINE_PRINTER_ADDRESS, inetIpAddress);
            assertEquals(false, isReachable);
            
            mSignal.await(500, TimeUnit.MILLISECONDS);
            
            isReachable = NetUtils.connectToIpv6Address(IPV6_STD_OFFLINE_PRINTER_ADDRESS + "%wlan0", inetIpAddress);
            assertEquals(false, isReachable);
            
            mSignal.await(500, TimeUnit.MILLISECONDS);
        } catch (NullPointerException | UnknownHostException | InterruptedException e) {
            fail(); // Error should not be thrown
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

    public void testConnectToIpv6Address_NullIpAddress() {
        try {
            InetAddress inetIpAddress = InetAddress.getByName(IPV6_STD_PRINTER_ADDRESS);
            NetUtils.connectToIpv6Address(null, inetIpAddress);
            
            mSignal.await(500, TimeUnit.MILLISECONDS);
        } catch (NullPointerException | InterruptedException | UnknownHostException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testConnectToIpv6Address_NullInetAddressObject() {
        try {
            String ipv6Addr = getLocalIpv6Address();
            assertNotNull(ipv6Addr);
            NetUtils.connectToIpv6Address(ipv6Addr, null);
            
            mSignal.await(500, TimeUnit.MILLISECONDS);
        } catch (NullPointerException | InterruptedException e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testConnectToIpv6Address_WifiDisabled() {
        try {
            turnWifi(false);
            boolean isReachable;

            isReachable = NetUtils.connectToIpv6Address(IPV6_OFFLINE_PRINTER_ADDRESS, null);
            assertEquals(false, isReachable);
            
            turnWifiOn();
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }
    
    // ================================================================================
    // Tests - trimZeroes
    // ================================================================================

    public void testTrimZeroes_ipV6() {
        String ipV6Addr;
        for (int i = 0; i < IPv6_TRIMMED_VALID_ADDRESS.length; i++) {
            ipV6Addr = IPv6_TRIMMED_VALID_ADDRESS[i];

            assertEquals(ipV6Addr, NetUtils.trimZeroes(IPv6_VALID_ADDRESS[i]));
        }
    }

    public void testTrimZeroes_ipV4() {
        String ipV4Addr;
        for (int i = 0; i < IPv4_TRIMMED_VALID_ADDRESS.length; i++) {
            ipV4Addr = IPv4_TRIMMED_VALID_ADDRESS[i];

            assertEquals(ipV4Addr, NetUtils.trimZeroes(IPv4_VALID_ADDRESS[i]));
        }
    }
    
    public void testTrimZeroes_nullIpAddress() {
        assertNotNull(NetUtils.trimZeroes(null));
    }

    // ================================================================================
    // Tests - validateIpAddress
    // ================================================================================

    public void testValidateIpAddress_ipV6() {
        String ipV6Addr;
        for (int i = 0; i < IPv6_TRIMMED_VALID_ADDRESS.length; i++) {
            ipV6Addr = IPv6_TRIMMED_VALID_ADDRESS[i];

            assertEquals(ipV6Addr, NetUtils.validateIpAddress(IPv6_VALID_ADDRESS[i]));
        }
    }

    public void testValidateIpAddress_ipV4() {
        String ipV4Addr;
        for (int i = 0; i < IPv4_TRIMMED_VALID_ADDRESS.length; i++) {
            ipV4Addr = IPv4_TRIMMED_VALID_ADDRESS[i];

            assertEquals(ipV4Addr, NetUtils.validateIpAddress(IPv4_VALID_ADDRESS[i]));
        }
    }

    public void testValidateIpAddress_nullIpAddress() {
        assertNull(NetUtils.validateIpAddress(null));
    }

    public void testValidateIpAddress_invalid() {
        for (int i = 0; i < IPv6_INVALID_ADDRESS.length; i++) {
            assertNull(NetUtils.validateIpAddress(IPv6_INVALID_ADDRESS[i]));
        }
    }
    
    // ================================================================================
    // Private
    // ================================================================================

    private void turnWifi(boolean enabled) {
        try {
            WifiManager wifiManager = (WifiManager) getInstrumentation()
                    .getTargetContext().getSystemService(Context.WIFI_SERVICE);
            wifiManager.setWifiEnabled(enabled);
        } catch (Exception ignored) {
        }
    }
    
    private void turnWifiOn() {
        try {
            turnWifi(true);
            // Wait for connection to be established
            mSignal.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
    }
    
    private String getLocalIpv6Address() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }
}
