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
        
        /// <summary>
        /// Printer Name, used to display name of printer searched.
        /// </summary>
        public string Name
        {
            get { return _name; }
            set
            {
                this._name = value;
                OnPropertyChanged("Name");
            }
        }

        /// <summary>
        /// Printer IP Address, displays the ip address of the printer searched.
        /// </summary>
        public string Ip_address
        {
            get { return _ip_address; }
            set
            {
                this._ip_address = value;
                OnPropertyChanged("Ip_address");
            }
        }

        /// <summary>
        /// Flag used to determine if the printer searched is already added.
        /// </summary>
        public bool IsInPrinterList
        {
            get { return _isInPrinterList; }
            set
            {
                this._isInPrinterList = value;

                OnPropertyChanged("IsInPrinterList");
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
    }
}
