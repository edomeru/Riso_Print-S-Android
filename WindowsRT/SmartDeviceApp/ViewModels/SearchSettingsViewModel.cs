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
        private double _height;

        private ViewControlViewModel _viewControlViewModel;

        private ViewOrientation _viewOrientation;

        /// <summary>
        /// Holds the value of the SNMP Community Name used to check for the printer to be added
        /// </summary>
        public string SnmpCommunityName
        {
            get { return SettingController.Instance.GetSnmpCommunityName(); }
            set
            {
                SettingController.Instance.SaveSnmpCommunityName(value);                
                OnPropertyChanged("SnmpCommunityName");
            }
        }

        /// <summary>
        /// Constructor for SearchSettingsViewModel.
        /// </summary>
        public SearchSettingsViewModel()
        {
            _viewControlViewModel = new ViewModelLocator().ViewControlViewModel;
            SnmpCommunityName = SettingController.Instance.GetSnmpCommunityName();

            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ResetSearchPane(viewOrientation));
            Messenger.Default.Register<NotificationMessage<MessageType>>(this, (notificationMessage) => HandleNotificationMessage(notificationMessage));            
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

        /// <summary>
        /// Handles notification messages sent via Light Messenger
        /// Kindly note that since .Notification is always a string
        /// and .Content type can be changed, the .Content is the
        /// MessageType ID and the .Notification is the actual value
        /// </summary>
        private async Task HandleNotificationMessage(NotificationMessage<MessageType> notificationMessage)
        {
            if (notificationMessage.Content == MessageType.SnmpCommunityNamePasteInvalid)
            {
                await DialogService.Instance.ShowError("IDS_ERR_MSG_INVALID_COMMUNITY_NAME", "IDS_LBL_SEARCH_PRINTERS_SETTINGS", "IDS_LBL_OK", null);
            }
            else if (notificationMessage.Content == MessageType.SaveSnmpCommunityName)
            {
                SnmpCommunityName = notificationMessage.Notification;
            }
        }
    }
}
