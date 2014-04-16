using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.DummyControllers;
using SmartDeviceApp.Controllers;
using System.Collections.ObjectModel;
using System.Windows.Input;
using SmartDeviceApp.Common;
using GalaSoft.MvvmLight.Messaging;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using System.ComponentModel;
using System.Diagnostics;
using GalaSoft.MvvmLight.Command;
using Windows.UI.Xaml;
using System.Windows.Input;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.ViewModels
{
    public class PrintersViewModel : ViewModelBase, INotifyPropertyChanged
    {
        //contains data that binds with PrintersPage
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;
        
        private PrintersRightPaneMode _rightPaneMode;


        private ICommand _deletePrinter;

        private ObservableCollection<Printer> _printerList;

        private int _height;

        
        /**
         * 
         * Delegates for controllers
         * 
         * */

        public event SmartDeviceApp.Controllers.PrinterController.DeletePrinterHandler DeletePrinterHandler;


        public int Height
        {
            get { return this._height; }
            set
            {
                _height = value;
                OnPropertyChanged("Height");
            }
        }

        public ObservableCollection<Printer> PrinterList
        {
            get{ return this._printerList; }
            set
            {
                _printerList = value;
                OnPropertyChanged("PrinterList");
            }
        }

        
        private string _searchPrintersPaneTitleText;
        public string SearchPrintersPaneTitleText
        {
            get { return this._searchPrintersPaneTitleText; }
            set
            {
                _searchPrintersPaneTitleText = value;
                OnPropertyChanged("SearchPrintersPaneTitleText");
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }


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
                    OnPropertyChanged("RightPaneMode");

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

        public ICommand DeletePrinter
        {
            get
            {
                if (_deletePrinter == null)
                {
                    _deletePrinter = new RelayCommand<string>(
                        (ip) => DeletePrinterExecute(ip),
                        (ip) => true
                    );
                }
                return _deletePrinter;
            }
        }

        private void DeletePrinterExecute(string ipAddress)
        {
            DeletePrinterHandler(ipAddress);
        }
    }

}
