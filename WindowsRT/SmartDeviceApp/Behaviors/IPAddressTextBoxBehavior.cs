using GalaSoft.MvvmLight.Messaging;
using Microsoft.Xaml.Interactivity;
using SmartDeviceApp.Common.Enum;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;

namespace SmartDeviceApp.Common.Utilities
{
    public class IPAddressTextBoxBehavior : DependencyObject, IBehavior
    {
        private const string REGEX_NON_ALPHANUMERIC = "[^a-zA-Z0-9]";

        private string lastValidText;

        public DependencyObject AssociatedObject { get; private set; }
        public void Attach(DependencyObject associatedObject)
        {
            var textBox = associatedObject as TextBox;
            if (textBox == null)
                throw new ArgumentException(
                    "FilteredTextBoxBehavior can be attached only to TextBox");

            AssociatedObject = associatedObject;

            lastValidText = textBox.Text;
            textBox.KeyDown += OnKeyDown;
            textBox.KeyUp += OnKeyUp;
            //textBox.TextChanged += OnTextChanged;

            var inputScope = new InputScope();
            inputScope.Names.Add(new InputScopeName(InputScopeNameValue.Number));
            textBox.InputScope = inputScope;
        }

        public void Detach()
        {
        }

        private void OnKeyUp(object sender, Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        {
            if (e.Key == VirtualKey.Shift)
                IsShiftPressed = false;

            if (e.Key == VirtualKey.Enter)
            {
                Messenger.Default.Send<MessageType>(MessageType.AddPrinter);

                //workaround to hide softkeyboard after printer search
                //since we don't have direct control over the keyboard
                ((TextBox)sender).IsEnabled = false;
                ((TextBox)sender).IsEnabled = true;
            }
        }

        private void OnKeyDown(object sender, Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        {
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

        public bool IsShiftPressed
        {
            get;
            set;
        }


        //private void OnKeyDown(Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        //{
        //    //base.OnKeyDown(e);

            
        //}

        //protected override void OnKeyUp(Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        //{
        //    //base.OnKeyUp(e);

            
        //}

        
    }
}
