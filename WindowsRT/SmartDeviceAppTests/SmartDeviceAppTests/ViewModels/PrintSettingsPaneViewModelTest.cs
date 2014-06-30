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
        private ViewControlViewModel viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
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

            printSettingsPaneMode = PrintSettingsPaneMode.PrintSettingOptions;
            printSettingsPaneViewModel.PrintSettingsPaneMode = printSettingsPaneMode;
            Assert.AreEqual(printSettingsPaneMode, printSettingsPaneViewModel.PrintSettingsPaneMode);
        }

        [TestMethod]
        public void Test_SetPrintSettingsPaneMode()
        {
            var printSettingsPaneMode = PrintSettingsPaneMode.PrintSettingOptions;
            printSettingsPaneViewModel.PrintSettingsPaneMode = printSettingsPaneMode;
            Assert.AreEqual(printSettingsPaneMode, printSettingsPaneViewModel.PrintSettingsPaneMode);

            // Test not printpreview screen
            viewControlViewModel.ScreenMode = ScreenMode.Home;
            viewControlViewModel.ViewMode = ViewMode.MainMenuPaneVisible;
            
            viewControlViewModel.ScreenMode = ScreenMode.PrintPreview;
            viewControlViewModel.ViewMode = ViewMode.FullScreen;
            Assert.AreEqual(PrintSettingsPaneMode.PrintSettings, printSettingsPaneViewModel.PrintSettingsPaneMode);
        }
    }
}
