﻿using System;
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
    public class PrinterNameToTextConverterTest
    {
        private PrinterNameToTextConverter printerNameToTextConverter = new PrinterNameToTextConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printerNameToTextConverter.Convert(null, null, null, null);
            Assert.AreEqual("No Name", result);

            // Test empty string
            result = printerNameToTextConverter.Convert(String.Empty, null, null, null);
            Assert.AreEqual("No Name", result);

            var value = "PRINTER_NAME";
            result = printerNameToTextConverter.Convert(value, null, null, null);
            Assert.AreEqual("PRINTER_NAME", result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => printerNameToTextConverter.ConvertBack(null, null, null, null));
        }
    }
}
