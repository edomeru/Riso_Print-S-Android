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
    public class PrinterSearchItemToSeparatorVisibility : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var index = -1;
            var isLastItem = false;
            if (value == null || !(value is PrinterSearchItem)) return Visibility.Collapsed;
            var searchItem = value as PrinterSearchItem;
            var printerSearchList = new ViewModelLocator().SearchPrinterViewModel.PrinterSearchList;
            foreach (PrinterSearchItem group in printerSearchList)
            {
                if (printerSearchList.Contains(searchItem))
                {
                    index = printerSearchList.IndexOf(searchItem);
                    isLastItem = (index == printerSearchList.Count - 1);
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
