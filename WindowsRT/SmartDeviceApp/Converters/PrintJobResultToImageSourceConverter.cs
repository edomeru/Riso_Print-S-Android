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

namespace SmartDeviceApp.Converters
{
    public class PrintJobResultToImageSourceConverter : IValueConverter
    {
        private const string RESOURCE_IMAGES_LOCATION = "ms-appx:///Resources/Images/";
        private const string IMAGE_JOB_STATUS_OK = "img_btn_job_status_ok.png";
        private const string IMAGE_JOB_STATUS_NG = "img_btn_job_status_ng.png";
        //private const string FILE_PATH_RES_IMAGE_JOB_STATUS_OK = "Resources/Images/img_btn_job_status_ok.png";
        //private const string FILE_PATH_RES_IMAGE_JOB_STATUS_NG = "Resources/Images/img_btn_job_status_ng.png";
        
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is int)) return null;
            if ((int)value == 0) // OK
            {
                return new Uri(RESOURCE_IMAGES_LOCATION + IMAGE_JOB_STATUS_OK);
            }
            else if ((int)value == 1) // NG
            {
                return new Uri(RESOURCE_IMAGES_LOCATION + IMAGE_JOB_STATUS_NG);
            }
            return null;

        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
