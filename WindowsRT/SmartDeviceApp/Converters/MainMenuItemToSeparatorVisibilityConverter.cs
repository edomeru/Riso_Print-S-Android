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
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is MainMenuItem)) return Visibility.Collapsed;
            var mainMenuItem = value as MainMenuItem;
            var mainMenuItems = new ViewModelLocator().ViewControlViewModel.MainMenuItems;
            bool isLastItem = (mainMenuItem == mainMenuItems[mainMenuItems.Count - 1]) ? true : false;
            return (isLastItem ? Visibility.Collapsed : Visibility.Visible);
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
