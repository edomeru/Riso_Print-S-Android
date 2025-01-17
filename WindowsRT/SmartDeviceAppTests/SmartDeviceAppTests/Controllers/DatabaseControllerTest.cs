﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using System.Threading.Tasks;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;
using Windows.Storage;
using SmartDeviceAppTests.Common.Utilities;
using SQLite;
using System.IO;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class DatabaseControllerTest
    {

        private const string FILE_NAME_DATABASE = "SmartDeviceAppDB.db";

        private SQLiteAsyncConnection _dbConnection = new SQLite.SQLiteAsyncConnection(Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE));

        // Cover Unit Tests using dotCover does not call TestInitialize
        //[TestInitialize]
        //public async Task Initialize()
        private async Task Initialize()
        {
            await UnitTestUtility.CreateAllTables(_dbConnection);
            await DatabaseController.Instance.Initialize();
        }

        // Cover Unit Tests using dotCover does not call TestCleanup
        //[TestCleanup]
        //public async Task Cleanup()
        private async Task Cleanup()
        {
            await UnitTestUtility.DropAllTables(_dbConnection);
            DatabaseController.Instance.Cleanup();
        }

        [TestMethod]
        public async Task Test_Initialize()
        {
            await DatabaseController.Instance.Initialize();
            // Note: no public property to assert

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Initialize_AlreadyExists()
        {
            await DatabaseController.Instance.Initialize();
            await DatabaseController.Instance.Initialize();
            // Note: no public property to assert

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_InsertPrinter_Null()
        {
            bool result = await DatabaseController.Instance.InsertPrinter(null);
            Assert.IsTrue(result);
        }

        [TestMethod]
        public async Task Test_InsertPrinter_Invalid()
        {
            // Delete database to force error
            DatabaseController.Instance.Cleanup();
            await StorageFileUtility.DeleteFile(FILE_NAME_DATABASE, ApplicationData.Current.LocalFolder);

            Printer printer = new Printer();
            bool result = await DatabaseController.Instance.InsertPrinter(printer); // Insert twice
            Assert.IsFalse(result);
        }

        [TestMethod]
        public async Task Test_InsertPrinter_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            Printer printer = new Printer();
            bool result = await DatabaseController.Instance.InsertPrinter(printer);
            Assert.IsTrue(result);
            Assert.IsTrue(printer.Id > -1);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_GetPrinters()
        {
            List<Printer> result = await DatabaseController.Instance.GetPrinters();
            Assert.IsNotNull(result);
            // TODO: Test count of printers
        }

        [TestMethod]
        public async Task Test_UpdatePrinter_Null()
        {
            int result = await DatabaseController.Instance.UpdatePrinter(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_UpdatePrinter_Invalid()
        {
            Printer printer = new Printer();
            int result = await DatabaseController.Instance.UpdatePrinter(printer);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_UpdatePrinter_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            printer.PortSetting = 1;
            int result = await DatabaseController.Instance.UpdatePrinter(printer);
            Assert.AreEqual(1, result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_DeletePrinter_Null()
        {
            bool result = await DatabaseController.Instance.DeletePrinter(null);
            Assert.IsTrue(result);
        }

        [TestMethod]
        public async Task Test_DeletePrinter_Invalid()
        {
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            await DatabaseController.Instance.DeletePrinter(printer);
            bool result = await DatabaseController.Instance.DeletePrinter(printer); // Delete twice
            Assert.IsFalse(result);
        }

        [TestMethod]
        public async Task Test_DeletePrinter_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            bool result = await DatabaseController.Instance.DeletePrinter(printer);
            Assert.IsTrue(result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_GetPrinter_Invalid()
        {
            Printer result = await DatabaseController.Instance.GetPrinter(-1);
            Assert.IsNull(result);
        }

        [TestMethod]
        public async Task Test_GetPrinter_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);

            Printer result = await DatabaseController.Instance.GetPrinter(printer.Id);
            Assert.IsNotNull(result);
            Assert.AreEqual(printer.Id, result.Id);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_GetDefaultPrinter_Null()
        {
            await DatabaseController.Instance.DeleteDefaultPrinter();

            DefaultPrinter result = await DatabaseController.Instance.GetDefaultPrinter();
            Assert.IsNotNull(result);
            Assert.AreEqual((uint)0, result.PrinterId);
        }

        [TestMethod]
        public async Task Test_GetDefaultPrinter_Exists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await DatabaseController.Instance.DeleteDefaultPrinter();
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            await DatabaseController.Instance.SetDefaultPrinter(printer.Id);

            DefaultPrinter result = await DatabaseController.Instance.GetDefaultPrinter();
            Assert.IsNotNull(result);
            Assert.AreEqual(printer.Id, (int)result.PrinterId);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_SetDefaultPrinter_New()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);

            bool result = await DatabaseController.Instance.SetDefaultPrinter(printer.Id);
            Assert.IsTrue(result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_SetDefaultPrinter_Invalid()
        {
            // Delete database to force error
            DatabaseController.Instance.Cleanup();
            await StorageFileUtility.DeleteFile(FILE_NAME_DATABASE, ApplicationData.Current.LocalFolder);

            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            await DatabaseController.Instance.SetDefaultPrinter(printer.Id);

            bool result = await DatabaseController.Instance.SetDefaultPrinter(printer.Id); // Twice
            Assert.IsFalse(result);
        }

        [TestMethod]
        public async Task Test_SetDefaultPrinter_Overwrite()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            Printer printer1 = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer1);
            await DatabaseController.Instance.SetDefaultPrinter(printer1.Id);

            Printer printer2 = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer2);
            bool result = await DatabaseController.Instance.SetDefaultPrinter(printer2.Id);
            Assert.IsTrue(result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_DeleteDefaultPrinter_NotExists()
        {
            await DatabaseController.Instance.DeleteDefaultPrinter();
            bool result = await DatabaseController.Instance.DeleteDefaultPrinter(); // Delete twice
            Assert.IsTrue(result);
        }

        [TestMethod]
        public async Task Test_DeleteDefaultPrinter_Exists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            await DatabaseController.Instance.SetDefaultPrinter(printer.Id);

            bool result = await DatabaseController.Instance.DeleteDefaultPrinter();
            Assert.IsTrue(result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_GetPrintSettings_Invalid()
        {
            PrintSettings result = await DatabaseController.Instance.GetPrintSettings(-1);
            Assert.IsNull(result);
        }

        [TestMethod]
        public async Task Test_GetPrintSettings_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            PrintSettings printSettings = new PrintSettings();
            await DatabaseController.Instance.InsertPrintSettings(printSettings);

            PrintSettings result = await DatabaseController.Instance.GetPrintSettings(printSettings.Id);
            Assert.IsNotNull(result);
            Assert.AreEqual(printSettings.Id, result.Id);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_InsertPrintSettings_Null()
        {
            bool result = await DatabaseController.Instance.InsertPrintSettings(null);
            Assert.IsTrue(result);
        }

        [TestMethod]
        public async Task Test_InsertPrintSettings_Invalid()
        {
            // Delete database to force error
            DatabaseController.Instance.Cleanup();
            await StorageFileUtility.DeleteFile(FILE_NAME_DATABASE, ApplicationData.Current.LocalFolder);

            PrintSettings printSettings = new PrintSettings();
            bool result = await DatabaseController.Instance.InsertPrintSettings(printSettings);
            Assert.IsFalse(result);
        }

        [TestMethod]
        public async Task Test_InsertPrintSettings_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            PrintSettings printSettings = new PrintSettings();
            bool result = await DatabaseController.Instance.InsertPrintSettings(printSettings);
            Assert.IsTrue(result);
            Assert.IsTrue(printSettings.Id > -1);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_UpdatePrintSettings_Null()
        {
            bool result = await DatabaseController.Instance.UpdatePrintSettings(null);
            Assert.IsTrue(result);
        }

        [TestMethod]
        public async Task Test_UpdatePrintSettings_Invalid()
        {
            PrintSettings printSettings = new PrintSettings();
            bool result = await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            Assert.IsFalse(result);
        }

        [TestMethod]
        public async Task Test_UpdatePrintSettings_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            PrintSettings printSettings = new PrintSettings();
            await DatabaseController.Instance.InsertPrintSettings(printSettings);
            printSettings.Imposition = (int)Imposition.TwoUp;
            bool result = await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            Assert.IsTrue(result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        //[TestMethod]
        //public async Task Test_DeletePrintSettings_Null()
        //{
        //    int result = await DatabaseController.Instance.DeletePrintSettings(null);
        //    Assert.AreEqual(0, result);
        //}

        //[TestMethod]
        //public async Task Test_DeletePrintSettings_Invalid()
        //{
        //    PrintSettings printSettings = new PrintSettings();
        //    await DatabaseController.Instance.InsertPrintSettings(printSettings);
        //    await DatabaseController.Instance.DeletePrintSettings(printSettings);
        //    int result = await DatabaseController.Instance.DeletePrintSettings(printSettings); // Delete twice
        //    Assert.AreEqual(0, result);
        //}

        //[TestMethod]
        //public async Task Test_DeletePrintSettings_Valid()
        //{
        //    await Initialize(); // Workaround for Cover Unit Tests using dotCover

        //    PrintSettings printSettings = new PrintSettings();
        //    await DatabaseController.Instance.InsertPrintSettings(printSettings);
        //    int result = await DatabaseController.Instance.DeletePrintSettings(printSettings);
        //    Assert.AreEqual(1, result);

        //    await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        //}

        [TestMethod]
        public async Task Test_GetPrintJobs()
        {
            List<PrintJob> result = await DatabaseController.Instance.GetPrintJobs();
            Assert.IsNotNull(result);
            // TODO: Test count of print jobs
        }

        [TestMethod]
        public async Task Test_InsertPrintJob_Null()
        {
            bool result = await DatabaseController.Instance.InsertPrintJob(null);
            Assert.IsTrue(result);
        }

        [TestMethod]
        public async Task Test_InsertPrintJob_Invalid()
        {
            // Delete database to force error
            DatabaseController.Instance.Cleanup();
            await StorageFileUtility.DeleteFile(FILE_NAME_DATABASE, ApplicationData.Current.LocalFolder);

            PrintJob PrintJob = new PrintJob();
            bool result = await DatabaseController.Instance.InsertPrintJob(PrintJob);
            Assert.IsFalse(result);
        }

        [TestMethod]
        public async Task Test_InsertPrintJob_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            PrintJob printJob = new PrintJob();
            bool result = await DatabaseController.Instance.InsertPrintJob(printJob);
            Assert.IsTrue(result);
            Assert.IsTrue(printJob.Id > -1);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        //[TestMethod]
        //public async Task Test_UpdatePrintJob_Null()
        //{
        //    bool result = await DatabaseController.Instance.UpdatePrintJob(null);
        //    Assert.IsTrue(result);
        //}

        //[TestMethod]
        //public async Task Test_UpdatePrintJob_Invalid()
        //{
        //    PrintJob PrintJob = new PrintJob();
        //    bool result = await DatabaseController.Instance.UpdatePrintJob(PrintJob);
        //    Assert.IsTrue(result);
        //}

        //[TestMethod]
        //public async Task Test_UpdatePrintJob_Valid()
        //{
        //    await Initialize(); // Workaround for Cover Unit Tests using dotCover

        //    PrintJob PrintJob = new PrintJob();
        //    await DatabaseController.Instance.InsertPrintJob(PrintJob);
        //    PrintJob.Result = (int)PrintJobResult.Error;
        //    bool result = await DatabaseController.Instance.UpdatePrintJob(PrintJob);
        //    Assert.IsTrue(result);

        //    await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        //}

        [TestMethod]
        public async Task Test_DeletePrintJob_Null()
        {
            bool result = await DatabaseController.Instance.DeletePrintJob(null);
            Assert.IsTrue(result);
        }

        [TestMethod]
        public async Task Test_DeletePrintJob_Invalid()
        {
            PrintJob PrintJob = new PrintJob();
            await DatabaseController.Instance.InsertPrintJob(PrintJob);
            await DatabaseController.Instance.DeletePrintJob(PrintJob);
            bool result = await DatabaseController.Instance.DeletePrintJob(PrintJob); // Delete twice
            Assert.IsFalse(result);
        }

        [TestMethod]
        public async Task Test_DeletePrintJob_Valid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            PrintJob PrintJob = new PrintJob();
            await DatabaseController.Instance.InsertPrintJob(PrintJob);
            bool result = await DatabaseController.Instance.DeletePrintJob(PrintJob);
            Assert.IsTrue(result);

            await Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

    }
}
