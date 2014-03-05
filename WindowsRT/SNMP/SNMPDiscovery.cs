using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Networking;

namespace SNMP
{
    public class SNMPDiscovery
    {
        //GCDAsyncUdpSocket *udpSocket;
        UDPSocket udpSocket;
        //NSString *communityName;
        string communityName;
        //NSTimeInterval *timeOut;
    
        //NSMutableSet *snmpDevices;
        List<SNMPDevice> snmpDevices;
    
        //NSArray *requestMIB;
        string[] requestMIB;

        //__weak id<SNMPDiscoveryDelegate> delegate;

        string broadcastAddress;


        //- (id) initWithDelegate:(id<SNMPDiscoveryDelegate>)toDelegate readCommunityName:(NSString *)readCommunityName
        public SNMPDiscovery()
        {
            //return [self initWithDelegate:toDelegate readCommunityName:readCommunityName broadcastAddress:BROADCAST_ADDRESS];
        }

        //- (id) initWithDelegate:(id<SNMPDiscoveryDelegate>)toDelegate readCommunityName:(NSString *)readCommunityName broadcastAddress:(NSString *)address
        public SNMPDiscovery(string readCommunityName,string address)
        {
            //if (self = [super init])
            {
                //udpSocket = [[GCDAsyncUdpSocket alloc] initWithDelegate:self delegateQueue:dispatch_get_main_queue()];
                //[udpSocket setPreferIPv4];
                udpSocket = new UDPSocket();
                udpSocket.assignDelegate(receiveData);
        
                //NSError *err;
                /*
                if (![udpSocket enableBroadcast:YES error:&err])
                {
                    LOG_PRINTER_SEARCH(@"Error in broadcast: %@", [err description]);
                }
        
                if (![udpSocket bindToPort:0 error:&err])
                {
                    LOG_PRINTER_SEARCH(@"Error in binding: %@", [err description]);
                }
                */
        
                //snmpDevices = [[NSMutableSet alloc] init];
                snmpDevices = new List<SNMPDevice>();
                //self.communityName = readCommunityName;
                communityName = readCommunityName;
                //self.broadcastAddress = [self getValidHostNameFromAddress:address];
                broadcastAddress = address;
                //self.timeOut = nil;
                //timeOut = null;
        
                /*
                requestMIB = [NSArray arrayWithObjects:MIB_RICOH_SYS_PRODUCT_OID,
                              MIB_GETNEXTOID_LOC,
                              MIB_GETNEXTOID_DESC,
                              MIB_GETNEXTOID_MACADDRESS,
                              MIB_GETNEXTOID_PRINTERMIB, 
                              MIB_GETNEXTOID_NAME, nil];
                 */
                requestMIB = new string[]{SNMPConstants.MIB_RICOH_SYS_PRODUCT_OID,
                              SNMPConstants.MIB_GETNEXTOID_LOC,
                              SNMPConstants.MIB_GETNEXTOID_DESC,
                              SNMPConstants.MIB_GETNEXTOID_MACADDRESS,
                              SNMPConstants.MIB_GETNEXTOID_PRINTERMIB, 
                              SNMPConstants.MIB_GETNEXTOID_NAME};
                //requestMIB = new string[] { "1.3.6.1.2.1.1.1.0" };


                //[self setDelegate:toDelegate];
            }
            //return self;
        }

        //- (void) startDiscover
        public void startDiscover()
        {
            //SNMPMessage *message = [[SNMPMessage alloc] initRequestWithVersion:SNMP_V1 withCommunityString:self.communityName withRequestPDUType:SNMP_GET_NEXT_REQUEST withRequestId:1 varbindOids:requestMIB];
            SNMPMessage message = new SNMPMessage(SNMPConstants.SNMP_V1,communityName,SNMPConstants.SNMP_GET_NEXT_REQUEST,1,requestMIB);
    
            //NSData *data = [message generateDataForTransmission];
            byte[] data = message.generateDataForTransmission();
    
            /*
            NSError *err;
            if (![udpSocket beginReceiving:&err])
            {
                LOG_PRINTER_SEARCH(@"%@", [err description]);
            }
            */
    
            //[udpSocket sendData:data toHost:self.broadcastAddress port:SNMP_PORT withTimeout:SNMP_BROADCAST_SEND_TIMEOUT tag:0];
            udpSocket.sendData(data,broadcastAddress,SNMPConstants.SNMP_PORT,SNMPConstants.SNMP_BROADCAST_SEND_TIMEOUT,0);
    
            //LOG_PRINTER_SEARCH(@"SNMP Discovery Started");
        }


