﻿using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.Converters
{
    public class ResourceStringToTextConverter : IValueConverter
    {
        /// <summary>
        /// Converts resource strings to texts.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            try
            {
                if (value == null || !(value is string) || String.IsNullOrEmpty(value.ToString())) return String.Empty;
                var textMode = TextMode.None;
                if (parameter != null && !String.IsNullOrEmpty(parameter.ToString()))
                {
                    TextMode tempTextMode;
                    var success = Enum.TryParse<TextMode>(parameter.ToString(), out tempTextMode);
                    if (success) textMode = tempTextMode;
                }

                var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
                var text = loader.GetString(value.ToString().ToUpper());
                if (String.IsNullOrEmpty(text))
                {
                    throw new ArgumentException(LogUtility.ERROR_RESOURCE_STRING_NOT_FOUND, value.ToString());
                }

                switch (textMode)
                {
                    case TextMode.None:
                        break;
                    case TextMode.Uppercase:
                        text = text.ToUpper();
                        break;
                }
                text = text.Replace("\\n", "\n");
                return text;
            }
            catch (Exception ex)
            {
                LogUtility.LogError(ex);
                return String.Empty;
            }
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

    public enum TextMode
    {
        Uppercase,
        None
    }
}
