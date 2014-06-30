using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Controllers;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class SettingsViewModelTest
    {
        private SettingsViewModel settingsViewModel = new ViewModelLocator().SettingsViewModel;

        [TestMethod]
        public void Test_SettingsViewModel()
        {
            Assert.IsNotNull(settingsViewModel);
        }

        private void Test_CardIdValueChangedEventHandler(string value)
        {
        }           

        [TestMethod]
        public void Test_CardId()
        {
            SettingController.CardIdValueChangedEventHandler eventHandler = new SettingController.CardIdValueChangedEventHandler(Test_CardIdValueChangedEventHandler);
            settingsViewModel.CardIdValueChangedEventHandler += eventHandler;

            var cardid = "CARD_ID";
            settingsViewModel.CardId = cardid;
            Assert.AreEqual(cardid, settingsViewModel.CardId);
        }
    }
}
