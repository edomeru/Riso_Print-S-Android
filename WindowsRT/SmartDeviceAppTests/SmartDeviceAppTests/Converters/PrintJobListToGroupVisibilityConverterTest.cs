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
    public class PrintJobListToGroupVisibilityConverterTest
    {
        private PrintJobListToGroupVisibilityConverter printJobListToGroupVisibilityConverter = new PrintJobListToGroupVisibilityConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printJobListToGroupVisibilityConverter.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test wrong type
            result = printJobListToGroupVisibilityConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var value = 0;
            result = printJobListToGroupVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            value = 1;
            result = printJobListToGroupVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);
        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => printJobListToGroupVisibilityConverter.ConvertBack(null, null, null, null));
        }
    }
}
