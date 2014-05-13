using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartDeviceApp.Controllers;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class NetworkControllerTest
    {
        [TestMethod]
        public void Test_NetworkController_GetIsConnectedToNetwork()
        {
            Assert.IsInstanceOfType(NetworkController.IsConnectedToNetwork, typeof(bool));
        }

        [TestMethod]
        public void Test_NetworkController_PingDeviceSuccess()
        {
            // Note: Test for coverage only; No tests to assert
            //change ip to a device that is online
            string ip = "192.168.0.199";
            NetworkController.Instance.pingDevice(ip);
        }

        [TestMethod]
        public void Test_NetworkController_PingDeviceFail()
        {
            // Note: Test for coverage only; No tests to assert
            //change ip to a device that is not online
            string ip = "192.168.0.180";
            NetworkController.Instance.pingDevice(ip);
        }
    }
}
