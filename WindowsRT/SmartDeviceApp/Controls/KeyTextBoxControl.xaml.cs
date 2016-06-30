using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Windows.Input;
using Microsoft.Xaml.Interactivity;
using Windows.ApplicationModel.DataTransfer;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using GalaSoft.MvvmLight.Command;
using SmartDeviceApp.Behaviors;
using SmartDeviceApp.Common.Enum;
using System.Text.RegularExpressions;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.Controls
{
    public sealed partial class KeyTextBoxControl : KeyValueControl
    {
        private bool _isTextBoxLoaded;
        private ICommand _setFocus;
        private TextBox _textBox;

        /// <summary>
        /// Constructor for KeyTextBoxControl.
        /// </summary>
        public KeyTextBoxControl()
        {
            this.InitializeComponent();
            _textBox = this.textBox;
            this.Command = SetFocus;
        }

        public new static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(KeyTextBoxControl), new PropertyMetadata(false, SetValueText));

        public static readonly DependencyProperty TextBoxWidthProperty =
            DependencyProperty.Register("TextBoxWidth", typeof(double), typeof(KeyTextBoxControl), null);

        public static readonly DependencyProperty TextBoxAlignmentProperty =
            DependencyProperty.Register("TextBoxAlignment", typeof(TextAlignment), typeof(KeyTextBoxControl), null);

        public static readonly DependencyProperty TextBoxMaxLengthProperty =
            DependencyProperty.Register("TextBoxMaxLength", typeof(int), typeof(KeyTextBoxControl), null);

        public static readonly DependencyProperty TextBoxBehaviorProperty =
            DependencyProperty.Register("TextBoxBehavior", typeof(TextBoxBehavior), typeof(KeyTextBoxControl), new PropertyMetadata(TextBoxBehavior.Alphanumeric, SetTextBoxBehavior));

        public static readonly DependencyProperty TextBoxValidValueProperty =
            DependencyProperty.Register("TextBoxValidValue", typeof(string), typeof(KeyTextBoxControl), null);

        public static readonly DependencyProperty TextBoxErrorLabelProperty =
            DependencyProperty.Register("TextBoxErrorLabel", typeof(string), typeof(KeyTextBoxControl), null);

        public static readonly DependencyProperty TextBoxErrorMessageProperty =
            DependencyProperty.Register("TextBoxErrorMessage", typeof(string), typeof(KeyTextBoxControl), null);

        /// <summary>
        /// Text property of the Value element of the control.
        /// </summary>
        public new string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        /// <summary>
        /// Width of the Textbox.
        /// </summary>
        public double TextBoxWidth
        {
            get { return (double)GetValue(TextBoxWidthProperty); }
            set { SetValue(TextBoxWidthProperty, value); }
        }

        /// <summary>
        /// Alignment for the Textbox.
        /// </summary>
        public TextAlignment TextBoxAlignment
        {
            get { return (TextAlignment)GetValue(TextBoxAlignmentProperty); }
            set { SetValue(TextBoxAlignmentProperty, value); }
        }

        /// <summary>
        /// Max length of the text in Textbox.
        /// </summary>
        public int TextBoxMaxLength
        {
            get { return (int)GetValue(TextBoxMaxLengthProperty); }
            set { SetValue(TextBoxMaxLengthProperty, value); }
        }

        /// <summary>
        /// Behavior of the Textbox.
        /// </summary>
        public TextBoxBehavior TextBoxBehavior
        {
            get { return (TextBoxBehavior)GetValue(TextBoxBehaviorProperty); }
            set { SetValue(TextBoxBehaviorProperty, value); }
        }

        /// <summary>
        /// The text box valid values
        /// </summary>
        public string TextBoxValidValue
        {
            get { return (string)GetValue(TextBoxValidValueProperty); }
            set { SetValue(TextBoxValidValueProperty, value); }
        }

        /// <summary>
        /// The text box error label
        /// </summary>
        public string TextBoxErrorLabel
        {
            get { return (string)GetValue(TextBoxErrorLabelProperty); }
            set { SetValue(TextBoxErrorLabelProperty, value); }
        }

        /// <summary>
        /// The text box error message
        /// </summary>
        public string TextBoxErrorMessage
        {
            get { return (string)GetValue(TextBoxErrorMessageProperty); }
            set { SetValue(TextBoxErrorMessageProperty, value); }
        }

        /// <summary>
        /// Sets the focus to the textbox when any part of the button is tapped
        /// </summary>
        public ICommand SetFocus
        {
            get
            {
                if (_setFocus == null)
                {
                    _setFocus = new RelayCommand(
                        () => SetFocusExecute(),
                        () => true
                    );
                }
                return _setFocus;
            }
        }

        private void OnTextBoxLoaded(object obj, RoutedEventArgs args)
        {
            if (!_isTextBoxLoaded)
            {
                KeyTextBoxControl context = this;
                var textBox = ((TextBox)obj);
                textBox.KeyUp += OnFinalizeTextBox;
                textBox.TextChanged += (sender, e) => TextChanged(sender, context);
                textBox.LostFocus += OnLostFocus;
                textBox.Paste += OnPaste;
                textBox.Width = TextBoxWidth;
                textBox.TextAlignment = TextBoxAlignment;
                textBox.MaxLength = TextBoxMaxLength;
                _isTextBoxLoaded = true;
            }
        }

        // Updates the value source binding every time the text is changed
        private void TextChanged(object sender, KeyTextBoxControl control)
        {
            ValueText = ((TextBox)sender).Text;
        }  

        private static void SetValueText(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ((KeyTextBoxControl)obj).textBox.Text = e.NewValue.ToString();
        }

        private void SetFocusExecute()
        {
            _textBox.Focus(FocusState.Programmatic);
        }

        private void OnLostFocus(object sender, RoutedEventArgs e)
        {
            if ((TextBoxBehavior)GetValue(TextBoxBehaviorProperty) == TextBoxBehavior.Numeric)
            {
                var value = ((TextBox)sender).Text;
                int intValue;
                if (int.TryParse(value, out intValue) || string.IsNullOrEmpty(value))
                {
                    if (intValue == 0) ValueText = "1"; // Set value to 1 instead of 0
                    else ValueText = ((TextBox)sender).Text;
                }
            }
            FocusManager.TryMoveFocus(FocusNavigationDirection.Next);
        }

        private async void OnPaste(object sender, TextControlPasteEventArgs e)
        {
            // Handle the paste event
            e.Handled = true;

            var contents = Clipboard.GetContent();
            if (contents.Contains(StandardDataFormats.Text))
            {
                var text = await contents.GetTextAsync();
                // Check if pasted text contains invalid characters
                if (Regex.IsMatch(text, TextBoxValidValue))
                {
                    _textBox.Text.Insert(_textBox.SelectionStart, text);
                }
                else
                {
                    // Display warning message
                    await DialogService.Instance.ShowError(TextBoxErrorMessage, TextBoxErrorLabel, "IDS_LBL_OK", null);
                }
            }
        }

        private void OnFinalizeTextBox(object sender, KeyRoutedEventArgs e)
        {
            if (e.Key == VirtualKey.Enter)
            {
                OnLostFocus(sender, null);
            }
        }

        private static void SetTextBoxBehavior(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is TextBoxBehavior)) return;
            IBehavior behavior = null; 
            switch ((TextBoxBehavior)e.NewValue)
            {
                case TextBoxBehavior.Numeric:
                    behavior = new NumericTextBoxBehavior();
                    behavior.Attach(((KeyTextBoxControl)obj).textBox);
                    break;
                case TextBoxBehavior.Alphanumeric:
                    behavior = new AlphanumericTextBoxBehavior();
                    behavior.Attach(((KeyTextBoxControl)obj).textBox);
                    break;
            }
        }
    }
}
