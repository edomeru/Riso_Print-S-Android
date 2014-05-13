using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class IntToStringConverterTest
    {
        private IntToStringConverter intToStringConverter = new IntToStringConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = intToStringConverter.Convert(null, null, null, null);
            Assert.AreEqual(String.Empty, result);

            // Test wrong type
            result = intToStringConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(String.Empty, result);

            var value = 0;
            result = intToStringConverter.Convert(value, null, null, null);
            Assert.AreEqual("0", result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            // Test null
            var result = intToStringConverter.ConvertBack(null, null, null, null);
            Assert.AreEqual(0, result);

            var value = "0";
            result = intToStringConverter.ConvertBack(value, null, null, null);
            Assert.AreEqual(0, result);

            // Test fail
            result = intToStringConverter.ConvertBack("TEST", null, null, null);
            Assert.AreEqual(0, result);
        }
    }
}
