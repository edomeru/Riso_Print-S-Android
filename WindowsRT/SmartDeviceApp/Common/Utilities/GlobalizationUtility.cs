//
//  GlobalizationUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/07.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using Windows.Globalization;

namespace SmartDeviceApp.Common.Utilities
{
    public static class GlobalizationUtility
    {
        private const string REGION_CODE_JAPANESE = "ja-JP";
        private const string FORMAT_LANGUAGE_REGION = "{0}-{1}";

        /// <summary>
        /// Determines if current language and region is Japanese (Japan).
        /// </summary>
        /// <returns>true if Japanese (Japan), false otherwise</returns>
        public static bool IsJapaneseLocale()
        {
            GeographicRegion region = new GeographicRegion(); // Region-Location dependent
            string languageCode = Language.CurrentInputMethodLanguageTag; // Keyboard dependent
            string langRegionCode = String.Format(FORMAT_LANGUAGE_REGION, languageCode,
                region.CodeTwoLetter);
            return string.Equals(REGION_CODE_JAPANESE, langRegionCode);
        }
    }
}
