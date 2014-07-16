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

namespace SmartDeviceApp.Behaviors
{
    public sealed class NumericPasswordBehavior : DependencyObject, IBehavior
    {
        private const string REGEX_NUMERIC = "^[0-9]*$";

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
            var passwordBox = associatedObject as PasswordBox;
            if (passwordBox == null)
                throw new ArgumentException(
                    "FilteredPasswordBoxBehavior can be attached only to PasswordBox");

            AssociatedObject = associatedObject;

            lastValidText = passwordBox.Password;
            passwordBox.KeyDown += OnKeyDown;
            passwordBox.PasswordChanged += OnPasswordChanged;

            // Note: No inputscope supported in PasswordBox control
            // TODO: Show numeric keyboard
        }

        private void OnKeyDown(object sender, KeyRoutedEventArgs e)
        {
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

        private void OnPasswordChanged(object sender, RoutedEventArgs e)
        {
            var passwordBox = AssociatedObject as PasswordBox;
            if (passwordBox != null && !string.IsNullOrWhiteSpace(REGEX_NUMERIC))
            {
                if (Regex.IsMatch(passwordBox.Password, REGEX_NUMERIC))
                {
                    // The text matches the regular expression.
                    lastValidText = passwordBox.Password;
                }
                else
                {
                    // The text doesn't match the regular expression.
                    // Restore the last valid value.
                    //var caretPosition = passwordBox.SelectionStart;
                    passwordBox.Password = lastValidText;
                    //passwordBox.SelectionStart = (caretPosition > 0) ? caretPosition - 1 : 0;
                }
            }
        }

        /// <summary>
        /// Detaches the object associated with this behavior.
        /// </summary>
        public void Detach()
        {
            var passwordBox = AssociatedObject as PasswordBox;
            if (passwordBox != null)
                passwordBox.PasswordChanged -= this.OnPasswordChanged;
        }
    }
}
