using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using SmartDeviceApp.Models;
using System.Collections.ObjectModel;
using Windows.Storage;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class PrinterControllerTest
    {
        private const string KEY_ISSAMPLEDATAALREADYLOADED = "IsSampleDataAlreadyLoaded";

        [TestInitialize]
        public async Task Initialize()
        {
            // Initilize database
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values[KEY_ISSAMPLEDATAALREADYLOADED] = true; // avoid loading of sample data
            await DatabaseController.Instance.Initialize();
        }




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

        }

        [TestMethod]
        public void Test_PrinterController_AddPrinterPrinterAlreadyInList()
        {
            string ip = "192.168.0.198";
            int firstCount = PrinterController.Instance.PrinterList.Count;
            PrinterController.Instance.addPrinter(ip);
            PrinterController.Instance.addPrinter(ip);

        }

        [TestMethod]
        public void Test_PrinterController_AddPrinterPrinterMax()
        {
            string[] ips = { "192.168.0.1", "192.168.0.2", "192.168.0.3", "192.168.0.4", "192.168.0.5",
                           "192.168.0.6", "192.168.0.7", "192.168.0.8", "192.168.0.9", "192.168.0.10", "192.168.0.11"};
            int firstCount = PrinterController.Instance.PrinterList.Count;
            foreach(var ip in ips)
            {
                PrinterController.Instance.addPrinter(ip);
            }

        }

        [TestMethod]
        public void Test_PrinterController_HandleAdd()
        {
            DatabaseController.Instance.Initialize();

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

            //PrinterController.Instance.handleAddPrinterStatus(ip, name, isOnline, capabilities);

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

        [TestMethod]
        public void Test_PrinterController_SetPolling()
        {
            PrinterController.Instance.setPolling(true);
            PrinterController.Instance.setPolling(false);
        }

        [TestMethod]
        public void Test_PrinterController_GetSetPrinterList()
        {
            ObservableCollection<Printer> printers = new ObservableCollection<Printer>();
            PrinterController.Instance.PrinterList = printers;

            Assert.AreEqual(printers, PrinterController.Instance.PrinterList);
        }

        [TestMethod]
        public void Test_PrinterController_GetSetPrinterSearchList()
        {
            ObservableCollection<PrinterSearchItem> printerSearch = new ObservableCollection<PrinterSearchItem>();
            PrinterController.Instance.PrinterSearchList = printerSearch;

            Assert.AreEqual(printerSearch, PrinterController.Instance.PrinterSearchList);
        }

        [TestMethod]
        public void Test_PrinterController_IsValidIPValid()
        {
            PrinterController.Instance.isValidIpAddress("192.168.0.0.0");
            PrinterController.Instance.isValidIpAddress("192.999.0.0");
            PrinterController.Instance.isValidIpAddress("192.168.0.1");
        }

        [TestMethod]
        public void Test_PrinterController_DeletePrinter()
        {

        }

        [TestMethod]
        public void Test_PrinterController_AddFromPrinterSearchMax()
        {
            string[] ips = { "192.168.0.1", "192.168.0.2", "192.168.0.3", "192.168.0.4", "192.168.0.5",
                           "192.168.0.6", "192.168.0.7", "192.168.0.8", "192.168.0.9", "192.168.0.10"};
            int firstCount = PrinterController.Instance.PrinterList.Count;
            foreach (var ip in ips)
            {
                PrinterController.Instance.addPrinter(ip);
            }

            PrinterController.Instance.addPrinterFromSearch("192.168.0.11");
        }

        [TestMethod]
        public void Test_PrinterController_AddFromPrinterSearchSame()
        {
            string[] ips = { "192.168.0.1", "192.168.0.2", "192.168.0.3", "192.168.0.4", "192.168.0.5",
                           "192.168.0.6", "192.168.0.7", "192.168.0.8", "192.168.0.9"};
            int firstCount = PrinterController.Instance.PrinterList.Count;
            foreach (var ip in ips)
            {
                PrinterController.Instance.addPrinter(ip);
            }

            PrinterController.Instance.addPrinterFromSearch("192.168.0.9");
        }

        [TestMethod]
        public void Test_PrinterController_AddFromPrinterSearch()
        {
            PrinterController.Instance.addPrinterFromSearch("192.168.0.9");
        }
    }
}
