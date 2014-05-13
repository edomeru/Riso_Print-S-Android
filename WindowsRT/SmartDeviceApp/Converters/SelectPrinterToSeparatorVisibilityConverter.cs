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

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
