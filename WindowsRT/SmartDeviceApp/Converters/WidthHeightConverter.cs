using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Converters
{
    public class WidthConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            return Window.Current.Bounds.Width;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }

    public class ResizedViewWidthConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value != null)
            {
                AppViewMode appViewMode;
                var success = Enum.TryParse<AppViewMode>(value.ToString(), out appViewMode);
                if (success)
                {
                    if (appViewMode == AppViewMode.RightPaneVisible_ResizedView)
                    {
                        //return Window.Current.Bounds.Width - (double)Application.Current.Resources["SIZE_SidePaneWidth"];
                        return Window.Current.Bounds.Width - 400;
                    }
                }
            }
            return Window.Current.Bounds.Width;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }

    public class HeightConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            return Window.Current.Bounds.Height;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
