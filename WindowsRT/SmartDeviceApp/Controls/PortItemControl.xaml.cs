using GalaSoft.MvvmLight.Command;
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

// The User Control item template is documented at http://go.microsoft.com/fwlink/?LinkId=234236

namespace SmartDeviceApp.Controls
{
    public sealed partial class PortItemControl : KeyValueControl
    {
        public PortItemControl()
        {
            this.InitializeComponent();
        }

        private bool _isRawToggled;

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(PortItemControl), null);

        public static readonly DependencyProperty SelectedIndexProperty =
            DependencyProperty.Register("SelectedIndex", typeof(int), typeof(PortItemControl), null);

        public static readonly DependencyProperty IsRawSelectedProperty =
            DependencyProperty.Register("IsRawSelected", typeof(bool), typeof(PortItemControl), new PropertyMetadata(false, SetRawChecked));

        private static void SetRawChecked(DependencyObject d, DependencyPropertyChangedEventArgs e)
        {
            ((PortItemControl)d).RawToggle.IsChecked = bool.Parse(e.NewValue.ToString());
        }


        public static readonly DependencyProperty IsLPRSelectedProperty =
            DependencyProperty.Register("IsLPRSelected", typeof(bool), typeof(PortItemControl), new PropertyMetadata(false, SetLPRChecked));

        private static void SetLPRChecked(DependencyObject d, DependencyPropertyChangedEventArgs e)
        {
            ((PortItemControl)d).LPRToggle.IsChecked = bool.Parse(e.NewValue.ToString());
        }

        

        //public static readonly DependencyProperty DeleteCommandProperty =
        //    DependencyProperty.Register("DeleteCommand", typeof(ICommand), typeof(KeyDropDownListControl), null);

        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        public int SelectedIndex
        {
            get { return (int)GetValue(SelectedIndexProperty); }
            set { SetValue(SelectedIndexProperty, value); }
        }

        public bool IsRawSelected
        {
            get { return (bool)GetValue(IsRawSelectedProperty); }
            set { SetValue(IsRawSelectedProperty, value); }
        }

        public bool IsLPRSelected
        {
            get { return (bool)GetValue(IsLPRSelectedProperty); }
            set { SetValue(IsLPRSelectedProperty, value); }
        }

        

        private ICommand _updatePort;
        public ICommand UpdatePort
        {
            get
            {
                if (_updatePort == null)
                {
                    _updatePort = new RelayCommand(
                        () => UpdatePortExecute(),
                        () => true
                    );
                }
                return _updatePort;
            }
        }

        private void UpdatePortExecute()
        {
            IsRawSelected = !IsLPRSelected;
        }

        private ICommand _updateRawPort;
        public ICommand UpdateRawPort
        {
            get
            {
                if (_updateRawPort == null)
                {
                    _updateRawPort = new RelayCommand(
                        () => UpdateRawPortExecute(),
                        () => true
                    );
                }
                return _updateRawPort;
            }
        }

        private void UpdateRawPortExecute()
        {
            IsLPRSelected = !IsRawSelected;
        }

        private void OnRawPortLoaded(object obj, RoutedEventArgs args)
        {
            UpdateRawPortExecute();

            PortItemControl context = this;

            ((ToggleButton)obj).Tapped += (sender, e) => RawToggled(sender, context);
        }

        private void OnLPRPortLoaded(object obj, RoutedEventArgs args)
        {
            //UpdateRawPortExecute();

            PortItemControl context = this;

            ((ToggleButton)obj).Tapped += (sender, e) => LPRToggled(sender, context);
        }

        private void LPRToggled(object sender, PortItemControl control)
        {
            control.SetValue(IsLPRSelectedProperty, ((ToggleButton)sender).IsChecked);
            control.SetValue(IsRawSelectedProperty, !((ToggleButton)sender).IsChecked);
        }

        private void RawToggled(object sender, PortItemControl control)
        {
            control.SetValue(IsLPRSelectedProperty, !((ToggleButton)sender).IsChecked);
            control.SetValue(IsRawSelectedProperty, ((ToggleButton)sender).IsChecked);
        }
    }
}
