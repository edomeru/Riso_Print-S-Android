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
        /// <summary>
        /// Formats the page number displayed below the slider.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || !(value is PageNumberInfo)) return String.Empty;
            var pageNumber = value as PageNumberInfo;
            string formattedPageNumber = String.Empty;
            formattedPageNumber = (pageNumber.PageIndex + 1).ToString();
            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
            var pageNumberFormat = loader.GetString("IDS_LBL_PAGE_DISPLAYED");
            formattedPageNumber = String.Format(pageNumberFormat, formattedPageNumber, pageNumber.PageTotal);

            return formattedPageNumber;
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
