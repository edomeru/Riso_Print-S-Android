using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintSettingsPaneViewModelTest
    {
        private PrintSettingsPaneViewModel printSettingsPaneViewModel = new ViewModelLocator().PrintSettingsPaneViewModel;

        [TestMethod]
        public void Test_PrintSettingsPaneViewModel()
        {
            Assert.IsNotNull(printSettingsPaneViewModel);
        }

        [TestMethod]
        public void Test_PrintSettingsPaneMode()
        {
            var printSettingsPaneMode = PrintSettingsPaneMode.PrintSettings;
            printSettingsPaneViewModel.PrintSettingsPaneMode = printSettingsPaneMode;
            Assert.AreEqual(printSettingsPaneMode, printSettingsPaneViewModel.PrintSettingsPaneMode);
        }
    }
}
