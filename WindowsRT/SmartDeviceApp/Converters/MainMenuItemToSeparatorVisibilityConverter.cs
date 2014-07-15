using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media.Imaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Converters
{
    public class MainMenuItemToSeparatorVisibilityConverter : IValueConverter
    {
        /// <summary>
        /// Returns Visibility based on the position of the item value.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is MainMenuItem)) return Visibility.Collapsed;
            var mainMenuItem = value as MainMenuItem;
            var mainMenuItems = new ViewModelLocator().ViewControlViewModel.MainMenuItems;
            bool isLastItem = (mainMenuItem == mainMenuItems[mainMenuItems.Count - 1]) ? true : false;
            return (isLastItem ? Visibility.Collapsed : Visibility.Visible);
        }

        /// <summary>
        /// Not implemented
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
