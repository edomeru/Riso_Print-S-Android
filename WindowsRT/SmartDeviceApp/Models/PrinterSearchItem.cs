using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Models
{
    public class PrinterSearchItem : INotifyPropertyChanged
    {
        private string _name;
        private string _ip_address;
        private bool _isInPrinterList;
        
        public string Name
        {
            get { return _name; }
            set
            {
                this._name = value;
                OnPropertyChanged("Name");
            }
        }

        public string Ip_address
        {
            get { return _ip_address; }
            set
            {
                this._ip_address = value;
                OnPropertyChanged("Ip_address");
            }
        }

        public bool IsInPrinterList
        {
            get { return _isInPrinterList; }
            set
            {
                this._isInPrinterList = value;

                OnPropertyChanged("IsInPrinterList");
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }
    }
}
