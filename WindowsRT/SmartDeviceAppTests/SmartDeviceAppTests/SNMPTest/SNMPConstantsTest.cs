using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceAppTests.SNMPTest
{
    [TestClass]
    public class SNMPConstantsTest
    {
        [TestMethod]
        public void Test_SNMPConstants()
        {
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_PORT, 161);           
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_V1, 0x00);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_V2, 0x01);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_INTEGER, 0x02);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_BIT_STRING, 0x03);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_OCTET_STRING, 0x04);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_NULL, 0x05);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_OBJECT_IDENTIFIER, 0x06);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_SEQUENCE, 0x30);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_GET_REQUEST, 0xA0);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_GET_NEXT_REQUEST, 0xA1);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_GET_RESPONSE, 0xA2);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_SET_RESPONSE, 0xA3);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_TRAP, 0xA4);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_GET_BULK_REQUEST, 0xA5);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_INFORM_REQUEST, 0xA6);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_SNMPV2_TRAP, 0xA7);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_BROADCAST_SEND_TIMEOUT, 10);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_GETCAPABILITY_SEND_TIMEOUT, 10);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_GETCAPABILITY_RECEIVE_TIMEOUT, 10);
            Assert.AreEqual(SNMP.SNMPConstants.MIB_RICOH_SYS_PRODUCT_OID, "1.3.6.1.4.1.367.3.2.1.1.1.5");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_LOC, "1.3.6.1.2.1.1.6");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_DESC, "1.3.6.1.2.1.1.1");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_MACADDRESS, "1.3.6.1.2.1.2.2.1.6");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_PRINTERMIB, "1.3.6.1.2.1.43");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_NAME, "1.3.6.1.2.1.1.5");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANG, "1.3.6.1.2.1.43.15.1.1.2");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_PRINTERINTERPRETERLANGLEVEL, "1.3.6.1.2.1.43.15.1.1.3");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_SYSID, "1.3.6.1.2.1.1.2");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_SYSDESC, "1.3.6.1.2.1.1.1");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_GENERALNAME, "1.3.6.1.4.1.24807.1.2.1.1.1");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_BOOKLET, "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.3");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_STAPLER, "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.20");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_4HOLES, "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.1");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_3HOLES, "1.3.6.1.4.1.24807.1.2.2.2.4.1.2.2");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_TRAY_FACEDOWN, "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.1");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_TRAY_AUTO, "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.2");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_TRAY_TOP, "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.3");
            Assert.AreEqual(SNMP.SNMPConstants.MIB_GETNEXTOID_TRAY_STACK, "1.3.6.1.4.1.24807.1.2.1.2.2.1.2.4");
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_2_EXP_7, 128);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_2_EXP_14, 16384);
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_DOTTERMINATOR_VALUE, '.');
            Assert.AreEqual(SNMP.SNMPConstants.RFC3805_LANGPCL, 3);
            Assert.AreEqual(SNMP.SNMPConstants.RFC3805_LANGPJL, 5);
            Assert.AreEqual(SNMP.SNMPConstants.RFC3805_LANGPDF, 54);
            Assert.AreEqual(SNMP.SNMPConstants.RFC3805_LANGJPEG, 61);
            Assert.AreEqual(SNMP.SNMPConstants.KEY_OID, "oid");
            Assert.AreEqual(SNMP.SNMPConstants.KEY_VAL, "val");
            Assert.AreEqual(SNMP.SNMPConstants.BROADCAST_ADDRESS, "255.255.255.255");
            Assert.AreEqual(SNMP.SNMPConstants.DEFAULT_COMMUNITY_NAME, "public");
            Assert.AreEqual(SNMP.SNMPConstants.READ_COMMUNITY_NAME_SPECIAL_CHARS, " \\'#\"");
            Assert.AreEqual(SNMP.SNMPConstants.SNMP_DISCOVERY_HOSTNAME_SUFFIX, ".local");
        }
    }



}
