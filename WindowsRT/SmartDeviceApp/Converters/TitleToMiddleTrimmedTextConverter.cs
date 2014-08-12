﻿//
//  TitleToMiddleTrimmedTextConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/08/11.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.ViewModels;
using System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class TitleToMiddleTrimmedTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the middle trimmed text of a title text
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()) ||
                !(parameter is double))
            {
                return String.Empty;
            }

            String text = value as string;
            Style style = (Style)Application.Current.Resources["STYLE_TextHeader"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = (double)parameter;
            if (actualWidth > desiredWidth)
            {
                return ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text,
                    style, desiredWidth);
            }

            return text; // Return as is
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
