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
            device.setSysId(testString);
            Assert.AreEqual(testString, device.getSysId());
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetLocation()
        {
            device.setLocation(testString);
            Assert.AreEqual(testString, device.getLocation());
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetMacAddress()
        {
            device.setMacAddress(testString);
            Assert.AreEqual(testString, device.getMacAddress());
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetCommunityName()
        {
            device.setCommunityName(testString);
            Assert.AreEqual(testString, device.getCommunityName());
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetDescription()
        {
            device.setDescription(testString);
            Assert.AreEqual(testString, device.getDescription());
        }

        [TestMethod]
        public void Test_SNMPDevice_GetSetSysName()
        {
            device.setSysName(testString);
            Assert.AreEqual(testString, device.getSysName());
        }

        [TestMethod]
        public void Test_SNMPDevice_SetIpAddress()
        {
            device.setIpAddress("192.168.0.1");
            Assert.AreEqual("192.168.0.1", device.getIpAddress());
        }
    }
}
