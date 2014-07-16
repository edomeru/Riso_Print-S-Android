using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media;

namespace SmartDeviceApp.Converters
{
    public class BooleanToColor : IValueConverter
    {
        /// <summary>
        /// Returns a Color value based on the Boolean parameter.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is bool))
                return (new SolidColorBrush(Colors.White));
            var isInvert = false;
            if (parameter != null) isInvert = System.Convert.ToBoolean(parameter);

            bool objValue = (bool)value;
            if (isInvert) objValue = !objValue;
            if (objValue)
            {
                return (new SolidColorBrush(new Color() { A = 0xFF, R = 0x57/*0x85*/, G = 0x07/*0x41*/, B = 0xB6/*0xD8*/}));
            }
            return (new SolidColorBrush(Colors.White));
        }

        /// <summary>
        /// Converts back Color to Boolean. Not Implemented.
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
