//
//  PrinterStatusToImageSourceConverter.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/05/01
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Converters
{
    public class PrinterStatusToImageSourceConverter : IValueConverter
    {
        /// <summary>
        /// Returns the correct image for the printer's online status.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is bool))
                return DependencyProperty.UnsetValue;
            var isInvert = false;
            if (parameter != null) isInvert = System.Convert.ToBoolean(parameter);

            bool objValue = (bool)value;
            if (isInvert) objValue = !objValue;
            if (objValue)
            {
                return "ms-appx:///Resources/Images/img_btn_printer_status_online.png";
            }
            return "ms-appx:///Resources/Images/img_btn_printer_status_offline.png";
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
