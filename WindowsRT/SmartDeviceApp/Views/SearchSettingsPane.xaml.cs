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
using System.Collections.ObjectModel;
using SmartDeviceApp.Models;
using System.Windows.Input;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Converters;

namespace SmartDeviceApp.Views
{
    public sealed partial class SearchSettingsPane : UserControl
    {
        /// <summary>
        /// Constructor for SearchSettingsPane class.
        /// </summary>
        public SearchSettingsPane()
        {
            try
            {
                this.InitializeComponent();
            }
            catch
            {
                // Do nothing
            }
        }

        /// <summary>
        /// Holds the data context of this xaml.
        /// </summary>
        public SearchSettingsViewModel ViewModel
        {
            get
            {
                return (SearchSettingsViewModel)DataContext;
            }
        }

        private void SearchSettingsSidePane_Loaded(object sender, RoutedEventArgs e)
        {
            var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
            ViewModel.Height = (double)((new SidePanesHeightConverter()).Convert(this, null, null, null)) - titleHeight;
        }
    }
}
