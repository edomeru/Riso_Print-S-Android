using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Data;
using SmartDeviceApp.Controllers;

namespace SmartDeviceApp.Converters
{
    public class PageIndexToSliderValueConverter : IValueConverter
    {
        /// <summary>
        /// Returns slider value from page index input.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null) return 0;

            // If document has only one page, set page number to 0
            var pageNumber = (DocumentController.Instance.PageCount > 1) ? System.Convert.ToDouble(value) + 1.0 : 0;
            return pageNumber;
        }

        /// <summary>
        /// Returns the page number based on the slider index.
        /// </summary>
        /// <param name="value">The value produced by the binding source.</param>
        /// <param name="targetType">The type of the binding target property.</param>
        /// <param name="parameter">The converter parameter to use.</param>
        /// <param name="language">The culture to use in the converter.</param>
        /// <returns>A converted value. If the method returns null, the valid null value is used.</returns>
        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            if (value == null) return 0;

            // If document has only one page, set page number to 0
            var pageNumber = (DocumentController.Instance.PageCount > 1) ? System.Convert.ToInt32(value) - 1 : 0;
            return pageNumber;
        }
    }
}
