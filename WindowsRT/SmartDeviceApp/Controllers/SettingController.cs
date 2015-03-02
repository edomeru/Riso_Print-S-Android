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
        /// License agreement status delegate
        /// </summary>
        /// <param name="isAgreed"></param>
        public delegate void SetLicenseAgreedEventHandler();
        private SetLicenseAgreedEventHandler _setLicenseAgreedEventHandler;

        /// <summary>
        /// Log-in ID value changed delegate
        /// </summary>
        /// <param name="pinCode"></param>
        public delegate void CardIdValueChangedEventHandler(string pinCode);
        private CardIdValueChangedEventHandler _cardIdValueChangedEventHandler;

        private const string KEY_SETTINGS_LICENSE_AGREEMENT_STATUS = "key_license_agreement_status";
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

            _setLicenseAgreedEventHandler = new SetLicenseAgreedEventHandler(SetLicenseAgreed);
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

            new ViewModelLocator().LicenseViewModel.SetLicenseAgreedEventHandler += _setLicenseAgreedEventHandler;
        }

        /// <summary>
        /// Shows the license agreement if the user has not yet agreed
        /// </summary>
        public static void ShowLicenseAgreement()
        {
            var localSettings = ApplicationData.Current.LocalSettings;
            bool isAgreed = false;
            string key = KEY_SETTINGS_LICENSE_AGREEMENT_STATUS;
            if (localSettings.Values.ContainsKey(key))
            {
                try
                {
                    isAgreed = Convert.ToBoolean(localSettings.Values[key]);
                }
                catch
                {
                    isAgreed = false;                    
                }
            }
            else
            {
                // First time to show license agreement, set to false
                isAgreed = false;
                localSettings.Values[key] = Convert.ChangeType(isAgreed, isAgreed.GetType());
            }
            if (!isAgreed)
            {
                new ViewModelLocator().ViewControlViewModel.GoToLicensePage.Execute(null);
            }
        }

        /// <summary>
        /// Event handler for license agreement
        /// </summary>
        /// <param name="cardId"></param>
        public void SetLicenseAgreed()
        {
            bool isAgreed = true;
            UpdateLocalSettings(KEY_SETTINGS_LICENSE_AGREEMENT_STATUS, isAgreed, isAgreed.GetType());
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
                CardId = cardId;
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
