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
    public class PrintSettingOptionToSeparatorVisibilityConverterTest
    {
        private PrintSettingOptionToSeparatorVisibilityConverter printSettingOptionToSeparatorVisibilityConverter = new PrintSettingOptionToSeparatorVisibilityConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printSettingOptionToSeparatorVisibilityConverter.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test wrong type
            result = printSettingOptionToSeparatorVisibilityConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var printSetting = new PrintSetting();
            printSetting.Text = "PRINT_SETTING";

            // Test options = null
            new ViewModelLocator().PrintSettingsViewModel.SelectedPrintSetting = printSetting;
            result = printSettingOptionToSeparatorVisibilityConverter.Convert(0, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test 1 option
            var option1 = new PrintSettingOption();
            option1.Text = "OPTION1";
            var options = new List<PrintSettingOption>();
            options.Add(option1);
            printSetting.Options = options;
            new ViewModelLocator().PrintSettingsViewModel.SelectedPrintSetting = printSetting;
            result = printSettingOptionToSeparatorVisibilityConverter.Convert(0, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test 2 options, last item
            var option2= new PrintSettingOption();
            option2.Text = "OPTION2";
            options.Add(option2);
            printSetting.Options = options;
            new ViewModelLocator().PrintSettingsViewModel.SelectedPrintSetting = printSetting;
            result = printSettingOptionToSeparatorVisibilityConverter.Convert(1, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test 2 options, not last item
            var option3 = new PrintSettingOption();
            option3.Text = "OPTION3";
            options.Add(option3);
            printSetting.Options = options;
            new ViewModelLocator().PrintSettingsViewModel.SelectedPrintSetting = printSetting;
            result = printSettingOptionToSeparatorVisibilityConverter.Convert(1, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = printSettingOptionToSeparatorVisibilityConverter.ConvertBack(null, null, null, null);
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
