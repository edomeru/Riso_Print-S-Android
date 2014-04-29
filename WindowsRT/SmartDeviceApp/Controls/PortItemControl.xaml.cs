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
    public sealed partial class PortItemControl : UserControl
    {
        public PortItemControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(PortItemControl), null);

        public static readonly DependencyProperty SelectedIndexProperty =
            DependencyProperty.Register("SelectedIndex", typeof(int), typeof(PortItemControl), null);
        public static readonly DependencyProperty LPRSelectedProperty =
            DependencyProperty.Register("LPRSelected", typeof(int), typeof(PortItemControl), null);

        public static readonly DependencyProperty IconVisibilityProperty =
            DependencyProperty.Register("IconVisibility", typeof(Visibility), typeof(PortItemControl), null);

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

        public int LPRSelected
        {
            get { return (int)GetValue(LPRSelectedProperty); }
            set { SetValue(LPRSelectedProperty, value); }
        }

        public string IconVisibility
        {
            get { return (string)GetValue(IconVisibilityProperty); }
            set { SetValue(IconVisibilityProperty, value); }
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
            if (SelectedIndex == 0)
            {
                SelectedIndex = 1;
                LPRSelected = 0;
            }
            else
            {
                SelectedIndex = 0;
                LPRSelected = 1;
            }
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
            if (SelectedIndex == 0)
            {
                LPRSelected = 1;
            }
            else
            {
                LPRSelected = 0;
            }
        }

        private void OnPortLoaded(object obj, RoutedEventArgs args)
        {
            UpdateRawPortExecute();
        }
    }
}
