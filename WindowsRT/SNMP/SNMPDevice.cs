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
        private int _langfamily;
        private string _description;
    
        UDPSocket udpSocket;
    
        private string _communityName;
    
        Timer receiveTimeoutTimer;

        List<string> RISODeviceMIB;//MIB for confirming supported devices
        int nextMIBIndex;
        string[] capabilityMIB;//MIB for device capabilities

        bool callbackCalled = false;
        bool isSupportedDevice = false;
	
        private List<string> _capabilitiesList;
        private List<string> capabilityLevelsList;

        public SNMPDevice(string host)
        {
            _ipAddress = host;
            _capabilitiesList = new List<string>();
            capabilityLevelsList = new List<string>();
        
            _communityName = null;
            _communityName = SNMPConstants.DEFAULT_COMMUNITY_NAME; //temp communityName
        
            _description = null;

            isSupportedDevice = false;


            RISODeviceMIB = new List<string>(){};
            nextMIBIndex = 0;
            capabilityMIB = new string[]
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
    
            //first, check if device is a supported RISO Printer
            RISODeviceMIB.Add(SNMPConstants.MIB_GETNEXTOID_4HOLES);
            RISODeviceMIB.Add(SNMPConstants.MIB_GETNEXTOID_DESC);
            RISODeviceMIB.Add(SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGFAMILY);

            //check if supported printer
            sendData(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT, RISODeviceMIB.ToArray());
        }

        void endRetrieveCapabilitiesSuccess()
        {
            _capabilitiesList.Clear();
            capabilityLevelsList.Clear();
        
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

        private void sendData(byte timeout, string[] dataMIB)
        {
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


        private void receiveData(HostName sender, byte[] responsedata)
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice Receive Data for ip: ");
            System.Diagnostics.Debug.WriteLine(_ipAddress);
            SNMPMessage response = new SNMPMessage(responsedata);
    
            if (response != null)
            {
                List<Dictionary<string,string>> values = response.extractOidAndValues();
                bool supportsMultiFunctionFinisher = false;

                //expect 3 values for confirm mib
                if (values.Count == RISODeviceMIB.Count())//
                {
                    for (int i = 0; i < RISODeviceMIB.Count(); i++)
                    {
                        Dictionary<string, string> dictionary = values[i];
                        string oid = dictionary[SNMPConstants.KEY_OID];
                        string val = dictionary[SNMPConstants.KEY_VAL];

                        if (i == 0)
                            supportsMultiFunctionFinisher = true;
                        //if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_DESC))
                        if (i == 1)
                            this._description = val;
                        //if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGFAMILY))
                        if (i == 2)
                            this._langfamily = (byte)val[0];
                    }

                    //verify RISO device
                    //if (supportsMultiFunctionFinisher && this._langfamily == 54)
                    {
                        //desc value should be "RISO IS1000C-J" "RISO IS1000C-G" "RISO IS950C-G" to Consider as AZA
                        if (this._description == "RISO IS1000C-J" || 
                            this._description == "RISO IS1000C-JG" ||
                            this._description == "RISO IS950C-G")
                        {
                            //AZA PRINTER
                        }
                        else
                        {
                            //DIO PRINTER
                        }
                        isSupportedDevice = true;

                        //from here, confirm capabilities
                        sendData(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT, capabilityMIB.ToArray());
                    }
                }
                else if (values.Count == capabilityMIB.Count()) //retrieve capabilities
                {



                    endRetrieveCapabilitiesSuccess();
                }
                else
                {
                    endRetrieveCapabilitiesFailed();
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
                if (nextMIBIndex >= RISODeviceMIB.Count && !callbackCalled)
                {
                    if (snmpControllerDeviceTimeOut != null)
                        snmpControllerDeviceTimeOut(this);
                }
                else
                {
                    /*
                    if (!callbackCalled)
                    {
                        nextMIBIndex++;
                        if (nextMIBIndex < RISODeviceMIB.Count)
                            sendData((byte)(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT/RISODeviceMIB.Count));
                        else
                            if (snmpControllerDeviceTimeOut != null)
                                snmpControllerDeviceTimeOut(this);
                    }
                     * */
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
