using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Controls;

namespace SmartDeviceApp.Controls
{
    public class PasswordTextbox : TextBox
    {
        private string _originalText = "";

        /// <summary>
        /// Contains the pin code.
        /// </summary>
        public string OriginalText
        {
            get;
            set;
        }
    }
}
