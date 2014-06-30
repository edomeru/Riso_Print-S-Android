using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Models;
using System.Collections.ObjectModel;

namespace SmartDeviceAppTests.Converters
{
    [TestClass]
    public class SelectPrinterToSeparatorVisibilityConverterTest
    {
        private SelectPrinterToSeparatorVisibilityConverter selectPrinterToSeparatorVisibilityConverter = new SelectPrinterToSeparatorVisibilityConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = selectPrinterToSeparatorVisibilityConverter.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test wrong type
            result = selectPrinterToSeparatorVisibilityConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var printer1 = new Printer();
            printer1.Id = 0;
            printer1.Name = "PRINTER1";
            var printerList = new ObservableCollection<Printer>();
            printerList.Add(printer1);
            new ViewModelLocator().SelectPrinterViewModel.PrinterList = printerList;

            // Test last printer
            var value = 0;
            result = selectPrinterToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var printer2 = new Printer();
            printer2.Id = 1;
            printer2.Name = "PRINTER2";
            printerList.Add(printer2);
            new ViewModelLocator().SelectPrinterViewModel.PrinterList = printerList;
            result = selectPrinterToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);

            value = 1;
            result = selectPrinterToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => selectPrinterToSeparatorVisibilityConverter.ConvertBack(null, null, null, null));
        }
    }
}
