using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintSettingOptionToBooleanConverterTest
    {
        private PrintSettingOptionToBooleanConverter printSettingOptionToBooleanConverter = new PrintSettingOptionToBooleanConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printSettingOptionToBooleanConverter.Convert(null, null, null, null);
            Assert.AreEqual(false, result);

            // Test wrong type
            result = printSettingOptionToBooleanConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(false, result);

            var option1 = 0;
            var printSetting1 = new PrintSetting();
            printSetting1.Text = "PRINT_SETTING1";
            printSetting1.Value = option1;
            var option2 = 1;

            new ViewModelLocator().PrintSettingsViewModel.SelectedPrintSetting = printSetting1;
            var value = option1;
            result = printSettingOptionToBooleanConverter.Convert(value, null, null, null);
            Assert.AreEqual(true, result);

            value = option2;
            result = printSettingOptionToBooleanConverter.Convert(value, null, null, null);
            Assert.AreEqual(false, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = printSettingOptionToBooleanConverter.ConvertBack(null, null, null, null);
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
