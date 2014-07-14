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
using SmartDeviceApp.Controls;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Behaviors;
using Microsoft.Xaml.Interactivity;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Views
{
    public sealed partial class AddPrinterPane : UserControl
    {
        /// <summary>
        /// Constructor of AddPrinterPane.
        /// </summary>
        public AddPrinterPane()
        {
            this.InitializeComponent();

        }

        private void ipTextBox_Loaded(object sender, RoutedEventArgs e)
        {
            IBehavior behavior = null;
            behavior = new IPAddressTextBoxBehavior();
            behavior.Attach((TextBox)sender);
        }

        /// <summary>
        /// Holds the data context for this xaml.
        /// </summary>
        public AddPrinterViewModel ViewModel
        {
            get
            {
                return (AddPrinterViewModel)DataContext;
            }
        }

        private void AddSidePane_Loaded(object sender, RoutedEventArgs e)
        {
            ViewModel.Height = (double)((new SidePanesHeightConverter()).Convert(this, null, null, null));
        }
    }
}
