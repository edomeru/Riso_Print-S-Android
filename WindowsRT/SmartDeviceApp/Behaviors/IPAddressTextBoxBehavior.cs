using GalaSoft.MvvmLight.Messaging;
using Microsoft.Xaml.Interactivity;
using SmartDeviceApp.Common.Enum;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Windows.System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;

namespace SmartDeviceApp.Common.Utilities
{
    public class IPAddressTextBoxBehavior : DependencyObject, IBehavior
    {
        private const string REGEX_IPADRESS_CHARS = "[^a-fA-F0-9:.]";

        private string lastValidText;

        /// <summary>
        /// Object which this behavior is associated to.
        /// </summary>
        public DependencyObject AssociatedObject { get; private set; }

        /// <summary>
        /// Attaches this behavior to an object.
        /// </summary>
        /// <param name="associatedObject">Object to be associated with this behavior</param>
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
            textBox.TextChanged += OnTextChanged;

            var inputScope = new InputScope();
            inputScope.Names.Add(new InputScopeName(InputScopeNameValue.Number));
            textBox.InputScope = inputScope;
        }

        /// <summary>
        /// Detaches the object associated with this behavior.
        /// </summary>
        public void Detach()
        {
            var textBox = AssociatedObject as TextBox;
            if (textBox != null)
            {
                textBox.KeyDown -= OnKeyDown;
                textBox.KeyUp -= OnKeyUp;
            }
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

            // do something
            e.Handled = false;
            System.Diagnostics.Debug.WriteLine("Accepted");
        }

        /// <summary>
        /// Flag to check whether Shift key is pressed
        /// </summary>
        public bool IsShiftPressed
        {
            get;
            set;
        }

        private void OnTextChanged(object sender, TextChangedEventArgs e)
        {
            var textBox = AssociatedObject as TextBox;
            if (textBox != null && !string.IsNullOrWhiteSpace(REGEX_IPADRESS_CHARS))
            {
                if (!Regex.IsMatch(textBox.Text, REGEX_IPADRESS_CHARS))
                {
                    // The text matches the regular expression.
                    lastValidText = textBox.Text;
                }
                else
                {
                    // The text doesn't match the regular expression.
                    // Restore the last valid value.
                    var caretPosition = textBox.SelectionStart;
                    textBox.Text = lastValidText;
                    textBox.SelectionStart = (caretPosition > 0) ? caretPosition - 1 : 0;
                }
            }
        }



    }
}
