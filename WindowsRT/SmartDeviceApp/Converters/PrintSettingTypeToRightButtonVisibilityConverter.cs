using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;

namespace SmartDeviceApp.Converters
{
    public class PrintSettingTypeToRightButtonVisibilityConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || String.IsNullOrEmpty(value.ToString())) return null;
            PrintSettingType type;
            var success = Enum.TryParse<PrintSettingType>(value.ToString(), out type);
            if (!success) return null;
            else
            {
                switch (type)
                {
                    case PrintSettingType.numeric:
                    case PrintSettingType.boolean:
                        return Visibility.Collapsed;
                    case PrintSettingType.list:
                        return Visibility.Visible;
                }
            }
            return Visibility.Collapsed;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
