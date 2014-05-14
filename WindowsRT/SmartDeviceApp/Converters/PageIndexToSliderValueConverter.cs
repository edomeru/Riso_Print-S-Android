using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class PageIndexToSliderValueConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is double)) return 0;

            var pageNumber = System.Convert.ToDouble(value) + 1.0;
            return pageNumber;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is double)) return 0;

            var pageNumber = System.Convert.ToInt32(value) - 1;
            return pageNumber;
        }
    }
}
