using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Models;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace SmartDeviceApp.ViewModels
{
    public class AddPrinterViewModel : ViewModelBase, INotifyPropertyChanged
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private string _ipAddress;
        private string _username;
        private string _password;

        private ICommand _addPrinter;

        public event SmartDeviceApp.Controllers.PrinterController.AddPrinterHandler AddPrinterHandler;

        public AddPrinterViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            IpAddress = "";
            Username = "";
            Password = "";
        }

        public event PropertyChangedEventHandler PropertyChanged;
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
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
                //display error message TODO
                return;
            }

            //add to printer controller
            AddPrinterHandler(IpAddress);
            
        }

        public void handleAddIsSuccessful(bool isSuccessful)
        {
            string caption = "";
            string content = "";

            if (isSuccessful)
            {
                content = "The new printer was added successfully.";
                //clear data
                IpAddress = "";
                Username = "";
                Password = "";
            }
            else
            {
                content = "The new printer is not online but was added successfully with default printer settings.";
            }
            caption = "Add Printer Info";

            DisplayMessage(caption, content);
        }

        public void DisplayMessage(string caption, string content)
        {
            MessageAlert ma = new MessageAlert();
            ma.Caption = caption;
            ma.Content = content;
            Messenger.Default.Send<MessageAlert>(ma);
        }
    }
}
