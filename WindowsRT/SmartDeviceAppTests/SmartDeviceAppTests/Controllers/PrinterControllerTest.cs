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
using SNMP;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceAppTests.Common.Utilities;
using SQLite;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class PrinterControllerTest
    {
        private const string FILE_NAME_DATABASE = "SmartDeviceAppDB.db";
        private SQLiteAsyncConnection _dbConnection = new SQLite.SQLiteAsyncConnection(FILE_NAME_DATABASE);

        [TestInitialize]
        public async Task Initialize()
        {
            // Initilize database
            await DatabaseController.Instance.Initialize();
            await UnitTestUtility.DropAllTables(_dbConnection);
            await UnitTestUtility.CreateAllTables(_dbConnection);
        }

        [TestMethod]
        public async Task Test_PrinterController_Initialize()
        {
            Printer printer = new Printer();
            printer.IpAddress = "192.168.0.1";
            await DatabaseController.Instance.InsertPrinter(printer);

            Printer printer2 = new Printer();
            printer.IpAddress = "192.168.0.2";
            await DatabaseController.Instance.InsertPrinter(printer2);

            await DatabaseController.Instance.SetDefaultPrinter(printer2.Id);
            await PrinterController.Instance.Initialize();

            Assert.IsNotNull(PrinterController.Instance.PrinterList);
        }

        [TestMethod]
        public async Task Test_PrinterController_InitializeNoDefaultPrinter()
        {
            Printer printer = new Printer();
            printer.IpAddress = "192.168.0.1";
            await DatabaseController.Instance.InsertPrinter(printer);

            await PrinterController.Instance.Initialize();


            Assert.IsNotNull(PrinterController.Instance.PrinterList);
        }

        [TestMethod]
        public async Task Test_PrinterController_AddPrinter()
        {
            await PrinterController.Instance.Initialize();
            string ip = "192.168.0.198";
            int firstCount = PrinterController.Instance.PrinterList.Count;
            await PrinterController.Instance.addPrinter(ip);
        }

        [TestMethod]
        public async Task Test_PrinterController_AddPrinterPrinterAlreadyInList()
        {
            await PrinterController.Instance.Initialize();
            string ip = "192.168.0.198";
            Printer printer = new Printer();
            printer.IpAddress = ip;
            PrinterController.Instance.PrinterList.Add(printer);
            int firstCount = PrinterController.Instance.PrinterList.Count;
            await PrinterController.Instance.addPrinter(ip);
        }

        [TestMethod]
        public async Task Test_PrinterController_AddPrinterPrinterMax()
        {
            await PrinterController.Instance.Initialize();
            string[] ips = { "192.168.0.1", "192.168.0.2", "192.168.0.3", "192.168.0.4", "192.168.0.5",
                           "192.168.0.6", "192.168.0.7", "192.168.0.8", "192.168.0.9", "192.168.0.10"};
            
            foreach(var ip in ips)
            {
                Printer printer = new Printer();
                printer.IpAddress = ip;
                PrinterController.Instance.PrinterList.Add(printer);
            }

            await PrinterController.Instance.addPrinter("192.168.0.11");
        }


        [TestMethod]
        public async Task Test_PrinterController_AddPrinterInvalidIP()
        {
            await PrinterController.Instance.Initialize();
            
            await PrinterController.Instance.addPrinter("192.168.0.1111");
        }

        [TestMethod]
        public async Task Test_PrinterController_SetPolling()
        {
            await PrinterController.Instance.Initialize();
            PrinterController.Instance.PrinterList.Add(
                new Printer() { IpAddress = "192.168.0.1" });
            PrinterController.Instance.setPolling(true);
            PrinterController.Instance.setPolling(false);
        }

        [TestMethod]
        public async Task Test_PrinterController_GetPrinterList()
        {
            await PrinterController.Instance.Initialize();

            Assert.IsNotNull(PrinterController.Instance.PrinterList);
        }

        [TestMethod]
        public async Task Test_PrinterController_GetPrinterSearchList()
        {
            await PrinterController.Instance.Initialize();

            Assert.IsNotNull(PrinterController.Instance.PrinterSearchList);
        }

        [TestMethod]
        public void Test_PrinterController_IsValidIPValid()
        {
            PrinterController.Instance.isValidIpAddress("192.168.0.0.0");
            PrinterController.Instance.isValidIpAddress("192.999.0.0");
            PrinterController.Instance.isValidIpAddress("192.168.0.1");
        }

        [TestMethod]
        public async Task Test_PrinterController_DeletePrinter()
        {
            Printer printer = new Printer();
            printer.IpAddress = "192.168.0.1";

            await PrinterController.Instance.Initialize();
            await DatabaseController.Instance.InsertPrinter(printer);
            PrinterController.Instance.PrinterList.Add(printer);
            
            
            PrinterController.Instance.setPolling(true);

            
            await DatabaseController.Instance.SetDefaultPrinter(printer.Id);
            int firstCount = PrinterController.Instance.PrinterList.Count - 1;

            await PrinterController.Instance.deletePrinter("192.168.0.1");

            Assert.AreEqual(firstCount, PrinterController.Instance.PrinterList.Count);
        }

        [TestMethod]
        public async Task Test_PrinterController_DeletePrinterWithDeletePrinterItemsEventHandler()
        {
            Printer printer = new Printer();
            printer.IpAddress = "192.168.0.1";

            await PrinterController.Instance.Initialize();
            await DatabaseController.Instance.InsertPrinter(printer);

            PrinterController.Instance.PrinterList.Add(printer);


            PrinterController.Instance.setPolling(true);

            

            PrinterController.Instance.DeletePrinterItemsEventHandler += RemoveGroupedJobsByPrinter;

            await PrinterController.Instance.deletePrinter("192.168.0.1");
        }

        private void RemoveGroupedJobsByPrinter(Printer printer)
        {
            
        }

        [TestMethod]
        public async Task Test_PrinterController_AddFromPrinterSearchMax()
        {

            await PrinterController.Instance.Initialize();
            string[] ips = { "192.168.0.1", "192.168.0.2", "192.168.0.3", "192.168.0.4", "192.168.0.5",
                           "192.168.0.6", "192.168.0.7", "192.168.0.8", "192.168.0.9", "192.168.0.10"};
            
            foreach (var ip in ips)
            {
                Printer printer = new Printer();
                printer.IpAddress = ip;
                PrinterController.Instance.PrinterList.Add(printer);
            }
            
            await PrinterController.Instance.addPrinterFromSearch("192.168.0.11");
        }

        [TestMethod]
        public async Task Test_PrinterController_AddFromPrinterSearchSame()
        {
            await PrinterController.Instance.Initialize();
            string[] ips = { "192.168.0.1", "192.168.0.2", "192.168.0.3", "192.168.0.4", "192.168.0.5",
                           "192.168.0.6", "192.168.0.7", "192.168.0.8", "192.168.0.9"};
            int firstCount = PrinterController.Instance.PrinterList.Count;
            foreach (var ip in ips)
            {
                Printer printer = new Printer();
                printer.IpAddress = ip;
                PrinterController.Instance.PrinterList.Add(printer);
            }

            await PrinterController.Instance.addPrinterFromSearch("192.168.0.9");
        }

        [TestMethod]
        public async Task Test_PrinterController_AddFromPrinterSearch()
        {
            await PrinterController.Instance.Initialize();
            string ip = "192.168.0.2";
            
            
            SNMPController.Instance.Discovery.SnmpDevices.Add(new SNMPDevice(ip));
            Printer printer = SNMPController.Instance.getPrinterFromSNMPDevice(ip);
            
            await PrinterController.Instance.addPrinterFromSearch("192.168.0.2");
        }

        

        [TestMethod]
        public async Task Test_PrinterController_AddFromPrinterSearchInPrinterSearchList()
        {
            await PrinterController.Instance.Initialize();
            string ip = "192.168.0.2";


            SNMPController.Instance.Discovery.SnmpDevices.Add(new SNMPDevice(ip));
            Printer printer = SNMPController.Instance.getPrinterFromSNMPDevice(ip);

            PrinterSearchItem item = new PrinterSearchItem();
            item.Ip_address = ip;
            PrinterController.Instance.PrinterSearchList.Add(item);

            await PrinterController.Instance.addPrinterFromSearch("192.168.0.2");
        }

        [TestMethod]
        public async Task Test_PrinterController_AddFromPrinterSearchNull()
        {
            await PrinterController.Instance.Initialize();
            //string ip = "192.168.0.1";
            SNMPController.Instance.Initialize();
            await PrinterController.Instance.addPrinterFromSearch("192.168.0.1");
        }

        [TestMethod]
        public void Test_PrinterController_RegisterPrintSettingValueChange()
        {
            // Note: Test for coverage only; No tests to assert
            PrinterController.Instance.RegisterPrintSettingValueChange();
        }

        [TestMethod]
        public void Test_PrinterController_UnregisterPrintSettingValueChange()
        {
            // Note: Test for coverage only; No tests to assert
            PrinterController.Instance.UnregisterPrintSettingValueChange();
        }

        [TestMethod]
        public async Task Test_PrinterController_SearchPrinters()
        {
            await PrinterController.Instance.Initialize();
            PrinterController.Instance.searchPrinters();
        }

        [TestMethod]
        public async Task Test_PrinterController_PropertyChanged()
        {
            await PrinterController.Instance.Initialize();
            Printer printer = new Printer();
            printer.IpAddress = "192.168.0.1";
            PrinterController.Instance.PrinterList.Add(printer);

            Printer printer2 = new Printer();
            printer2.IpAddress = "192.168.0.2";
            PrinterController.Instance.PrinterList.Add(printer2);

            

            PrinterController.Instance.PrinterList.ElementAt(0).IsDefault = true;
            PrinterController.Instance.PrinterList.ElementAt(0).WillBeDeleted = true;
            
        }
    }
}
