using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using SmartDeviceApp.Models;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class PrinterControllerTest
    {
        [TestMethod]
        public void Test_PrinterController_Initialize()
        {
            PrinterController.Instance.Initialize();

            Assert.IsNotNull(PrinterController.Instance.PrinterList);
        }

        [TestMethod]
        public void Test_PrinterController_AddPrinter()
        {
            string ip = "192.168.0.198";
            int firstCount = PrinterController.Instance.PrinterList.Count;
            PrinterController.Instance.addPrinter(ip);

            Assert.AreEqual(firstCount, PrinterController.Instance.PrinterList.Count);
        }

        [TestMethod]
        public void Test_PrinterController_HandleAdd()
        {
            string ip = "192.168.0.198";
            string name = "Test_Printer";
            bool isOnline = true;
            List<string> capabilities = new List<string>();
            capabilities.Add("false");
            capabilities.Add("true");
            capabilities.Add("false");
            capabilities.Add("true");
            capabilities.Add("true");
            capabilities.Add("true");
            capabilities.Add("true");
            capabilities.Add("true");
            int firstCount = PrinterController.Instance.PrinterList.Count;

            PrinterController.Instance.handleAddPrinterStatus(ip, name, isOnline, capabilities);

            Printer printer = PrinterController.Instance.PrinterList.LastOrDefault();


            Assert.AreEqual(firstCount + 1, PrinterController.Instance.PrinterList.Count);
            Assert.AreEqual(printer.IpAddress, ip);
            Assert.AreEqual(printer.Name, name);
            Assert.AreEqual(printer.IsOnline, isOnline);
            Assert.AreEqual(printer.EnabledBooklet, false);
            Assert.AreEqual(printer.EnabledStapler, true);
            Assert.AreEqual(printer.EnabledPunchFour, false);
            Assert.AreEqual(printer.EnabledTrayFacedown, true);
            Assert.AreEqual(printer.EnabledTrayAutostack, true);
            Assert.AreEqual(printer.EnabledTrayTop, true);
            Assert.AreEqual(printer.EnabledTrayStack, true);
        }

    }
}
