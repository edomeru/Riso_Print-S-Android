﻿//
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

        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is int)) return false;
            int index = (int)value;
            if ((int)_printSettingsViewModel.SelectedPrintSetting.Value == index) return true;
            return false;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
