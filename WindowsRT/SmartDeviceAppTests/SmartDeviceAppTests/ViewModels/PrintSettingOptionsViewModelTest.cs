using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintSettingOptionsViewModelTest
    {
        private PrintSettingOptionsViewModel printSettingOptionsViewModel = new ViewModelLocator().PrintSettingOptionsViewModel;

        [TestMethod]
        public void Test_PrintSettingOptionsViewModel()
        {
            Assert.IsNotNull(printSettingOptionsViewModel);
        }

        [TestMethod]
        public void Test_PrintSetting()
        {
            var printSetting = new PrintSetting();
            printSetting.Text = "PRINT_SETTING_TEXT";
            printSettingOptionsViewModel.PrintSetting = printSetting;
            Assert.AreEqual(printSetting, printSettingOptionsViewModel.PrintSetting);
        }

        [TestMethod]
        public void Test_SelectPrintSettingOption()
        {
            var option = new PrintSettingOption();
            option.Text = "PRINT_SETTING_OPTION_TEXT";
            option.Index = 0;
            var printSetting = new PrintSetting();
            printSetting.Text = "PRINT_SETTING_TEXT";
            printSettingOptionsViewModel.PrintSetting = printSetting;
            printSettingOptionsViewModel.SelectPrintSettingOption.Execute(option.Index);
            Assert.IsNotNull(printSettingOptionsViewModel.SelectPrintSettingOption);
            Assert.AreEqual(0, printSettingOptionsViewModel.PrintSetting.Value);
        }

        [TestMethod]
        public void Test_BackToPrintSettings()
        {
            printSettingOptionsViewModel.BackToPrintSettings.Execute(null);
            Assert.IsNotNull(printSettingOptionsViewModel.BackToPrintSettings);
        }
    }
}
