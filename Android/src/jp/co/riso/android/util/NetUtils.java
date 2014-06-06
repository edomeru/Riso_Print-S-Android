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
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetUtils {
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
     * @return true if the IP Address is a valid IPv4 Address.
     */
    public static boolean isIPv4Address(final String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        return IPV4_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv4 Multicast Address.
     * 
     * @param ipAddress
     *            IP Address
     * @return true if the IP Address is a valid IPv4 Multicast Address.
     */
    public static boolean isIPv4MulticastAddress(final String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        return IPV4_MULTICAST_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv6 Address.
     * 
     * @param ipAddress
     *            IP Address
     * @return true if the IP Address is a valid IPv6 Address.
     */
    public static boolean isIPv6Address(final String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
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
     * @return true if the IPv6 Address is reachable.
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
            Logger.logWarn(NetUtils.class, "UnknownHostException caused by InetAddress.getByName()");
        } catch (IOException e) {
            Logger.logWarn(NetUtils.class, "IOException caused by inetIpAddress.isReachable()");
        } catch (NullPointerException e) {
            Logger.logWarn(NetUtils.class, "NullPointerException caused by ipAddress.contains()");
        }
        return false;
    }
    
    /**
     * Determines network connectivity
     * 
     * @param context
     *
     * @return true if is connected to network
     */
    protected static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }
    
    /**
     * Determines wi-fi connectivity
     * 
     * @param context
     *
     * @return true if is connected to the network using wi-fi
     */
    public static boolean isWifiAvailable(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (wifi != null && wifi.isConnected());
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv6 Standard Address.
     * 
     * @param ipAddress
     *            IP Address
     * @return true if the IP Address is a valid IPv6 Standard Address.
     */
    private static boolean isIPv6StdAddress(final String ipAddress) {
        return IPV6_STD_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv6 Compressed Address.
     * 
     * @param ipAddress
     *            IP Address
     * @return true if the IP Address is a valid IPv6 Compressed Address.
     */
    private static boolean isIPv6HexCompressedAddress(final String ipAddress) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv6 Link Local Address.
     * 
     * @param ipAddress
     *            IP Address
     * @return true if the IP Address is a valid IPv6 Link Local Address.
     */
    private static boolean isIPv6LinkLocalAddress(final String ipAddress) {
        return IPV6_LINK_LOCAL_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * Validates an IP Address.
     * <p>
     * Checks if the IP Address is a valid IPv6 Address derived from IPv4 Address.
     * 
     * @param ipAddress
     *            IP Address
     * @return true if the IP Address is a valid IPv6 Address derived from IPv4 Address.
     */
    private static boolean isIPv6Ipv4DerivedAddress(final String ipAddress) {
        return IPV6_IPv4_DERIVED_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * Obtain pattern for standard IPv4 address
     * 
     * @return Pattern object for IPv4 Address
     */
    private static Pattern initializeIpv4Pattern_Standard() {
        String ipV4Segment = "(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
        return Pattern.compile(ipV4Segment);
    }
    
    /**
     * Obtain pattern for IPv4 multicast address
     * 
     * @return Pattern object for IPv4 Multicast Address
     */
    private static Pattern initializeIpv4Pattern_Multicast() {
        // 224.0.0.0 - 239.255.255.250 Multicast address
        // 224.0.0.0 to 224.0.0.255, 224.0.1.0 to 238.255.255.255, 239.0.0.0 to 239.255.255.255
        // 255.255.255.255 Broadcast address
        return Pattern.compile("(2(?:2[4-9]|3\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d?|0)){3}|(255.){3}255)");
    }
    
    /**
     * Obtain pattern for standard IPv6 address
     * 
     * @return Pattern object for IPv6 Address
     */
    private static Pattern initializeIpv6Pattern_Standard() {
        String ipV6Segment = "[0-9a-fA-F]{1,4}";
        return Pattern.compile("((" + ipV6Segment + ":){7,7}" + ipV6Segment + ")"); // Pattern # 1
    }
    
    /**
     * Obtain pattern for compressed IPv6 address
     * 
     * @return Pattern object for IPv6 Compressed Address
     */
    private static Pattern initializeIpv6Pattern_Compressed() {
        String ipV6Segment = "[0-9a-fA-F]{1,4}";
        return Pattern.compile("((" + ipV6Segment + ":){1,7}:" + // Pattern # 2
                "|" + "(" + ipV6Segment + ":){1,6}:" + ipV6Segment + // Pattern # 3
                "|" + "(" + ipV6Segment + ":){1,5}(:" + ipV6Segment + "){1,2}" + // Pattern # 4
                "|" + "(" + ipV6Segment + ":){1,4}(:" + ipV6Segment + "){1,3}" + // Pattern # 5
                "|" + "(" + ipV6Segment + ":){1,3}(:" + ipV6Segment + "){1,4}" + // Pattern # 6
                "|" + "(" + ipV6Segment + ":){1,2}(:" + ipV6Segment + "){1,5}" + // Pattern # 7
                "|" + ipV6Segment + ":((:" + ipV6Segment + "){1,6})" + // Pattern # 8
                "|" + ":((:" + ipV6Segment + "){1,7}|:)" + // Pattern # 9
                ")");
    }
    
    /**
     * Obtain pattern for local IPv6 address
     * 
     * @return Pattern object for IPv6 Local Address
     */
    private static Pattern initializeIpv6Pattern_Local() {
        String ipV6Segment = "[0-9a-fA-F]{1,4}";
        return Pattern.compile("(fe80:(:" + ipV6Segment + "){0,4}%[0-9a-zA-Z]{1,})"); // Pattern # 10
        
    }
    
    /**
     * Obtain pattern for standard IPv6 address
     * 
     * @return Pattern object for IPv6 Derived from Ipv4 Address
     */
    private static Pattern initializeIpv6Pattern_Ipv4Derived() {
        String ipV4Segment = "(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
        String ipV6Segment = "[0-9a-fA-F]{1,4}";
        
        return Pattern.compile("(::(ffff(:0{1,4}){0,1}:){0,1}" + ipV4Segment + // Pattern # 11
                "|" + "(" + ipV6Segment + ":){1,4}:" + ipV4Segment + ")"); // Pattern # 12
    }
    
    /**
     * Obtain Ipv6 Interface list
     * 
     * @return IPv6 Interface list
     */
    private static List<String> initializeIpv6InterfaceList() {
        String localInterface = "wlan0";
        
        List<String> list = new ArrayList<String>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (int i = 0; i < interfaces.size(); i++) {
                list.add(interfaces.get(i).getName());
            }
        } catch (SocketException e) {
            Logger.logWarn(NetUtils.class, "SocketException");
            
            list.add(localInterface);
        }
        
        if (list.contains(localInterface)) {
            list.set(0, localInterface);
        }
        return list;
    }
    
    static {

        IPV4_PATTERN = initializeIpv4Pattern_Standard();       
        IPV4_MULTICAST_PATTERN = initializeIpv4Pattern_Multicast();

        IPV6_INTERFACE_NAMES = initializeIpv6InterfaceList();

        IPV6_STD_PATTERN = initializeIpv6Pattern_Standard();
        IPV6_HEX_COMPRESSED_PATTERN = initializeIpv6Pattern_Compressed();
        IPV6_LINK_LOCAL_PATTERN = initializeIpv6Pattern_Local();
        IPV6_IPv4_DERIVED_PATTERN = initializeIpv6Pattern_Ipv4Derived();
    }
}
