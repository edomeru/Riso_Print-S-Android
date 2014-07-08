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
        public event SmartDeviceApp.Controllers.SettingController.CardIdValueChangedEventHandler CardIdValueChangedEventHandler;

        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private string _cardId;
        
        public SettingsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Messenger.Default.Register<ViewMode>(this, (viewMode) => EnableMode(viewMode));
        }

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

        private void CardIdValueChanged()
        {
            if (CardIdValueChangedEventHandler != null)
            {
                CardIdValueChangedEventHandler(_cardId);
            }
        }

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

        public Grid SettingsGestureGrid
        {
            get;
            set;
        }
    }
}
