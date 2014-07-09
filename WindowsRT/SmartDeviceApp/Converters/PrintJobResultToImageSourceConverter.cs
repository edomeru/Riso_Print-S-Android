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

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
