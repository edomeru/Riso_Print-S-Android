using GalaSoft.MvvmLight.Command;
using Microsoft.Xaml.Interactivity;
using SmartDeviceApp.Behaviors;
using SmartDeviceApp.Common.Enum;
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

namespace SmartDeviceApp.Controls
{
    public sealed partial class KeyPasswordBoxControl : KeyValueControl
    {
        private bool _isPasswordBoxLoaded;
        private ICommand _setFocus;
        private PasswordBox _passwordBox;

        /// <summary>
        /// Constructor for KeyPasswordBoxControl.
        /// </summary>
        public KeyPasswordBoxControl()
        {
            this.InitializeComponent();
            _passwordBox = this.passwordBox;
            this.Command = SetFocus;
        }

        public new static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(KeyPasswordBoxControl), new PropertyMetadata(false, SetValueText));

        public static readonly DependencyProperty PasswordMaxLengthProperty =
            DependencyProperty.Register("PasswordMaxLength", typeof(int), typeof(KeyPasswordBoxControl), null);

        public static readonly DependencyProperty PasswordBoxWidthProperty =
            DependencyProperty.Register("PasswordBoxWidth", typeof(double), typeof(KeyPasswordBoxControl), null);

        public static readonly DependencyProperty PasswordBoxBehaviorProperty =
            DependencyProperty.Register("PasswordBoxBehavior", typeof(TextBoxBehavior), typeof(KeyTextBoxControl), new PropertyMetadata(TextBoxBehavior.Alphanumeric, SetPasswordBoxBehavior));

        /// <summary>
        /// Text property of the Value element of the control.
        /// </summary>
        public new string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        /// <summary>
        /// Holds the max length of the password.
        /// </summary>
        public int PasswordMaxLength
        {
            get { return (int)GetValue(PasswordMaxLengthProperty); }
            set { SetValue(PasswordMaxLengthProperty, value); }
        }

        /// <summary>
        /// Holds the width of the PasswordBox.
        /// </summary>
        public double PasswordBoxWidth
        {
            get { return (double)GetValue(PasswordBoxWidthProperty); }
            set { SetValue(PasswordBoxWidthProperty, value); }
        }

        /// <summary>
        /// Behavior for the PasswordBox.
        /// </summary>
        public TextBoxBehavior PasswordBoxBehavior
        {
            get { return (TextBoxBehavior)GetValue(PasswordBoxBehaviorProperty); }
            set { SetValue(PasswordBoxBehaviorProperty, value); }
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

        private void OnPasswordBoxLoaded(object obj, RoutedEventArgs args)
        {
            if (!_isPasswordBoxLoaded)
            {
                KeyPasswordBoxControl context = this;
                var passwordBox = ((PasswordBox)obj);
                passwordBox.KeyUp += OnFinalizePasswordBox;
                passwordBox.LostFocus += OnLostFocus;
                passwordBox.Width = PasswordBoxWidth;
                passwordBox.MaxLength = PasswordMaxLength;
                var behavior = new NumericPasswordBehavior();
                behavior.Attach(passwordBox);
                _isPasswordBoxLoaded = true;
            }
        }

        private static void SetValueText(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ((KeyPasswordBoxControl)obj).passwordBox.Password = e.NewValue.ToString();
        }
        
        private void SetFocusExecute()
        {
            _passwordBox.Focus(FocusState.Programmatic);
        }

        private void OnLostFocus(object sender, RoutedEventArgs e)
        {
            ValueText = ((PasswordBox)sender).Password;
            var scope = FocusManager.TryMoveFocus(FocusNavigationDirection.Next);
        }

        private void OnFinalizePasswordBox(object sender, KeyRoutedEventArgs e)
        {
            if (e.Key == VirtualKey.Enter)
            {
                OnLostFocus(sender, null);
            }
        }

        private static void SetPasswordBoxBehavior(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is TextBoxBehavior)) return;
            IBehavior behavior = null;
            switch ((TextBoxBehavior)e.NewValue)
            {
                case TextBoxBehavior.Numeric:
                    behavior = new NumericPasswordBehavior();
                    behavior.Attach(((KeyPasswordBoxControl)obj).passwordBox);
                    break;
                case TextBoxBehavior.Alphanumeric:
                    // TODO: Not yet needed
                    break;
            }
        }
    }
}
