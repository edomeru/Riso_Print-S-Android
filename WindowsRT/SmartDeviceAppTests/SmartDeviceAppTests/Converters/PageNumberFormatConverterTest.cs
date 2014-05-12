using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PageNumberFormatConverterTest
    {
        private PageNumberFormatConverter pageNumberFormatConverter = new PageNumberFormatConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = pageNumberFormatConverter.Convert(null, null, null, null);
            Assert.AreEqual(String.Empty, result);

            // Test wrong type
            result = pageNumberFormatConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(String.Empty, result);

            var value = new PageNumberInfo(0, 10, PageViewMode.SinglePageView);
            result = pageNumberFormatConverter.Convert(value, null, null, null);
            Assert.AreEqual("Page 1 / 10", result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = pageNumberFormatConverter.ConvertBack(null, null, null, null);
                Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
