using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Converters
{
    public class PageNumberFormatConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {   
            var pageNumber = value as PageNumberInfo;
            string formattedPageNumber = String.Empty;
            if (pageNumber.ViewMode == PageViewMode.SinglePageView)
            {
                formattedPageNumber = (pageNumber.RightPageIndex + 1).ToString();
            }
            else if (pageNumber.ViewMode == PageViewMode.TwoPageView)
            {
                formattedPageNumber = String.Format("{0} - {1}", pageNumber.LeftPageIndex + 1, pageNumber.RightPageIndex + 1);
            }
            
            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
            var pageNumberFormat = loader.GetString("IDS_LBL_PAGE_NUMBER");
            formattedPageNumber = String.Format(pageNumberFormat, formattedPageNumber, pageNumber.PageTotal);

            return formattedPageNumber;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}
