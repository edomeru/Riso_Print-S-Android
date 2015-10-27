using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Windows.Networking;

namespace SNMP
{
    public class SNMPDevice
    {
        /// <summary>
        /// Call back when snmp device is done.
        /// </summary>
        public Action<SNMPDevice> snmpControllerDeviceCallBack { get; set; } 
        /// <summary>
        /// Call back when retrieving capabilities is done.
        /// </summary>
        public Action<SNMPDevice> snmpControllerCallBackGetCapability { get; set; }
        /// <summary>
        /// Call back when retrieving capabilities has timed out.
        /// </summary>
        public Action<SNMPDevice> snmpControllerDeviceTimeOut { get; set; }

        public Action snmpControllerErrorCallbBack { get; set; }


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
        bool errorOccurred = false;
        /// <summary>
        /// Flag to check if the current snmp device is supported.
        /// </summary>
        public bool isSupportedDevice = false;
	
        private List<string> _capabilitiesList;
        private List<string> capabilityLevelsList;

        /// <summary>
        /// Constructor for SNMPDevice.
        /// </summary>
        /// <param name="host"></param>
        public SNMPDevice(string host)
        {
            _ipAddress = host;
            _capabilitiesList = new List<string>();
            capabilityLevelsList = new List<string>();
        
            _communityName = null;
            _communityName = SNMPConstants.DEFAULT_COMMUNITY_NAME; //temp communityName
        
            _description = "";

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
            udpSocket.assignErrorDelegate(handleError);

            //udpSocket.assignTimeoutDelegate(timeout); // BTS#14788 - Do not use socket timeout. Use own timer, see startTimer().
        
        }

        private void handleError(HostName sender, byte[] args)
        {
            if (snmpControllerErrorCallbBack != null)
            {
                if (udpSocket != null)
                { 
                    udpSocket.close();
                }
                errorOccurred = true;
                snmpControllerErrorCallbBack();
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

        /// <summary>
        /// Begins retrieving capabilities of the SNMP device.
        /// </summary>
        public void beginRetrieveCapabilities()
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice Begin Capability Retrieval for ip: ");
            System.Diagnostics.Debug.WriteLine(_ipAddress);
    
            //first, check if device is a supported RISO Printer
            RISODeviceMIB.Add(SNMPConstants.MIB_GETNEXTOID_4HOLES);
            RISODeviceMIB.Add(SNMPConstants.MIB_GETNEXTOID_DESC);
            RISODeviceMIB.Add(SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGFAMILY);

            //reset previous capabilities
            _capabilitiesList = new List<string>();
            _capabilitiesList.Clear();
            capabilityCheckStarted = false;
 
            //check if supported printer
            sendData(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT, RISODeviceMIB.ToArray());

            startTimer(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT);
        }

        void endRetrieveCapabilitiesSuccess()
        {        
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

        private void sendData(byte timeout, string[] dataMIB)
        {
            bool bulk = false;
            if (!bulk)
            {
                try
                {
                    for (int i = 0; i < dataMIB.Length; i++)
                    {
                        string[] strdata = { dataMIB[i] };
                        SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1, _communityName, SNMPConstants.SNMP_GET_REQUEST, 1, strdata);

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
                catch (Exception e)
                {
                    return;
                }
            }  else { 
                //TODO: not working
                SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1, _communityName, SNMPConstants.SNMP_GET_REQUEST, 1, dataMIB);

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

        private bool capabilityCheckStarted = false;
        private bool supportsMultiFunctionFinisher = false;
        private void receiveData(HostName sender, byte[] responsedata)
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice Receive Data for ip: ");
            System.Diagnostics.Debug.WriteLine(_ipAddress);
            SNMPMessage response = new SNMPMessage(responsedata);

            if (response != null)
            {
                List<Dictionary<string, string>> values = response.extractOidAndValues();

                //individually send snmp requests
                if (values.Count == 1)
                {
                    try {
                        Dictionary<string, string> dictionary = values[0];
                        string oid = dictionary[SNMPConstants.KEY_OID];
                        string val = dictionary[SNMPConstants.KEY_VAL];

                        if (capabilityCheckStarted)
                        {
                            //check if oid is in capability list
                            if (oid.StartsWith(capabilityMIB[_capabilitiesList.Count()]))
                            {
                                //capability is supported
                                if (val == "1")
                                    this._capabilitiesList.Add("true");
                                else
                                    this._capabilitiesList.Add("false");
                            }
                            else
                            {
                                //capability is not supported
                                this._capabilitiesList.Add("false");
                            }

                            checkNextCapability();
                            //endRetrieveCapabilitiesSuccess();

                        }
                        else
                        {
                            if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_4HOLES))
                            {
                                supportsMultiFunctionFinisher = true;
                            }
                            else
                                if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_DESC))
                                {
                                    this.Description = val;
                                }
                                else
                                    if (oid.StartsWith(SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGFAMILY))
                                    {
                                        this._langfamily = 54;// (byte)val[0];
                                    }

                            if (supportsMultiFunctionFinisher && this._langfamily == 54)
                            {
                                //supported RISO printer
                                //AZA: RISO IS1000C-J, RISO IS1000C-G, or RISO IS950C-G
                                if (isRISOAZADevice())
                                {
                                    isSupportedDevice = true;

                                    //start actual check capabilities from here
                                    _capabilitiesList = new List<string>();
                                    //check the first one
                                    capabilityCheckStarted = true;
                                    sendData(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT, new string[] { capabilityMIB[_capabilitiesList.Count()] });
                                }
                                else
                                {
                                    //endRetrieveCapabilitiesSuccess();
                                }
                            }
                        }
                    } catch (Exception e){
                        return;
                    }
                }

            }

        }

