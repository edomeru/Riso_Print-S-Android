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
    public class PrintSettingToValueConverterTest
    {
        private PrintSettingToValueConverter printSettingToValueConverter = new PrintSettingToValueConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null value
            var result = printSettingToValueConverter.Convert(null, null, null, null);
            Assert.AreEqual(null, result);

            // Test empty value
            result = printSettingToValueConverter.Convert(String.Empty, null, null, null);
            Assert.AreEqual(null, result);

            // Test null parameter
            result = printSettingToValueConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(null, result);

            // Test empty parameter
            result = printSettingToValueConverter.Convert("TEST", null, String.Empty, null);
            Assert.AreEqual(null, result);

            var value1 = "0";
            var value2 = "true";
            var intValue = 0;
            var boolValue = true;
            
            var parameter = "copies";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "scaleToFit";
            result = printSettingToValueConverter.Convert(value2, null, parameter, null);
            Assert.AreEqual(boolValue, result);

            parameter = "booklet";
            result = printSettingToValueConverter.Convert(value2, null, parameter, null);
            Assert.AreEqual(boolValue, result);

            parameter = "colorMode";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "orientation";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "duplex";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "paperSize";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "paperType";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "inputTray";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "imposition";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "impositionOrder";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "sort";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "bookletFinish";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "bookletLayout";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "finishingSide";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "staple";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "punch";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "outputTray";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(intValue, result);

            parameter = "securePrint";
            result = printSettingToValueConverter.Convert(value2, null, parameter, null);
            Assert.AreEqual(boolValue, result);

            parameter = "pinCode";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(value1, result);

            // Test wrong parameter
            parameter = "TEST";
            result = printSettingToValueConverter.Convert(value1, null, parameter, null);
            Assert.AreEqual(null, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => printSettingToValueConverter.ConvertBack(null, null, null, null));
        }
    }
}
