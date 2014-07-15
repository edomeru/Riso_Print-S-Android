using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Windows.Input;
using Windows.Foundation;
using Windows.Foundation.Collections;
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
    public sealed partial class KeyToggleCheckControl : KeyValueControl
    {
        private bool _isToggleSwitchLoaded;
        private ICommand _toggleSwitchCommand;

        /// <summary>
        /// Constructor of KeyToggleCheckControl.
        /// </summary>
        public KeyToggleCheckControl()
        {
            this.InitializeComponent();
            this.Command = ToggleSwitchCommand;
        }

        private static bool _isToggled;

        public static readonly DependencyProperty IsOnProperty =
            DependencyProperty.Register("IsOn", typeof(bool), typeof(KeyToggleCheckControl), new PropertyMetadata(false, SetIsOn));

        public static readonly DependencyProperty ToggleVisibilityProperty =
            DependencyProperty.Register("ToggleVisibility", typeof(Visibility), typeof(KeyToggleCheckControl), null);

        /// <summary>
        /// Toggles the switch when any part of the button is tapped
        /// </summary>
        public ICommand ToggleSwitchCommand
        {
            get
            {
                if (_toggleSwitchCommand == null)
                {
                    _toggleSwitchCommand = new RelayCommand(
                        () => ToggleSwitchExecute(),
                        () => true
                    );
                }
                return _toggleSwitchCommand;
            }
        }

        /// <summary>
        /// Flag to check whether the toggle is on.
        /// </summary>
        public bool IsOn
        {
            get { return (bool)GetValue(IsOnProperty); }
            set { SetValue(IsOnProperty, value); }
        }

        /// <summary>
        /// Visibility property for the toggle switch.
        /// </summary>
        public Visibility ToggleVisibility
        {
            get { return (Visibility)GetValue(ToggleVisibilityProperty); }
            set { SetValue(ToggleVisibilityProperty, value); }
        }

        private void OnToggleSwitchLoaded(object obj, RoutedEventArgs args)
        {
            if (!_isToggleSwitchLoaded)
            {
                KeyToggleCheckControl context = this;
                ((ToggleSwitch)obj).Toggled += (sender, e) => Toggled(sender, context);

                _isToggleSwitchLoaded = true;
            }
        }

        private static void SetIsOn(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            _isToggled = false; // Set this value so that Toggled handler will not be called by the next line
            ((KeyToggleCheckControl)obj).toggleSwitch.IsOn = bool.Parse(e.NewValue.ToString());
            if (bool.Parse(e.NewValue.ToString()) == true)
            {
                ((KeyToggleCheckControl)obj).toggleSwitch.Visibility = Visibility.Collapsed;
            }
            else
            {
                ((KeyToggleCheckControl)obj).toggleSwitch.Visibility = Visibility.Visible;
            }
            _isToggled = true;
        }

        // Updates the value source binding every time the switch is toggled
        private static void Toggled(object sender, KeyToggleCheckControl control)
        {
            if (!_isToggled) return;
            control.SetValue(IsOnProperty, ((ToggleSwitch)sender).IsOn);
        }

        private void ToggleSwitchExecute()
        {
            if (!IsOn) // if not default printer
                IsOn = true;
        }
    }
}
