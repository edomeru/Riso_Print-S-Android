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
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;
using System.ComponentModel;
using System.Diagnostics;
using GalaSoft.MvvmLight.Command;

namespace SmartDeviceApp.ViewModels
{
    public class PrintersViewModel : ViewModelBase, INotifyPropertyChanged
    {
        //contains data that binds with PrintersPage
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _toggleMainMenuPane;
        private ICommand _toggleAddPrinterPane;
        private ICommand _toggleScanPrintersPane;

        private ICommand _addPrinter;

        private ICommand _printerSearchItemSelected;

        private ObservableCollection<Printer> _printerList;
        private ObservableCollection<PrinterSearchItem> _printerSearchList;

        private PrinterController printerController = new PrinterController();

        private PrintersViewMode _printersViewMode;

        /*
         * Titles for Center Pane and side Panes
         */
        private string _pageTitleText;
        private string _addPrinterPaneTitleText;
        private string _searchPrintersPaneTitleText;


        /**
         * 
         * Variables for Add Printer feature
         * 
         * */
        private string _ipAddress;
        private string _username;
        private string _password;

        /**
         * 
         * Variables for icons
         * 
         * */
        private ImageSource _mainMenuButtonImage;
        private ImageSource _addButtonImage;
        private ImageSource _searchButtonImage;
        private ImageSource _doneButtonImage;

        /**
         * 
         * Variables for paths of icons to be used
         * 
         * */
        
        
        private readonly string _AddImageNormal = "ms-appx:///Resources/Images/img_btn_add_printer_normal.png";
        private readonly string _AddImagePressed = "ms-appx:///Resources/Images/img_btn_add_printer_pressed.png";
        private readonly string _SearchImageNormal = "ms-appx:///Resources/Images/img_btn_search_printer_normal.png";
        private readonly string _SearchImagePressed = "ms-appx:///Resources/Images/img_btn_search_printer_pressed.png";
        private readonly string _AddPrinterOkImageNormal = "ms-appx:///Resources/Images/img_btn_add_printer_ok_normal.png";
        private readonly string _MainMenuImageNormal = "ms-appx:///Resources/Images/img_btn_main_menu_normal.png";
        private readonly string _MainMenuImagePressed = "ms-appx:///Resources/Images/img_btn_main_menu_pressed.png";

        

        /**
         * 
         * Public Getters and Setters
         * 
         * */

        public ObservableCollection<Printer> PrinterList
        {
            get{ return this._printerList; }
            set
            {
                _printerList = value;
                OnPropertyChanged("PrinterList");
            }
        }

