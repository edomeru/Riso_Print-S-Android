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
    public class PageIndexToSliderValueConverterTest
    {
        private PageIndexToSliderValueConverter pageIndexToSliderValueConverter = new PageIndexToSliderValueConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = pageIndexToSliderValueConverter.Convert(null, null, null, null);
            Assert.AreEqual(0, result);

            double value = 0;
            result = pageIndexToSliderValueConverter.Convert(value, null, null, null);
            Assert.AreEqual(1.0, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            // Test null
            var result = pageIndexToSliderValueConverter.ConvertBack(null, null, null, null);
            Assert.AreEqual(0, result);
            
            double value = 1;
            result = pageIndexToSliderValueConverter.ConvertBack(value, null, null, null);
            Assert.AreEqual(0, result);
        }
    }
}
