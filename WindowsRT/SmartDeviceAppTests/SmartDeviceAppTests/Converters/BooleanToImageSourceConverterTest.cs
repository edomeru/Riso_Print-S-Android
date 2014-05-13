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
    public class BooleanToImageSourceConverterTest
    {
        private BooleanToImageSourceConverter booleanToImageSourceConverter = new BooleanToImageSourceConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = booleanToImageSourceConverter.Convert(null, null, null, null);
            Assert.AreEqual(DependencyProperty.UnsetValue, result);

            // Test wrong type
            result = booleanToImageSourceConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(DependencyProperty.UnsetValue, result);

            var value = true;
            result = booleanToImageSourceConverter.Convert(value, null, null, null);
            Assert.AreEqual("ms-appx:///Resources/Images/img_btn_printer_status_online.scale - 100.png", result);

            value = false;
            result = booleanToImageSourceConverter.Convert(value, null, null, null);
            Assert.AreEqual("ms-appx:///Resources/Images/img_btn_printer_status_offline.scale - 100.png", result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = booleanToImageSourceConverter.ConvertBack(null, null, null, null);
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
