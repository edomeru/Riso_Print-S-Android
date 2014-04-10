using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.ViewModels
{
    public class PrintersViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private PrintersRightPaneMode _rightPaneMode;

        public PrintersViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Messenger.Default.Register<VisibleRightPane>(this, (visibleRightPane) => SetRightPaneMode(visibleRightPane));
        }

        public PrintersRightPaneMode RightPaneMode
        {
            get { return _rightPaneMode; }
            set
            {
                if (_rightPaneMode != value)
                {
                    _rightPaneMode = value;
                    RaisePropertyChanged("RightPaneMode");
                }
            }
        }

        private void SetRightPaneMode(VisibleRightPane visibleRightPane)
        {
            switch (visibleRightPane)
            {
                case VisibleRightPane.Pane1:
                    RightPaneMode = PrintersRightPaneMode.SearchPrinter;
                    break;
                case VisibleRightPane.Pane2:
                    RightPaneMode = PrintersRightPaneMode.AddPrinter;
                    break;
            }
        }

    }
}
