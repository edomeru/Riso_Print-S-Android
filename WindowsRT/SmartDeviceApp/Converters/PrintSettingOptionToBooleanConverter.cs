//
//  PrintSettingOptionToBooleanConverter.cs
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
using System.Collections.Generic;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;
using Windows.UI.Xaml;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Converters
{
    public class PrintSettingOptionToBooleanConverter : IValueConverter
    {
        private PrintSettingsViewModel _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;

        /// <summary>
        /// Returns boolean from print setting option.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is int)) return false;
            int index = (int)value;
            if ((int)_printSettingsViewModel.SelectedPrintSetting.Value == index) return true;
            return false;
        }

        /// <summary>
        /// Not implemented.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
