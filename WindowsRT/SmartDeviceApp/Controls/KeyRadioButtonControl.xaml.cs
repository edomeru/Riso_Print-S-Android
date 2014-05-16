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
using SmartDeviceApp.Models;
using GalaSoft.MvvmLight.Command;

namespace SmartDeviceApp.Controls
{
    public sealed partial class KeyRadioButtonControl : KeyValueControl
    {
        public KeyRadioButtonControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty GroupNameProperty =
            DependencyProperty.Register("GroupName", typeof(string), typeof(KeyRadioButtonControl), null);

        public static readonly DependencyProperty IsCheckedProperty =
           DependencyProperty.Register("IsChecked", typeof(bool), typeof(KeyRadioButtonControl), new PropertyMetadata(false, SetIsChecked));
        
        public static new readonly DependencyProperty IsEnabledProperty =
           DependencyProperty.Register("IsEnabled", typeof(bool), typeof(KeyRadioButtonControl), new PropertyMetadata(true, SetIsEnabled));
        
        public static readonly DependencyProperty IndexProperty =
            DependencyProperty.Register("Index", typeof(int), typeof(KeyRadioButtonControl), null);

        public string GroupName
        {
            get { return (string)GetValue(GroupNameProperty); }
            set { SetValue(GroupNameProperty, value); }
        }

        public bool IsChecked
        {
            get { return (bool)GetValue(IsCheckedProperty); }
            set { SetValue(IsCheckedProperty, value); }
        }

        public new bool IsEnabled
        {
            get { return (bool)GetValue(IsEnabledProperty); }
            set { SetValue(IsEnabledProperty, value); }
        }

        public int Index
        {
            get { return (int)GetValue(IndexProperty); }
            set { SetValue(IndexProperty, value); }
        }

        private static void SetIsChecked(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is bool)) return;
            if ((bool)e.NewValue)
            {
                ((KeyRadioButtonControl)obj).radioButton.IsChecked = true;
            }
            else
            {
                ((KeyRadioButtonControl)obj).radioButton.IsChecked = false;
            }
        }

        private static void SetIsEnabled(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is bool)) return;
            if ((bool)e.NewValue)
            {
                ((KeyRadioButtonControl)obj).Visibility = Visibility.Visible;
            }
            else
            {
                ((KeyRadioButtonControl)obj).Visibility = Visibility.Collapsed;
            }
        }

        private void OnTapped(object sender, RoutedEventArgs e)
        {
            radioButton.IsChecked = true;
        }
    }
}
