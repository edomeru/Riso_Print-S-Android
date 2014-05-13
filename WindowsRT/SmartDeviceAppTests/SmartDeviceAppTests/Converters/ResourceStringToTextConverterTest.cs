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
    public class ResourceStringToTextConverterTest
    {
        private ResourceStringToTextConverter resourceStringToTextConverter = new ResourceStringToTextConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = resourceStringToTextConverter.Convert(null, null, null, null);
            Assert.AreEqual(String.Empty, result);

            // Test wrong type
            result = resourceStringToTextConverter.Convert(0, null, null, null);
            Assert.AreEqual(String.Empty, result);

            // Test empty string
            result = resourceStringToTextConverter.Convert(String.Empty, null, null, null);
            Assert.AreEqual(String.Empty, result);

            // Test parameter not null but wrong type
            var parameter = "TEST";
            result = resourceStringToTextConverter.Convert(String.Empty, null, parameter, null);
            Assert.AreEqual(String.Empty, result);

            // Test value
            var value = "IDS_APP_NAME";
            result = resourceStringToTextConverter.Convert(value, null, null, null);
            Assert.AreEqual("RISO Smart Print", result);

            // Test uppercase
            value = "IDS_APP_NAME";
            parameter = "Uppercase";
            result = resourceStringToTextConverter.Convert(value, null, parameter, null);
            Assert.AreEqual("RISO SMART PRINT", result);

            // Test wrong value
            value = "TEST";
            result = resourceStringToTextConverter.Convert(value, null, null, null);
            Assert.AreEqual(String.Empty, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = resourceStringToTextConverter.ConvertBack(null, null, null, null);
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
