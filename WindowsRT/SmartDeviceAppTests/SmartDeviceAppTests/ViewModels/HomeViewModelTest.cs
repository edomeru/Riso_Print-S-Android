using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class HomeViewModelTest
    {
        private HomeViewModel homeViewModel = new ViewModelLocator().HomeViewModel;

        [TestMethod]
        public void Test_HomeViewModel()
        {
            Assert.IsNotNull(homeViewModel);
        }

        [TestMethod]
        public void Test_OpenDocumentCommand()
        {
            Assert.IsNotNull(homeViewModel.OpenDocumentCommand);
            homeViewModel.OpenDocumentCommand.Execute(null);
        }

        [TestMethod]
        public void Test_IsProgressRingActive()
        {
            homeViewModel.IsProgressRingActive = true;
            Assert.IsTrue(homeViewModel.IsProgressRingActive);
        }
    }
}
