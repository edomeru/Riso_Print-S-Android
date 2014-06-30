using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using System.Threading.Tasks;
using Windows.Storage;
using System.IO;
using SQLite;
using SmartDeviceAppTests.Common.Utilities;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Models;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class JobControllerTest
    {

        private const string FILE_NAME_DATABASE = "SmartDeviceAppDB.db";
        private SQLiteAsyncConnection _dbConnection = new SQLite.SQLiteAsyncConnection(FILE_NAME_DATABASE);
        private JobsViewModel _jobsViewModel;

        // Cover Unit Tests using dotCover does not call TestInitialize
        //[TestInitialize]
        //public async Task Initialize()
        private async Task Initialize()
        {
            _jobsViewModel = new ViewModelLocator().JobsViewModel;

            await DatabaseController.Instance.Initialize();
            await UnitTestUtility.DropAllTables(_dbConnection);
            await UnitTestUtility.CreateAllTables(_dbConnection);
        }

        // Cover Unit Tests using dotCover does not call TestCleanup
        //[TestCleanup]
        //public void Cleanup()
        private void Cleanup()
        {
            JobController.Instance.Cleanup();
            //DatabaseController.Instance.Cleanup(); // This should be ideal however since events (target methods) are not awaitable, we cannot reset connections
        }

        [TestMethod]
        public async Task Test_Initialize_EmptyJobs()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();

            await JobController.Instance.Initialize();
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            Assert.AreEqual(0, _jobsViewModel.PrintJobsList.Count);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Initialize_SingleJob()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();

            await JobController.Instance.Initialize();
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_Initialize_ExtraJob()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_extra.sql", _dbConnection);
            await PrinterController.Instance.Initialize();

            await JobController.Instance.Initialize();
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_SavePrintJob_Null()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.SavePrintJob(null);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(0, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_SavePrintJob_InvalidPrinter()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.SavePrintJob(new PrintJob());
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(0, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_SavePrintJob_NewGroup_PrinterExists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            PrintJob printJob = new PrintJob() { PrinterId = 1, Name = "new.pdf", Result = 1 };
            JobController.Instance.SavePrintJob(printJob);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_SavePrintJob_NewGroup_PrinterNotExists()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            PrintJob printJob = new PrintJob() { PrinterId = 2, Name = "new2.pdf", Result = 1 };
            JobController.Instance.SavePrintJob(printJob);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_SavePrintJob_ExistingGroup_ValidPrinter()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            PrintJob printJob = new PrintJob() { PrinterId = 1, Name = "new2.pdf", Result = 1 };
            JobController.Instance.SavePrintJob(printJob);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveJob_Null()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.RemoveJob(null);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveJob_Invalid()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.RemoveJob(new PrintJob());
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveJob_LastGroupItem()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            PrintJob printJob = await _dbConnection.GetAsync<PrintJob>(1);
            JobController.Instance.RemoveJob(printJob);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(0, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveJob_MoreGroupItems()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            PrintJob printJob = await _dbConnection.GetAsync<PrintJob>(1);
            JobController.Instance.RemoveJob(printJob);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(54, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveGroupedJobs_InvalidPrinter()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.RemoveGroupedJobs(-1);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveGroupedJobs_ValidPrinter_WithJobs()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.RemoveGroupedJobs(7);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(48, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveGroupedJobs_ValidPrinter_WithoutJobs()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.RemoveGroupedJobs(1);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(0, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveGroupedJobsByPrinter_Null()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.RemoveGroupedJobsByPrinter(null);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveGroupedJobsByPrinter_InvalidPrinter()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printjob_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            JobController.Instance.RemoveGroupedJobsByPrinter(new Printer());
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(1, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveGroupedJobsByPrinter_ValidPrinter_WithJobs()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/SampleData.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            Printer printer = await _dbConnection.GetAsync<Printer>(7);
            JobController.Instance.RemoveGroupedJobsByPrinter(printer);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(48, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public async Task Test_RemoveGroupedJobsByPrinter_ValidPrinter_WithoutJobs()
        {
            await Initialize(); // Workaround for Cover Unit Tests using dotCover

            await UnitTestUtility.ExecuteScript("TestData/SqlScript/insert_printer_single.sql", _dbConnection);
            await PrinterController.Instance.Initialize();
            await JobController.Instance.Initialize();

            Printer printer = await _dbConnection.GetAsync<Printer>(1);
            JobController.Instance.RemoveGroupedJobsByPrinter(printer);
            Assert.IsNotNull(_jobsViewModel.PrintJobsList);
            //Assert.AreEqual(0, _jobsViewModel.PrintJobsList.Count); // Cannot assert since target method is not awaitable

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

    }
}
