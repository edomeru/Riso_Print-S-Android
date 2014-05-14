using System;
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
        private const string KEY_ISSAMPLEDATAALREADYLOADED = "IsSampleDataAlreadyLoaded";

        [TestInitialize]
        public async Task Initialize()
        {
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values[KEY_ISSAMPLEDATAALREADYLOADED] = true; // avoid loading of sample data
            await DatabaseController.Instance.Initialize();
        }

        [TestCleanup]
        public async Task Cleanup()
        {
            string _databasePath = Path.Combine(ApplicationData.Current.LocalFolder.Path, FILE_NAME_DATABASE);
            SQLiteAsyncConnection _dbConnection = new SQLite.SQLiteAsyncConnection(_databasePath);
            await UnitTestUtility.DropAllTables(_dbConnection);
            DatabaseController.Instance.Cleanup();
        }

        [TestMethod]
        public async Task Test_InsertSampleData_ExistingKey()
        {
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values[KEY_ISSAMPLEDATAALREADYLOADED] = false;
            await DatabaseController.Instance.Initialize();
            // Note: no public property to assert
        }

        [TestMethod]
        public async Task Test_InsertSampleData_KeyNotFound()
        {
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values.Remove(KEY_ISSAMPLEDATAALREADYLOADED);
            await DatabaseController.Instance.Initialize();
            // Note: no public property to assert
        }

        [TestMethod]
        public async Task Test_InsertPrinter_Null()
        {
            int result = await DatabaseController.Instance.InsertPrinter(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_InsertPrinter_Invalid()
        {
            // Delete database to force error
            DatabaseController.Instance.Cleanup();
            await StorageFileUtility.DeleteFile(FILE_NAME_DATABASE, ApplicationData.Current.LocalFolder);

            Printer printer = new Printer();
            int result = await DatabaseController.Instance.InsertPrinter(printer); // Insert twice
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_InsertPrinter_Valid()
        {
            Printer printer = new Printer();
            int result = await DatabaseController.Instance.InsertPrinter(printer);
            Assert.AreEqual(1, result);
            Assert.IsTrue(printer.Id > -1);
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
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            printer.PortSetting = 1;
            int result = await DatabaseController.Instance.UpdatePrinter(printer);
            Assert.AreEqual(1, result);
        }

        [TestMethod]
        public async Task Test_DeletePrinter_Null()
        {
            int result = await DatabaseController.Instance.DeletePrinter(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_DeletePrinter_Invalid()
        {
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            await DatabaseController.Instance.DeletePrinter(printer);
            int result = await DatabaseController.Instance.DeletePrinter(printer); // Delete twice
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_DeletePrinter_Valid()
        {
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            int result = await DatabaseController.Instance.DeletePrinter(printer);
            Assert.AreEqual(1, result);
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
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);

            Printer result = await DatabaseController.Instance.GetPrinter(printer.Id);
            Assert.IsNotNull(result);
            Assert.AreEqual(printer.Id, result.Id);
        }

        [TestMethod]
        public async Task Test_GetDefaultPrinter_Null()
        {
            await DatabaseController.Instance.DeleteDefaultPrinter();

            DefaultPrinter result = await DatabaseController.Instance.GetDefaultPrinter();
            Assert.IsNull(result);
        }

        [TestMethod]
        public async Task Test_GetDefaultPrinter_Exists()
        {
            await DatabaseController.Instance.DeleteDefaultPrinter();
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            await DatabaseController.Instance.SetDefaultPrinter(printer.Id);

            DefaultPrinter result = await DatabaseController.Instance.GetDefaultPrinter();
            Assert.IsNotNull(result);
            Assert.AreEqual(printer.Id, (int)result.PrinterId);
        }

        [TestMethod]
        public async Task Test_SetDefaultPrinter_New()
        {
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);

            int result = await DatabaseController.Instance.SetDefaultPrinter(printer.Id);
            Assert.AreEqual(1, result);
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

            int result = await DatabaseController.Instance.SetDefaultPrinter(printer.Id); // Twice
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_SetDefaultPrinter_Overwrite()
        {
            Printer printer1 = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer1);
            await DatabaseController.Instance.SetDefaultPrinter(printer1.Id);

            Printer printer2 = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer2);
            int result = await DatabaseController.Instance.SetDefaultPrinter(printer2.Id);
            Assert.AreEqual(1, result);
        }

        [TestMethod]
        public async Task Test_DeleteDefaultPrinter_NotExists()
        {
            await DatabaseController.Instance.DeleteDefaultPrinter();
            int result = await DatabaseController.Instance.DeleteDefaultPrinter(); // Delete twice
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_DeleteDefaultPrinter_Exists()
        {
            Printer printer = new Printer();
            await DatabaseController.Instance.InsertPrinter(printer);
            await DatabaseController.Instance.SetDefaultPrinter(printer.Id);

            int result = await DatabaseController.Instance.DeleteDefaultPrinter();
            Assert.AreEqual(1, result);
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
            PrintSettings printSettings = new PrintSettings();
            await DatabaseController.Instance.InsertPrintSettings(printSettings);

            PrintSettings result = await DatabaseController.Instance.GetPrintSettings(printSettings.Id);
            Assert.IsNotNull(result);
            Assert.AreEqual(printSettings.Id, result.Id);
        }

        [TestMethod]
        public async Task Test_InsertPrintSettings_Null()
        {
            int result = await DatabaseController.Instance.InsertPrintSettings(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_InsertPrintSettings_Invalid()
        {
            // Delete database to force error
            DatabaseController.Instance.Cleanup();
            await StorageFileUtility.DeleteFile(FILE_NAME_DATABASE, ApplicationData.Current.LocalFolder);

            PrintSettings printSettings = new PrintSettings();
            int result = await DatabaseController.Instance.InsertPrintSettings(printSettings);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_InsertPrintSettings_Valid()
        {
            PrintSettings printSettings = new PrintSettings();
            int result = await DatabaseController.Instance.InsertPrintSettings(printSettings);
            Assert.AreEqual(1, result);
            Assert.IsTrue(printSettings.Id > -1);
        }

        [TestMethod]
        public async Task Test_UpdatePrintSettings_Null()
        {
            int result = await DatabaseController.Instance.UpdatePrintSettings(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_UpdatePrintSettings_Invalid()
        {
            PrintSettings printSettings = new PrintSettings();
            int result = await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_UpdatePrintSettings_Valid()
        {
            PrintSettings printSettings = new PrintSettings();
            await DatabaseController.Instance.InsertPrintSettings(printSettings);
            printSettings.Imposition = (int)Imposition.TwoUp;
            int result = await DatabaseController.Instance.UpdatePrintSettings(printSettings);
            Assert.AreEqual(1, result);
        }

        [TestMethod]
        public async Task Test_DeletePrintSettings_Null()
        {
            int result = await DatabaseController.Instance.DeletePrintSettings(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_DeletePrintSettings_Invalid()
        {
            PrintSettings printSettings = new PrintSettings();
            await DatabaseController.Instance.InsertPrintSettings(printSettings);
            await DatabaseController.Instance.DeletePrintSettings(printSettings);
            int result = await DatabaseController.Instance.DeletePrintSettings(printSettings); // Delete twice
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_DeletePrintSettings_Valid()
        {
            PrintSettings printSettings = new PrintSettings();
            await DatabaseController.Instance.InsertPrintSettings(printSettings);
            int result = await DatabaseController.Instance.DeletePrintSettings(printSettings);
            Assert.AreEqual(1, result);
        }

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
            int result = await DatabaseController.Instance.InsertPrintJob(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_InsertPrintJob_Invalid()
        {
            // Delete database to force error
            DatabaseController.Instance.Cleanup();
            await StorageFileUtility.DeleteFile(FILE_NAME_DATABASE, ApplicationData.Current.LocalFolder);

            PrintJob PrintJob = new PrintJob();
            int result = await DatabaseController.Instance.InsertPrintJob(PrintJob);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_InsertPrintJob_Valid()
        {
            PrintJob printJob = new PrintJob();
            int result = await DatabaseController.Instance.InsertPrintJob(printJob);
            Assert.AreEqual(1, result);
            Assert.IsTrue(printJob.Id > -1);
        }

        [TestMethod]
        public async Task Test_UpdatePrintJob_Null()
        {
            int result = await DatabaseController.Instance.UpdatePrintJob(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_UpdatePrintJob_Invalid()
        {
            PrintJob PrintJob = new PrintJob();
            int result = await DatabaseController.Instance.UpdatePrintJob(PrintJob);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_UpdatePrintJob_Valid()
        {
            PrintJob PrintJob = new PrintJob();
            await DatabaseController.Instance.InsertPrintJob(PrintJob);
            PrintJob.Result = (int)PrintJobResult.Error;
            int result = await DatabaseController.Instance.UpdatePrintJob(PrintJob);
            Assert.AreEqual(1, result);
        }

        [TestMethod]
        public async Task Test_DeletePrintJob_Null()
        {
            int result = await DatabaseController.Instance.DeletePrintJob(null);
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_DeletePrintJob_Invalid()
        {
            PrintJob PrintJob = new PrintJob();
            await DatabaseController.Instance.InsertPrintJob(PrintJob);
            await DatabaseController.Instance.DeletePrintJob(PrintJob);
            int result = await DatabaseController.Instance.DeletePrintJob(PrintJob); // Delete twice
            Assert.AreEqual(0, result);
        }

        [TestMethod]
        public async Task Test_DeletePrintJob_Valid()
        {
            PrintJob PrintJob = new PrintJob();
            await DatabaseController.Instance.InsertPrintJob(PrintJob);
            int result = await DatabaseController.Instance.DeletePrintJob(PrintJob);
            Assert.AreEqual(1, result);
        }

    }
}
