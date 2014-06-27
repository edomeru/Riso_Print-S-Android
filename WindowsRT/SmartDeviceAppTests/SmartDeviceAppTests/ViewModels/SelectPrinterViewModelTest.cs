using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using System.Collections.ObjectModel;
using SmartDeviceApp.Controllers;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class SelectPrinterViewModelTest
    {
        private SelectPrinterViewModel selectPrinterViewModel = new ViewModelLocator().SelectPrinterViewModel;

        [TestMethod]
        public void Test_PrintSettingOptionsViewModel()
        {
            Assert.IsNotNull(selectPrinterViewModel);
            PrinterController.Instance.Initialize();
        }

        [TestMethod]
        public async Task Test_PrinterList()
        {
            await PrinterController.Instance.Initialize();
            PrinterController.Instance.PrinterList.Add(new Printer() { IpAddress = "192.168.0.1" });
            var printerList = new ObservableCollection<Printer>();
            Printer printer = PrinterController.Instance.PrinterList.LastOrDefault();
            printerList.Add(printer);
            selectPrinterViewModel.PrinterList = printerList;
            Assert.AreEqual(printerList, selectPrinterViewModel.PrinterList);
        }

        private void Test_SelectedPrinterChangedEventHandler(int id)
        {
        }

        [TestMethod]
        public void Test_SelectPrinter()
        {
            PrintPreviewController.SelectedPrinterChangedEventHandler eventHandler = new PrintPreviewController.SelectedPrinterChangedEventHandler(Test_SelectedPrinterChangedEventHandler);
            selectPrinterViewModel.SelectPrinterEvent += eventHandler;
            Printer printer = new Printer();
            printer.Id = 0;
            selectPrinterViewModel.SelectPrinter.Execute(printer.Id);
            Assert.IsNotNull(selectPrinterViewModel.SelectPrinter);
        }

        [TestMethod]
        public void Test_BackToPrintSettings()
        {
            selectPrinterViewModel.BackToPrintSettings.Execute(null);
            Assert.IsNotNull(selectPrinterViewModel.BackToPrintSettings);
        }
    }
}
