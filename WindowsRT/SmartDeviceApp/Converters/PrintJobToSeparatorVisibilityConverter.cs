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
    public class PrintJobToSeparatorVisibilityConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var index = -1;
            var isLastItem = false;
            if (value == null || !(value is PrintJob)) return Visibility.Collapsed;
            var printJob = value as PrintJob;
            var printJobs = new ViewModelLocator().JobsViewModel.PrintJobsList;
            foreach (PrintJobGroup group in printJobs)
            {
                if (group.Jobs.Contains(printJob))
                {
                    index = group.Jobs.IndexOf(printJob);
                    isLastItem = (index == group.Jobs.Count - 1);
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
