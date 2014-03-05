using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SNMP
{
    public class SNMPVariable
    {
        byte _type;
        byte[] _data = null;

        public SNMPVariable()
        {

        }

        //+ (id) snmpVariableofType:(uint8_t)type withIntValue:(int)value
        public SNMPVariable(byte type, int value) : this(type)
        {
            //SNMPVariable *snmpVar;
    
            //snmpVar = [[SNMPVariable alloc] initWithType:type withData:[NSData dataWithBytes:&value length:1]];
            byte[] _data = new byte[1];
            _data[0] = (byte)value;
        }

        //+ (id) snmpVariableNilofType:(uint8_t)type
        public SNMPVariable(byte type)
        {
            //SNMPVariable *snmpVar;
    
            //snmpVar = [[SNMPVariable alloc] initWithType:type withData:nil];
            _data = null;
            _type = type;
        }

        //+ (id) snmpVariableofType:(uint8_t)type withStringValue:(NSString *)value
        public SNMPVariable(byte type, string value) : this(type)
        {
            //SNMPVariable *snmpVar;
    
            //snmpVar = [[SNMPVariable alloc] initWithType:type withData:[value dataUsingEncoding:NSUTF8StringEncoding]];
            if (value != null)
            {
                _data = System.Text.Encoding.UTF8.GetBytes(value.ToCharArray());
            }
            else
            {
                _data = null;
            }
        }

        //+ (id) snmpVariableofType:(uint8_t)type withNSData:(NSData *)value
        /*
        public SNMPVariable(byte type, byte[] value) : this(type)
        {
            //SNMPVariable *snmpVar;
    
            //snmpVar = [[SNMPVariable alloc] initWithType:type withData:[NSData dataWithData:value]];
            MemoryStream d = new MemoryStream();
            d.Write(value,0,value.Length);

            _data = d.ToArray();
        }
        */

        //+ (id) snmpVariableofType:(uint8_t)type withSNMPObjects:(id)firstSnmpVar, ...
        public SNMPVariable(byte type, params SNMPVariable[] SNMPvars) : this(type)
        {
            //SNMPVariable *snmpVar;
            //SNMPVariable snmpVar;
    
            //NSMutableData *data = [[NSMutableData alloc] init];
            MemoryStream data = new MemoryStream();

            //id eachObject;
            SNMPVariable eachObject;
            //va_list argumentList;
            //if (firstSnmpVar) // The first argument isn't part of the varargs list,
            if (SNMPvars[0] != null)
            {                                   // so we'll handle it separately.
                //[data appendData:[firstSnmpVar getFormattedData]];
                byte[] formattedData = SNMPvars[0].getFormattedData();
                data.Write(formattedData,0,formattedData.Length);
        
                //va_start(argumentList, firstSnmpVar); // Start scanning for arguments after firstObject.
                //while ((eachObject = va_arg(argumentList, id))) // As many times as we can get an argument of type "id"
                for (int i = 1; i < SNMPvars.Length; i++)
                {
                    if (SNMPvars[i] == null) continue;

                    //[data appendData:[(SNMPVariable *)eachObject getFormattedData]];
                    byte[] appendData = SNMPvars[i].getFormattedData();
                    data.Write(appendData,0,appendData.Length);
                }
                //va_end(argumentList);
            }
    
            //snmpVar = [[SNMPVariable alloc] initWithType:type withData:data];
            _data = data.ToArray();
        }

        //+ (id) snmpVariableofType:(uint8_t)type withArray:(NSArray *)snmpVars
        /*
        public SNMPVariable(byte type, SNMPVariable[] snmpVars) : this(type)
        {
            //SNMPVariable *snmpVar = null;
            //NSMutableData *data = [[NSMutableData alloc] init];
            MemoryStream data = new MemoryStream();

            //for (SNMPVariable *snmpVar in snmpVars)
            foreach (SNMPVariable s in snmpVars)
            {                
                //[data appendData:[snmpVar getFormattedData]];
                byte[] formattedData = s.getFormattedData();
                data.Write(formattedData,0,formattedData.Length);
            }
    
            //snmpVar = [[SNMPVariable alloc] initWithType:type withData:data];
            _data = data.ToArray();
        }
        */

        //- (id) initWithType:(uint8_t)type withData:(NSData *)data
        public SNMPVariable(byte type, byte[] data) : this(type)
        {
            //if (self = [super init])            
            {
                if (data != null)
                {
                    //_data = [NSData dataWithData:data];
                    MemoryStream d = new MemoryStream();
                    d.Write(data, 0, data.Length);
                    _data = d.ToArray();
                }
                else
                {
                    _data = null;
                }
            }

            //return self;
        }

        //- (int) extractSNMPInformation:(NSData *)data
        public int extractSNMPInformation(byte[] data)
        {
           int ret = -1;
    
            //if ([data length] > 0)
            if (data!= null && data.Length > 0)
            {
                int totalLength = 0;
                //uint8_t *bytes = (uint8_t *)[data bytes];
                byte[] bytes = data;

                //uint8_t *p = bytes;
                byte p = 0;
        
                //if ([data length] > 2)
                if (data.Length > 2)
                {
                    //_type = *p;
                    _type = bytes[p];
            
                    p++;
                    totalLength++;
            
                    //uint16_t length = p[0];
                    byte length = bytes[p];
            
                    if (length >= 0x80)
                    {
                        p++;
                        totalLength++;
                        int numberOfBytes = length - 0x80;
                
                        int val = 0;
                        for (int i = numberOfBytes - 1; i >= 0; i--)
                        {
                            int multiplier = 1;
                            for (int j = 0; j < i; j++)
                            {
                                multiplier *= 0x100;
                            }
                    
                            //val += *p * multiplier;
                            val += bytes[p] * multiplier;
                    
                            p++;
                        }
                
                        //length = val;
                        length = (byte)val;
                        totalLength += numberOfBytes;
                    }
                    else
                    {
                        p++;
                        totalLength++;
                    }
            

                    //@try
                    try
                    {
                        //_data = [NSData dataWithBytes:p length:length];
                        MemoryStream dataWithBytes = new MemoryStream();
                        dataWithBytes.Write(bytes, p, length);
                        _data = dataWithBytes.ToArray();
                        totalLength += length;
                    }
                    //@catch (NSException *exception)
                    catch (Exception ex)
                    {
                        //_data = nil;
                        _data = null;
                    }
                    //@finally
                    finally
                    {
                    }

            
                    if (_data != null)
                    {
                        ret = totalLength;
                    }
                }
        
            }
    
            return ret;
        }


        //- (NSData *) getFormattedData
        public byte[] getFormattedData()
        {

            //NSMutableData *mutableData;
            //mutableData = [[NSMutableData alloc] init];
            MemoryStream mutableData = new MemoryStream();
    
            //[mutableData appendBytes:&_type length:1];
            mutableData.WriteByte(_type);
    
            int length = 0;
    
            if (_data != null)
            {
                //length = [_data length];
                length = _data.Length;
            }

            if (length > 0x80)
            {
                int count = 0;
        
                while (length > 0)
                {
                    length /= 0x100;
                    count++;
                }
        
                int lengthFormat = 0x80 + count;
                //[mutableData appendBytes:&lengthFormat length:1];
                mutableData.WriteByte((byte)lengthFormat);
        
                //NSLog(@"length %d", lengthFormat);
        
                //length = [_data length];
                length = _data.Length;
                for (int i = count - 1; i >= 0; i--)
                {
                    int multiplier = 1;
                    for (int j = 0; j < i; j++)
                    {
                        multiplier *= 0x100;
                    }
            
                    int valueToAppend = length / multiplier;
            
                    //[mutableData appendBytes:&valueToAppend length:1];
            
                    length %= multiplier;
                    //NSLog(@"val %d %d", i, valueToAppend);
                }
            }
            else
            {
                //[mutableData appendBytes:&length length:1];
                mutableData.WriteByte((byte)length);
            }
    
            if (_data != null)
            {
                //[mutableData appendData:_data];
                byte[] appendData = _data.ToArray();
                mutableData.Write(appendData,0,appendData.Length);
            }


            return mutableData.ToArray();
        }

        //- (uint8_t) getType
        public byte getType()
        {
            return _type;
        }

        //- (NSData *) getData
        public byte[] getData()
        {
            return _data;
    
        }
    }
}
