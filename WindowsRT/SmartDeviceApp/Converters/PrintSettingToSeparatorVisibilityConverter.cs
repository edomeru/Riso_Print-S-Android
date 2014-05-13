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
    public class PrintSettingToSeparatorVisibilityConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var index = -1;
            var isLastItem = false;
            if (value == null || !(value is PrintSetting)) return Visibility.Collapsed;
            var printSetting = value as PrintSetting;
            var printSettings = new ViewModelLocator().PrintSettingsViewModel.PrintSettingsList;
            if (printSettings != null)
            {
                foreach (PrintSettingGroup group in printSettings)
                {
                    if (group.PrintSettings.Contains(printSetting))
                    {
                        if (group.PrintSettings.Count == 1)
                        {
                            return Visibility.Collapsed;
                        }
                        index = group.PrintSettings.IndexOf(printSetting);
                        isLastItem = (index == group.PrintSettings.Count - 1);
                        break;
                    }
                }
            }
            return (isLastItem ? Visibility.Collapsed : Visibility.Visible);
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
