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
    public sealed class AlphanumericTextBoxBehavior : DependencyObject, IBehavior
    {
        private const string REGEX_NON_ALPHANUMERIC = "[^a-zA-Z0-9]";

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
            textBox.TextChanged += OnTextChanged;

            var inputScope = new InputScope();
            inputScope.Names.Add(new InputScopeName(InputScopeNameValue.AlphanumericHalfWidth));
            textBox.InputScope = inputScope;
        }

        private void OnKeyDown(object sender, KeyRoutedEventArgs e)
        {
            // Note: Shift + another key are regarded as separate KeyDown events for
            // certain keys e.g. $, #, &
            // Need to ignore event when shift is pressed
            var shiftKeyState = CoreWindow.GetForCurrentThread().GetKeyState(VirtualKey.Shift);
            var isShiftPressed = (shiftKeyState & CoreVirtualKeyStates.Down) == CoreVirtualKeyStates.Down;
            // Check if key is numeric
            if ((e.Key >= VirtualKey.Number0 && e.Key <= VirtualKey.Number9 && !isShiftPressed) ||
                (e.Key >= VirtualKey.A && e.Key <= VirtualKey.Z))
            {
                // Do nothing, let OnTextChanged handle event
                return;
            }
            e.Handled = true; // Ignore key
        }

        private void OnTextChanged(object sender, TextChangedEventArgs e)
        {
            var textBox = AssociatedObject as TextBox;
            if (textBox != null && !string.IsNullOrWhiteSpace(REGEX_NON_ALPHANUMERIC))
            {
                if (!Regex.IsMatch(textBox.Text, REGEX_NON_ALPHANUMERIC))
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

        /// <summary>
        /// Detaches the object associated with this behavior.
        /// </summary>
        public void Detach()
        {
            var textBox = AssociatedObject as TextBox;
            if (textBox != null)
                textBox.TextChanged -= this.OnTextChanged;
        }
    }
}
