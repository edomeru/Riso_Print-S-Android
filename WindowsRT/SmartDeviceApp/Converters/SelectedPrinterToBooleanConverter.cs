//
//  SelectedPrinterToBooleanConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/04/29
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
    public class SelectedPrinterToBooleanConverter : IValueConverter
    {
        private PrintSettingsViewModel _printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;

        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is int))
            {
                return false;
            }
            return (_printSettingsViewModel.PrinterId == (int)value);
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
