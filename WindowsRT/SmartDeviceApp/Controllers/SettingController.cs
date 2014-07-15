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

        /// <summary>
        /// Log-in ID value changed delegate
        /// </summary>
        /// <param name="pinCode"></param>
        public delegate void CardIdValueChangedEventHandler(string pinCode);
        private CardIdValueChangedEventHandler _cardIdValueChangedEventHandler;

        private const string KEY_SETTINGS_CARD_READER_CARD_ID = "key_card_reader_card_id";

        /// <summary>
        /// Log-in ID value
        /// </summary>
        public string CardId { get; private set; }
        
        private SettingsViewModel _settingsViewModel;

        // Explicit static constructor to tell C# compiler
        // not to mark type as beforefieldinit
        // http://csharpindepth.com/Articles/General/Singleton.aspx
        static SettingController() { }

        private SettingController()
        {
            _settingsViewModel = new ViewModelLocator().SettingsViewModel;

            _cardIdValueChangedEventHandler = new CardIdValueChangedEventHandler(CardIdTextChanged);
        }

        /// <summary>
        /// SettingController singleton instance
        /// </summary>
        public static SettingController Instance
        {
            get { return _instance; }
        }

        /// <summary>
        /// Initialize SettingController. Fetches values from App Data local store.
        /// </summary>
        public void Initialize()
        {
            var localSettings = ApplicationData.Current.LocalSettings;

            CardId = string.Empty;
            if (localSettings.Values.ContainsKey(KEY_SETTINGS_CARD_READER_CARD_ID))
            {
                CardId = localSettings.Values[KEY_SETTINGS_CARD_READER_CARD_ID].ToString();
            }
            _settingsViewModel.CardId = CardId;

            _settingsViewModel.CardIdValueChangedEventHandler += _cardIdValueChangedEventHandler;
        }

        /// <summary>
        /// Event handler for Log-in ID text
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
            CardId = (string)value;
        }

    }
}
