using System;
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
        string communityName;
        List<SNMPDevice> snmpDevices;
    
        string[] requestMIB;

        string broadcastAddress;

        ThreadPoolTimer timer;

        public Action<SNMPDevice> snmpControllerDiscoverCallback { get; set; }
        public Action<string> snmpControllerDiscoverTimeOut { get; set; }

        public bool FromPrinterSearch{ get; set; }

        public List<SNMPDevice> SnmpDevices { 
            get {
                return snmpDevices;
                } 

        }

        public SNMPDiscovery(string readCommunityName,string address)
        {
            {
                udpSocket = new UDPSocket();
                udpSocket.assignDelegate(receiveData);
                udpSocket.assignTimeoutDelegate(timeout);
        
                snmpDevices = new List<SNMPDevice>();
                communityName = readCommunityName;
                broadcastAddress = address;
                requestMIB = new string[]{SNMPConstants.MIB_RICOH_SYS_PRODUCT_OID,
                              SNMPConstants.MIB_GETNEXTOID_LOC,
                              SNMPConstants.MIB_GETNEXTOID_DESC,
                              SNMPConstants.MIB_GETNEXTOID_MACADDRESS,
                              SNMPConstants.MIB_GETNEXTOID_PRINTERMIB, 
                              SNMPConstants.MIB_GETNEXTOID_NAME};
            }
        }

        public void startDiscover()
        {
            snmpDevices.Clear();
            SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1,communityName,SNMPConstants.SNMP_GET_NEXT_REQUEST,1,requestMIB);
    
            byte[] data = message.generateDataForTransmission();
    
            udpSocket.sendData(data,broadcastAddress,SNMPConstants.SNMP_PORT,SNMPConstants.SNMP_BROADCAST_SEND_TIMEOUT,0);
    
        }


        private void receiveData(HostName sender, byte[] responsedata)
        {
            if (sender.ToString() == SNMPConstants.BROADCAST_ADDRESS)
            {
                return;
            }
    
            SNMPMessage response = new SNMPMessage(responsedata);
    
            if (response != null)
            {
                List<Dictionary<string,string>> values = response.extractOidAndValues();
        
                //sysid, loc and desc, etc
                if (values.Count() == requestMIB.Count())
                {
                    
                    Dictionary<string,string> sysIdDict = values[0];       
                    Dictionary<string,string> locDict = values[1];   
                    Dictionary<string,string> descDict = values[2];
                    Dictionary<string,string> macAddressDict = values[3];
                    Dictionary<string,string> printerMibDict = values[4];
                    Dictionary<string,string> sysNameDict = values[5];
            
                    string printerMibOid = printerMibDict[SNMPConstants.KEY_OID];
                    if (printerMibOid != null)
                    {
                        if (printerMibOid.StartsWith(SNMPConstants.MIB_GETNEXTOID_PRINTERMIB))
                        { 
                            string host = sender.ToString();
                    
                            SNMPDevice snmpDevice = new SNMPDevice(host);

                            if (!FromPrinterSearch) // addition of printer, pass the handlers.
                            {
                                snmpDevice.snmpControllerDeviceCallBack = snmpControllerDiscoverCallback;
                            }
                    
                            snmpDevice.IpAddress = host;
                            snmpDevice.Location = locDict[SNMPConstants.KEY_VAL];
                            snmpDevice.Description = descDict[SNMPConstants.KEY_VAL];
                            snmpDevice.MacAddress = macAddressDict[SNMPConstants.KEY_VAL];
                            snmpDevice.SysName = sysNameDict[SNMPConstants.KEY_VAL];
                            snmpDevice.CommunityName = this.communityName;
                    
                            snmpDevices.Add(snmpDevice);

                            snmpDevice.beginRetrieveCapabilities();
                            if (!FromPrinterSearch)
                            {
                                snmpControllerDiscoverTimeOut = null;
                            }
                            else // if printer search
                            {
                                snmpControllerDiscoverCallback(snmpDevice);
                            }
                            //call callback
                        }
                    }

                 }
            }

            return;

        }

        private void timeout(HostName sender, byte[] responsedata)
        {
            if (udpSocket != null)
            {



                System.Diagnostics.Debug.WriteLine("Closing udpSocket");
                udpSocket.close();
                //call callback if timedout during search or add
                
                if (snmpControllerDiscoverTimeOut != null)
                {
                    snmpControllerDiscoverTimeOut(sender.ToString());
                }

            }
        }
    }
}
