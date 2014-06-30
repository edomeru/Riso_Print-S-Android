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
    public class DateTimeToStringConverterTest
    {
        private DateTimeToStringConverter dateTimeToStringConverter = new DateTimeToStringConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = dateTimeToStringConverter.Convert(null, null, null, null);
            Assert.AreEqual(String.Empty, result);

            // Test wrong type
            result = dateTimeToStringConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(String.Empty, result);

            var value = new DateTime(2014, 5, 12, 9, 0,  0);
            result = dateTimeToStringConverter.Convert(value, null, null, null);
            Assert.AreEqual("2014/05/12 09:00", result);
        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => dateTimeToStringConverter.ConvertBack(null, null, null, null));
        }
    }
}
