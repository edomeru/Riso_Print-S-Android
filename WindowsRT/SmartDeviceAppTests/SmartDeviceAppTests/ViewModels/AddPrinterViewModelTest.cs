using GalaSoft.MvvmLight.Messaging;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.UI.Xaml;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class AddPrinterViewModelTest
    {
        AddPrinterViewModel viewModel = new ViewModelLocator().AddPrinterViewModel;

        [TestMethod]
        public void Test_AddPrinterViewModel()
        {
            Assert.IsNotNull(viewModel);
            Assert.AreEqual("", viewModel.IpAddress);
            Assert.AreEqual(true, viewModel.IsButtonVisible);
            Assert.AreEqual(false, viewModel.IsProgressRingVisible);
        }


        [TestMethod]
        public void Test_AddPrinterViewModel_SetVisibilities()
        {
            viewModel.setVisibilities();

            Assert.AreEqual(true, viewModel.IsButtonVisible);
            Assert.AreEqual(false, viewModel.IsProgressRingVisible);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel_GetSetIpAddress()
        {
            viewModel.IpAddress = "192.168.0.1";
            Assert.AreEqual("192.168.0.1", viewModel.IpAddress);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel_GetSetPrinterSearchList()
        {
            ObservableCollection<PrinterSearchItem> tempList = new ObservableCollection<PrinterSearchItem>();

            viewModel.PrinterSearchList = tempList;

            Assert.AreEqual(tempList, viewModel.PrinterSearchList);


        }


        [TestMethod]
        public void Test_AddPrinterViewModel_GetSetIsProgressRingVisible()
        {
            viewModel.IsProgressRingVisible = true;
            Assert.AreEqual(true, viewModel.IsProgressRingVisible);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel_GetSetIsButtonVisible()
        {
            viewModel.IsButtonVisible = true;
            Assert.AreEqual(true, viewModel.IsButtonVisible);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel_HandleAddPrinterSuccess()
        {
            viewModel.handleAddIsSuccessful(true);

            Assert.AreEqual(true, viewModel.IsButtonVisible);
            Assert.AreEqual(false, viewModel.IsProgressRingVisible);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel_HandleAddPrinterFailedWithDetails()
        {
            viewModel.handleAddIsSuccessful(false);

            Assert.AreEqual(true, viewModel.IsButtonVisible);
            Assert.AreEqual(false, viewModel.IsProgressRingVisible);
            Assert.AreEqual("", viewModel.IpAddress);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel_AddPrinterCommandIpIsEmpty()
        {
            PrinterController.Instance.Initialize();
            viewModel.IpAddress = "";
            viewModel.AddPrinter.Execute(null);
            Assert.IsNotNull(viewModel.AddPrinter);
            Assert.AreEqual("", viewModel.IpAddress);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel_AddPrinterCommandFail()
        {
            PrinterController.Instance.Initialize();
            viewModel.IpAddress = "192.168.0000.0";
            viewModel.AddPrinter.Execute(null);
            Assert.IsNotNull(viewModel.AddPrinter);
            Assert.AreEqual("192.168.0000.0", viewModel.IpAddress);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel_SetRightPaneMode()
        {
            // Note: Test for coverage only; No tests to assert
            var viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            viewControlViewModel.ScreenMode = ScreenMode.Printers;
            Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane2);
        }
    }
}
