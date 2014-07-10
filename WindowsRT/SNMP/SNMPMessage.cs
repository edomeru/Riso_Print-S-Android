using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;

namespace SNMP
{
    public class SNMPMessage
    {
        UInt16 _requestType;
    
        SNMPVariable _version;
        SNMPVariable _community;
    
        SNMPVariable _requestId;
        SNMPVariable _error;
        SNMPVariable _errorIndex;
    
        //NSMutableArray *_varbindSequences;
        List<SNMPVariable> _varbindSequences;

        //- (id) initRequestWithVersion:(uint8_t)version 
        //      withCommunityString:(NSString *)community
        //       withRequestPDUType:(uint16_t)requestType
        //            withRequestId:(uint16_t)requestId
        //              varbindOids:(NSArray *)varbindOids
        public SNMPMessage(byte version,
                    string community,
                    UInt16 requestType,
                    UInt16 requestID,
                    string[] varbindOids)
        {
            //if (self = [super init])
            {
                //_version = [SNMPVariable snmpVariableofType:SNMP_INTEGER withIntValue:version];
                _version = new SNMPVariable(SNMPConstants.SNMP_INTEGER,(int)version);
                //_community = [SNMPVariable snmpVariableofType:SNMP_OCTET_STRING withStringValue:community];
                _community = new SNMPVariable(SNMPConstants.SNMP_OCTET_STRING,community);
                _requestType = requestType;
                //_requestId = [SNMPVariable snmpVariableofType:SNMP_INTEGER withIntValue:requestId];
                _requestId = new SNMPVariable(SNMPConstants.SNMP_INTEGER);
                //_varbindSequences = [[NSMutableArray alloc] init];
                _varbindSequences = new List<SNMPVariable>();
                //for (NSString *str in varbindOids)
                if (varbindOids != null)
                foreach(string str in varbindOids)
                {
                    //SNMPVariable *varOid = [SNMPVariable snmpVariableofType:SNMP_OBJECT_IDENTIFIER withNSData:[self convertOidToNSData:str]];
                    SNMPVariable varOid = new SNMPVariable(SNMPConstants.SNMP_OBJECT_IDENTIFIER, convertOidToNSdata(str));
                    //SNMPVariable *varValue = [SNMPVariable snmpVariableNilofType:SNMP_NULL];
                    SNMPVariable varValue = new SNMPVariable(SNMPConstants.SNMP_NULL);
                    //SNMPVariable *var = [SNMPVariable snmpVariableofType:SNMP_SEQUENCE withSNMPObjects:varOid, varValue, nil];
                    SNMPVariable var = new SNMPVariable(SNMPConstants.SNMP_SEQUENCE, varOid, varValue);
                    //[_varbindSequences addObject:var];
                    _varbindSequences.Add(var);
                }
        
                //_error = [SNMPVariable snmpVariableofType:SNMP_INTEGER withIntValue:version];
                _error = new SNMPVariable(SNMPConstants.SNMP_INTEGER,version);
                //_errorIndex = [SNMPVariable snmpVariableofType:SNMP_INTEGER withIntValue:version];
                _errorIndex = new SNMPVariable(SNMPConstants.SNMP_INTEGER,version);
            }
            //return self;
        }

        //- (id) initWithResponse:(NSData *)data
        public SNMPMessage(byte[] data)
        {
            //if (self = [super init])
            {
                //_varbindSequences = [[NSMutableArray alloc] init];
                _varbindSequences = new List<SNMPVariable>();
        
                //if (![self parseSNMPMessageFromData:data])
                if (!parseSNMPMessageFromData(data))
                {
                    //return nil;
                    //return null;
                }
            }
            //return self;
        }

