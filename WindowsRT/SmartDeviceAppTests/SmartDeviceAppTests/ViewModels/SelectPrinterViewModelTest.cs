﻿using SmartDeviceApp.Models;
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
        public void Test_PrinterList()
        {
            var printerList = new ObservableCollection<Printer>();
            Printer printer = PrinterController.Instance.PrinterList.LastOrDefault();
            printerList.Add(printer);
            selectPrinterViewModel.PrinterList = printerList;
            Assert.AreEqual(printerList, selectPrinterViewModel.PrinterList);
        }

        [TestMethod]
        public void Test_SelectedPrinterId()
        {
            var selectedPrinterId = 1;
            selectPrinterViewModel.SelectedPrinterId = selectedPrinterId;
            Assert.AreEqual(selectedPrinterId, selectPrinterViewModel.SelectedPrinterId);
        }

        [TestMethod]
        public void Test_SelectPrinter()
        {
            var printer = new Printer();
            printer.Id = 0;
            selectPrinterViewModel.SelectPrinter.Execute(printer);
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
