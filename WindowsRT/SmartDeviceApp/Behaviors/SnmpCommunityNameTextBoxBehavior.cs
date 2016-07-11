using GalaSoft.MvvmLight.Messaging;
using Microsoft.Xaml.Interactivity;
using SmartDeviceApp.Common.Enum;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Windows.ApplicationModel.DataTransfer;
using Windows.System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;
using SmartDeviceApp.Controllers;

namespace SmartDeviceApp.Common.Utilities
{
    public class SnmpCommunityNameTextBoxBehavior : DependencyObject, IBehavior
    {
        private const string REGEX_SNMP_COMMUNITY_NAME_CHARS = "^[a-zA-Z0-9,./:;@\\[\\\\\\]\\^_]*$";

        private string _lastValidText;        

        /// <summary>
        /// This prevents reverting back to the saved Snmp Community Name
        /// and is useful for onPaste with invalid paste data since the 
        /// alert triggers onLostFocus
        /// </summary>
        private Boolean _preventRevertOnLostFocus = false;

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

            _lastValidText = textBox.Text;
            textBox.KeyDown += OnKeyDown;
            textBox.KeyUp += OnKeyUp;
            textBox.Paste += Paste;
            textBox.TextChanged += OnTextChanged;
            textBox.LostFocus += OnLostFocus;

            textBox.IsTextPredictionEnabled = false;

            var inputScope = new InputScope();
            inputScope.Names.Add(new InputScopeName(InputScopeNameValue.Number));
            textBox.InputScope = inputScope;
        }        

        private void SaveSnmpCommunityName(string snmpCommunityName)
        {
            Messenger.Default.Send(new NotificationMessage<MessageType>(MessageType.SaveSnmpCommunityName, snmpCommunityName));
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
                textBox.Paste -= Paste;
                textBox.LostFocus -= OnLostFocus;
            }
        }

        private void OnKeyUp(object sender, Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        {
            if (e.Key == VirtualKey.Shift)
            {
                IsShiftPressed = false;
            }

            if (e.Key == VirtualKey.Enter)
            {
                TextBox textBox = (TextBox) sender;
                SaveSnmpCommunityName(textBox.Text);
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

        private async void Paste(object sender, TextControlPasteEventArgs e)
        {           
            TextBox textBox = (TextBox) sender;
            var contents = Clipboard.GetContent();
            var text = await contents.GetTextAsync();

            if (!Regex.IsMatch(text, REGEX_SNMP_COMMUNITY_NAME_CHARS))
            {
                e.Handled = true; // set as handled to block appending to textbox
                Messenger.Default.Send(new NotificationMessage<MessageType>(MessageType.SnmpCommunityNamePasteInvalid, null));
                ResetToLastValidText(textBox); // revert back to last valid text since onTextChange only handles already-truncated text (handling for special case where invalid character is out of bounds)
                _preventRevertOnLostFocus = true;
            }
        }

        private void OnLostFocus(object sender, RoutedEventArgs e)
        {
            TextBox textBox = (TextBox)sender;

            if (!string.IsNullOrEmpty(textBox.Text))
            {
                SaveSnmpCommunityName(textBox.Text);
            }
            else if (!_preventRevertOnLostFocus)
            {
                textBox.Text = SettingController.Instance.GetSnmpCommunityName(); // Reset to saved local setting value if textbox is empty                
            }

            _preventRevertOnLostFocus = false;
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

            // check if valid SNMP Community Name characters
            if (textBox != null && !string.IsNullOrWhiteSpace(REGEX_SNMP_COMMUNITY_NAME_CHARS))
            {
                if (Regex.IsMatch(textBox.Text, REGEX_SNMP_COMMUNITY_NAME_CHARS))
                {
                    // The text matches the regular expression.
                    _lastValidText = textBox.Text;
                }
                else
                {
                    // The text doesn't match the regular expression.
                    // Restore the last valid value.
                    ResetToLastValidText(textBox);
                }
            }
        }

        private void ResetToLastValidText(TextBox textBox)
        {
            if (textBox != null)
            {
                textBox.Text = _lastValidText;
                textBox.SelectionStart = _lastValidText.Length;
            }            
        }
    }
}
