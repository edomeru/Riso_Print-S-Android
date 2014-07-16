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
    public class BooleanToVisibilityConverter : IValueConverter
    {
        /// <summary>
        /// Returns a Visibility value based on the Boolean input.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is bool))
                return Visibility.Collapsed;
            var isInvert = false;
            if (parameter != null) isInvert = System.Convert.ToBoolean(parameter);

            bool objValue = (bool)value;
            if (isInvert) objValue = !objValue;
            if (objValue)
            {
                return Visibility.Visible;
            }
            return Visibility.Collapsed;
        }

        /// <summary>
        /// Returns Boolean from Visibility input
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            return value is Visibility && (Visibility)value == Visibility.Visible;
        }
    }
}