        //- (NSData *) generateDataForTransmission
        public byte[] generateDataForTransmission()
        {
            //SNMPVariable *snmpMessage;
            SNMPVariable snmpMessage;
            //SNMPVariable *pduRequest;
            SNMPVariable pduRequest;
            //SNMPVariable *varbindList;
            SNMPVariable varbindList;
    
            //varbindList = [SNMPVariable snmpVariableofType:SNMP_SEQUENCE withArray:_varbindSequences];
            varbindList = new SNMPVariable(SNMPConstants.SNMP_SEQUENCE, _varbindSequences.ToArray());
            //pduRequest = [SNMPVariable snmpVariableofType:_requestType withSNMPObjects:_requestId, _error, _errorIndex, varbindList, nil];
            pduRequest = new SNMPVariable((byte)_requestType, _requestId, _error, _errorIndex, varbindList);

            //snmpMessage = [SNMPVariable snmpVariableofType:SNMP_SEQUENCE withSNMPObjects:_version, _community, pduRequest, nil];
            snmpMessage = new SNMPVariable(SNMPConstants.SNMP_SEQUENCE, _version, _community, pduRequest);

            //return [snmpMessage getFormattedData];
            return snmpMessage.getFormattedData();
        }

        //- (BOOL) parseSNMPMessageFromData:(NSData *)data
        public bool parseSNMPMessageFromData(byte[] data)
        {
               
            //BOOL valid = NO;
            bool valid = false;
            
            //SNMPVariable *snmpMessage = [[SNMPVariable alloc] init];
            SNMPVariable snmpMessage = new SNMPVariable();
    
            //int length = [snmpMessage extractSNMPInformation:data];
            int length = snmpMessage.extractSNMPInformation(data);
    
            if (length >= 0)
            {
                //NSArray *snmpSeq = [self extractSNMPSequences:snmpMessage];
                SNMPVariable[] snmpSeq = extractSNMPSequences(snmpMessage);
        
                //if ([snmpSeq count] == 3)
                if (snmpSeq.Length == 3)
                {
                    //_version = [snmpSeq objectAtIndex:0];
                    _version = snmpSeq[0];
                    //_community = [snmpSeq objectAtIndex:1];
                    _community = snmpSeq[1];
                    //SNMPVariable *pduRequest = [snmpSeq objectAtIndex:2];
                    SNMPVariable pduRequest = snmpSeq[2];
            
                    //NSArray *pduSeq = [self extractSNMPSequences:pduRequest];
                    SNMPVariable[] pduSeq = extractSNMPSequences(pduRequest);
            
                    //if ([pduSeq count] == 4) //requestid, error, errorindex, varbindlist
                    if (pduSeq.Length == 4)
                    {
                        //_requestId = [pduSeq objectAtIndex:0];
                        _requestId = pduSeq[0];
                        //_error = [pduSeq objectAtIndex:1];
                        _error = pduSeq[1];
                        //_errorIndex = [pduSeq objectAtIndex:2];
                        _errorIndex = pduSeq[2];
                
                        //SNMPVariable *varbindList = [pduSeq objectAtIndex:3];
                        SNMPVariable varbindList = pduSeq[3];
                
                        //NSArray *varbindSeq = [self extractSNMPSequences:varbindList];
                        SNMPVariable[] varbindSeq = extractSNMPSequences(varbindList);
                
                        //[_varbindSequences addObjectsFromArray:varbindSeq];
                        _varbindSequences.AddRange(varbindSeq);
                
                        //we can check if lahat ng type ay 30
                
                        //valid = YES;
                        valid = true;
                    }
                }
            }

            return valid;
        }

        //- (NSMutableArray *) extractSNMPSequences:(SNMPVariable *)var
        SNMPVariable[] extractSNMPSequences(SNMPVariable var)
        {
            //NSMutableArray *arr = [[NSMutableArray alloc] init];
            List<SNMPVariable> arr = new List<SNMPVariable>();

            //NSData *data = [var getData];
            byte[] data = var.getData();
    
            int counter = 0;
            //char *p = (char *)[data bytes];
            int p = 0;
    
            //while (YES)
            while (true)
            {
                //SNMPVariable *snmpVar = [[SNMPVariable alloc] init];
                SNMPVariable snmpVar = new SNMPVariable();
        
                //int length = [snmpVar extractSNMPInformation:[NSData dataWithBytes:p length:[data length] - counter]];
                MemoryStream d = new MemoryStream(data, p, data.Length - counter);
                int length = snmpVar.extractSNMPInformation(d.ToArray());
        
                if (length >= 0)
                {
                    //[arr addObject:snmpVar];
                    arr.Add(snmpVar);
                    counter += length;
                    p += length;
            
                    //if (counter >= [data length])
                    if (counter >= data.Length)
                    {
                        break;
                    }
                }
                else
                {
                    break;
                }
            }


            return  arr.ToArray();            
        }

