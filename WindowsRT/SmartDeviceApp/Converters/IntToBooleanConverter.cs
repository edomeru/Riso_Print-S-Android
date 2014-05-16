using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class IntToBooleanConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (!(value is int))
                return false;
            

            int objValue = (int)value;
            if (objValue == 0)
                return false;
            else
                return true;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            if (!(value is bool))
                return 0;
            

            bool objValue = (bool)value;
            if (objValue == false)
                return 0;
            else
                return 1;
            
        }
    }
}
