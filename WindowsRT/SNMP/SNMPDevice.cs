using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using Windows.Networking;

namespace SNMP
{
    public class SNMPDevice
    {
        
        public Action<SNMPDevice> snmpControllerDeviceCallBack { get; set; } //PrintersModule
        public Action<SNMPDevice> snmpControllerCallBackGetCapability { get; set; }
        public Action<SNMPDevice> snmpControllerDeviceTimeOut { get; set; }


        private string ipAddress;
        private string sysId;
        private string location;
        private string description;
        private string macAddress;
        private string sysName;
    
        UDPSocket udpSocket;
    
        List<string> tempCapabilities;

        List<string> tempCapabilyLevels;
    
        private string communityName;
    
        Timer receiveTimeoutTimer;

        List<string> MIBList;
        int nextMIBIndex;
        string[] requestMIBs;
        bool DETECTALL = true;

        bool callbackCalled = false;
    
	
        private List<string> capabilitiesList;
        private List<string> capabilityLevelsList;

        public SNMPDevice(string host)
        {
            {
                ipAddress = host;
                capabilitiesList = new List<string>();
                capabilityLevelsList = new List<string>();
                tempCapabilities = new List<string>();
                tempCapabilyLevels = new List<string>();
        
                communityName = null;
                communityName = "public"; //temp communityName
        
                sysId = null;
                description = null;
                location = null;
                macAddress = null;
                sysName = null;


                DETECTALL = true;
                MIBList = new List<string>()
                {
                    SNMPConstants.MIB_GETNEXTOID_BOOKLET,
                    SNMPConstants.MIB_GETNEXTOID_STAPLER,
                    SNMPConstants.MIB_GETNEXTOID_4HOLES,
                    SNMPConstants.MIB_GETNEXTOID_3HOLES,
                    SNMPConstants.MIB_GETNEXTOID_TRAY_FACEDOWN,
                    SNMPConstants.MIB_GETNEXTOID_TRAY_AUTO,
                    SNMPConstants.MIB_GETNEXTOID_TRAY_TOP,
                    SNMPConstants.MIB_GETNEXTOID_TRAY_STACK
                };
                nextMIBIndex = 0;
                requestMIBs = new string[]
                {
                    SNMPConstants.MIB_GETNEXTOID_BOOKLET,
                    SNMPConstants.MIB_GETNEXTOID_STAPLER,
                    SNMPConstants.MIB_GETNEXTOID_4HOLES,
                    SNMPConstants.MIB_GETNEXTOID_3HOLES,
                    SNMPConstants.MIB_GETNEXTOID_TRAY_FACEDOWN,
                    SNMPConstants.MIB_GETNEXTOID_TRAY_AUTO,
                    SNMPConstants.MIB_GETNEXTOID_TRAY_TOP,
                    SNMPConstants.MIB_GETNEXTOID_TRAY_STACK
                };

                
                udpSocket = new UDPSocket();
                udpSocket.assignDelegate(receiveData);

                udpSocket.assignTimeoutDelegate(timeout);
        
            }
        }

        ~SNMPDevice()
        {
            if (receiveTimeoutTimer != null)
            {
                receiveTimeoutTimer.Dispose();
            }
            udpSocket.close();
        }

        public void beginRetrieveCapabilities()
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice Begin Capability Retrieval for ip: ");
            System.Diagnostics.Debug.WriteLine(ipAddress);
            tempCapabilities.Clear();
            tempCapabilyLevels.Clear();
    

            if (this.sysName == null)
                MIBList.Add(SNMPConstants.MIB_GETNEXTOID_NAME);

            if (this.macAddress == null)
                MIBList.Add(SNMPConstants.MIB_GETNEXTOID_MACADDRESS);

            if (this.location == null)
                MIBList.Add(SNMPConstants.MIB_GETNEXTOID_LOC);

            if (this.description == null)
                MIBList.Add(SNMPConstants.MIB_GETNEXTOID_DESC);

            sendData(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT);
        }

        void endRetrieveCapabilitiesSuccess()
        {
            capabilitiesList.Clear();
            capabilityLevelsList.Clear();
            for (int i = 0; i < tempCapabilities.Count(); i++)
            {
                capabilitiesList.Add(tempCapabilities[i]);
                capabilityLevelsList.Add(tempCapabilyLevels[i]);
            }
        
            //callback to SNMPController
            System.Diagnostics.Debug.WriteLine("SNMPDeviice success for ip: ");
            System.Diagnostics.Debug.WriteLine(ipAddress);
           
            if (snmpControllerCallBackGetCapability != null)
            {
                snmpControllerCallBackGetCapability(this);
                callbackCalled = true;
            }
        }

