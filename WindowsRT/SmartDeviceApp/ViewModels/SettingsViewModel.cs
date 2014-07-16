using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using Windows.UI.Xaml.Controls;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.ViewModels
{
    public class SettingsViewModel : ViewModelBase
    {
        /// <summary>
        /// Log-in ID value changed event handler
        /// </summary>
        public event SmartDeviceApp.Controllers.SettingController.CardIdValueChangedEventHandler CardIdValueChangedEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private string _cardId;

        /// <summary>
        /// SettingsViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public SettingsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Messenger.Default.Register<ViewMode>(this, (viewMode) => EnableMode(viewMode));
        }

        /// <summary>
        /// Gets/sets the Log-in ID value
        /// </summary>
        public string CardId
        {
            get { return _cardId; }
            set
            {
                if (_cardId != value)
                {
                    _cardId = value;
                    RaisePropertyChanged("CardId");
                    CardIdValueChanged();
                }
            }
        }

        /// <summary>
        /// Log-in ID value changed event handler
        /// </summary>
        private void CardIdValueChanged()
        {
            if (CardIdValueChangedEventHandler != null)
            {
                CardIdValueChangedEventHandler(_cardId);
            }
        }

        /// <summary>
        /// Sets the enabled mode of this view
        /// </summary>
        /// <param name="viewMode">view mode</param>
        private void EnableMode(ViewMode viewMode)
        {
            if (viewMode == ViewMode.FullScreen)
            {
                if (SettingsGestureGrid != null)
                {
                    SettingsGestureGrid.Visibility = Windows.UI.Xaml.Visibility.Collapsed;
                }
            }
            else
            {
                if (SettingsGestureGrid != null)
                {
                    SettingsGestureGrid.Visibility = Windows.UI.Xaml.Visibility.Visible;
                }
            }
        }

        /// <summary>
        /// Grid control for enabling/disabling gestures in Settings Screen
        /// </summary>
        public Grid SettingsGestureGrid
        {
            get;
            set;
        }
    }
}
