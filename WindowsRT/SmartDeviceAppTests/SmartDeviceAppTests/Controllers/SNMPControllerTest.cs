using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SNMP;
using SmartDeviceApp.Models;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class SNMPControllerTest
    {

        [TestMethod]
        public void Test_SNMPController_Initialize()
        {
            // Note: Test for coverage only; No tests to assert
            SNMPController.Instance.Initialize();
        }

        [TestMethod]
        public void Test_SNMPController_GetDeviceSuccess()
        {
            // Note: Test for coverage only; No tests to assert
            string ip = "192.168.0.199";
            PrinterController.Instance.Initialize();
            SNMPController.Instance.getDevice(ip);
            SNMPController.Instance.printerControllerAddPrinterCallback = new Action<string,string,bool,List<string>>(Test_SNMPController_AddCallBack);
            SNMPController.Instance.printerControllerAddTimeout = new Action<string,string,List<string>>(Test_SNMPController_HandleGetDeviceTimeout);
        }

        [TestMethod]
        public void Test_SNMPController_GetDeviceTimeout()
        {
            // Note: Test for coverage only; No tests to assert
            string ip = "192.168.0.180";
            PrinterController.Instance.Initialize();
            SNMPController.Instance.getDevice(ip);
            SNMPController.Instance.printerControllerAddPrinterCallback = new Action<string,string,bool,List<string>>(Test_SNMPController_AddCallBack);
            SNMPController.Instance.printerControllerAddTimeout = new Action<string,string,List<string>>(Test_SNMPController_HandleGetDeviceTimeout);
        }

        
        private void Test_SNMPController_AddCallBack(string ip, string name, bool isOnline, List<string> cp)
        {
            // Note: Test for coverage only; No tests to assert
        }

        
        private void Test_SNMPController_HandleGetDeviceTimeout(string ip, string name,List<string> cp)
        {
               // Note: Test for coverage only; No tests to assert
        }

        [TestMethod]
        public void Test_SNMPController_DiscoverSuccess()
        {
            // Note: Test for coverage only; No tests to assert
            SNMPController.Instance.Initialize();
            SNMPController.Instance.printerControllerDiscoverCallback = new Action<PrinterSearchItem>(Test_SNMPController_DiscoverCallback);
            SNMPController.Instance.printerControllerTimeout = new Action<string>(Test_SNMPController_Timeout);
            SNMPController.Instance.startDiscover();

        }

        
        private void Test_SNMPController_DiscoverCallback(PrinterSearchItem item)
        {
            // Note: Test for coverage only; No tests to assert
        }

        
        private void Test_SNMPController_Timeout(string ip)
        {
            // Note: Test for coverage only; No tests to assert
        }

        [TestMethod]
        public void Test_SNMPController_GetPrinterFromSNMPDeviceNoSearchedPrinter()
        {
            string ip = "192.168.0.1";
            SNMPController.Instance.Initialize();
            //SNMPController.Instance.Discovery.SnmpDevices.Add(new SNMPDevice(ip));
            Printer printer = SNMPController.Instance.getPrinterFromSNMPDevice(ip);
            Assert.IsNull(printer);
        }

        [TestMethod]
        public void Test_SNMPController_GetPrinterFromSNMPDeviceNoCapabilities()
        {
            string ip = "192.168.0.1";
            SNMPController.Instance.Initialize();
            SNMPController.Instance.Discovery.SnmpDevices.Add(new SNMPDevice(ip));
            Printer printer = SNMPController.Instance.getPrinterFromSNMPDevice(ip);
            Assert.IsNotNull(printer);
        }

        [TestMethod]
        public void Test_SNMPController_GetPrinterFromSNMPDeviceWithCapabilities()
        {
            string ip = "192.168.0.1";
            SNMPController.Instance.Initialize();
            SNMPDevice device = new SNMPDevice(ip);
            device.CapabilitiesList.Add("true");
            device.CapabilitiesList.Add("true");
            device.CapabilitiesList.Add("true");
            device.CapabilitiesList.Add("true");
            device.CapabilitiesList.Add("true");
            device.CapabilitiesList.Add("true");
            device.CapabilitiesList.Add("true");
            device.CapabilitiesList.Add("true");
            SNMPController.Instance.Discovery.SnmpDevices.Add(device);
            Printer printer = SNMPController.Instance.getPrinterFromSNMPDevice(ip);
            Assert.IsNotNull(printer);
        }

    }
}
