using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using SmartDeviceApp.Models;
using System.Collections.ObjectModel;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrinterSearchItemToSeparatorVisibilityTest
    {
        private PrinterSearchItemToSeparatorVisibility printerSearchItemToSeparatorVisibility = new PrinterSearchItemToSeparatorVisibility();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printerSearchItemToSeparatorVisibility.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test wrong type
            result = printerSearchItemToSeparatorVisibility.Convert("TEST", null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var printerSearchItem1 = new PrinterSearchItem();
            printerSearchItem1.Name = "PRINTER_SEARCH_ITEM1";
            printerSearchItem1.Ip_address = "192.168.1.1";
            var printerSearchItem2 = new PrinterSearchItem();
            printerSearchItem2.Name = "PRINTER_SEARCH_ITEM2";
            printerSearchItem2.Ip_address = "192.168.1.2";
            var printerSearchList = new ObservableCollection<PrinterSearchItem>();
            printerSearchList.Add(printerSearchItem1);
            printerSearchList.Add(printerSearchItem2);
            new ViewModelLocator().SearchPrinterViewModel.PrinterSearchList = printerSearchList;

            var value = printerSearchItem1;
            result = printerSearchItemToSeparatorVisibility.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);

            value = printerSearchItem2;
            result = printerSearchItemToSeparatorVisibility.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = printerSearchItemToSeparatorVisibility.ConvertBack(null, null, null, null);
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
