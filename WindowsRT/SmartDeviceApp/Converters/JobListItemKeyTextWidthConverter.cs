using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Converters
{
    public class JobListItemKeyTextWidthConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            // Workaround to get JobsViewModel data context in JobListItemControl
            return new ViewModelLocator().JobsViewModel.KeyTextWidth;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
