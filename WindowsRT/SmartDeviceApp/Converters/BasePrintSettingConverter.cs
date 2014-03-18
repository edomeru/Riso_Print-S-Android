using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class BasePrintSettingConverter
    {
        public class BasePrintSettingToIntConverter : IValueConverter
        {

            public object Convert(object value, Type targetType, object parameter, string language)
            {
                int convertedValue = -1;

                if (value != null)
                {
                    convertedValue = (int)value;
                }

                return convertedValue;
            }

            public object ConvertBack(object value, Type targetType, object parameter, string language)
            {
                throw new NotImplementedException();
            }
        }

    }
}
