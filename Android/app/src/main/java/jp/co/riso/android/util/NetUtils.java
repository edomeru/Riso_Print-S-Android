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
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import jp.co.riso.smartdeviceapp.AppConstants;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

/**
 * @class NetUtils
 * 
 * @brief Utility class for network operations.
 */
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
     * @brief Validates an IP Address. <br>
     * 
     * Checks if the IP Address is a valid IPv4 Address
     * 
     * @param ipAddress IP Address
     * 
     * @retval true IP Address is a valid IPv4 Address
     * @retval false IP Address is not an IPv4 Address
     */
    public static boolean isIPv4Address(final String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        return IPV4_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * @brief Validates an IP Address. <br>
     * 
     * Checks if the IP Address is a valid IPv4 Multicast Address
     * 
     * @param ipAddress IP Address
     * 
     * @retval true IP Address is a valid IPv4 Multicast Address
     * @retval false IP Address not a valid IPv4 Multicast Address
     */
    public static boolean isIPv4MulticastAddress(final String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        return IPV4_MULTICAST_PATTERN.matcher(ipAddress).matches();
    }

    /**
     * @brief Check an IP Address if it is reachable. <br>
     *
     * Checks if the IPv4 Address is reachable
     *
     * @param ipAddress IPv4 Address
     *
     * @retval true IPv4 Address is reachable
     * @retval false IPv4 address is not reachable
     */
    public static boolean connectToIpv4Address(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }

        SocketAddress sockAddr = new InetSocketAddress(ipAddress, AppConstants.CONST_PORT_HTTP);
        Socket sock = new Socket();
        boolean isReachable;

        try {
            sock.connect(sockAddr, AppConstants.CONST_TIMEOUT_PING);
            isReachable = true;
        } catch (Exception e) {
            isReachable = false;
        } finally {
            try {
                sock.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return isReachable;
        
    }
    
    /**
     * @brief Validates an IP Address. <br>
     * 
     * Checks if the IP Address is a valid IPv6 Address
     * 
     * @param ipAddress IP Address
     * 
     * @retval true IP Address is a valid IPv6 Address
     * @retval false IP Address in not an IPv6 Address
     */
    public static boolean isIPv6Address(final String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        return isIPv6StdAddress(ipAddress) || isIPv6HexCompressedAddress(ipAddress) || isIPv6LinkLocalAddress(ipAddress) || isIPv6Ipv4DerivedAddress(ipAddress);
    }
    
    /**
     * @brief Check an IP Address if it is reachable. <br>
     * 
     * Checks if the IPv6 Address is reachable
     * 
     * @param ipAddress IPv6 Address
     * @param inetIpAddress IP Address object
     * 
     * @retval true The IPv6 Address is reachable
     * @retval false IPv6 address is not reachable
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
            inetIpAddress = InetAddress.getByName(ip);
            if (inetIpAddress.isReachable(AppConstants.CONST_TIMEOUT_PING)) {
                return true;
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
     * @brief Validates an IP Address.
     * 
     * @param ipAddress IP Address to be validated
     * 
     * @return Valid IP address. 
     * @retval null Invalid IP Address.
     */
    public static String validateIpAddress(String ipAddress) {
        String validatedIp = null;
        
        if (NetUtils.isIPv4Address(ipAddress) || NetUtils.isIPv6Address(ipAddress)) {
            validatedIp = NetUtils.trimZeroes(ipAddress);
        }
        return validatedIp;
    }
    
    /**
     * @brief Determines network connectivity.
     * 
     * @param context Application context
     * 
     * @retval true Connected to network
     * @retval false Not connected to network
     */
    protected static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean result = false;

        Network[] networks = connManager.getAllNetworks();

        for (Network network : networks) {
            NetworkCapabilities capabilities = connManager.getNetworkCapabilities(network);
            if (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                result = true;
                break;
            }
        }

        return result;
    }

    /**
     * @brief Determines wi-fi connectivity.
     * 
     * @param context Application context
     * 
     * @retval true Connected to the network using wi-fi
     * @retval false Not connected to the network using wi-fi 
     */
    public static boolean isWifiAvailable(Context context) {
        if (context == null) {
            return false;
        }
        
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean result = false;

        Network[] networks = connManager.getAllNetworks();

        for (Network network : networks) {
            NetworkCapabilities capabilities = connManager.getNetworkCapabilities(network);
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                result = true;
                break;
            }
        }

        return result;
    }
    
    /**
     * @brief Trim leading zeroes from an IP Address
     * 
     * @param ipAddress Input IP Address
     * 
     * @return IP Address with leading zeroes trimmed
     * @retval "" IP Address is null
     */
    public static String trimZeroes(String ipAddress) {
        if (ipAddress == null) {
            return "";
        }
        List<String> ipv6part = null;
        String ipv4Addr = null;
        StringBuilder ipAddrBuilder = null;
        String newIpAddress = null;
        
        if (isIPv4Address(ipAddress)) {
            ipAddrBuilder = new StringBuilder();
            ipv4Addr = ipAddress;
        }
        if (isIPv6Address(ipAddress)) {
            ipAddrBuilder = new StringBuilder();
            ipv6part = new ArrayList<String>(Arrays.asList(ipAddress.split("\\:")));
            if (isIPv6Ipv4DerivedAddress(ipAddress)) {
                ipv4Addr = ipv6part.get(ipv6part.size() - 1);
                ipv6part.remove(ipv4Addr);
            }
            for (int i = 0; i < ipv6part.size(); i++) {
                try {
                    ipAddrBuilder.append(Integer.toHexString(Integer.parseInt(ipv6part.get(i), 16)));
                    if (i < ipv6part.size() - 1 || ipv4Addr != null) {
                        ipAddrBuilder.append(':');
                    }
                } catch (NumberFormatException e) {
                    ipAddrBuilder.append(':');
                }
            }
            if (ipAddress.endsWith("::")) {
                ipAddrBuilder.append("::");
            }
        }
        if (ipv4Addr != null) {
            String[] ipv4part = ipv4Addr.split("\\.");
            for (int i = 0; i < ipv4part.length; i++) {
                ipAddrBuilder.append(Integer.parseInt(ipv4part[i]));
                if (i < ipv4part.length - 1) {
                    ipAddrBuilder.append('.');
                }
            }
        }
        newIpAddress = ipAddrBuilder.toString();
        return newIpAddress;
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    /**
     * @brief Validates an IP Address. <br>
     * 
     * Checks if the IP Address is a valid IPv6 Standard Address
     * 
     * @param ipAddress IP Address
     * 
     * @retval true IP Address is a valid IPv6 Standard Address
     * @retval false IP Address is an invalid IPv6 Standard Address
     */
    private static boolean isIPv6StdAddress(final String ipAddress) {
        return IPV6_STD_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * @brief Validates an IP Address. <br>
     * 
     * Checks if the IP Address is a valid IPv6 Compressed Address
     * 
     * @param ipAddress IP Address
     * 
     * @retval true IP Address is a valid IPv6 Compressed Address
     * @retval false IP Address is an invalid IPv6 Compressed Address
     */
    private static boolean isIPv6HexCompressedAddress(final String ipAddress) {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * @brief Validates an IP Address. <br>
     * 
     * Checks if the IP Address is a valid IPv6 Link Local Address
     * 
     * @param ipAddress IP Address
     * 
     * @retval true IP Address is a valid IPv6 Link Local Address
     * @retval false IP Address is an invalid IPv6 Link Local Address
     */
    private static boolean isIPv6LinkLocalAddress(final String ipAddress) {
        return IPV6_LINK_LOCAL_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * @brief Validates an IP Address. <br>
     * 
     * Checks if the IP Address is a valid IPv6 Address derived from IPv4 Address
     * 
     * @param ipAddress IP Address
     * 
     * @retval true IP Address is a valid IPv6 Address derived from IPv4 Address
     * @retval false IP Address is an invalid IPv6 Address derived from IPv4 Address
     */
    private static boolean isIPv6Ipv4DerivedAddress(final String ipAddress) {
        return IPV6_IPv4_DERIVED_PATTERN.matcher(ipAddress).matches();
    }
    
    /**
     * @brief Obtain pattern for standard IPv4 address
     * 
     * @return Pattern object for IPv4 Address
     */
    private static Pattern initializeIpv4Pattern_Standard() {
        String ipV4Segment = "0*(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.0*(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
        return Pattern.compile(ipV4Segment);
    }
    
    /**
     * @brief Obtain pattern for IPv4 multicast address
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
     * @brief Obtain pattern for standard IPv6 address
     * 
     * @return Pattern object for IPv6 Address
     */
    private static Pattern initializeIpv6Pattern_Standard() {
        String ipV6Segment = "0*[0-9a-fA-F]{0,4}";
        return Pattern.compile("((" + ipV6Segment + ":){7,7}" + ipV6Segment + ")"); // Pattern # 1
    }
    
    /**
     * @brief Obtain pattern for compressed IPv6 address
     * 
     * @return Pattern object for IPv6 Compressed Address
     */
    private static Pattern initializeIpv6Pattern_Compressed() {
        String ipV6Segment = "0*[0-9a-fA-F]{0,4}";
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
     * @brief Obtain pattern for local IPv6 address
     * 
     * @return Pattern object for IPv6 Local Address
     */
    private static Pattern initializeIpv6Pattern_Local() {
        String ipV6Segment = "0*[0-9a-fA-F]{0,4}";
        return Pattern.compile("([f|F][e|E]80:(:" + ipV6Segment + "){0,4}%[0-9a-zA-Z]{1,})"); // Pattern # 10        
    }
    
    /**
     * @brief Obtain pattern for standard IPv6 address
     * 
     * @return Pattern object for IPv6 Derived from Ipv4 Address
     */
    private static Pattern initializeIpv6Pattern_Ipv4Derived() {
        String ipV4Segment = "0*(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.0*(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}";
        String ipV6Segment = "0*[0-9a-fA-F]{0,4}";
        
        return Pattern.compile("(::0*([f|F]{4}(:0{1,4}){0,1}:){0,1}" + ipV4Segment + // Pattern # 11
                "|" + "(" + ipV6Segment + ":){1,4}:" + ipV4Segment + ")"); // Pattern # 12
    }
    
    /**
     * @brief Obtain Ipv6 Interface list
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
