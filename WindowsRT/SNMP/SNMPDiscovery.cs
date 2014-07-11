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
                requestMIB = new string[]{
                              //SNMPConstants.MIB_GETNEXTOID_4HOLES,//ijHardwareConnectStatus should be supported
                              SNMPConstants.MIB_GETNEXTOID_DESC, //value should be "RISO IS1000C-J" "RISO IS1000C-G" "RISO IS950C-G" to Consider as AZA
                              //SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGFAMILY//value should be 54 (langPDF)
                };
            }
        }

        public void startDiscover()
        {
            snmpDevices.Clear();
            SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1,communityName,SNMPConstants.SNMP_GET_REQUEST,1,requestMIB);
    
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
                     
                    Dictionary<string,string> identifier = values[0];

                    string printerMibOid = identifier[SNMPConstants.KEY_OID];
                    if (printerMibOid != null)
                    {

                        string host = sender.ToString();
                    
                        SNMPDevice snmpDevice = new SNMPDevice(host);

                        if (!FromPrinterSearch) // addition of printer, pass the handlers.
                        {
                            snmpDevice.snmpControllerDeviceCallBack = snmpControllerDiscoverCallback;
                        }
                    
                        snmpDevice.IpAddress = host;
                        snmpDevice.CommunityName = this.communityName;
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
                            if (snmpDevice.isRISOAZADevice())
                            {
                                snmpControllerDiscoverCallback(snmpDevice);
                            }
                        }
                        //call callback

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