        public ObservableCollection<PrinterSearchItem> PrinterSearchList
        {
            get{ return this._printerSearchList; }
            set
            {
                _printerSearchList = value;
                OnPropertyChanged("PrinterSearchList");
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

        
        public string IpAddress
        {
            get { return _ipAddress; }
            set 
            { 
                this._ipAddress = value;
                OnPropertyChanged("IpAddress");
            }
        }

        public string Username
        {
            get { return _username; }
            set
            {
                this._username = value;
                OnPropertyChanged("Username");
            }
        }

        public string Password
        {
            get { return _password; }
            set
            {
                this._password = value;
                OnPropertyChanged("Password");
            }
        }


        public ImageSource MainMenuButtonImage
        {
            get { return _mainMenuButtonImage; }
            set
            {
                this._mainMenuButtonImage = value;
                OnPropertyChanged("MainMenuButtonImage");
            }
        }

        
        public ImageSource AddButtonImage
        {
            get { return _addButtonImage; }
            set
            {
                this._addButtonImage = value;
                OnPropertyChanged("AddButtonImage");
            }
        }
        
        public ImageSource SearchButtonImage
        {
            get { return _searchButtonImage; }
            set
            {
                this._searchButtonImage = value;
                OnPropertyChanged("SearchButtonImage");
            }
        }

        
        public ImageSource DoneButtonImage
        {
            get { return _doneButtonImage; }
            set
            {
                this._doneButtonImage = value;
                OnPropertyChanged("DoneButtonImage");
            }
        }

        public PrinterSearchItem SelectedItem
        {
            get;
            set;
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
            PrinterSearchList = printerController.PrinterSearchList;


            setMainMenuButtonImage(_MainMenuImageNormal);
            setAddButtonImage(_AddImageNormal);
            setSearchButtonImage(_SearchImageNormal);
            setDoneButtonImage(_AddPrinterOkImageNormal);
            populateScreen();
        }

        private void populateScreen()
        {
            printerController.populatePrintersScreen();
            PrinterList = printerController.PrinterList;
        }


        /**
         * 
         * Commands for Icons in the Center Pane
         * 
         * */
        

        public ICommand ToggleMainMenuPane
        {
            get
            {
                if (_toggleMainMenuPane == null)
                {
                    _toggleMainMenuPane = new SmartDeviceApp.Common.RelayCommand(
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
                        setMainMenuButtonImage(_MainMenuImageNormal);
                        SetPrintersView(PrintersViewMode.PrintersFullScreen);
                        break;
                    }

                case PrintersViewMode.PrintersFullScreen:
                    {
                        setMainMenuButtonImage(_MainMenuImagePressed);
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
                    _toggleAddPrinterPane = new SmartDeviceApp.Common.RelayCommand(
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
                        setAddButtonImage(_AddImagePressed);
                        break;
                    }

                case PrintersViewMode.AddPrinterPaneVisible:
                    {
                        setAddButtonImage(_AddImageNormal);
                        setSearchButtonImage(_SearchImageNormal);
                        SetPrintersView(PrintersViewMode.PrintersFullScreen);

                        break;
                    }
                case PrintersViewMode.ScanPrintersPaneVisible:
                    {
                        setAddButtonImage(_AddImagePressed);
                        setSearchButtonImage(_SearchImageNormal);
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
                    _toggleScanPrintersPane = new SmartDeviceApp.Common.RelayCommand(
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
                        setAddButtonImage(_AddImageNormal);
                        setSearchButtonImage(_SearchImagePressed);
                        SetPrintersView(PrintersViewMode.ScanPrintersPaneVisible);
                        printerController.scanPrinters();
                        break;
                    }

                case PrintersViewMode.AddPrinterPaneVisible:
                    {
                        setAddButtonImage(_AddImageNormal);
                        setSearchButtonImage(_SearchImagePressed);
                        SetPrintersView(PrintersViewMode.ScanPrintersPaneVisible);
                        printerController.scanPrinters();
                        break;
                    }
                case PrintersViewMode.ScanPrintersPaneVisible:
                    {
                        setAddButtonImage(_AddImageNormal);
                        setSearchButtonImage(_SearchImageNormal);
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



        /**
         * 
         * Responds when the "Check" button in the Add Printer Pane is tapped
         * 
         * */

        public ICommand AddPrinter
        {
            get
            {
                if (_addPrinter == null)
                {
                    _addPrinter = new SmartDeviceApp.Common.RelayCommand(
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
                //display error message TODO

                return;
            }

            //display
            MessageAlert ma = new MessageAlert();
            ma.Content = "The new printer was added successfully.";
            ma.Caption = "Add Printer Info";

            Messenger.Default.Send<MessageAlert>(ma);


            //clear data
            IpAddress = "";
            Username = "";
            Password = "";
        }

        public ICommand PrinterSearchItemSelected
        {
            get
            {
                if (_printerSearchItemSelected == null)
                {
                    _printerSearchItemSelected = new RelayCommand<PrinterSearchItem>(
                        (item) => PrinterSearchItemSelectedExecute(item),
                        (item) => true
                    );
                }
                return _printerSearchItemSelected;
            }
        }


        private void PrinterSearchItemSelectedExecute(PrinterSearchItem item)
        {
            //Check if already added
            if (!item.IsInPrinterList)
            {
                //add to printer
                bool isSuccessful = printerController.addPrinter(item.Ip_address);

                if (!isSuccessful)
                {
                    //display error message TODO

                    return;
                }

                //display
                MessageAlert ma = new MessageAlert();
                ma.Content = "The new printer was added successfully.";
                ma.Caption = "Add Printer Info";

                Messenger.Default.Send<MessageAlert>(ma);

                item.IsInPrinterList = true;
                
            }
        }



        /**
         * 
         * Setters for icons
         * 
         * */

        private void setMainMenuButtonImage(string strUri)
        {
            MainMenuButtonImage = setImageSource(strUri);
        }

        private void setAddButtonImage(string strUri)
        {
            AddButtonImage = setImageSource(strUri);
        }

        private void setSearchButtonImage(string strUri)
        {
            SearchButtonImage = setImageSource(strUri);
        }

        private void setDoneButtonImage(string strUri)
        {
            DoneButtonImage = setImageSource(strUri);
        }

        private ImageSource setImageSource(string strUri)
        {
            BitmapImage image = new BitmapImage(new Uri(strUri));
            ImageSource imgSrc = image;
            return imgSrc;
        }

    }

}