        //- (NSMutableArray *) extractOidAndValues
        public List<Dictionary<string,string>> extractOidAndValues()
        {
            //NSMutableArray *arr = [[NSMutableArray alloc] init];
            List<Dictionary<string,string>> arr = new List<Dictionary<string,string>>();
    
            //for (SNMPVariable *snmpVar in _varbindSequences)
            foreach (SNMPVariable snmpVar in _varbindSequences)
            {
                //if ([snmpVar getType] == SNMP_SEQUENCE)
                if (snmpVar.getType() == SNMPConstants.SNMP_SEQUENCE)
                {
                    //NSArray *snmpSeq = [self extractSNMPSequences:snmpVar];
                    SNMPVariable[] snmpSeq = extractSNMPSequences(snmpVar);
            
                    //SNMPVariable *oid = [snmpSeq objectAtIndex:0];
                    SNMPVariable oid = snmpSeq[0];
            
                    //NSMutableDictionary *dictionary = [[NSMutableDictionary alloc] init];
                    Dictionary<string,string> dictionary = new Dictionary<string,string>();
            
                    //[dictionary setObject:[self getStringValue:oid] forKey:KEY_OID];
                    dictionary.Add(SNMPConstants.KEY_OID,getStringValue(oid));
            
                    //if ([snmpSeq count] > 1)
                    if (snmpSeq.Length > 1)
                    {
                        //SNMPVariable *val = [snmpSeq objectAtIndex:1];
                        SNMPVariable val = snmpSeq[1];
                        //[dictionary setObject:[self getStringValue:val] forKey:KEY_VAL];
                        dictionary.Add(SNMPConstants.KEY_VAL,getStringValue(val));
                    }
                    else
                    {
                        dictionary.Add(SNMPConstants.KEY_VAL, "");
                    }
            
                    //[arr addObject:dictionary];
                    arr.Add(dictionary);
                }
            }
    
            return arr;
        }

        //- (NSString *) getStringValue:(SNMPVariable *)snmpVar
        string getStringValue(SNMPVariable snmpVar)
        {
            //NSMutableString *str = [[NSMutableString alloc] init];
            string str = "";
    
            byte[] snmpVarData = snmpVar.getData();

            //switch ([snmpVar getType])
            switch (snmpVar.getType())
            {
                case SNMPConstants.SNMP_OCTET_STRING:
                {                    
                   // NSUInteger length = [[snmpVar getData] length];
                    int length = snmpVarData.Length;
            
                    //uint8_t *p = (uint8_t *)[[snmpVar getData] bytes]; 
            
                    //BOOL isPhysAddress = NO;
                    bool isPhysAddress = false;
            
                    // Check if stringData contains control codes
                    // If yes, treat as PhysAddress type
                    //for (NSUInteger i = 0; i < length; i++) {
                    for (int i = 0; i < length; i++) {
                        if (snmpVarData[i] < 0x20)
                        {
                            //isPhysAddress = YES;
                            isPhysAddress = true;
                            break;
                        }
                    }
            
                    if (!isPhysAddress)
                    {
                        //NSString *octet = [[NSString alloc] initWithData:[snmpVar getData] encoding:NSUTF8StringEncoding];
                        string octet = System.Text.Encoding.UTF8.GetString(snmpVar.getData(),0,snmpVarData.Length);
                        if (octet != null)
                        {
                            //[str appendString:octet];
                            str = str + octet;
                        }
                    }
                    else {
                        // Treat as physical address (mac address). Ex: 00-80-87-81-8D-EB
                        //for (NSUInteger i = 0; i < length; i++)
                        for (int i = 0; i < length; i++)
                        {
                            //[str appendString:[NSString stringWithFormat:@"%02X", p[i]]];
                            str = str + String.Format("{0:X2}",snmpVarData[i]);
                            if (i < length - 1)
                            {
                                //[str appendString:@"-"];
                                str += "-";
                            }
                        }
                    }
            
            
                    break;
                }
                case SNMPConstants.SNMP_INTEGER:
                {
                    //NSUInteger length = [[snmpVar getData] length];
                    int length = snmpVarData.Length;
            
                    //uint8_t *p = (uint8_t *)[[snmpVar getData] bytes]; 
                    
                    //int32_t integerValue = 0x00;
                    uint integerValue =0x00;
            
                    // Check if negative number
                    //if(p[0] & 0x80)
                    if((snmpVarData[0] & 0x80) == 1)
                    {
                        integerValue = 0xFFFFFFFF;
                    }
            
                    //for (NSInteger i = 0; i < length; i++) {
                    for (int i = 0; i < length; i++) {
                        //integerValue = ((integerValue << 8) | p[i]);
                        integerValue = ((integerValue << 8) | snmpVarData[i]);                        
                    }
            
                    //[str appendFormat:@"%d", integerValue];
                    str += String.Format("{0}", integerValue);
                    break;
                }
                case SNMPConstants.SNMP_OBJECT_IDENTIFIER:
                {
                    //[str appendString:[self convertNSDataToOid:[snmpVar getData]]];
                    str += convertNSDataToOid(snmpVarData);
                    break;
                }
            }
    
            return str;
        }

