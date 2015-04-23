using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Microsoft.Xaml.Interactivity;
using Windows.UI.Xaml.Input;
using System.Text.RegularExpressions;
using Windows.System;
using Windows.UI.Core;
using SmartDeviceApp.Controls;

namespace SmartDeviceApp.Behaviors
{
    public sealed class NumericPasswordBehavior : DependencyObject, IBehavior
    {
        private const string REGEX_NUMERIC = "^[0-9]*$";

        private string lastValidText = String.Empty;
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
            var passwordBox = associatedObject as PasswordTextbox;
            if (passwordBox == null)
                throw new ArgumentException(
                    "FilteredPasswordBoxBehavior can be attached only to PasswordBox");

            AssociatedObject = associatedObject;

            lastValidText = passwordBox.OriginalText;
            passwordBox.KeyDown += OnKeyDown;
            passwordBox.KeyUp += OnKeyUp;
            passwordBox.IsEnabledChanged += OnEnabledChange;
            passwordBox.Paste += OnPaste;
            passwordBox.TextChanged += OnTextChanged;

            // Note: No inputscope supported in PasswordBox control
            // TODO: Show numeric keyboard
        }

        private void OnPaste(object sender, TextControlPasteEventArgs e)
        {
            e.Handled = true; //ignore paste
        }

        private void OnEnabledChange(object sender, DependencyPropertyChangedEventArgs e)
        {
            var passwordBox = AssociatedObject as PasswordTextbox;
            if ((bool)e.NewValue == true)
            {
                passwordBox.Text = Regex.Replace(lastValidText, @".", "*");
                passwordBox.OriginalText = lastValidText;
            }
            else 
            {
                lastValidText = passwordBox.OriginalText;
            }
        }

        private void OnKeyUp(object sender, KeyRoutedEventArgs e)
        {
            var passwordBox = AssociatedObject as PasswordTextbox;
            // Note: Shift + another key are regarded as separate KeyDown events for
            // certain keys e.g. $, #, &
            // Need to ignore event when shift is pressed
            var shiftKeyState = CoreWindow.GetForCurrentThread().GetKeyState(VirtualKey.Shift);
            var isShiftPressed = (shiftKeyState & CoreVirtualKeyStates.Down) == CoreVirtualKeyStates.Down;

            var textLength = passwordBox.OriginalText.Length;
            var caretPosition = passwordBox.SelectionStart;
            // Check if key is numeric
            if (e.Key >= VirtualKey.Number0 && e.Key <= VirtualKey.Number9 && !isShiftPressed && textLength < 8)
            {
                int key = (int)e.Key;
                if (caretPosition < passwordBox.OriginalText.Length)
                {
                    passwordBox.OriginalText = passwordBox.OriginalText.Insert(caretPosition - 1, (key - 48).ToString());
                }
                else
                {
                    passwordBox.OriginalText = passwordBox.OriginalText + (key - 48).ToString();
                }
                // 
            }
            else if (e.Key == VirtualKey.Back) //backspace
            {
                if (passwordBox.Text.Length != passwordBox.OriginalText.Length)
                {
                    var difference = Math.Abs(passwordBox.Text.Length - passwordBox.OriginalText.Length);
                    passwordBox.OriginalText = passwordBox.OriginalText.Remove(caretPosition, difference);//.Substring(caretPosition, passwordBox.OriginalText.Length - difference);
                }
                
            }
            if (e.Key == VirtualKey.Enter)
            {
                return;
            }

            if (passwordBox != null && !string.IsNullOrWhiteSpace(REGEX_NUMERIC))
            {

                
                if (Regex.IsMatch(passwordBox.OriginalText, REGEX_NUMERIC))
                {
                    // The text matches the regular expression.
                    lastValidText = passwordBox.OriginalText;
                    passwordBox.Text = Regex.Replace(lastValidText, @".", "*");
                    passwordBox.SelectionStart = (caretPosition > 0) ? caretPosition : 0;
                }
                else
                {
                    // The text doesn't match the regular expression.
                    // Restore the last valid value.
                    //var caretPosition = passwordBox.SelectionStart;
                    passwordBox.OriginalText = lastValidText;
                    passwordBox.Text = Regex.Replace(lastValidText, @".", "*");
                    passwordBox.SelectionStart = (caretPosition > 0) ? caretPosition : 0;
                }
            }

            e.Handled = true; // Ignore key
        }

        private void OnKeyDown(object sender, KeyRoutedEventArgs e)
        {
            var passwordBox = AssociatedObject as PasswordTextbox;
            // Note: Shift + another key are regarded as separate KeyDown events for
            // certain keys e.g. $, #, &
            // Need to ignore event when shift is pressed
            var shiftKeyState = CoreWindow.GetForCurrentThread().GetKeyState(VirtualKey.Shift);
            var isShiftPressed = (shiftKeyState & CoreVirtualKeyStates.Down) == CoreVirtualKeyStates.Down;

            // Check if key is numeric
            if (e.Key >= VirtualKey.Number0 && e.Key <= VirtualKey.Number9 && !isShiftPressed)
            {
                // Do nothing, let OnTextChanged handle event
                return;
            }
            e.Handled = true; // Ignore key
        }

        /// <summary>
        /// Detaches the object associated with this behavior.
        /// </summary>
        public void Detach()
        {
            var passwordBox = AssociatedObject as PasswordTextbox;
            if (passwordBox != null)
            {
                passwordBox.KeyDown -= OnKeyDown;
                passwordBox.KeyUp -= OnKeyUp;
            }
        }

        private void OnTextChanged(object sender, TextChangedEventArgs e)
        {
            var passwordBox = AssociatedObject as PasswordTextbox;
            
            if (passwordBox != null && !string.IsNullOrWhiteSpace(REGEX_NUMERIC))
            {
                // Handle pressing clear 'X' behavior
                if (String.IsNullOrEmpty(passwordBox.Text))
                {
                    passwordBox.OriginalText = String.Empty;
                }
                // Let OnKeyUp handle the rest
            }
        }
    }
}
