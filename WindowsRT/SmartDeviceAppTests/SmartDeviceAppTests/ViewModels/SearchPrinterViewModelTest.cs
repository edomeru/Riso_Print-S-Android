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
using Windows.Networking.Connectivity;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class SearchPrinterViewModelTest
    {
        SearchPrinterViewModel viewModel = new ViewModelLocator().SearchPrinterViewModel;

        [TestMethod]
        public void Test_SearchPrinterViewModel()
        {
            Assert.IsNotNull(viewModel);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_GetSetPrinterSearchList()
        {
            ObservableCollection<PrinterSearchItem> tempList = new ObservableCollection<PrinterSearchItem>();

            viewModel.PrinterSearchList = tempList;
            ObservableCollection<PrinterSearchItem> tempList2 = viewModel.PrinterSearchList;

            Assert.AreEqual(tempList, tempList2);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_GetSetWillRefresh()
        {
            viewModel.WillRefresh = true;
            bool willRefreshTest = viewModel.WillRefresh;

            Assert.AreEqual(true, willRefreshTest);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_SetRefreshState()
        {
            viewModel.SetStateRefreshState();

            Assert.AreEqual(true, viewModel.WillRefresh);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_PrinterSearchItemSelectedFail()
        {
            PrinterController.Instance.Initialize();
            PrinterSearchItem p = new PrinterSearchItem();
            p.Ip_address = "19.2.2222.2";

            viewModel.PrinterSearchItemSelected.Execute(p);
            Assert.IsNotNull(viewModel.PrinterSearchItemSelected);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_PrinterSearchItemSelectedSuccess()
        {
            PrinterController.Instance.Initialize();
            PrinterSearchItem p = new PrinterSearchItem();
            p.Ip_address = "19.2.2.2";

            viewModel.PrinterSearchItemSelected.Execute(p);
            Assert.IsNotNull(viewModel.PrinterSearchItemSelected);
        }

        [Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer.UITestMethod]
        public void Test_SearchPrinterViewModel_PrinterSearchItemSelectedNotInList()
        {
            string ip = "192.168.0.1";
            PrinterController.Instance.Initialize();
            PrinterSearchItem p = new PrinterSearchItem();
            p.Ip_address = ip;
            p.IsInPrinterList = true;

            viewModel.PrinterSearchItemSelected.Execute(p);
            Assert.IsNotNull(viewModel.PrinterSearchItemSelected);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_SetViewModeWithNetwork()
        {
            // Note: Test for coverage only; No tests to assert
            var viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            viewControlViewModel.ScreenMode = ScreenMode.Printers;
            Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane1);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_SetViewModeWithOutNetwork()
        {
            // Note: Test for coverage only; No tests to assert
            var viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            viewControlViewModel.ScreenMode = ScreenMode.Printers;
            Messenger.Default.Send<VisibleRightPane>(VisibleRightPane.Pane1);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_SearchTimeout()
        {
            // Note: Test for coverage only; No tests to assert
            viewModel.SearchTimeout();
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_PrinterSearchRefreshed()
        {
            viewModel.PrinterSearchRefreshed.Execute(null);
            Assert.IsNotNull(viewModel.PrinterSearchRefreshed);
        }
    }
}