        private void checkNextCapability()
        {

            if (_capabilitiesList.Count() < capabilityMIB.Count())
            {
                //check the next capability
                sendData(SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT, new string[] { capabilityMIB[_capabilitiesList.Count()] });
            }
            else
            {
                //end capability check
                capabilityCheckStarted = false;
                endRetrieveCapabilitiesSuccess();
            }
        }

        /// <summary>
        /// Checks if the device is a RISO AZA device.
        /// </summary>
        /// <returns></returns>
        public bool isRISOAZADevice()
        {
            return this.Description.Equals("RISO IS1000C-J") ||
                   this.Description.Equals("RISO IS1000C-G") ||
                   this.Description.Equals("RISO IS950C-G");
        }

        private void timeoutHandler() //HostName sender, byte[] responsedata)
        {
            if (udpSocket != null)
            {
                System.Diagnostics.Debug.WriteLine("Closing udpSocket");
                udpSocket.close();

                if (snmpControllerDeviceCallBack != null)
                {
                    //for testing
                    snmpControllerDeviceCallBack(this);
                }

                if (capabilityCheckStarted && _capabilitiesList.Count() < capabilityMIB.Count())
                {
                    this._capabilitiesList.Add("false");
                    checkNextCapability();
                }
                else if (snmpControllerDeviceTimeOut != null)
                {
                    snmpControllerDeviceTimeOut(this);
                }
            }
        }

        private async void startTimer(byte timeout)
        {
            await Task.Delay(timeout * 1000);

            if (!callbackCalled && !errorOccurred)
            {
                timeoutHandler();
            }
        }

        /// <summary>
        /// Ip address of the SNMP Device.
        /// </summary>
        public string IpAddress
        {
            get { return _ipAddress; }
            set { _ipAddress = value;  }
        }

        /// <summary>
        /// Description of the SNMP Device.
        /// </summary>
        public string Description
        {
            get { return _description; }
            set { _description = value; }
        }

        /// <summary>
        /// Community name of the SNMP Device.
        /// </summary>
        public string CommunityName
        {
            get { return _communityName; }
            set { _communityName = value; }
        }

        /// <summary>
        /// List of capabilities of the SNMP Device.
        /// </summary>
        public List<string> CapabilitiesList
        {
            get { return _capabilitiesList; }
        }
    }
}
