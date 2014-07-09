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


        private string _ipAddress;
        private string _langfamily;
        private string _description;
    
        UDPSocket udpSocket;
    
        List<string> tempCapabilities;

        List<string> tempCapabilyLevels;
    
        private string _communityName;
    
        Timer receiveTimeoutTimer;

        List<string> MIBList;
        int nextMIBIndex;
        string[] requestMIBs;

        bool callbackCalled = false;
        bool isSupportedDevice = false;
	
        private List<string> _capabilitiesList;
        private List<string> capabilityLevelsList;

        public SNMPDevice(string host)
        {
            _ipAddress = host;
            _capabilitiesList = new List<string>();
            capabilityLevelsList = new List<string>();
            tempCapabilities = new List<string>();
            tempCapabilyLevels = new List<string>();
        
            _communityName = null;
            _communityName = SNMPConstants.DEFAULT_COMMUNITY_NAME; //temp communityName
        
            _description = null;

            isSupportedDevice = false;


            MIBList = new List<string>()
            {
                SNMPConstants.MIB_GETNEXTOID_BOOKLET,
                SNMPConstants.MIB_GETNEXTOID_STAPLER,
                SNMPConstants.MIB_GETNEXTOID_4HOLES,
                SNMPConstants.MIB_GETNEXTOID_3HOLES,
                SNMPConstants.MIB_GETNEXTOID_TRAY_FACEDOWN,
                SNMPConstants.MIB_GETNEXTOID_TRAY_AUTO,
                SNMPConstants.MIB_GETNEXTOID_TRAY_TOP,
                SNMPConstants.MIB_GETNEXTOID_TRAY_STACK,
                SNMPConstants.MIB_GETNEXTOID_LWPAPER,
                SNMPConstants.MIB_GETNEXTOID_INPUT_TRAY_1,
                SNMPConstants.MIB_GETNEXTOID_INPUT_TRAY_2,
                SNMPConstants.MIB_GETNEXTOID_INPUT_TRAY_3
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
                SNMPConstants.MIB_GETNEXTOID_TRAY_STACK,
                SNMPConstants.MIB_GETNEXTOID_LWPAPER,
                SNMPConstants.MIB_GETNEXTOID_INPUT_TRAY_1,
                SNMPConstants.MIB_GETNEXTOID_INPUT_TRAY_2,
                SNMPConstants.MIB_GETNEXTOID_INPUT_TRAY_3
            };

                
            udpSocket = new UDPSocket();
            udpSocket.assignDelegate(receiveData);

            udpSocket.assignTimeoutDelegate(timeout);
        
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
            System.Diagnostics.Debug.WriteLine(_ipAddress);
            tempCapabilities.Clear();
            tempCapabilyLevels.Clear();
    
            //first, check if device is a supported RISO Printer

            if (this._description == null)
                MIBList.Add(SNMPConstants.MIB_GETNEXTOID_DESC);
            if (this._langfamily == null)
                MIBList.Add(SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGFAMILY);

            sendData(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT);
        }

        void endRetrieveCapabilitiesSuccess()
        {
            _capabilitiesList.Clear();
            capabilityLevelsList.Clear();
            for (int i = 0; i < tempCapabilities.Count(); i++)
            {
                _capabilitiesList.Add(tempCapabilities[i]);
                capabilityLevelsList.Add(tempCapabilyLevels[i]);
            }
        
            //callback to SNMPController
            System.Diagnostics.Debug.WriteLine("SNMPDeviice success for ip: ");
            System.Diagnostics.Debug.WriteLine(_ipAddress);
           
            if (snmpControllerCallBackGetCapability != null)
            {
                snmpControllerCallBackGetCapability(this);
                callbackCalled = true;
            }
        }

        void endRetrieveCapabilitiesFailed()
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice failed for ip: ");
            System.Diagnostics.Debug.WriteLine(_ipAddress);

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
                SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1, _communityName, SNMPConstants.SNMP_GET_NEXT_REQUEST, 1, dataMIB);

                byte[] data = message.generateDataForTransmission();
                try
                {
                    udpSocket.sendData(data, _ipAddress, SNMPConstants.SNMP_PORT, timeout, 0);
                }
                catch (Exception e)
                {
                    if (snmpControllerDeviceTimeOut != null)
                    {
                        snmpControllerDeviceTimeOut(this);
                    }
                }
                
            }
            
        }


        private void receiveData(HostName sender, byte[] responsedata)
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice Receive Data for ip: ");
            System.Diagnostics.Debug.WriteLine(_ipAddress);
            SNMPMessage response = new SNMPMessage(responsedata);
    
            if (response != null)
            {
                List<Dictionary<string,string>> values = response.extractOidAndValues();
        
                if (values.Count == 1)
                {
                    Dictionary<string, string> dictionary = values[0];

                    string oid = dictionary[SNMPConstants.KEY_OID];
                    string val = dictionary[SNMPConstants.KEY_VAL];

                    if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_DESC))
                        this._description = val;
                    if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGFAMILY))
                        this._description = val;

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

        public string IpAddress
        {
            get { return _ipAddress; }
            set { _ipAddress = value;  }
        }

        public string Description
        {
            get { return _description; }
            set { _description = value; }
        }

        public string CommunityName
        {
            get { return _communityName; }
            set { _communityName = value; }
        }

        public List<string> CapabilitiesList
        {
            get { return _capabilitiesList; }
        }
    }
}