        private void receiveData(HostName sender, byte[] responsedata)
        {
            //LOG_PRINTER_SEARCH(@"udpSocketDidReceiveData");
    
            //SNMPMessage *response = [[SNMPMessage alloc] initWithResponse:data];
            SNMPMessage response = new SNMPMessage(responsedata);
    
            if (response != null)
            {
                //NSArray *values = [response extractOidAndValues];
                List<Dictionary<string,string>> values = response.extractOidAndValues();
        
                //sysid, loc and desc, etc
                //if ([values count] == [requestMIB count])
                if (values.Count() == requestMIB.Count())
                {
                    
                    //NSDictionary *sysIdDict = [values objectAtIndex:0];
                    Dictionary<string,string> sysIdDict = values[0];                    
                    //NSDictionary *locDict = [values objectAtIndex:1];
                    Dictionary<string,string> locDict = values[1];   
                    //NSDictionary *descDict = [values objectAtIndex:2];
                    Dictionary<string,string> descDict = values[2];
                    //NSDictionary *macAddressDict = [values objectAtIndex:3];
                    Dictionary<string,string> macAddressDict = values[3];
                    //NSDictionary *printerMibDict = [values objectAtIndex:4];
                    Dictionary<string,string> printerMibDict = values[4];
                    //NSDictionary *sysNameDict = [values objectAtIndex:5];
                    Dictionary<string,string> sysNameDict = values[5];
            
                    //if ([printerMibDict objectForKey:KEY_VAL])
                    string printerMibOid = printerMibDict[SNMPConstants.KEY_OID];
                    if (printerMibOid != null)
                    {
                        //NSString *printerMibOid = [printerMibDict objectForKey:KEY_OID];
                        //if (printerMibOid && [printerMibOid hasPrefix:MIB_GETNEXTOID_PRINTERMIB])
                        if (printerMibOid != null && printerMibOid.StartsWith(SNMPConstants.MIB_GETNEXTOID_PRINTERMIB))
                        { 
                            //NSString *host = [GCDAsyncUdpSocket hostFromAddress:address];
                            string host = sender.ToString();
                    
                            //SNMPDevice *snmpDevice = [[SNMPDevice alloc] initWithHost:host];
                            SNMPDevice snmpDevice = new SNMPDevice(host);
                    
                            //[snmpDevice setIpAddress:[GCDAsyncUdpSocket hostFromAddress:address]];
                            snmpDevice.setIpAddress(host);
                            //[snmpDevice setLocation:[locDict objectForKey:KEY_VAL]];
                            snmpDevice.setLocation(locDict[SNMPConstants.KEY_VAL]);
                            //[snmpDevice setDescription:[descDict objectForKey:KEY_VAL]];
                            snmpDevice.setDescription(descDict[SNMPConstants.KEY_VAL]);
                            //[snmpDevice setMacAddress:[macAddressDict objectForKey:KEY_VAL]];
                            snmpDevice.setMacAddress(macAddressDict[SNMPConstants.KEY_VAL]);
                            //[snmpDevice setSysName:[sysNameDict objectForKey:KEY_VAL]];
                            snmpDevice.setSysName(sysNameDict[SNMPConstants.KEY_VAL]);
                            //[snmpDevice setDelegate:self];
                            //?
                            //[snmpDevice setCommunityName:self.communityName];
                            snmpDevice.setCommunityName(this.communityName);
                    
        // Added checking so that arguments are not evaluated
        /*
        #ifdef LOG_PRINTERSEARCH
                        
                            LOG_PRINTER_SEARCH(@"Received IP Address: %@", [GCDAsyncUdpSocket hostFromAddress:address]);
                            LOG_PRINTER_SEARCH(@"Received SysID OID: %@", [sysIdDict objectForKey:KEY_OID]);
                            LOG_PRINTER_SEARCH(@"Received SysID value: %@", [sysIdDict objectForKey:KEY_VAL]);
        #endif
        */
                    
                            // Returned OID must be the same
                            /*
                            if ([[sysIdDict objectForKey:KEY_OID] hasPrefix:MIB_RICOH_SYS_PRODUCT_OID]) 
                            {
                                [snmpDevice setSysId:[sysIdDict objectForKey:KEY_VAL]];
                            }
                            else 
                            {
                                LOG_PRINTER_SEARCH(@"Setting sysID to nil since received sysID OID is not equal to %@", MIB_RICOH_SYS_PRODUCT_OID);
                                [snmpDevice setSysId:nil];
                            }
                            */
                    
                            //[snmpDevices addObject:snmpDevice];
                            snmpDevices.Add(snmpDevice);
                            //[snmpDevice beginRetrieveCapabilities];
                            snmpDevice.beginRetrieveCapabilities();
                            //LOG_PRINTER_SEARCH(@"Began retrieving capabilities of SNMP Device");
                        }
                    }

                 }
            }
            else
            {
                //NSLog(@"Invalid Response");
            }

            return;

        }
    }
}
