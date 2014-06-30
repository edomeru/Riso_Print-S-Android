using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SNMP;
namespace SmartDeviceAppTests.SNMPTest
{
    [TestClass]
    public class UDPHelperTest
    {
        UDPHelper helper = new UDPHelper();

        [TestMethod]
        public void Test_UDPHelper_Process()
        {
            helper.process();
        }

        [TestMethod]
        public void Test_UDPHelper_GetSNMPPacket()
        {
            helper.getSNMPPacket("get", "public", SNMPConstants.MIB_GETNEXTOID_NAME);
            helper.getSNMPPacket("get", "public", SNMPConstants.MIB_RICOH_SYS_PRODUCT_OID);
        }
    }
}
