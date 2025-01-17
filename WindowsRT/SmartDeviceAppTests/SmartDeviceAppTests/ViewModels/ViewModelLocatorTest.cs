﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.ViewModels;
using GalaSoft.MvvmLight;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class ViewModelLocatorTest
    {
        private ViewModelLocator viewModelLocator = new ViewModelLocator();

        [TestMethod]
        public void Test_ViewModelLocator()
        {
            var test = new ViewModelLocator();
            var viewModel = new ViewModelLocator().ViewControlViewModel;
            // Note: Cannot change value of ViewModelBase.IsInDesignModeStatic
            Assert.IsNotNull(viewModelLocator);
        }

        [TestMethod]
        public void Test_ViewControlViewModel()
        {
            Assert.IsNotNull(viewModelLocator.ViewControlViewModel);
        }

        [TestMethod]
        public void Test_HomeViewModel()
        {
            Assert.IsNotNull(viewModelLocator.HomeViewModel);
        }

        [UI.UITestMethod]
        public void Test_PrintPreviewViewModel()
        {
            Assert.IsNotNull(viewModelLocator.PrintPreviewViewModel);
        }

        [TestMethod]
        public void Test_PrintersViewModel()
        {
            Assert.IsNotNull(viewModelLocator.PrintersViewModel);
        }

        [TestMethod]
        public void Test_JobsViewModel()
        {
            Assert.IsNotNull(viewModelLocator.JobsViewModel);
        }

        [TestMethod]
        public void Test_SettingsViewModel()
        {
            Assert.IsNotNull(viewModelLocator.SettingsViewModel);
        }

        [TestMethod]
        public void Test_HelpViewModel()
        {
            Assert.IsNotNull(viewModelLocator.HelpViewModel);
        }

        [TestMethod]
        public void Test_LegalViewModel()
        {
            Assert.IsNotNull(viewModelLocator.LegalViewModel);
        }

        [TestMethod]
        public void Test_PrintSettingsPaneViewModel()
        {
            Assert.IsNotNull(viewModelLocator.PrintSettingsPaneViewModel);
        }

        [TestMethod]
        public void Test_PrintSettingsViewModel()
        {
            Assert.IsNotNull(viewModelLocator.PrintSettingsViewModel);
        }

        [TestMethod]
        public void Test_SelectPrinterViewModel()
        {
            Assert.IsNotNull(viewModelLocator.SelectPrinterViewModel);
        }

        [TestMethod]
        public void Test_PrintSettingOptionsViewModel()
        {
            Assert.IsNotNull(viewModelLocator.PrintSettingOptionsViewModel);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel()
        {
            Assert.IsNotNull(viewModelLocator.SearchPrinterViewModel);
        }

        [TestMethod]
        public void Test_AddPrinterViewModel()
        {
            Assert.IsNotNull(viewModelLocator.AddPrinterViewModel);
        }

        [TestMethod]
        public void Test_Cleanup()
        {
            ViewModelLocator.Cleanup();
        }
    }
}
