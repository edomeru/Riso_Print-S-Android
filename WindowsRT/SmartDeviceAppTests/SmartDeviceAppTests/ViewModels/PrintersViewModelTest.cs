using GalaSoft.MvvmLight.Messaging;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintersViewModelTest
    {
        PrintersViewModel viewModel = new ViewModelLocator().PrintersViewModel;

        [TestMethod]
        public void Test_PrintersViewModel_GetSetPrinterList()
        {
            ObservableCollection<Printer> printerList = new ObservableCollection<Printer>();
            viewModel.PrinterList = printerList;

            Assert.IsNotNull(viewModel.PrinterList);
            Assert.AreEqual(printerList, viewModel.PrinterList);
        }

        [TestMethod]
        public void Test_PrintersViewModel_GetSetGestureControl()
        {
            PrintersGestureController gc = new PrintersGestureController();
            viewModel.GestureController = gc;

            Assert.IsNotNull(viewModel.GestureController);
            Assert.AreEqual(gc, viewModel.GestureController);
        }

        [TestMethod]
        public void Test_PrintersViewModel_GetSetRightPaneMode()
        {
            PrintersRightPaneMode rpm = PrintersRightPaneMode.AddPrinter;

            Assert.IsNotNull(viewModel.RightPaneMode);
            Assert.AreEqual(rpm, viewModel.RightPaneMode);
        }

        [TestMethod]
        public void Test_PrintersViewModel_DeleteCommand()
        {
            PrinterController.Instance.Initialize();
            viewModel.DeletePrinter.Execute("192.168.0.1");
            Assert.IsNotNull(viewModel.DeletePrinter);
        }

        [TestMethod]
        public void Test_PrintersViewModel_GridTapped()
        {
            PrinterController.Instance.Initialize();
            PrinterController.Instance.PrinterList.Add(new Printer()
                {
                    IpAddress = "192.168.0.1"
                });
            Messenger.Default.Send<string>("ClearDelete");
        }

        [TestMethod]
        public void Test_PrintersViewModel_OpenDefaultPrintSettingsCommand()
        {
            Printer printer = new Printer();
            printer.IpAddress = "192.168.0.1";
            viewModel.OpenDefaultPrinterSettings.Execute(printer);

            Assert.IsNotNull(viewModel.OpenDefaultPrinterSettings);
        }

        [TestMethod]
        public void Test_PrintersViewModel_OnNavigatedTo()
        {
            PrinterController.Instance.Initialize();
            viewModel.OnNavigatedTo();

        }

        [TestMethod]
        public void Test_PrintersViewModel_OnNavigatedFrom()
        {
            PrinterController.Instance.Initialize();
            viewModel.OnNavigatedFrom();

        }

        [TestMethod]
        public void Test_PrintersViewModel_ScreenMode()
        {
            var viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            PrinterController.Instance.Initialize();
            viewControlViewModel.ScreenMode = ScreenMode.Home;
        }

        [TestMethod]
        public void Test_PrintersViewModel_ViewModeFullScreen()
        {
            PrinterController.Instance.Initialize();
            PrintersGestureController gc = new PrintersGestureController();
            viewModel.GestureController = gc;

            var viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            viewControlViewModel.ScreenMode = ScreenMode.Printers;
            viewControlViewModel.ViewMode = ViewMode.FullScreen;
        }

        [TestMethod]
        public void Test_PrintersViewModel_ViewModeRightPane()
        {
            PrinterController.Instance.Initialize();
            PrintersGestureController gc = new PrintersGestureController();
            viewModel.GestureController = gc;

            var viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            viewControlViewModel.ScreenMode = ScreenMode.Printers;
            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible;
        }

        [TestMethod]
        public void Test_PrintersViewModel_PropertyChanged()
        {
            PrinterController.Instance.Initialize();
            viewModel.PropertyChanged += new PropertyChangedEventHandler(viewModel_PropertyChanged);
            viewModel.RightPaneMode = PrintersRightPaneMode.AddPrinter;
            
        }

        private void viewModel_PropertyChanged(object sender, System.ComponentModel.PropertyChangedEventArgs e)
        {
            // Note: Test for coverage only; No tests to assert
        }
    }
}
