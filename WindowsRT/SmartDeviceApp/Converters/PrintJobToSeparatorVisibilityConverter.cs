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
        /// <summary>
        /// Returns the visibility of the separator from the position of the current print job.
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
