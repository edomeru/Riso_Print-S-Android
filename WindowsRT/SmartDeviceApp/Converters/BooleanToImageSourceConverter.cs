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
    public class BooleanToImageSourceConverter : IValueConverter
    {
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
                return "ms-appx:///Resources/Images/img_btn_printer_status_online.scale - 100.png";
            }
            return "ms-appx:///Resources/Images/img_btn_printer_status_offline.scale - 100.png";
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
