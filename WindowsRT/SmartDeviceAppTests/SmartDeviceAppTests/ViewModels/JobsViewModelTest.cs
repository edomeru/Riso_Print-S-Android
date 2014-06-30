using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;
using System.Collections.ObjectModel;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class JobsViewModelTest
    {
        private JobsViewModel jobsViewModel = new ViewModelLocator().JobsViewModel;

        private void Test_RemoveGroupedJobsEventHandler(int id)
        {
        }

        private void Test_RemoveJobEventHandler(PrintJob job)
        {
        }

        [TestMethod]
        public void Test_JobsViewModel()
        {
            Assert.IsNotNull(jobsViewModel);
        }

        [UI.UITestMethod]
        public void Test_DeleteAllJobsCommand()
        {
            JobController.RemoveGroupedJobsEventHandler eventHandler = new JobController.RemoveGroupedJobsEventHandler(Test_RemoveGroupedJobsEventHandler);
            jobsViewModel.RemoveGroupedJobsEventHandler += eventHandler;
            jobsViewModel.DeleteAllJobsCommand.Execute(1);
            Assert.IsNotNull(jobsViewModel.DeleteAllJobsCommand);
        }

        [UI.UITestMethod]
        public void Test_DeleteJobCommand()
        {
            JobController.RemoveJobEventHandler eventHandler = new JobController.RemoveJobEventHandler(Test_RemoveJobEventHandler);
            jobsViewModel.RemoveJobEventHandler += eventHandler;
            var printJob = new PrintJob();
            printJob.Id = 1;
            printJob.Name = "PRINT_JOB";
            jobsViewModel.DeleteJobCommand.Execute(printJob);
            Assert.IsNotNull(jobsViewModel.DeleteJobCommand);
        }

        [TestMethod]
        public void Test_PrintJobsList()
        {
            var printJobList = new PrintJobList();
            var jobs = new ObservableCollection<PrintJob>();
            var printJob1 = new PrintJob();
            printJob1.Id = 1;
            printJob1.Name = "PRINT_JOB1";
            jobs.Add(printJob1);
            var printJob2 = new PrintJob();
            printJob2.Id = 2;
            printJob2.Name = "PRINT_JOB2";
            jobs.Add(printJob2);
            var group = new PrintJobGroup("PRINTER_NAME", "192.168.1.1", jobs);
            printJobList.Add(group);
            jobsViewModel.PrintJobsList = printJobList;
            Assert.AreEqual(printJobList, jobsViewModel.PrintJobsList);
        }

        [TestMethod]
        public void Test_PrintJobsColumn1()
        {
            var printJobList = new PrintJobList();
            var jobs = new ObservableCollection<PrintJob>();
            var printJob1 = new PrintJob();
            printJob1.Id = 1;
            printJob1.Name = "PRINT_JOB1";
            jobs.Add(printJob1);
            var printJob2 = new PrintJob();
            printJob2.Id = 2;
            printJob2.Name = "PRINT_JOB2";
            jobs.Add(printJob2);
            var group = new PrintJobGroup("PRINTER_NAME", "192.168.1.1", jobs);
            printJobList.Add(group);
            jobsViewModel.PrintJobsColumn1 = printJobList;
            Assert.AreEqual(printJobList, jobsViewModel.PrintJobsColumn1);
        }

        [TestMethod]
        public void Test_PrintJobsColumn2()
        {
            var printJobList = new PrintJobList();
            var jobs = new ObservableCollection<PrintJob>();
            var printJob1 = new PrintJob();
            printJob1.Id = 1;
            printJob1.Name = "PRINT_JOB1";
            jobs.Add(printJob1);
            var printJob2 = new PrintJob();
            printJob2.Id = 2;
            printJob2.Name = "PRINT_JOB2";
            jobs.Add(printJob2);
            var group = new PrintJobGroup("PRINTER_NAME", "192.168.1.1", jobs);
            printJobList.Add(group);
            jobsViewModel.PrintJobsColumn2 = printJobList;
            Assert.AreEqual(printJobList, jobsViewModel.PrintJobsColumn2);
        }

        [TestMethod]
        public void Test_PrintJobsColumn3()
        {
            var printJobList = new PrintJobList();
            var jobs = new ObservableCollection<PrintJob>();
            var printJob1 = new PrintJob();
            printJob1.Id = 1;
            printJob1.Name = "PRINT_JOB1";
            jobs.Add(printJob1);
            var printJob2 = new PrintJob();
            printJob2.Id = 2;
            printJob2.Name = "PRINT_JOB2";
            jobs.Add(printJob2);
            var group = new PrintJobGroup("PRINTER_NAME", "192.168.1.1", jobs);
            printJobList.Add(group);
            jobsViewModel.PrintJobsColumn3 = printJobList;
            Assert.AreEqual(printJobList, jobsViewModel.PrintJobsColumn3);
        }

        [TestMethod]
        public void Test_GestureController()
        {
            var jobGestureController = new JobGestureController();
            jobsViewModel.GestureController = jobGestureController;
            Assert.AreEqual(jobGestureController, jobsViewModel.GestureController);
        }

        [TestMethod]
        public void Test_SetViewMode()
        {
            var jobGestureController = new JobGestureController();
            jobsViewModel.GestureController = jobGestureController;
            new ViewModelLocator().ViewControlViewModel.ScreenMode = ScreenMode.Jobs;
            // Note: Test for coverage only; No tests to assert
            Messenger.Default.Send<ViewMode>(ViewMode.MainMenuPaneVisible);
            Messenger.Default.Send<ViewMode>(ViewMode.FullScreen);
            Messenger.Default.Send<ViewMode>(ViewMode.RightPaneVisible);
            Messenger.Default.Send<ViewMode>(ViewMode.RightPaneVisible_ResizedWidth);

            new ViewModelLocator().ViewControlViewModel.ScreenMode = ScreenMode.Home;
            Messenger.Default.Send<ViewMode>(ViewMode.MainMenuPaneVisible);
        }

        [TestMethod()]
        public void Test_SortPrintJobsListToColumns()
        {
            // Note: Test for coverage only; No tests to assert
            var printJobList = new PrintJobList();
            var printJobList1 = new PrintJobList();
            var printJobList2 = new PrintJobList();
            var printJobList3 = new PrintJobList();

            var jobs1 = new ObservableCollection<PrintJob>();
            var printJob1 = new PrintJob();
            printJob1.Id = 1;
            printJob1.Name = "PRINT_JOB1";
            jobs1.Add(printJob1);
            var printJob2 = new PrintJob();
            printJob2.Id = 2;
            printJob2.Name = "PRINT_JOB2";
            jobs1.Add(printJob2);
            var group1 = new PrintJobGroup("PRINTER_NAME1", "192.168.1.1", jobs1);
            printJobList.Add(group1);
            printJobList1.Add(group1);

            var jobs2 = new ObservableCollection<PrintJob>();
            var printJob3 = new PrintJob();
            printJob3.Id = 1;
            printJob3.Name = "PRINT_JOB1";
            jobs2.Add(printJob3);
            var printJob4 = new PrintJob();
            printJob4.Id = 2;
            printJob4.Name = "PRINT_JOB2";
            jobs2.Add(printJob4);
            var group2 = new PrintJobGroup("PRINTER_NAME2", "192.168.1.1", jobs2);
            printJobList.Add(group2);
            printJobList2.Add(group2);

            var jobs3 = new ObservableCollection<PrintJob>();
            var printJob5 = new PrintJob();
            printJob5.Id = 1;
            printJob5.Name = "PRINT_JOB1";
            jobs3.Add(printJob5);
            var printJob6 = new PrintJob();
            printJob6.Id = 2;
            printJob6.Name = "PRINT_JOB2";
            jobs3.Add(printJob6);
            var group3 = new PrintJobGroup("PRINTER_NAME3", "192.168.1.1", jobs3);
            printJobList.Add(group3);
            printJobList3.Add(group3);

            jobsViewModel.PrintJobsList = printJobList;

            jobsViewModel.SortPrintJobsListToColumns();
            Assert.IsNotNull(jobsViewModel.PrintJobsColumn1);
            Assert.IsNotNull(jobsViewModel.PrintJobsColumn2);
            Assert.IsNotNull(jobsViewModel.PrintJobsColumn3);
        }
    }
}
