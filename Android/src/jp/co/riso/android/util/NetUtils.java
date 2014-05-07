/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * NetUtils.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.android.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import jp.co.riso.smartdeviceapp.AppConstants;
import android.util.Log;

public class NetUtils {
    private static final String TAG = "NetUtils";
    private static final Pattern IPV4_PATTERN;
    private static final Pattern IPV4_MULTICAST_PATTERN;
    private static final Pattern IPV6_STD_PATTERN;
    private static final Pattern IPV6_HEX_COMPRESSED_PATTERN;
    private static final Pattern IPV6_LINK_LOCAL_PATTERN;
    private static final Pattern IPV6_IPv4_DERIVED_PATTERN;
    private static final List<String> IPV6_INTERFACE_NAMES;
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv4 Address.
     * 
     * @param ipAddress
     *            IP Address
     */
    public static boolean isIPv4Address(final String ipAddress) {
        return IPV4_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv4 Multicast Address.
     * 
     * @param ipAddress
     *            IP Address
     */
    public static boolean isIPv4MulticastAddress(final String ipAddress) {
        return IPV4_MULTICAST_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv6 Address.
     * 
     * @param ipAddress
     *            IP Address
     */
    public static boolean isIPv6Address(final String ipAddress) {
        return isIPv6StdAddress(ipAddress) || isIPv6HexCompressedAddress(ipAddress) || isIPv6LinkLocalAddress(ipAddress) || isIPv6Ipv4DerivedAddress(ipAddress);
    }
    
    /**
     * Check an IP Address if it is reachable.
     * <p>
     * Checks if the IPv6 Address is reachable.
     * 
     * @param ipAddress
     *            IPv6 Address
     * @param inetIpAddress
     *            IP Address object
     */
    public static boolean connectToIpv6Address(String ipAddress, InetAddress inetIpAddress) {
        try {
            String ip = ipAddress;
            
            if (ipAddress.contains("%")) {
                String[] newIpString = ipAddress.split("%");
                if (newIpString != null) {
                    ip = newIpString[0];
                    if (IPV6_INTERFACE_NAMES.contains(newIpString[1])) {
                        inetIpAddress = InetAddress.getByName(ipAddress);
                        return inetIpAddress.isReachable(AppConstants.CONST_TIMEOUT_PING);
                    }
                }
            }
            for (int i = 0; i < IPV6_INTERFACE_NAMES.size(); i++) {
                inetIpAddress = InetAddress.getByName(ip + "%" + IPV6_INTERFACE_NAMES.get(i));
                if (inetIpAddress.isReachable(AppConstants.CONST_TIMEOUT_PING)) {
                    return true;
                }
            }
        } catch (UnknownHostException e) {
            Log.w(TAG, "UnknownHostException");
        } catch (IOException e) {
            Log.w(TAG, "IOException");
        }
        return false;
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    private static boolean isIPv6StdAddress(final String ipAddress) {
        return IPV6_STD_PATTERN.matcher(ipAddress).matches();
    }
    
    private static boolean isIPv6HexCompressedAddress(final String ipAddress) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(ipAddress).matches();
    }
    
    private static boolean isIPv6LinkLocalAddress(final String ipAddress) {
        return IPV6_LINK_LOCAL_PATTERN.matcher(ipAddress).matches();
    }
    
    private static boolean isIPv6Ipv4DerivedAddress(final String ipAddress) {
        return IPV6_IPv4_DERIVED_PATTERN.matcher(ipAddress).matches();
    }
    
    static {
        String ipV4Segment = "(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
        String ipV6Segment = "[0-9a-fA-F]{1,4}";
        String localInterface = "wlan0";
        
        IPV4_PATTERN = Pattern.compile(ipV4Segment);
        
        // 224.0.0.0 - 239.255.255.250 Multicast address
        // 224.0.0.0 to 224.0.0.255, 224.0.1.0 to 238.255.255.255, 239.0.0.0 to 239.255.255.255
        // 255.255.255.255 Broadcast address
        IPV4_MULTICAST_PATTERN = Pattern.compile("(2(?:2[4-9]|3\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d?|0)){3}|(255.){3}255)");
        
        IPV6_STD_PATTERN = Pattern.compile("((" + ipV6Segment + ":){7,7}" + ipV6Segment + ")"); // Pattern # 1
        IPV6_HEX_COMPRESSED_PATTERN = Pattern.compile("((" + ipV6Segment + ":){1,7}:" + // Pattern # 2
                "|" + "(" + ipV6Segment + ":){1,6}:" + ipV6Segment + // Pattern # 3
                "|" + "(" + ipV6Segment + ":){1,5}(:" + ipV6Segment + "){1,2}" + // Pattern # 4
                "|" + "(" + ipV6Segment + ":){1,4}(:" + ipV6Segment + "){1,3}" + // Pattern # 5
                "|" + "(" + ipV6Segment + ":){1,3}(:" + ipV6Segment + "){1,4}" + // Pattern # 6
                "|" + "(" + ipV6Segment + ":){1,2}(:" + ipV6Segment + "){1,5}" + // Pattern # 7
                "|" + ipV6Segment + ":((:" + ipV6Segment + "){1,6})" + // Pattern # 8
                "|" + ":((:" + ipV6Segment + "){1,7}|:)" + // Pattern # 9
                ")");
        IPV6_LINK_LOCAL_PATTERN = Pattern.compile("(fe80:(:" + ipV6Segment + "){0,4}%[0-9a-zA-Z]{1,})"); // Pattern # 10
        IPV6_IPv4_DERIVED_PATTERN = Pattern.compile("(::(ffff(:0{1,4}){0,1}:){0,1}" + ipV4Segment + // Pattern # 11
                "|" + "(" + ipV6Segment + ":){1,4}:" + ipV4Segment + ")"); // Pattern # 12
        
        IPV6_INTERFACE_NAMES = new ArrayList<String>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (int i = 0; i < interfaces.size(); i++) {
                IPV6_INTERFACE_NAMES.add(interfaces.get(i).getName());
            }
        } catch (SocketException e) {
            Log.w(TAG, "SocketException");
            IPV6_INTERFACE_NAMES.add(localInterface);
        }
        if (IPV6_INTERFACE_NAMES.contains(localInterface)) {
            IPV6_INTERFACE_NAMES.set(0, localInterface);
        }
    }
}
