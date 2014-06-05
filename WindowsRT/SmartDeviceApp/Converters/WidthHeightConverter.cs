﻿using System;
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
            double width = 0.0;
            if (parameter != null && parameter is ViewOrientation)
            {
                // Need this workaround because OrientationChange event is fired before 
                // window bounds are updated
                var viewOrientation = (ViewOrientation)parameter;
                if (viewOrientation == ViewOrientation.Landscape)
                {
                    width = (Window.Current.Bounds.Width >= Window.Current.Bounds.Height) ?
                        Window.Current.Bounds.Width : Window.Current.Bounds.Height;
                }
                else if (viewOrientation == ViewOrientation.Portrait)
                {
                    width = (Window.Current.Bounds.Width <= Window.Current.Bounds.Height) ?
                        Window.Current.Bounds.Width : Window.Current.Bounds.Height;
                }
            }
            else 
            {
                width = Window.Current.Bounds.Width;
            }
            
            if (value != null)
            {
                ViewMode viewMode;
                var success = Enum.TryParse<ViewMode>(value.ToString(), out viewMode);
                if (success)
                {
                    if (viewMode == ViewMode.RightPaneVisible_ResizedWidth)
                    {
                        width -= (double)Application.Current.Resources["SIZE_SidePaneWidth"];
                    }
                }
            }
            return width;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }

    public class PrintJobListWidthConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var columns = 3;
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            var columnWidth = (Window.Current.Bounds.Width - (defaultMargin * 2) - (defaultMargin * (columns - 1))) / 3;
            return columnWidth;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }

    public class PrintersListWidthConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var columns = 3;
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            var columnWidth = (Window.Current.Bounds.Width - (defaultMargin * 2) - (defaultMargin * (columns - 1))) / 3;
            return columnWidth + 20;
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
            if (value == null || !(value is ViewOrientation)) return null;
            // Need this workaround because OrientationChange event is fired before 
            // window bounds are updated
            var viewOrientation = (ViewOrientation)value;
            if (viewOrientation == ViewOrientation.Landscape)
            {
                return (Window.Current.Bounds.Height <= Window.Current.Bounds.Width) ?
                    Window.Current.Bounds.Height : Window.Current.Bounds.Width;
            }
            // Portrait
            return (Window.Current.Bounds.Height >= Window.Current.Bounds.Width) ?
                Window.Current.Bounds.Height : Window.Current.Bounds.Width;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
