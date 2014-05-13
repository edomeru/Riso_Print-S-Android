using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using Windows.Storage;

namespace SmartDeviceAppTests.Controllers
{
    [TestClass]
    public class SettingControllerTest
    {

        private const string SAMPLE_CARD_ID_VALUE = "sample_card_id_value";

        // from SettingController.cs
        private const string KEY_SETTINGS_CARD_READER_CARD_ID = "key_card_reader_card_id";

        [TestInitialize]
        public void Initialize()
        {
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values.Remove(KEY_SETTINGS_CARD_READER_CARD_ID);
        }

        [TestMethod]
        public void Test_Initialize()
        {
            SettingController.Instance.Initialize();
            // Note: no public property to assert
        }

        [TestMethod]
        public void Test_CardIdTextChanged_Null()
        {
            var localSettings = ApplicationData.Current.LocalSettings;

            string before = string.Empty;
            if (localSettings.Values.ContainsKey(KEY_SETTINGS_CARD_READER_CARD_ID))
            {
                before = localSettings.Values[KEY_SETTINGS_CARD_READER_CARD_ID].ToString();
            }

            SettingController.Instance.CardIdTextChanged(null);

            string after = string.Empty;
            if (localSettings.Values.ContainsKey(KEY_SETTINGS_CARD_READER_CARD_ID))
            {
                after = localSettings.Values[KEY_SETTINGS_CARD_READER_CARD_ID].ToString();
            }

            Assert.AreEqual(before, after);
        }

        [TestMethod]
        public void Test_CardIdTextChanged_Valid()
        {
            var localSettings = ApplicationData.Current.LocalSettings;

            SettingController.Instance.CardIdTextChanged(SAMPLE_CARD_ID_VALUE);

            string after = string.Empty;
            if (localSettings.Values.ContainsKey(KEY_SETTINGS_CARD_READER_CARD_ID))
            {
                after = localSettings.Values[KEY_SETTINGS_CARD_READER_CARD_ID].ToString();
            }

            Assert.AreEqual(SAMPLE_CARD_ID_VALUE, after);
        }

    }
}
