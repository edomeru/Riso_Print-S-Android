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
        /// <summary>
        /// Returns separator visibility based on the position of the print setting. Last print setting will have a collapsed visibility for the separator.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
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
