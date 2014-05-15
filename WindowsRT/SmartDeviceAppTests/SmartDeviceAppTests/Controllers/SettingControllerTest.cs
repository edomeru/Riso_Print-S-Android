using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using Windows.Storage;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class SettingControllerTest
    {

        private const string SAMPLE_CARD_ID_VALUE = "sample_card_id_value";

        private SettingsViewModel _settingsViewModel;

        // from SettingController.cs
        private const string KEY_SETTINGS_CARD_READER_CARD_ID = "key_card_reader_card_id";

        // Cover Unit Tests using dotCover does not call TestInitialize
        //[TestInitialize]
        //public void Initialize()
        private void Initialize()
        {
            _settingsViewModel = new ViewModelLocator().SettingsViewModel;
        }

        // Cover Unit Tests using dotCover does not call TestCleanup
        //[TestCleanup]
        //public void Cleanup()
        private void Cleanup()
        {
            _settingsViewModel = null;
        }

        [TestMethod]
        public void Test_Initialize_KeyNotExists()
        {
            Initialize(); // Workaround for Cover Unit Tests using dotCover

            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values.Remove(KEY_SETTINGS_CARD_READER_CARD_ID);

            SettingController.Instance.Initialize();
            Assert.AreEqual(string.Empty, _settingsViewModel.CardId);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_Initialize_KeyExists()
        {
            Initialize(); // Workaround for Cover Unit Tests using dotCover

            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values[KEY_SETTINGS_CARD_READER_CARD_ID] = SAMPLE_CARD_ID_VALUE;

            SettingController.Instance.Initialize();
            Assert.AreEqual(SAMPLE_CARD_ID_VALUE, _settingsViewModel.CardId);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_CardIdTextChanged_Null()
        {
            Initialize(); // Workaround for Cover Unit Tests using dotCover

            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values.Remove(KEY_SETTINGS_CARD_READER_CARD_ID);

            SettingController.Instance.CardIdTextChanged(null);

            string after = string.Empty;
            if (localSettings.Values.ContainsKey(KEY_SETTINGS_CARD_READER_CARD_ID))
            {
                after = localSettings.Values[KEY_SETTINGS_CARD_READER_CARD_ID].ToString();
            }

            Assert.AreEqual(string.Empty, after);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

        [TestMethod]
        public void Test_CardIdTextChanged_Valid()
        {
            Initialize(); // Workaround for Cover Unit Tests using dotCover

            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values.Remove(KEY_SETTINGS_CARD_READER_CARD_ID);

            SettingController.Instance.CardIdTextChanged(SAMPLE_CARD_ID_VALUE);

            string after = string.Empty;
            if (localSettings.Values.ContainsKey(KEY_SETTINGS_CARD_READER_CARD_ID))
            {
                after = localSettings.Values[KEY_SETTINGS_CARD_READER_CARD_ID].ToString();
            }

            Assert.AreEqual(SAMPLE_CARD_ID_VALUE, after);

            Cleanup(); // Workaround for Cover Unit Tests using dotCover
        }

    }
}
