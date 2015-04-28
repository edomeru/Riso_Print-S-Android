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
    public sealed partial class KeyToggleButtonControl : KeyValueControl
    {
        private ICommand _toggleValues;
        private ICommand _updateYesValue;
        private ICommand _updateNoValue;
        private bool isUnCheckSet = false;
        private bool isCheckSet = false;
        /// <summary>
        /// Constructor for the control.
        /// </summary>
        public KeyToggleButtonControl()
        {
            this.InitializeComponent();
            this.Command = ToggleValues;
            this.NoToggle.IsChecked = true;
            this.IsNo = true;
            this.IsYes = false;
            this.YesToggle.IsChecked = false;
        }

        public static readonly DependencyProperty IsDefaultProperty =
            DependencyProperty.Register("IsDefault", typeof(bool), typeof(KeyToggleButtonControl), new PropertyMetadata(false, SetDefault));

        public static readonly DependencyProperty IsYesProperty =
            DependencyProperty.Register("IsYes", typeof(bool), typeof(KeyToggleButtonControl), new PropertyMetadata(false, SetYes));

        public static readonly DependencyProperty IsNoProperty =
            DependencyProperty.Register("IsNo", typeof(bool), typeof(KeyToggleButtonControl), null);

        /// <summary>
        /// Gets/Sets values of the IsDefaultProperty. This is binded to the Printer Model.
        /// </summary>
        public bool IsDefault
        {
            get { return (bool)GetValue(IsDefaultProperty); }
            set { SetValue(IsDefaultProperty, value); }
        }

        /// <summary>
        /// Command used to toggle values of the ToggleButton.
        /// </summary>
        public ICommand ToggleValues
        {
            get {
                if (_toggleValues == null)
                {
                    _toggleValues = new RelayCommand(
                        () => ToggleValuesExecute(),
                        () => true
                        );
                }

                return _toggleValues;
            }
        }

        /// <summary>
        /// Binded to the Yes ToggleButton.
        /// </summary>
        public bool IsYes
        {
            get { return (bool)GetValue(IsYesProperty); }
            set { SetValue(IsYesProperty, value); }
        }

        /// <summary>
        /// Binded to the No ToggleButton.
        /// </summary>
        public bool IsNo
        {
            get { return (bool)GetValue(IsNoProperty); }
            set { SetValue(IsNoProperty, value); }
        }
        
        private void ToggleValuesExecute()
        {
            if (IsNo)
            {
                IsNo = !IsNo;
                IsYes = !IsNo;
            }
        }
        
        private static void SetDefault(DependencyObject d, DependencyPropertyChangedEventArgs e)
        {
            ((KeyToggleButtonControl)d).IsDefault = bool.Parse(e.NewValue.ToString());
            
            if (((KeyToggleButtonControl)d).IsDefault == true)
            {
                if (((KeyToggleButtonControl)d).IsYes == false)
                {
                    ((KeyToggleButtonControl)d).IsYes = true;
                    ((KeyToggleButtonControl)d).IsNo = false;
                }

                ((KeyToggleButtonControl)d).YesToggle.IsChecked = true;
                ((KeyToggleButtonControl)d).NoToggle.IsChecked = false;

                ((KeyToggleButtonControl)d).YesToggle.IsEnabled = false;
                ((KeyToggleButtonControl)d).NoToggle.IsEnabled = false;
            }
            else
            {

                if (((KeyToggleButtonControl)d).IsYes == true)
                {
                    ((KeyToggleButtonControl)d).IsYes = false;
                    ((KeyToggleButtonControl)d).IsNo = true;
                }
                

                ((KeyToggleButtonControl)d).YesToggle.IsEnabled = true;
                ((KeyToggleButtonControl)d).NoToggle.IsEnabled = true;

                ((KeyToggleButtonControl)d).YesToggle.IsChecked = false;
                ((KeyToggleButtonControl)d).NoToggle.IsChecked = true;
            }
        }

        private static void SetYes(DependencyObject d, DependencyPropertyChangedEventArgs e)
        {
            if (((KeyToggleButtonControl)d).IsDefault != bool.Parse(e.NewValue.ToString()))
            {
                ((KeyToggleButtonControl)d).IsDefault = bool.Parse(e.NewValue.ToString());
            }
        }

        private void OnNoToggleLoaded(object obj, RoutedEventArgs args)
        {
            if (!isUnCheckSet)
            {
                KeyToggleButtonControl context = this;

                ((ToggleButton)obj).Unchecked += (sender, e) => NoToggled(sender, context);
                isUnCheckSet = true;
            }
        }


        private void OnYesToggleLoaded(object obj, RoutedEventArgs args)
        {
            if (!isCheckSet)
            {
                KeyToggleButtonControl context = this;

                ((ToggleButton)obj).Checked += (sender, e) => YesToggled(sender, context);
                isCheckSet = true;
            }
        }

        private void YesToggled(object sender, KeyToggleButtonControl control)
        {
            control.SetValue(IsYesProperty, ((ToggleButton)sender).IsChecked);
            control.SetValue(IsNoProperty, !((ToggleButton)sender).IsChecked);
        }

        private void NoToggled(object sender, KeyToggleButtonControl control)
        {
            control.SetValue(IsYesProperty, !((ToggleButton)sender).IsChecked);
            control.SetValue(IsNoProperty, ((ToggleButton)sender).IsChecked);
        }
    }
}
