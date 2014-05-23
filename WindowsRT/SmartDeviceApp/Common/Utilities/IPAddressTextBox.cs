using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.System;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;

namespace SmartDeviceApp.Common.Utilities
{
    public class IPAddressTextBox : TextBox
    {
        public bool IsShiftPressed
        {
            get;
            set;
        }

        private string _text ;

        protected override void OnKeyDown(Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        {
            base.OnKeyDown(e);

            int keyValue = (int)e.Key;
            System.Diagnostics.Debug.WriteLine(e.Key);

            if (e.Key == VirtualKey.Shift)
                IsShiftPressed = true;


            if (((e.Key >= VirtualKey.Number0 && e.Key <= VirtualKey.Number9) // numbers
             || (e.Key >= VirtualKey.NumberPad0 && e.Key <= VirtualKey.NumberPad9) // numpad             
                || (e.Key == VirtualKey.Decimal) || (keyValue == 190)) /*period*/&& !IsShiftPressed)
            {
                // do something
                e.Handled = false;
                System.Diagnostics.Debug.WriteLine("Accepted");
            }
            else
            {
                e.Handled = true;
            }
        }

        protected override void OnKeyUp(Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        {
            base.OnKeyUp(e);

            if (e.Key == VirtualKey.Shift)
                IsShiftPressed = false;
            if (e.Key == VirtualKey.Enter)
            {
                Messenger.Default.Send<string>("AddPrinter");
            }
        }
    }
}
