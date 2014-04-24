//
//  SettingController.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/15.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.ViewModels;
using System;
using Windows.Storage;

namespace SmartDeviceApp.Controllers
{
    public sealed class SettingController
    {

        static readonly SettingController _instance = new SettingController();

        private const string KEY_SETTINGS_CARD_READER_CARD_ID = "key_card_reader_card_id";

        private SettingsViewModel _settingsViewModel;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static SettingController() { }

        private SettingController()
        {
            _settingsViewModel = new ViewModelLocator().SettingsViewModel;
        }

        public static SettingController Instance
        {
            get { return _instance; }
        }

        public void Initialize()
        {
            var localSettings = ApplicationData.Current.LocalSettings;

            string cardId = string.Empty;
            if (localSettings.Values.ContainsKey(KEY_SETTINGS_CARD_READER_CARD_ID))
            {
                cardId = localSettings.Values[KEY_SETTINGS_CARD_READER_CARD_ID].ToString();
            }
            _settingsViewModel.CardId = cardId;
        }

        /// <summary>
        /// Event handler for card ID text
        /// </summary>
        /// <param name="cardId"></param>
        public void CardIdTextChanged(string cardId)
        {
            if (cardId != null)
            {
                UpdateLocalSettings(KEY_SETTINGS_CARD_READER_CARD_ID, cardId.Trim(), cardId.GetType());
            }
        }

        /// <summary>
        /// Updates local settings based on key
        /// </summary>
        /// <param name="key">key</param>
        /// <param name="value">value</param>
        /// <param name="type">type</param>
        private void UpdateLocalSettings(string key, object value, Type type)
        {
            var localSettings = ApplicationData.Current.LocalSettings;
            localSettings.Values[key] = Convert.ChangeType(value, type);
        }

    }
}
