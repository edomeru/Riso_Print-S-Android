
package jp.co.riso.android.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    };
    private final String[] IPv4_VALID_ADDRESS = {
            "0.0.0.0",
            "224.0.0.1",
            "192.168.0.1",
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
            "::F"
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
    private static final String IPV6_ONLINE_PRINTER_ADDRESS = "fe80::2a0:deff:fe69:7fb2%wlan0";
    private static final String IPV6_OFFLINE_PRINTER_ADDRESS = "2001::4:216:97ff:fe1e:93e4%lo";
    private static final String IPV6_STD_PRINTER_ADDRESS = "fe80::2a0:deff:fe69:7fb2";

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
        boolean isIpV4Address = false;

        for (int i = 0; i < IPv4_VALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4Address(IPv4_VALID_ADDRESS[i]);
            assertEquals(true, isIpV4Address);
        }
    }

    public void testIsIPv4Address_InvalidIpv4Address() {
        boolean isIpV4Address = false;

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
        boolean isIpV4Address = false;

        for (int i = 0; i < IPv4_MULTICAST_VALID_ADDRESS.length; i++) {
            isIpV4Address = NetUtils.isIPv4MulticastAddress(IPv4_MULTICAST_VALID_ADDRESS[i]);
            assertEquals(true, isIpV4Address);
        }
    }

    public void testIsIPv4MulticastAddress_InvalidIpv4MulticastAddress() {
        boolean isIpV4Address = true;

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
        boolean isIpV6Address = false;

        for (int i = 0; i < IPv6_VALID_ADDRESS.length; i++) {
            isIpV6Address = NetUtils.isIPv6Address(IPv6_VALID_ADDRESS[i]);
            assertEquals(true, isIpV6Address);
        }
    }

    public void testIsIPv6Address_InvalidIpv6Address() {
        boolean isIpV6Address = true;

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

    // ================================================================================
    // Tests - connectToIpv6Address
    // ================================================================================

    public void testConnectToIpv6Address_OfflineIpv6Address() {
        try {
            boolean isReachable = true;

            isReachable = NetUtils.connectToIpv6Address(IPV6_OFFLINE_PRINTER_ADDRESS, null);
            assertEquals(false, isReachable);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testConnectToIpv6Address_OnlineIpv6Address() {
        try {
            boolean isReachable = false;

            isReachable = NetUtils.connectToIpv6Address(IPV6_ONLINE_PRINTER_ADDRESS, null);
            assertEquals(true, isReachable);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testConnectToIpv6Address_NullIpAddress() {
        try {
            InetAddress inetIpAddress = InetAddress.getByName(IPV6_STD_PRINTER_ADDRESS);
            NetUtils.connectToIpv6Address(null, inetIpAddress);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        } catch (UnknownHostException e) {
            fail(); // Error should not be thrown
        }
    }

    public void testConnectToIpv6Address_NullInetAddressObject() {
        try {
            NetUtils.connectToIpv6Address(IPV6_ONLINE_PRINTER_ADDRESS, null);
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
        }
    }
    
    public void testConnectToIpv6Address_WifiDisabled() {
        try {
            turnWifi(false);
            boolean isReachable = true;

            isReachable = NetUtils.connectToIpv6Address(IPV6_OFFLINE_PRINTER_ADDRESS, null);
            assertEquals(false, isReachable);
            
            turnWifiOn();
        } catch (NullPointerException e) {
            fail(); // Error should not be thrown
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
            mSignal.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail();
        }
    }    
}
