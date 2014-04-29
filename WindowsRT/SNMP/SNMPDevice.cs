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
        public Action<string, string, bool, List<string>> snmpControllerCallBackGetCapability { get; set; }

        //NSString *ipAddress;
        private string ipAddress;
        //NSString *sysId;
        private string sysId;
        //NSString *location;
        private string location;
        //NSString *description;
        private string description;
        //NSString *macAddress;
        private string macAddress;
        //NSString *sysName;
        private string sysName;
    
        //GCDAsyncUdpSocket* udpSocket;
        UDPSocket udpSocket;
    
        //NSMutableArray* tempCapabilities;
        List<string> tempCapabilities;

        //NSMutableArray* tempCapabilyLevels;
        List<string> tempCapabilyLevels;
    
        //NSString *communityName;
        private string communityName;
            
        //NSTimer *receiveTimeoutTimer;
        Timer receiveTimeoutTimer;

        string[] requestMIBs;
        bool DETECTALL = true;
    
	    //__unsafe_unretained id<SNMPDeviceCapabilityDelegate> delegate;


        //@property (nonatomic, retain) NSString *communityName;

        //@property (nonatomic, unsafe_unretained) id<SNMPDeviceCapabilityDelegate> delegate;
        //@property (nonatomic, retain) NSString *ipAddress;
        //@property (nonatomic, retain) NSString *sysId;
        //@property (nonatomic, retain) NSString *location;
        //@property (nonatomic, retain) NSString *description;
        //@property (nonatomic, retain) NSString *macAddress;
        //@property (nonatomic, retain) NSString *sysName;
        //@property (nonatomic, readonly) NSMutableArray *capabilitiesList;
        private List<string> capabilitiesList;
        //@property (nonatomic, readonly) NSMutableArray *capabilityLevelsList;
        private List<string> capabilityLevelsList;

        ///

        ///


        //- (id) initWithHost:(NSString *)host
        public SNMPDevice(string host)
        {
            //if (self = [super init])
            {
                //ipAddress = [host copy];
                ipAddress = host;
                //capabilitiesList = [[NSMutableArray alloc] init];
                capabilitiesList = new List<string>();
                //capabilityLevelsList = [[NSMutableArray alloc] init];
                capabilityLevelsList = new List<string>();
                //tempCapabilities = [[NSMutableArray alloc] init];
                tempCapabilities = new List<string>();
                //tempCapabilyLevels = [[NSMutableArray alloc] init];
                tempCapabilyLevels = new List<string>();
        
                //[self setCommunityName:nil];
                communityName = null;
                communityName = "public"; //temp communityName
        
                //[self setSysId:nil];
                sysId = null;
                //[self setDescription:nil];
                description = null;
                //[self setLocation:nil];
                location = null;
                //[self setMacAddress:nil];
                macAddress = null;
                //[self setSysName:nil];
                sysName = null;


                DETECTALL = true;

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
        
                //[self setDelegate:nil];
            }
            //return self;
        }

        ~SNMPDevice()
        {
            //if ([receiveTimeoutTimer isValid])
            if (receiveTimeoutTimer != null)
            {
                //[receiveTimeoutTimer invalidate];
                receiveTimeoutTimer.Dispose();
            }
            //[udpSocket pauseReceiving];
            //[udpSocket close];            
            udpSocket.close();
        }

        //- (void) beginRetrieveCapabilities
        public void beginRetrieveCapabilities()
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice Begin Capability Retrieval for ip: ");
            System.Diagnostics.Debug.WriteLine(ipAddress);
            //[tempCapabilities removeAllObjects];
            tempCapabilities.Clear();
            //[tempCapabilyLevels removeAllObjects];
            tempCapabilyLevels.Clear();
    
            //udpSocket = [[GCDAsyncUdpSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
            

            //[udpSocket enableBroadcast:YES error:nil];
    
            //NSError *err;
    
            /*
            if (![udpSocket beginReceiving:&err])
            {
                // NSLog(@"%@", [err description]);
            }
            */
            //[udpSocket beginReceiving:&err];
            //udpSocket.beginReceiving();
    
            //NSArray *varbindOIDs = [NSArray arrayWithObjects:MIB_GETNEXTOID_PRINTERINTERPRETERLANG, MIB_GETNEXTOID_PRINTERINTERPRETERLANGLEVEL, nil];
            string[] varvindOIDs = {SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANG, SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGLEVEL/*, null*/};

            //marjun
            //DETECTALL = true;
            //string[] varbindOIDs =
            //{
            //    SNMPConstants.MIB_GETNEXTOID_SYSID,
            //    (DETECTALL ?  SNMPConstants.MIB_GETNEXTOID_DESC : SNMPConstants.MIB_GETNEXTOID_GENERALNAME),
            //    SNMPConstants.MIB_GETNEXTOID_BOOKLET,
            //    SNMPConstants.MIB_GETNEXTOID_STAPLER,
            //    SNMPConstants.MIB_GETNEXTOID_4HOLES,
            //    SNMPConstants.MIB_GETNEXTOID_3HOLES,
            //    SNMPConstants.MIB_GETNEXTOID_TRAY_FACEDOWN,
            //    SNMPConstants.MIB_GETNEXTOID_TRAY_AUTO,
            //    SNMPConstants.MIB_GETNEXTOID_TRAY_TOP,
            //    SNMPConstants.MIB_GETNEXTOID_TRAY_STACK
            //};
            //SNMPMessage *message = [[SNMPMessage alloc] initRequestWithVersion:SNMP_V1 withCommunityString:self.communityName withRequestPDUType:SNMP_GET_NEXT_REQUEST withRequestId:1 varbindOids:varbindOIDs];
            SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1, communityName, SNMPConstants.SNMP_GET_NEXT_REQUEST, 1, requestMIBs);
    
            //NSData *data = [message generateDataForTransmission];
            byte[] data = message.generateDataForTransmission();
    
            //[udpSocket sendData:data toHost:ipAddress port:SNMP_PORT withTimeout:SNMP_GETCAPABILITY_SEND_TIMEOUT tag:0];
            udpSocket.sendData(data, ipAddress, SNMPConstants.SNMP_PORT, SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT, 0);
        }

        //- (void) endRetrieveCapabilitiesSuccess
        void endRetrieveCapabilitiesSuccess()
        {
            
            //[udpSocket pauseReceiving];
    
            //[capabilitiesList removeAllObjects];
            capabilitiesList.Clear();
            //[capabilityLevelsList removeAllObjects];
            capabilityLevelsList.Clear();
    
            //for (int i = 0; i < [tempCapabilities count]; i++) 
            for (int i = 0; i < tempCapabilities.Count(); i++)
            {
                //[capabilitiesList addObject:[tempCapabilities objectAtIndex:i]];
                capabilitiesList.Add(tempCapabilities[i]);
                //[capabilityLevelsList addObject:[tempCapabilyLevels objectAtIndex:i]];
                capabilityLevelsList.Add(tempCapabilyLevels[i]);
            }
        
            /*
            if (delegate)
            {
                if ([delegate respondsToSelector:@selector(snmpDeviceCapabilityRetrievedSuccess:)])
                {
                    [delegate snmpDeviceCapabilityRetrievedSuccess:self];
                }
            }
    
            [udpSocket close];
            */
            //callback to SNMPController
            System.Diagnostics.Debug.WriteLine("SNMPDeviice success for ip: ");
            System.Diagnostics.Debug.WriteLine(ipAddress);
            if (snmpControllerDeviceCallBack != null)
            {
                snmpControllerDeviceCallBack(this);
            }
                //snmpControllerCallBackGetStatus(ipAddress, true);


            if (snmpControllerCallBackGetCapability != null)
                snmpControllerCallBackGetCapability(ipAddress, sysName, true, capabilitiesList);
        }

        //- (void) endRetrieveCapabilitiesFailed:(NSError *)error
        void endRetrieveCapabilitiesFailed()
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice failed for ip: ");
            System.Diagnostics.Debug.WriteLine(ipAddress);

            if (snmpControllerDeviceCallBack != null)
            {
                snmpControllerDeviceCallBack(this);
            }



            //callback to SNMPController
            //snmpControllerCallBackGetStatus(ipAddress, false);
            /*
            [udpSocket pauseReceiving];
    
            if (delegate)
            {
                if ([delegate respondsToSelector:@selector(snmpDeviceCapabilityRetrievedFailed:withError:)])
                {
                    [delegate snmpDeviceCapabilityRetrievedFailed:self withError:error];
                }
            }
    
            [udpSocket close];
            */
        }

        //- (void) didNotReceiveData
        void didNotReceiveData()
        {
            /*
            if ([tempCapabilities count] > 0)
            {
                //but did not finish
                [self endRetrieveCapabilitiesSuccess];
            }
            else
            {
                [self endRetrieveCapabilitiesFailed:[NSError errorWithDomain:@"Receive Time-out" code:SNMPRequestReceiveTimeOutError userInfo:nil]];
            }
            */
        }


        private void receiveData(HostName sender, byte[] responsedata)
        {
            System.Diagnostics.Debug.WriteLine("SNMPDeviice Receive Data for ip: ");
            System.Diagnostics.Debug.WriteLine(ipAddress);
            //SNMPMessage *response = [[SNMPMessage alloc] initWithResponse:data];
            SNMPMessage response = new SNMPMessage(responsedata);
    
            //if (response)
            if (response != null)
            {
                //NSArray *values = [response extractOidAndValues];
                List<Dictionary<string,string>> values = response.extractOidAndValues();
        
                //only 1 data
                //if ([values count] == 2)
                if (values.Count == 8)
                {
                    //add the requested mibs to temp capabilites.
                    for (int i = 0; i < values.Count; i++)
                    {
                        Dictionary<string, string> dictionary = values[i];

                        string oid = dictionary[SNMPConstants.KEY_OID];
                        string val = dictionary[SNMPConstants.KEY_VAL];

                        if (oid.StartsWith(requestMIBs[i]))
                        {
                            tempCapabilities.Add(val);
                        }
                        i++;
                    }
                
                    this.endRetrieveCapabilitiesSuccess();
                }
            }
            else {
                //[self endRetrieveCapabilitiesFailed:[NSError errorWithDomain:@"Unexpected Result" code:SNMPRequestUnexpectedResponseError userInfo:nil]];
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
