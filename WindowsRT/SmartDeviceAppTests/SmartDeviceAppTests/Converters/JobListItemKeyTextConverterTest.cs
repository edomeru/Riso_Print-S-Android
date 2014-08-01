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
    public class JobListItemKeyTextConverterTest
    {
        private JobListItemKeyTextConverter jobListItemKeyTextConverter = new JobListItemKeyTextConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = jobListItemKeyTextConverter.Convert(null, null, null, null);
            Assert.AreEqual(String.Empty, result);

            var value = true;
            result = jobListItemKeyTextConverter.Convert(value, null, null, null);
            Assert.AreEqual(String.Empty, result);

            var strValue = string.Empty;
            result = jobListItemKeyTextConverter.Convert(strValue, null, null, null);
            Assert.AreEqual(String.Empty, result);

            strValue = "sample.pdf";
            result = jobListItemKeyTextConverter.Convert(strValue, null, null, null);
            Assert.AreNotEqual(String.Empty, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => jobListItemKeyTextConverter.ConvertBack(null, null, null, null));
        }
    }
}
