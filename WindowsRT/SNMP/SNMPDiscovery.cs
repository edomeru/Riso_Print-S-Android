﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking;
using Windows.System.Threading;

namespace SNMP
{
    public class SNMPDiscovery
    {
        UDPSocket udpSocket;
        private string _communityName;
        List<SNMPDevice> snmpDevices;
    
        string[] requestMIB;

        string broadcastAddress;

        ThreadPoolTimer timer;

        /// <summary>
        /// Call back when the discovery is done.
        /// </summary>
        public Action<SNMPDevice> snmpControllerDiscoverCallback { get; set; }
        /// <summary>
        /// Call back when the discovery has timed out.
        /// </summary>
        public Action<string> snmpControllerDiscoverTimeOut { get; set; }

        /// <summary>
        /// Flag to check if discovery is from printer search.
        /// </summary>
        public bool FromPrinterSearch{ get; set; }

        /// <summary>
        /// List of SNMP Devices discovered.
        /// </summary>
        public List<SNMPDevice> SnmpDevices
        {
            get {
                return snmpDevices;
                } 

        }

        public string SnmpCommunityName
        {
            get { return _communityName; }
            set { _communityName = value; }
        }

        /// <summary>
        /// Constructor for SNMPDiscovery
        /// </summary>
        /// <param name="readCommunityName">community name</param>
        /// <param name="address">broadcast address</param>
        public SNMPDiscovery(string readCommunityName,string address)
        {
            {
                udpSocket = new UDPSocket();
                udpSocket.assignDelegate(receiveData);
        
                snmpDevices = new List<SNMPDevice>();
                _communityName = readCommunityName;
                broadcastAddress = address;
                requestMIB = new string[]{
                              //SNMPConstants.MIB_GETNEXTOID_4HOLES,//ijHardwareConnectStatus should be supported
                              SNMPConstants.MIB_GETNEXTOID_DESC, //value should be "RISO IS1000C-J" "RISO IS1000C-G" "RISO IS950C-G" to Consider as AZA
                              //SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGFAMILY//value should be 54 (langPDF)
                };
            }
        }

        /// <summary>
        /// Starts discovering SNMP devices in the network.
        /// </summary>
        public void startDiscover()
        {
            snmpDevices.Clear();
            SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1, SnmpCommunityName, SNMPConstants.SNMP_GET_REQUEST, 1, requestMIB);
    
            byte[] data = message.generateDataForTransmission();
    
            udpSocket.sendData(data,broadcastAddress,SNMPConstants.SNMP_PORT,SNMPConstants.SNMP_BROADCAST_SEND_TIMEOUT,0);

            startDiscoveryTimer(SNMPConstants.SNMP_BROADCAST_SEND_TIMEOUT);
        }


        private void receiveData(HostName sender, byte[] responsedata)
        {
            if (sender.ToString() == SNMPConstants.BROADCAST_ADDRESS)
            {
                return;
            }

            try { 
                SNMPMessage response = new SNMPMessage(responsedata);

                if (response != null)
                {
                    List<Dictionary<string, string>> values = response.extractOidAndValues();

                    //sysid, loc and desc, etc
                    if (values.Count() == requestMIB.Count())
                    {

                        Dictionary<string, string> identifier = values[0];

                        if (isSupportedDevice(identifier[SNMPConstants.KEY_OID], identifier[SNMPConstants.KEY_VAL]))
                        {
                            string host = sender.ToString();

                            SNMPDevice snmpDevice = new SNMPDevice(host);

                            if (!FromPrinterSearch) // addition of printer, pass the handlers.
                            {
                                snmpDevice.snmpControllerDeviceCallBack = snmpControllerDiscoverCallback;
                            }

                            snmpDevice.IpAddress = host;
                            snmpDevice.CommunityName = _communityName;
                            snmpDevice.Description = identifier[SNMPConstants.KEY_VAL];

                            snmpDevices.Add(snmpDevice);
                            snmpDevice.beginRetrieveCapabilities();
                            //snmpControllerDiscoverCallback(snmpDevice);

                            if (!FromPrinterSearch)
                            {
                                snmpControllerDiscoverTimeOut = null;
                            }
                            else // if printer search
                            {
#if !DEBUG
                                if (snmpDevice.isRISODevice())//comment out to remove RISO filter
#endif
                                {
                                    snmpControllerDiscoverCallback(snmpDevice);

                                }
                            }
                            //call callback

                        }

                    }
                }
            }
            catch (Exception e)
            {
                return;
            }

            return;

        }

        private async void startDiscoveryTimer(byte timeout)
        {
            await Task.Delay(timeout * 1000);
            {
                udpSocket.close();
                if (snmpControllerDiscoverTimeOut != null)
                {
                    snmpControllerDiscoverTimeOut("255.255.255.255");
                }
            }
        }

        /// <summary>
        /// Checks if the device is a supported RISO device.
        /// </summary>
        /// <returns>true if supported, false otherwise</returns>
        private bool isSupportedDevice(string printerOID, string printerDesc)
        {
            if (printerOID != null && printerDesc != null && printerOID.StartsWith(SNMPConstants.MIB_GETNEXTOID_DESC))
            {
                return printerDesc.Equals("RISO IS1000C-J") ||
                   printerDesc.Equals("RISO IS1000C-G") ||
                   printerDesc.Equals("RISO IS950C-G") ||
                   printerDesc.Contains("FW") ||
                   printerDesc.Contains("GD");
            }
            return false;
        }
    }
}
