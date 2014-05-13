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
    public class StringToVisibilityConverterTest
    {
        private StringToVisibilityConverter stringToVisibilityConverter = new StringToVisibilityConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = stringToVisibilityConverter.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test wrong type
            result = stringToVisibilityConverter.Convert(0, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test empty string
            result = stringToVisibilityConverter.Convert(String.Empty, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var value = "TEST";
            result = stringToVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = stringToVisibilityConverter.ConvertBack(null, null, null, null);
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
