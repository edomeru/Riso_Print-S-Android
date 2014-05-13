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
    public class LegalViewModelTest
    {
        private LegalViewModel legalViewModel = new ViewModelLocator().LegalViewModel;

        [TestMethod]
        public void Test_LegalViewModel()
        {
            Assert.IsNotNull(legalViewModel);
        }
    }
}
