/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * NetUtils.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import jp.co.riso.smartdeviceapp.AppConstants
import java.io.IOException
import java.net.*
import java.util.*
import java.util.regex.Pattern

/**
 * @class NetUtils
 *
 * @brief Utility class for network operations.
 */
object NetUtils {

    private val IPV4_PATTERN: Pattern = initializeIpv4PatternStandard()
    private val IPV4_MULTICAST_PATTERN: Pattern = initializeIpv4PatternMulticast()
    private val IPV6_STD_PATTERN: Pattern = initializeIpv6PatternStandard()
    private val IPV6_HEX_COMPRESSED_PATTERN: Pattern = initializeIpv6PatternCompressed()
    private val IPV6_LINK_LOCAL_PATTERN: Pattern = initializeIpv6PatternLocal()
    private val IPV6_IPv4_DERIVED_PATTERN: Pattern = initializeIpv6PatternIpv4Derived()
    private val IPV6_INTERFACE_NAMES: List<String> = initializeIpv6InterfaceList()

    private val wifiNetworks: MutableList<Network> = ArrayList()
    private var wifiCallback: WifiCallback? = null

    // ================================================================================
    // Public Methods
    // ================================================================================
    /**
     * @brief Validates an IP Address.
     *
     * Checks if the IP Address is a valid IPv4 Address
     *
     * @param ipAddress IP Address
     *
     * @retval true IP Address is a valid IPv4 Address
     * @retval false IP Address is not an IPv4 Address
     */
    @JvmStatic
    fun isIPv4Address(ipAddress: String?): Boolean {
        return if (ipAddress == null) {
            false
        } else IPV4_PATTERN.matcher(ipAddress).matches()
    }

    /**
     * @brief Validates an IP Address.
     *
     * Checks if the IP Address is a valid IPv4 Multicast Address
     *
     * @param ipAddress IP Address
     *
     * @retval true IP Address is a valid IPv4 Multicast Address
     * @retval false IP Address not a valid IPv4 Multicast Address
     */
    @JvmStatic
    fun isIPv4MulticastAddress(ipAddress: String?): Boolean {
        return if (ipAddress == null) {
            false
        } else IPV4_MULTICAST_PATTERN.matcher(ipAddress).matches()
    }

