using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Windows.Input;
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

namespace SmartDeviceApp.Controls
{
    public sealed partial class KeyTextBoxControl : KeyValueControl
    {
        private bool _isTextBoxLoaded;
        private ICommand _setFocus;
        private TextBox _textBox;

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

        public new string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        public double TextBoxWidth
        {
            get { return (double)GetValue(TextBoxWidthProperty); }
            set { SetValue(TextBoxWidthProperty, value); }
        }

        public TextAlignment TextBoxAlignment
        {
            get { return (TextAlignment)GetValue(TextBoxAlignmentProperty); }
            set { SetValue(TextBoxAlignmentProperty, value); }
        }

        public int TextBoxMaxLength
        {
            get { return (int)GetValue(TextBoxMaxLengthProperty); }
            set { SetValue(TextBoxMaxLengthProperty, value); }
        }

        // Sets the focus to the textbox when any part of the button is tapped
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
                textBox.LostFocus += OnLostFocus;
                textBox.Width = TextBoxWidth;
                textBox.TextAlignment = TextBoxAlignment;
                textBox.MaxLength = TextBoxMaxLength;
                _isTextBoxLoaded = true;
            }
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
            ValueText = ((TextBox)sender).Text;
            var scope = FocusManager.TryMoveFocus(FocusNavigationDirection.Previous);
        }

        private void OnFinalizeTextBox(object sender, KeyRoutedEventArgs e)
        {
            if (e.Key == VirtualKey.Enter)
            {
                OnLostFocus(sender, null);
            }
        }
    }
}
