using System;
using System.Collections.Generic;
using System.Globalization;
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

        // BCP-47 Code for French taken from CurrentInputMethodLanguageTag
        // References:
        // https://tools.ietf.org/html/bcp47)
        // https://msdn.microsoft.com/library/windows/apps/hh700658

        private const string FRENCH_INPUT_LANGUAGE_CODE = "fr-FR"; 

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

        private void restoreLastSaved(PasswordTextbox passwordBox,string value)
        {
            // The text matches the regular expression.
            var caretPosition = passwordBox.SelectionStart;
            if (string.IsNullOrEmpty(value))
            {
                value = "";
            }
            passwordBox.OriginalText = value;
            lastValidText = value;
            passwordBox.Text = Regex.Replace(value, @".", "*");
            passwordBox.SelectionStart = (caretPosition > 0) ? caretPosition : 0;
        }


        private void OnKeyUp(object sender, KeyRoutedEventArgs e)
        {
            var passwordBox = AssociatedObject as PasswordTextbox;

            var textLength = passwordBox.OriginalText.Length;
            var caretPosition = passwordBox.SelectionStart;
            // Check if key is numeric
            if (e.Key >= VirtualKey.Number0 && e.Key <= VirtualKey.Number9 && !shouldPreventShift() && textLength < 8)
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
                 if (passwordBox.OriginalText.Length > passwordBox.Text.Length)
                {
                    var difference = Math.Abs(passwordBox.Text.Length - passwordBox.OriginalText.Length);
                    passwordBox.OriginalText = passwordBox.OriginalText.Remove(caretPosition, difference);//.Substring(caretPosition, passwordBox.OriginalText.Length - difference);
                }
            }
            if (e.Key == VirtualKey.Enter)
            {
                return;
            }

            if (passwordBox != null)
            {
                if (Regex.IsMatch(passwordBox.OriginalText, REGEX_NUMERIC))
                {                    
                    // The text matches the regular expression.
                    restoreLastSaved(passwordBox, passwordBox.OriginalText);
                }
                else
                {
                    // The text doesn't match the regular expression.
                    // Restore the last valid value.
                    //var caretPosition = passwordBox.SelectionStart;
                    restoreLastSaved(passwordBox, lastValidText);
                }
            }

            e.Handled = true; // Ignore key
        }

        private void OnKeyDown(object sender, KeyRoutedEventArgs e)
        {
          // Check if key is numeric
            if (e.Key >= VirtualKey.Number0 && e.Key <= VirtualKey.Number9 && !shouldPreventShift())
            {
                // Do nothing, let OnTextChanged handle event
                return;
            }
            e.Handled = true; // Ignore key
        }

        // Note: Shift + another key are regarded as separate KeyDown events for
        // certain keys e.g. $, #, &
        // Need to ignore event when shift is pressed
        // For French keyboards, it is the opposite: pressing shift outputs the numbers and not pressing outputs the symbols
        private bool shouldPreventShift()
        {
            var shiftKeyState = CoreWindow.GetForCurrentThread().GetKeyState(VirtualKey.Shift);

            var isShiftPressed = (shiftKeyState & CoreVirtualKeyStates.Down) == CoreVirtualKeyStates.Down;
            var isFrench = Windows.Globalization.Language.CurrentInputMethodLanguageTag == FRENCH_INPUT_LANGUAGE_CODE;

            return (!isFrench && isShiftPressed) || (isFrench && !isShiftPressed);
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
            var text = passwordBox.Text.Replace("*", "");
            if (passwordBox != null)
            {
                // Handle pressing clear 'X' behavior
                if (String.IsNullOrEmpty(passwordBox.Text))
                {
                    passwordBox.OriginalText = String.Empty;
                }
                // Let OnKeyUp handle the rest
            }
            
            if (!String.IsNullOrEmpty(text)  && !Regex.IsMatch(text, REGEX_NUMERIC))
            {
                restoreLastSaved(passwordBox, lastValidText);
               
            }
           
        }
    }
}
