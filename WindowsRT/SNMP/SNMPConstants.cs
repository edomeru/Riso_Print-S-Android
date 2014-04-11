﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;


namespace SNMP
{
    public class SNMPConstants
    {
        public const byte SNMP_PORT   = 161;

        public const byte SNMP_V1 = 0x00;
        public const byte SNMP_V2 = 0x01;


        public const byte SNMP_INTEGER                = 0x02;
        public const byte SNMP_BIT_STRING             = 0x03;
        public const byte SNMP_OCTET_STRING           = 0x04;
        public const byte SNMP_NULL                   = 0x05;
        public const byte SNMP_OBJECT_IDENTIFIER      = 0x06;

        public const byte SNMP_SEQUENCE = 0x30;

        public const byte SNMP_GET_REQUEST            = 0xA0;
        public const byte SNMP_GET_NEXT_REQUEST       = 0xA1;
        public const byte SNMP_GET_RESPONSE           = 0xA2;
        public const byte SNMP_SET_RESPONSE           = 0xA3;
        public const byte SNMP_TRAP                   = 0xA4;
        public const byte SNMP_GET_BULK_REQUEST       = 0xA5;
        public const byte SNMP_INFORM_REQUEST         = 0xA6;
        public const byte SNMP_SNMPV2_TRAP            = 0xA7;


        public const byte SNMP_BROADCAST_SEND_TIMEOUT         = 20;
        public const byte SNMP_GETCAPABILITY_SEND_TIMEOUT     = 20;
        public const byte SNMP_GETCAPABILITY_RECEIVE_TIMEOUT  = 20;

        enum SNMPRequestError
        {
	        SNMPRequestNoError = 0,          // Never used
	        SNMPRequestSendTimeOutError,       // Invalid configuration
	        SNMPRequestReceiveTimeOutError,        // Invalid parameter was passed
	        SNMPRequestInvalidDataError,     // A send operation timed out
	        SNMPRequestUnexpectedResponseError,          // The socket was closed
        };
        //typedef enum SNMPRequestError SNMPRequestError;

        //create varbind list
        // public const byte MIB_GETNEXTOID_SYSID        "1.3.6.1.2.1.1.2"
        public const string MIB_RICOH_SYS_PRODUCT_OID   = "1.3.6.1.4.1.367.3.2.1.1.1.5";
        public const string MIB_GETNEXTOID_LOC          = "1.3.6.1.2.1.1.6";
        public const string MIB_GETNEXTOID_DESC         = "1.3.6.1.2.1.1.1";
        public const string MIB_GETNEXTOID_MACADDRESS   = "1.3.6.1.2.1.2.2.1.6";
        public const string MIB_GETNEXTOID_PRINTERMIB   = "1.3.6.1.2.1.43";
        public const string MIB_GETNEXTOID_NAME         = "1.3.6.1.2.1.1.5";

        public const string MIB_GETNEXTOID_PRINTERINTERPRETERLANG       = "1.3.6.1.2.1.43.15.1.1.2";
        public const string MIB_GETNEXTOID_PRINTERINTERPRETERLANGLEVEL  = "1.3.6.1.2.1.43.15.1.1.3";

        // For OID tokens greater than 127
        public const int SNMP_2_EXP_7                = 128;
        public const int SNMP_2_EXP_14               = 16384;

        // For Location and Description replies only
        public const char SNMP_DOTTERMINATOR_VALUE	= '.';

        public const byte RFC3805_LANGPCL             = 3;
        public const byte RFC3805_LANGPJL             = 5;
        public const byte RFC3805_LANGPDF             = 54;
        public const byte RFC3805_LANGJPEG            = 61;


        //dictionary keys
        public const string KEY_OID                     = "oid";
        public const string KEY_VAL                     = "val";

        public const string BROADCAST_ADDRESS           = "255.255.255.255";

        public const string DEFAULT_COMMUNITY_NAME               = "public";
        public const string READ_COMMUNITY_NAME_SPECIAL_CHARS    = " \\'#\"";

        public const string SNMP_DISCOVERY_HOSTNAME_SUFFIX      = ".local";
    }
}