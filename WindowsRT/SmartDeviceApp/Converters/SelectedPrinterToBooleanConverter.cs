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

        /// <summary>
        /// Returns true if the printer id saved in the view model is equal to the input value, else false.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is int))
            {
                return false;
            }
            return (_printSettingsViewModel.PrinterId == (int)value);
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
