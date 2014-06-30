using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;

namespace SmartDeviceAppTests.Converters
{
    [TestClass]
    public class PrinterStatusToImageSourceConverterTest
    {
        private PrinterStatusToImageSourceConverter printerStatusToImageSourceConverter = new PrinterStatusToImageSourceConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printerStatusToImageSourceConverter.Convert(null, null, null, null);
            Assert.AreEqual(DependencyProperty.UnsetValue, result);

            // Test wrong type
            result = printerStatusToImageSourceConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(DependencyProperty.UnsetValue, result);

            var value = true;
            result = printerStatusToImageSourceConverter.Convert(value, null, null, null);
            Assert.AreEqual("ms-appx:///Resources/Images/img_btn_printer_status_online.png", result);

            value = false;
            result = printerStatusToImageSourceConverter.Convert(value, null, null, null);
            Assert.AreEqual("ms-appx:///Resources/Images/img_btn_printer_status_offline.png", result);

            // Test invert
            result = printerStatusToImageSourceConverter.Convert(value, null, true, null);
            Assert.AreEqual("ms-appx:///Resources/Images/img_btn_printer_status_online.png", result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() =>  printerStatusToImageSourceConverter.ConvertBack(null, null, null, null));
        }
    }
}
