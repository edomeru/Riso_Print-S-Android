using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

namespace SmartDeviceApp.Controls
{
    public sealed partial class KeyToggleSwitchControl : KeyValueControl
    {
        public KeyToggleSwitchControl()
        {
            this.InitializeComponent();
            KeyToggleSwitchControl context = this;
            toggleSwitch.Toggled += (sender, e) => Toggled(sender, context);
        }

        private static bool _isToggled;

        public static readonly DependencyProperty IsOnProperty =
            DependencyProperty.Register("IsOn", typeof(string), typeof(KeyToggleSwitchControl), new PropertyMetadata(false, SetIsOn));

        public bool IsOn
        {
            get { return (bool)GetValue(IsOnProperty); }
            set { SetValue(IsOnProperty, value); }
        }

        private static void SetIsOn(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            _isToggled = false; // Set this value so that Toggled hanlder will not be called by the next line
            ((KeyToggleSwitchControl)obj).toggleSwitch.IsOn = bool.Parse(e.NewValue.ToString());
            _isToggled = true;
        }

        // Updates the value source binding every time the switch is toggled
        private static void Toggled(object sender, KeyToggleSwitchControl control)
        {
            if (!_isToggled) return;
            control.SetValue(IsOnProperty, ((ToggleSwitch)sender).IsOn);
        }  
    }
}
