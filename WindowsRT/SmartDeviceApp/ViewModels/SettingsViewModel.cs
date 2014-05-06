using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
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
        private string _readCommunityName;
        
        public SettingsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
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

        public string ReadCommunityName
        {
            get { return _readCommunityName; }
            set
            {
                if (_readCommunityName != value)
                {
                    _readCommunityName = value;
                    RaisePropertyChanged("ReadCommunityName");
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
    }
}
