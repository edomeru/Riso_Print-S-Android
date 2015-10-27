using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Models;

namespace SmartDeviceApp.Converters
{
    public class WidthConverter : IValueConverter
    {
        /// <summary>
        /// Returns the current width of the Window.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            return Window.Current.Bounds.Width;
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

    public class ResizedViewWidthConverter : IValueConverter
    {
        /// <summary>
        /// Computes for the new width of the View.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
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

    public class PrintJobListWidthConverter : IValueConverter
    {
        /// <summary>
        /// Computes for the width of Print Job List.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var viewModel = new ViewModelLocator().JobsViewModel;
            var columns = viewModel.MaxColumns;
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            // Need this workaround because OrientationChange event is fired before 
            // window bounds are updated
            var width = 0.0;
            var viewOrientation = new ViewModelLocator().ViewControlViewModel.ViewOrientation;
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
            var columnWidth = (width - (defaultMargin * 2) - (defaultMargin * (columns - 1))) / columns;
            viewModel.ColumnWidth = columnWidth;
            return columnWidth;
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

    public class PrintersListWidthConverter : IValueConverter
    {
        /// <summary>
        /// Computes for the printer list's width based on the orientation.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var columns = 3;
            var width = Window.Current.Bounds.Width;
            if (parameter != null)
            {
                var itemParams = parameter as ViewItemParameters;
                columns = itemParams.columns;
                if (itemParams.viewOrientation != null)
                {
                    // Need this workaround because OrientationChange event is fired before 
                    // window bounds are updated
                    var viewOrientation = (ViewOrientation)itemParams.viewOrientation;
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
            }
            
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            var columnWidth = (width - (defaultMargin * 2) - (defaultMargin * (columns - 1))) / columns;
            return columnWidth ;
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

    public class HeightConverter : IValueConverter
    {
        /// <summary>
        /// Returns the current height of the window.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
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

    public class SidePanesHeightConverter : IValueConverter
    {
        /// <summary>
        /// Returns the height of the side pane.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null) return null;
            return Window.Current.Bounds.Height;
            
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
}
