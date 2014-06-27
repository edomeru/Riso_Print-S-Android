using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceAppTests.SNMPTest
{
    [TestClass]
    public class SNMPDeviceTest
    {
        SNMP.SNMPDevice device = new SNMP.SNMPDevice("192.168.0.1");
        string testString = "Test";

        [TestMethod]
        public void Test_SNMPDevice_GetSetSysId()
        {
            device.SysId = testString;
            Assert.AreEqual(testString, device.SysId);
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetLocation()
        {
            device.Location =testString;
            Assert.AreEqual(testString, device.Location);
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetMacAddress()
        {
            device.MacAddress = testString;
            Assert.AreEqual(testString, device.MacAddress);
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetCommunityName()
        {
            device.CommunityName = testString;
            Assert.AreEqual(testString, device.CommunityName);
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetDescription()
        {
            device.Description = testString;
            Assert.AreEqual(testString, device.Description);
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetSysName()
        {
            device.SysName = testString;
            Assert.AreEqual(testString, device.SysName);
        }

        [TestMethod]
        public void Test_SNMPDevice_SetIpAddress()
        {
            device.IpAddress = "192.168.0.1";
            Assert.AreEqual("192.168.0.1", device.IpAddress);
        }
    }
}
