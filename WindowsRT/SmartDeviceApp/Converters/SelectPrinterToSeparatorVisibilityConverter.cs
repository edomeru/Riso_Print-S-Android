using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class SelectPrinterToSeparatorVisibilityConverter : IValueConverter
    {
        /// <summary>
        /// Returns the visibility value of the separator based on the position of the printer.
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

            if (value == null || !(value is int))
            {
                return Visibility.Collapsed;
            }
            var printers = new ViewModelLocator().SelectPrinterViewModel.PrinterList;
            foreach (Printer printer in printers)
            {
                if (printer.Id == (int)value)
                {
                    index = printers.IndexOf(printer);
                    isLastItem = (index == printers.Count - 1);
                    break;
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
