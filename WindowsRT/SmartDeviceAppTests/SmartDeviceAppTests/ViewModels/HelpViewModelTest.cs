using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class HelpViewModelTest
    {
        private HelpViewModel homeViewModel = new ViewModelLocator().HelpViewModel;

        [TestMethod]
        public void Test_HelpViewModel()
        {
            Assert.IsNotNull(homeViewModel);
        }
    }
}