    /**
     * @brief Check an IP Address if it is reachable.
     *
     * Checks if the IPv4 Address is reachable
     *
     * @param ipAddress IPv4 Address
     *
     * @retval true IPv4 Address is reachable
     * @retval false IPv4 address is not reachable
     */
    @JvmStatic
    fun connectToIpv4Address(ipAddress: String?): Boolean {
        if (ipAddress == null) {
            return false
        }
        val sockAddr: SocketAddress = InetSocketAddress(ipAddress, AppConstants.CONST_PORT_HTTP)
        val sock = Socket()
        val isReachable: Boolean = try {
            sock.connect(sockAddr, AppConstants.CONST_TIMEOUT_PING)
            true
        } catch (e: Exception) {
            false
        } finally {
            try {
                sock.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return isReachable
    }

    /**
     * @brief Validates an IP Address.
     *
     * Checks if the IP Address is a valid IPv6 Address
     *
     * @param ipAddress IP Address
     *
     * @retval true IP Address is a valid IPv6 Address
     * @retval false IP Address in not an IPv6 Address
     */
    @JvmStatic
    fun isIPv6Address(ipAddress: String?): Boolean {
        return if (ipAddress == null) {
            false
        } else isIPv6StdAddress(ipAddress) || isIPv6HexCompressedAddress(
            ipAddress
        ) || isIPv6LinkLocalAddress(ipAddress) || isIPv6Ipv4DerivedAddress(ipAddress)
    }

    /**
     * @brief Check an IP Address if it is reachable.
     *
     * Checks if the IPv6 Address is reachable
     *
     * @param ipAddress IPv6 Address
     *
     * @retval true The IPv6 Address is reachable
     * @retval false IPv6 address is not reachable
     */
    @JvmStatic
    fun connectToIpv6Address(ipAddress: String?): Boolean {
        try {
            var inetIpAddress: InetAddress
            var ip = ipAddress
            if (ipAddress != null) {
                if (ipAddress.contains("%")) {
                    val newIpString = ipAddress.split("%").toTypedArray()
                    ip = newIpString[0]
                    if (IPV6_INTERFACE_NAMES.contains(newIpString[1])) {
                        inetIpAddress = InetAddress.getByName(ipAddress)
                        return inetIpAddress.isReachable(AppConstants.CONST_TIMEOUT_PING)
                    }
                }
            }
            inetIpAddress = InetAddress.getByName(ip)
            if (inetIpAddress.isReachable(AppConstants.CONST_TIMEOUT_PING)) {
                return true
            }
            for (i in IPV6_INTERFACE_NAMES.indices) {
                inetIpAddress = InetAddress.getByName(ip + "%" + IPV6_INTERFACE_NAMES[i])
                if (inetIpAddress.isReachable(AppConstants.CONST_TIMEOUT_PING)) {
                    return true
                }
            }
        } catch (e: UnknownHostException) {
            Logger.logWarn(
                NetUtils::class.java,
                "UnknownHostException caused by InetAddress.getByName()"
            )
        } catch (e: IOException) {
            Logger.logWarn(
                NetUtils::class.java,
                "IOException caused by inetIpAddress.isReachable()"
            )
        } catch (e: NullPointerException) {
            Logger.logWarn(
                NetUtils::class.java,
                "NullPointerException caused by ipAddress.contains()"
            )
        }
        return false
    }

    /**
     * @brief Validates an IP Address.
     *
     * @param ipAddress IP Address to be validated
     *
     * @return Valid IP address.
     * @retval null Invalid IP Address.
     */
    @JvmStatic
    fun validateIpAddress(ipAddress: String?): String? {
        var validatedIp: String? = null
        if (isIPv4Address(ipAddress) || isIPv6Address(ipAddress)) {
            validatedIp = trimZeroes(ipAddress)
        }
        return validatedIp
    }
    /**
     * @brief Determines wi-fi connectivity.
     *
     * @retval true Connected to the network using wi-fi
     * @retval false Not connected to the network using wi-fi
     */
    @JvmStatic
    val isWifiAvailable: Boolean
        get() = wifiNetworks.isNotEmpty()

    /**
     * @brief Register callback for monitoring of wifi networks
     *
     * @param context Application context
     */
    @JvmStatic
    fun registerWifiCallback(context: Context?) {
        if (context == null || wifiCallback != null) {
            return
        }
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiReq =
            NetworkRequest.Builder().build()
        wifiCallback = WifiCallback()
        connManager.registerNetworkCallback(wifiReq, wifiCallback!!)
    }

    /**
     * @brief Unregister callback for monitoring of wifi networks
     *
     * @param context Application context
     */
    @JvmStatic
    fun unregisterWifiCallback(context: Context?) {
        if (context == null || wifiCallback == null) {
            return
        }
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connManager.unregisterNetworkCallback(wifiCallback!!)
        wifiCallback = null
        wifiNetworks.clear()
    }

    /**
     * @brief Trim leading zeroes from an IP Address
     *
     * @param ipAddress Input IP Address
     *
     * @return IP Address with leading zeroes trimmed
     * @retval "" IP Address is null
     */
    @JvmStatic
    fun trimZeroes(ipAddress: String?): String {
        if (ipAddress == null) {
            return ""
        }
        var ipv4Addr: String? = null
        var ipAddrBuilder = StringBuilder()
        if (isIPv4Address(ipAddress)) {
            ipv4Addr = ipAddress
        }
        if (isIPv6Address(ipAddress)) {
            val ipv6part = ArrayList(listOf(*ipAddress.split(":").toTypedArray()))
            if (isIPv6Ipv4DerivedAddress(ipAddress)) {
                ipv4Addr = ipv6part[ipv6part.size - 1]
                ipv6part.remove(ipv4Addr)
            }
            for (i in ipv6part.indices) {
                try {
                    ipAddrBuilder.append(Integer.toHexString(ipv6part[i].toInt(16)))
                    if (i < ipv6part.size - 1 || ipv4Addr != null) {
                        ipAddrBuilder.append(':')
                    }
                } catch (e: NumberFormatException) {
                    if (i < ipv6part.size - 1 || ipv4Addr != null) {
                        ipAddrBuilder.append(':')
                    }
                }
            }
        }
        if (ipv4Addr != null) {
            val ipv4part = ipv4Addr.split(".").toTypedArray()
            for (i in ipv4part.indices) {
                ipAddrBuilder.append(ipv4part[i].toInt())
                if (i < ipv4part.size - 1) {
                    ipAddrBuilder.append('.')
                }
            }
        }
        return ipAddrBuilder.toString()
    }
    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Validates an IP Address.
     *
     * Checks if the IP Address is a valid IPv6 Standard Address
     *
     * @param ipAddress IP Address
     *
     * @retval true IP Address is a valid IPv6 Standard Address
     * @retval false IP Address is an invalid IPv6 Standard Address
     */
    private fun isIPv6StdAddress(ipAddress: String): Boolean {
        return IPV6_STD_PATTERN.matcher(ipAddress).matches()
    }

    /**
     * @brief Validates an IP Address.
     *
     * Checks if the IP Address is a valid IPv6 Compressed Address
     *
     * @param ipAddress IP Address
     *
     * @retval true IP Address is a valid IPv6 Compressed Address
     * @retval false IP Address is an invalid IPv6 Compressed Address
     */
    private fun isIPv6HexCompressedAddress(ipAddress: String): Boolean {
        return IPV6_HEX_COMPRESSED_PATTERN.matcher(ipAddress).matches()
    }

    /**
     * @brief Validates an IP Address.
     *
     * Checks if the IP Address is a valid IPv6 Link Local Address
     *
     * @param ipAddress IP Address
     *
     * @retval true IP Address is a valid IPv6 Link Local Address
     * @retval false IP Address is an invalid IPv6 Link Local Address
     */
    private fun isIPv6LinkLocalAddress(ipAddress: String): Boolean {
        return IPV6_LINK_LOCAL_PATTERN.matcher(ipAddress).matches()
    }

    /**
     * @brief Validates an IP Address.
     *
     * Checks if the IP Address is a valid IPv6 Address derived from IPv4 Address
     *
     * @param ipAddress IP Address
     *
     * @retval true IP Address is a valid IPv6 Address derived from IPv4 Address
     * @retval false IP Address is an invalid IPv6 Address derived from IPv4 Address
     */
    private fun isIPv6Ipv4DerivedAddress(ipAddress: String): Boolean {
        return IPV6_IPv4_DERIVED_PATTERN.matcher(ipAddress).matches()
    }

    /**
     * @brief Obtain pattern for standard IPv4 address
     *
     * @return Pattern object for IPv4 Address
     */
    private fun initializeIpv4PatternStandard(): Pattern {
        val ipV4Segment =
            "0*(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.0*(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}"
        return Pattern.compile(ipV4Segment)
    }

    /**
     * @brief Obtain pattern for IPv4 multicast address
     *
     * @return Pattern object for IPv4 Multicast Address
     */
    private fun initializeIpv4PatternMulticast(): Pattern {
        // 224.0.0.0 - 239.255.255.250 Multicast address
        // 224.0.0.0 to 224.0.0.255, 224.0.1.0 to 238.255.255.255, 239.0.0.0 to 239.255.255.255
        // 255.255.255.255 Broadcast address
        return Pattern.compile("(2(?:2[4-9]|3\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d?|0)){3}|(255.){3}255)")
    }

    /**
     * @brief Obtain pattern for standard IPv6 address
     *
     * @return Pattern object for IPv6 Address
     */
    private fun initializeIpv6PatternStandard(): Pattern {
        val ipV6Segment = "0*[0-9a-fA-F]{0,4}"
        return Pattern.compile("(($ipV6Segment:){7,7}$ipV6Segment)") // Pattern # 1
    }

    /**
     * @brief Obtain pattern for compressed IPv6 address
     *
     * @return Pattern object for IPv6 Compressed Address
     */
    private fun initializeIpv6PatternCompressed(): Pattern {
        val ipV6Segment = "0*[0-9a-fA-F]{0,4}"
        return Pattern.compile(
            "((" + ipV6Segment + ":){1,7}:" +  // Pattern # 2
                    "|" + "(" + ipV6Segment + ":){1,6}:" + ipV6Segment +  // Pattern # 3
                    "|" + "(" + ipV6Segment + ":){1,5}(:" + ipV6Segment + "){1,2}" +  // Pattern # 4
                    "|" + "(" + ipV6Segment + ":){1,4}(:" + ipV6Segment + "){1,3}" +  // Pattern # 5
                    "|" + "(" + ipV6Segment + ":){1,3}(:" + ipV6Segment + "){1,4}" +  // Pattern # 6
                    "|" + "(" + ipV6Segment + ":){1,2}(:" + ipV6Segment + "){1,5}" +  // Pattern # 7
                    "|" + ipV6Segment + ":((:" + ipV6Segment + "){1,6})" +  // Pattern # 8
                    "|" + ":((:" + ipV6Segment + "){1,7}|:)" +  // Pattern # 9
                    ")"
        )
    }

    /**
     * @brief Obtain pattern for local IPv6 address
     *
     * @return Pattern object for IPv6 Local Address
     */
    private fun initializeIpv6PatternLocal(): Pattern {
        val ipV6Segment = "0*[0-9a-fA-F]{0,4}"
        return Pattern.compile("([f|F][e|E]80:(:$ipV6Segment){0,4}%[0-9a-zA-Z]{1,})") // Pattern # 10
    }

    /**
     * @brief Obtain pattern for standard IPv6 address
     *
     * @return Pattern object for IPv6 Derived from Ipv4 Address
     */
    private fun initializeIpv6PatternIpv4Derived(): Pattern {
        val ipV4Segment =
            "0*(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.0*(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}"
        val ipV6Segment = "0*[0-9a-fA-F]{0,4}"
        return Pattern.compile(
            "(::0*([f|F]{4}(:0{1,4}){0,1}:){0,1}" + ipV4Segment +  // Pattern # 11
                    "|" + "(" + ipV6Segment + ":){1,4}:" + ipV4Segment + ")"
        ) // Pattern # 12
    }

    /**
     * @brief Obtain Ipv6 Interface list
     *
     * @return IPv6 Interface list
     */
    private fun initializeIpv6InterfaceList(): List<String> {
        val localInterface = "wlan0"
        val list: MutableList<String> = ArrayList()
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (i in interfaces.indices) {
                list.add(interfaces[i].name)
            }
        } catch (e: SocketException) {
            Logger.logWarn(NetUtils::class.java, "SocketException")
            list.add(localInterface)
        }
        if (list.contains(localInterface)) {
            list[0] = localInterface
        }
        return list
    }

    /**
     * @class Inner class for monitoring Wifi networks
     */
    internal class WifiCallback : NetworkCallback() {
        override fun onAvailable(network: Network) {
            wifiNetworks.add(network)
        }

        override fun onLost(network: Network) {
            wifiNetworks.remove(network)
        }

        override fun onUnavailable() {
            wifiNetworks.clear()
        }
    }
}