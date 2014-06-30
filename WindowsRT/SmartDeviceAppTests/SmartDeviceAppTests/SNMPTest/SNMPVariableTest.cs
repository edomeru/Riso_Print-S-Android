using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceAppTests.SNMPTest
{
    [TestClass]
    public class SNMPVariableTest
    {
        [TestMethod]
        public void Test_SNMPVariable_Constructor()
        {
            SNMP.SNMPVariable var = new SNMP.SNMPVariable(0x01, (string)null);
            SNMP.SNMPVariable var2 = new SNMP.SNMPVariable(0x01, (byte[])null);
        }

        [TestMethod]
        public void Test_SNMPVariable_ExtractSNMPInfo128Length()
        {
            byte[] input = new byte[132];
            input[0] = 0x80;
            input[1] = 0x82;

            for (int i = 2; i < input.Length; i++) 
            {
                input[i] = (byte)i;
            }

            SNMP.SNMPVariable var = new SNMP.SNMPVariable();
            var.extractSNMPInformation(input);

        }

        [TestMethod]
        public void Test_SNMPVariable_GetFormattedData128Length()
        {
            byte[] input = new byte[132];
            input[0] = 0x80;
            input[1] = 0x82;

            for (int i = 2; i < input.Length; i++)
            {
                input[i] = (byte)i;
            }

            SNMP.SNMPVariable var = new SNMP.SNMPVariable(0x01, input);

            var.getFormattedData();

        }
    }
}
