﻿using System;
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
    public sealed partial class KeyToggleSwitchControl : KeyValueControl
    {
        private bool _isToggleSwitchLoaded;
        private ICommand _toggleSwitchCommand;

        public KeyToggleSwitchControl()
        {
            this.InitializeComponent();
            this.Command = ToggleSwitchCommand;
        }

        private static bool _isToggled;

        public static readonly DependencyProperty IsOnProperty =
            DependencyProperty.Register("IsOn", typeof(string), typeof(KeyToggleSwitchControl), new PropertyMetadata(false, SetIsOn));

        // Toggles the switch when any part of the button is tapped
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

        public bool IsOn
        {
            get { return (bool)GetValue(IsOnProperty); }
            set { SetValue(IsOnProperty, value); }
        }

        private void OnToggleSwitchLoaded(object obj, RoutedEventArgs args)
        {
            if (!_isToggleSwitchLoaded)
            {
                KeyToggleSwitchControl context = this;
                ((ToggleSwitch)obj).Toggled += (sender, e) => Toggled(sender, context);

                _isToggleSwitchLoaded = true;
            }
        }

        private static void SetIsOn(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            _isToggled = false; // Set this value so that Toggled handler will not be called by the next line
            ((KeyToggleSwitchControl)obj).toggleSwitch.IsOn = bool.Parse(e.NewValue.ToString());
            _isToggled = true;
        }

        // Updates the value source binding every time the switch is toggled
        private static void Toggled(object sender, KeyToggleSwitchControl control)
        {
            if (!_isToggled) return;
            control.SetValue(IsOnProperty, ((ToggleSwitch)sender).IsOn);
        }

        private void ToggleSwitchExecute()
        {
            IsOn = !IsOn;
        }
    }
}
