using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class ItemSelectedConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var args = value as SelectionChangedEventArgs;

            if (args != null && args.AddedItems.Count > 0) // Workaround: This event is triggered also when selection is removed
                // TODO: Change to radio button to retain selection even if control is removed from view
                return args.AddedItems[0]; // Allow single selection only

            return null;
        }

        public object ConvertBack(object value, Type targetType, object parameter,
            string language)
        {
            throw new NotImplementedException();
        }
    }
}
