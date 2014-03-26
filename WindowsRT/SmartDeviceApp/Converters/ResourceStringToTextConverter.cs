using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Data;

namespace SmartDeviceApp.Converters
{
    public class ResourceStringToTextConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            if (value == null || String.IsNullOrEmpty(value.ToString())) return String.Empty;
            var textMode = TextMode.None;
            if (parameter != null && !String.IsNullOrEmpty(parameter.ToString()))
            {
                TextMode tempTextMode;
                var success = Enum.TryParse<TextMode>(parameter.ToString(), out tempTextMode);
                if (success) textMode = tempTextMode;
            }

            var loader = new Windows.ApplicationModel.Resources.ResourceLoader();
            var text = loader.GetString(value.ToString().ToUpper());
            if (String.IsNullOrEmpty(text)) return String.Empty;

            switch (textMode)
            {
                case TextMode.None:
                    break;
                case TextMode.Uppercase:
                    text = text.ToUpper();
                    break;
            }
            return text;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }

    public enum TextMode
    {
        Uppercase,
        None
    }
}
