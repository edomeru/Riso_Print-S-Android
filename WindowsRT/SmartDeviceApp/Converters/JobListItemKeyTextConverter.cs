//
//  JobListItemKeyTextConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/07/29.
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
    public class JobListItemKeyTextConverter : IValueConverter
    {
        /// <summary>
        /// Returns the trimmed text of a Job List Item
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString()))
            {
                return String.Empty;
            }

            String text = value as string;
            Style style = (Style)Application.Current.Resources["STYLE_TextKeyNoTextTrim"];

            double actualWidth = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(text, style);
            double desiredWidth = new ViewModelLocator().JobsViewModel.KeyTextWidth;
            if (actualWidth <= desiredWidth)
            {
                return text; // No text trimming
            }

            return ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(text, style, desiredWidth);
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
