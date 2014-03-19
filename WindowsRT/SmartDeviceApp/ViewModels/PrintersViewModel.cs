using GalaSoft.MvvmLight;
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

namespace SmartDeviceApp.ViewModels
{
    public class PrintersViewModel : ViewModelBase
    {
        //contains data that binds with PrintersPage
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _toggleMainMenuPane;
        private ICommand _toggleAddPrinterPane;
        private ICommand _toggleScanPrintersPane;

        private ICommand _addPrinter;

        private ObservableCollection<Printer> _printerList;
        private PrinterController printerController = new PrinterController();

        private string _pageTitleText;
        private string _addPrinterPaneTitleText;
        private string _scanPrintersPaneTitleText;

        private PrintersViewMode _printersViewMode;


        /**
         * 
         * Variables for Add Printer feature
         * 
         * */

        public string IpAddress
        {
            get;
            set;
        }

        public string Username
        {
            get;
            set;
        }

        public string Password
        {
            get;
            set;
        }

        public ObservableCollection<Printer> PrinterList
        {
            get
            {
                return this._printerList;
            }
            set
            {
                _printerList = value;

            }
        }

        public string PageTitleText
        {
            get;
            set;
        }
        public string AddPrinterPaneTitleText
        {
            get;
            set;
        }
        public string ScanPrintersPaneTitleText
        {
            get;
            set;
        }

        public PrintersViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            //_rightPageIndex = 0;
            //Messenger.Default.Register<DummyPageMessage>(this, (pageMessage) => OnPageImageLoaded(pageMessage));
            Initialize();
        }

        private void Initialize()
        {
            PageTitleText = "Printers";
            AddPrinterPaneTitleText = "Add Printer";
            ScanPrintersPaneTitleText = "Printer Search";

            IpAddress = "";
            Username = "";
            Password = "";
            _printersViewMode = PrintersViewMode.PrintersFullScreen;
            populateScreen();
        }

        private void populateScreen()
        {
            printerController.populatePrintersScreen();
            //btnTemp.Background = new SolidColorBrush(Color.FromArgb(255, 133, 65, 216));
            PrinterList = printerController.PrinterList;
        }

        

        public ICommand ToggleMainMenuPane
        {
            get
            {
                if (_toggleMainMenuPane == null)
                {
                    _toggleMainMenuPane = new RelayCommand(
                        () => ToggleMainMenuPaneExecute(),
                        () => true
                    );
                }
                return _toggleMainMenuPane;
            }
        }

        private void ToggleMainMenuPaneExecute()
        {
            switch (_printersViewMode)
            {
                case PrintersViewMode.MainMenuPaneVisible:
                    {
                        SetPrintersView(PrintersViewMode.PrintersFullScreen);
                        break;
                    }

                case PrintersViewMode.PrintersFullScreen:
                    {
                        SetPrintersView(PrintersViewMode.MainMenuPaneVisible);
                        break;
                    }

            }
        }

        public ICommand ToggleAddPrinterPane
        {
            get
            {
                if (_toggleAddPrinterPane == null)
                {
                    _toggleAddPrinterPane = new RelayCommand(
                        () => ToggleAddPrinterPaneExecute(),
                        () => true
                    );
                }
                return _toggleAddPrinterPane;
            }
        }

        private void ToggleAddPrinterPaneExecute()
        {
            switch (_printersViewMode)
            {
                case PrintersViewMode.MainMenuPaneVisible:
                    {
                        SetPrintersView(PrintersViewMode.PrintersFullScreen);
                        break;
                    }

                case PrintersViewMode.PrintersFullScreen:
                    {
                        SetPrintersView(PrintersViewMode.AddPrinterPaneVisible);
                        break;
                    }

                case PrintersViewMode.AddPrinterPaneVisible:
                    {
                        SetPrintersView(PrintersViewMode.PrintersFullScreen);

                        break;
                    }
                case PrintersViewMode.ScanPrintersPaneVisible:
                    {
                        SetPrintersView(PrintersViewMode.AddPrinterPaneVisible);
                        break;

                    }
            }
            }


        public ICommand ToggleScanPrintersPane
        {
            get
            {
                if (_toggleScanPrintersPane == null)
                {
                    _toggleScanPrintersPane = new RelayCommand(
                        () => ToggleScanPrintersPaneExecute(),
                        () => true
                    );
                }
                return _toggleScanPrintersPane;
            }
        }

        private void ToggleScanPrintersPaneExecute()
        {
            switch (_printersViewMode)
            {
                case PrintersViewMode.MainMenuPaneVisible:
                    {
                        SetPrintersView(PrintersViewMode.PrintersFullScreen);
                        break;
                    }

                case PrintersViewMode.PrintersFullScreen:
                    {
                        SetPrintersView(PrintersViewMode.ScanPrintersPaneVisible);
                        break;
                    }

                case PrintersViewMode.AddPrinterPaneVisible:
                    {
                        SetPrintersView(PrintersViewMode.ScanPrintersPaneVisible);

                        break;
                    }
                case PrintersViewMode.ScanPrintersPaneVisible:
                    {
                        SetPrintersView(PrintersViewMode.PrintersFullScreen);
                        break;

                    }
            }
        }



        private void SetPrintersView(PrintersViewMode printersViewMode)
        {
            Messenger.Default.Send<PrintersViewMode>(printersViewMode);
            //switch (printersViewMode)
            //{
            //    case PreviewViewMode.MainMenuPaneVisible:
            //    {
            //        DisablePreviewGestures();
            //        break;
            //    }

            //    case PreviewViewMode.PreviewViewFullScreen:
            //    {
            //        EnablePreviewGestures();
            //        break;
            //    }

            //    case PreviewViewMode.PrintSettingsPaneVisible:
            //    {
            //        EnablePreviewGestures();
            //        break;
            //    }                
            //}
            _printersViewMode = printersViewMode;
        }

        public ICommand AddPrinter
        {
            get
            {
                if (_addPrinter == null)
                {
                    _addPrinter = new RelayCommand(
                        () => AddPrinterExecute(),
                        () => true
                    );
                }
                return _addPrinter;
            }
        }

        private void AddPrinterExecute()
        {
            System.Diagnostics.Debug.WriteLine(IpAddress);

            //check if has data
            if (IpAddress.Equals("") || Username.Equals("") || Password.Equals(""))
            {
                //error please input data
                return;
            }

            //add to printer controller
            bool isSuccessful = printerController.addPrinter(IpAddress);
            if (!isSuccessful)
            {
                //display error message
                return;
            }
            //clear data
            IpAddress = "";
            Username = "";
            Password = "";
        }

    }

}
