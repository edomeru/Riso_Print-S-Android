using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Converters;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Windows.UI.Xaml;

namespace SmartDeviceApp.ViewModels
{
    public class SearchSettingsViewModel : ViewModelBase, INotifyPropertyChanged
    {
        private string _snmpCommunityName;

        private double _height;

        private ViewControlViewModel _viewControlViewModel;

        private ViewOrientation _viewOrientation;

        /// <summary>
        /// Holds the value of the SNMP Community Name used to check for the printer to be added
        /// </summary>
        public string SnmpCommunityName
        {
            get { return _snmpCommunityName; }
            set
            {
                this._snmpCommunityName = value;
                OnPropertyChanged("SnmpCommunityName");
            }
        }

        /// <summary>
        /// Constructor for SearchSettingsViewModel.
        /// </summary>
        public SearchSettingsViewModel()
        {
            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            SnmpCommunityName = SNMP.SNMPConstants.DEFAULT_COMMUNITY_NAME;

            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetSearchPane(viewOrientation));
        }

        private void ResetSearchPane(ViewOrientation viewOrientation)
        {
            var titleHeight = ((GridLength)Application.Current.Resources["SIZE_TitleBarHeight"]).Value;
            Height = (double)((new HeightConverter()).Convert(viewOrientation, null, null, null)) - titleHeight;

            ViewOrientation = viewOrientation;
        }

        /// <summary>
        /// Holds the value for the height of the Search Settings pane.
        /// </summary>
        public double Height
        {
            get { return this._height; }
            set
            {
                _height = value;
                OnPropertyChanged("Height");
            }
        }

        /// <summary>
        /// Event handler for property change.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// Notifies classes that a property has been changed.
        /// </summary>
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }

        /// <summary>
        /// Gets/sets the current view orientation
        /// </summary>
        public ViewOrientation ViewOrientation
        {
            get { return _viewOrientation; }
            set
            {
                if (_viewOrientation != value)
                {
                    _viewOrientation = value;
                    OnPropertyChanged("ViewOrientation");
                }
            }
        }
    }
}