        // returns length of oid in bytes
        //- (NSData *) convertOidToNSData:(NSString *)oid
        public byte[] convertOidToNSdata(string oid)
        {
            //NSMutableData *data = [[NSMutableData alloc] init];
            MemoryStream stream = new MemoryStream();
            using (BinaryWriter data = new BinaryWriter(stream)){    
                //NSArray *elemList = [oid componentsSeparatedByString:SNMP_DOTTERMINATOR_VALUE];
                string[] elemList = oid.Split(SNMPConstants.SNMP_DOTTERMINATOR_VALUE);//Regex.Split(oid, "("+SNMPConstants.SNMP_DOTTERMINATOR_VALUE+")");
    
                //int firstElement = [[elemList objectAtIndex:0] intValue];
                int firstElement = int.Parse(elemList[0]);
    
                //int firstByte = 40 * firstElement + [[elemList objectAtIndex:1] intValue];
                int firstByte = 40 * firstElement + int.Parse(elemList[1]);
    
                //[data appendBytes:&firstByte length:1];
                data.Write((byte)firstByte);
    
                //for (int i = 2; i < [elemList count]; i++)
                for (int i = 2; i < elemList.Length; i++)
                {
                    //int value = [[elemList objectAtIndex:i] intValue];
                    int value = int.Parse(elemList[i]);
        
                    byte[] tempbyte = BEREncode(value);
                    data.Write(tempbyte);

                }
                data.Flush();
            }
    
	        //return data;
            return stream.ToArray();
        }

        private byte[] BEREncode(int value)
        {
            List<byte> buffer = new List<byte>();
            while (value != 0)
            {
                byte[] b = BitConverter.GetBytes(value);
                // set high bit on each byte we are processing
                if ((b[0] & 0x80) == 0) b[0] |= 0x80;
                buffer.Insert(0, b[0]);
                value >>= 7; // shift value by 7 bits to the right
            }
            // Almost done. Clear high bit in the last byte
            buffer[buffer.Count - 1] = (byte)(buffer[buffer.Count - 1] & ~0x80);
            byte[] result = buffer.ToArray();

            return result;
        }

        //- (NSString *) convertNSDataToOid:(NSData *)data
        string convertNSDataToOid(byte[] data)
        {
            //NSMutableString *oid = [[NSMutableString alloc] init];
            string oid = "";

            //char *bytes = (char *) [data bytes];
            byte[] bytes = data;
            long val = 0;
    
            //for (int i = 0; i < [data length]; i++)
            for (int i = 0; i < data.Length; i++)
            {
                if (i == 0)
                {
                    //uint8_t b = bytes[0] % 40;
                    byte b = (byte)(bytes[0] % 40);
                    //uint8_t a = (bytes[0] - b) / 40;
                    byte a = (byte)((bytes[0] - b) / 40);
                    //[oid appendFormat:@"%d.%d", a, b];
                    oid += String.Format("{0}.{1}", a, b);
                }
                else
                {
            
                    //val = (val << 7) | ((uint8_t)(bytes[i] & 0x7f));
                    val = (val << 7) | ((byte)(bytes[i] & 0x7f));
            
                    if (!((bytes[i] & 0x80) == 0x80)) 
                    {
                        //[oid appendFormat:@".%d", val];
                        oid += String.Format(".{0}", val);
                        val = 0;
                    }
            
            
                }
            }
    
            return oid;
        }

    }
}
