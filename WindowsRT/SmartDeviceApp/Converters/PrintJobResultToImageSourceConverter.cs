using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Media.Imaging;
using Windows.Storage;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Converters
{
    public class PrintJobResultToImageSourceConverter : IValueConverter
    {        
        /// <summary>
        /// Returns the correct image based on the print job result.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is int)) return null;
            if ((int)value == (int)PrintJobResult.Success) // OK
            {
                return "ms-appx:///Resources/Images/img_btn_job_status_ok.png";
            }
            else if ((int)value == (int)PrintJobResult.Error) // NG
            {
                return "ms-appx:///Resources/Images/img_btn_job_status_ng.png";
            }
            return null;
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
