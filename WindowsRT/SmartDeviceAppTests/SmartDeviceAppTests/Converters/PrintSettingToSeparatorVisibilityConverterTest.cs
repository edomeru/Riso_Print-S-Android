using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using SmartDeviceApp.Models;
using System.Collections.ObjectModel;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintSettingToSeparatorVisibilityConverterTest
    {
        private PrintSettingToSeparatorVisibilityConverter printSettingToSeparatorVisibilityConverter = new PrintSettingToSeparatorVisibilityConverter();

        [TestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = printSettingToSeparatorVisibilityConverter.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test wrong type
            result = printSettingToSeparatorVisibilityConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test last item
            var printSetting1 = new PrintSetting();
            printSetting1.Text = "PRINT_SETTING1";
            var printSettings = new List<PrintSetting>();
            printSettings.Add(printSetting1);
            var group = new PrintSettingGroup();
            group.PrintSettings = printSettings;
            var printSettingsList = new PrintSettingList();
            printSettingsList.Add(group);
            new ViewModelLocator().PrintSettingsViewModel.PrintSettingsList = printSettingsList;
            var value = printSetting1;
            result = printSettingToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test not last item
            var printSetting2 = new PrintSetting();
            printSetting2.Text = "PRINT_SETTING1";
            printSettings.Add(printSetting2);
            group.PrintSettings = printSettings;
            printSettingsList = new PrintSettingList();
            printSettingsList.Add(group);
            new ViewModelLocator().PrintSettingsViewModel.PrintSettingsList = printSettingsList;
            result = printSettingToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = printSettingToSeparatorVisibilityConverter.ConvertBack(null, null, null, null);
                Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
