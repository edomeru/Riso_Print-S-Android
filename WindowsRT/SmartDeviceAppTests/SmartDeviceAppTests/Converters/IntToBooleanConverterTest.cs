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
    public class IntToBooleanConverterTest
    {
        private IntToBooleanConverter intToBooleanConverter = new IntToBooleanConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = intToBooleanConverter.Convert(null, null, null, null);
            Assert.AreEqual(false, result);

            var value = 0;
            result = intToBooleanConverter.Convert(value, null, null, null);
            Assert.AreEqual(false, result);

            value = 1;
            result = intToBooleanConverter.Convert(value, null, null, null);
            Assert.AreEqual(true, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            // Test null
            var result = intToBooleanConverter.ConvertBack(null, null, null, null);
            Assert.AreEqual(0, result);

            var value = true;
            result = intToBooleanConverter.ConvertBack(value, null, null, null);
            Assert.AreEqual(1, result);

            value = false;
            result = intToBooleanConverter.ConvertBack(value, null, null, null);
            Assert.AreEqual(0, result);
        }
    }
}
