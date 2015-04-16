//
//  InputValidationUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using Windows.Networking;

namespace SmartDeviceApp.Common.Utilities
{
    public static class InputValidationUtility
    {

        private const string STR_IPV4_LOOPBACK_ADDRESS    = "127.0.0.1";
        private const string STR_IPV6_LOOPBACK_ADDRESS    = "::1";
        private const string STR_IPV6_UNSPECIFIED_ADDRESS = "::";

        /// <summary>
        /// Checks if the given string is a valid IP address.
        /// Loopback and unspecified addresses are treated as invalid.
        /// </summary>
        /// <param name="rawIpAddress">IP address to be checked</param>
        /// <param name="ipAddress">shortened IP address if valid; same as raw IP address otherwise</param>
        /// <returns>true if string is a valid IPv4 or IPv6 address, else false</returns>
        public static bool IsValidIpAddress(string rawIpAddress, out string ipAddress)
        {
            ipAddress = rawIpAddress;

            try
            {
                HostName h = new HostName(rawIpAddress);

                // Valid IPv4 or IPv6
                if (h.Type != HostNameType.Ipv4 && h.Type != HostNameType.Ipv6)
                {
                    return false;
                }

                ipAddress = h.CanonicalName.ToString();

                // Loopback address
                if (ipAddress.Equals(STR_IPV4_LOOPBACK_ADDRESS) ||
                    ipAddress.Equals(STR_IPV6_LOOPBACK_ADDRESS) || ipAddress.Equals(STR_IPV6_UNSPECIFIED_ADDRESS))
                {
                    return false;
                }
            }
            catch (Exception)
            {
                // Invalid IP address
                return false;
            }

            return true;
        }
    }
}
