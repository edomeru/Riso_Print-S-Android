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

namespace SmartDeviceAppTests.Converters
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
            printSetting2.Text = "PRINT_SETTING2";
            printSettings.Add(printSetting2);
            group.PrintSettings = printSettings;
            printSettingsList = new PrintSettingList();
            printSettingsList.Add(group);
            new ViewModelLocator().PrintSettingsViewModel.PrintSettingsList = printSettingsList;
            result = printSettingToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);

            // Test more than 1 group
            var printSetting3 = new PrintSetting();
            printSetting3.Text = "PRINT_SETTING3";
            var printSettings2 = new List<PrintSetting>();
            printSettings2.Add(printSetting3);
            var group2 = new PrintSettingGroup();
            group2.PrintSettings = printSettings2;
            printSettingsList.Add(group2);
            new ViewModelLocator().PrintSettingsViewModel.PrintSettingsList = printSettingsList;
            value = printSetting3;
            result = printSettingToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => printSettingToSeparatorVisibilityConverter.ConvertBack(null, null, null, null));
        }
    }
}
