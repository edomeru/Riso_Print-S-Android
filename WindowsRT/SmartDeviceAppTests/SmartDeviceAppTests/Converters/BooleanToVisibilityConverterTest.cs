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
    public class BooleanToVisibilityConverterTest
    {
        private BooleanToVisibilityConverter booleanToVisibilityConverter = new BooleanToVisibilityConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = booleanToVisibilityConverter.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var value = true;
            result = booleanToVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);

            value = false;
            result = booleanToVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test parameter
            var parameter = true;
            value = true;
            result = booleanToVisibilityConverter.Convert(value, null, parameter, null);
            Assert.AreEqual(Visibility.Collapsed, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            var value = Visibility.Visible;
            var result = booleanToVisibilityConverter.ConvertBack(value, null, null, null);
            Assert.AreEqual(true, result);

            value = Visibility.Collapsed;
            result = booleanToVisibilityConverter.ConvertBack(value, null, null, null);
            Assert.AreEqual(false, result);
        }
    }
}
