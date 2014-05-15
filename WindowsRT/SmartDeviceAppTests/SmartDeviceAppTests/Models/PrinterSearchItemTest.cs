using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Models;
using System.ComponentModel;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Models
{
    [TestClass]
    public class PrinterSearchItemTest
    {

        [UI.UITestMethod]
        public void Test_PrinterSearchItem_Get()
        {
            PrinterSearchItem item = new PrinterSearchItem();
            item.Name = "name";
            item.Ip_address = "172.0.0.1";
            item.IsInPrinterList = false;

            Assert.IsNotNull(item.Name);
            Assert.IsNotNull(item.Ip_address);
            Assert.IsNotNull(item.IsInPrinterList);
            Assert.IsNotNull(item.ImageSource);
        }

        [UI.UITestMethod]
        public void Test_OnPropertyChanged()
        {
            PrinterSearchItem item = new PrinterSearchItem();
            item.PropertyChanged += MockOnPropertyChanged;

            item.Name = "name";
            item.Ip_address = "172.0.0.1";
            item.IsInPrinterList = false;

            Assert.IsNotNull(item.Name);
            Assert.IsNotNull(item.Ip_address);
            Assert.IsNotNull(item.IsInPrinterList);
            Assert.IsNotNull(item.ImageSource);

            item.PropertyChanged -= MockOnPropertyChanged;
        }

        #region Mock Functions

        public void MockOnPropertyChanged(object source, PropertyChangedEventArgs args)
        {
            // Do nothing
        }

        #endregion Mock Functions

    }
}