        void endRetrieveCapabilitiesFailed()
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice failed for ip: ");
            System.Diagnostics.Debug.WriteLine(ipAddress);

            if (snmpControllerDeviceCallBack != null)
            {
                snmpControllerDeviceCallBack(this);
            }

            if (snmpControllerCallBackGetCapability != null)
            {
                snmpControllerCallBackGetCapability(this);
                callbackCalled = true;
            }
        }

        //void didNotReceiveData()
        //{
        //}

        private void sendData(byte timeout)
        {
            if (nextMIBIndex < MIBList.Count)
            {
                string[] dataMIB = new string[] { MIBList.ElementAt(nextMIBIndex) };
                SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1, communityName, SNMPConstants.SNMP_GET_NEXT_REQUEST, 1, dataMIB);

                byte[] data = message.generateDataForTransmission();

                udpSocket.sendData(data, ipAddress, SNMPConstants.SNMP_PORT, timeout, 0);
            }
            
        }


        private void receiveData(HostName sender, byte[] responsedata)
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice Receive Data for ip: ");
            System.Diagnostics.Debug.WriteLine(ipAddress);
            SNMPMessage response = new SNMPMessage(responsedata);
    
            if (response != null)
            {
                List<Dictionary<string,string>> values = response.extractOidAndValues();
        
                if (values.Count == 1)
                {
                    Dictionary<string, string> dictionary = values[0];

                    string oid = dictionary[SNMPConstants.KEY_OID];
                    string val = dictionary[SNMPConstants.KEY_VAL];

                    if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_NAME))
                        this.sysName = val;

                    if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_DESC))
                        this.description = val;

                    if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_LOC))
                        this.location = val;

                    if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_MACADDRESS))
                        this.macAddress = val;

                    for (int i = 0; i < requestMIBs.Length; i++)
                    {
                        if (oid.StartsWith(requestMIBs[i]))
                        {
                            tempCapabilities.Add(val);
                            break;
                        }
                    }

                    if (++nextMIBIndex < MIBList.Count)
                        sendData(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT);
                    else
                        endRetrieveCapabilitiesSuccess();
                }
                
            }
            else {
                this.endRetrieveCapabilitiesFailed();
            }
        }

        private void timeout(HostName sender, byte[] responsedata)
        {
            if (udpSocket != null)
            {
                System.Diagnostics.Debug.WriteLine("Closing udpSocket");
                udpSocket.close();

                if (snmpControllerDeviceCallBack != null)
                {
                    //for testing
                    //capabilitiesList.Add("false");
                    //capabilitiesList.Add("false");
                    //capabilitiesList.Add("false");
                    //capabilitiesList.Add("false");
                    //capabilitiesList.Add("false");
                    //capabilitiesList.Add("false");
                    //capabilitiesList.Add("false");
                    //capabilitiesList.Add("false");
                    snmpControllerDeviceCallBack(this);
                } 
                if (nextMIBIndex >= MIBList.Count && !callbackCalled)
                {
                    if (snmpControllerDeviceTimeOut != null)
                        snmpControllerDeviceTimeOut(this);
                }
                else
                {
                    if (!callbackCalled)
                    {
                        nextMIBIndex++;
                        if (nextMIBIndex < MIBList.Count)
                            sendData((byte)(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT/MIBList.Count));
                        else
                            if (snmpControllerDeviceTimeOut != null)
                                snmpControllerDeviceTimeOut(this);
                    }
                }
            }
        }

        public void setIpAddress(string s){
            ipAddress = s;
        }
        
        public void setSysId(string s)
        {
            sysId = s;
        }

        public void setLocation(string s)
        {
            location = s;
        }

        public void setDescription(string s)
        {
            description = s;
        }

        public void setMacAddress(string s)
        {
            macAddress = s;
        }

        public void setSysName(string s)
        {
            sysName = s;
        }

        public void setCommunityName(string s)
        {
            communityName = s;
        }

        public string getIpAddress()
        {
            return ipAddress;
        }

        public string getSysId()
        {
            return sysId;
        }

        public string getLocation()
        {
            return location;
        }

        public string getDescription()
        {
            return description;
        }

        public string getMacAddress()
        {
            return macAddress;
        }

        public string getSysName()
        {
            return sysName;
        }

        public string getCommunityName()
        {
            return communityName;
        }

        public List<string> getCapabilities()
        {
            return capabilitiesList;
        }
    }
}
