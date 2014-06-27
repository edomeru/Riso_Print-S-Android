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
        private ImageSource _imageSource;

        private readonly string _AddImageNormal = "ms-appx:///Resources/Images/img_btn_add_printer_normal.scale-100.png";
        private readonly string _AddPrinterOkImagePressed = "ms-appx:///Resources/Images/img_btn_add_printer_search_ok.scale-100.png";


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

                setImageSource();
                OnPropertyChanged("IsInPrinterList");
            }
        }

        public ImageSource ImageSource
        {
            get { return _imageSource; }
            set
            {
                this._imageSource = value;
                OnPropertyChanged("ImageSource");
            }
        }

        public event PropertyChangedEventHandler PropertyChanged;
        public void OnPropertyChanged(String propertyName)
        {
            if (PropertyChanged != null)
                PropertyChanged(this, new PropertyChangedEventArgs(propertyName));
        }

        private async void setImageSource()
        {
            if (IsInPrinterList)
            {
                ImageSource = await convertImageSource(_AddPrinterOkImagePressed);
            } 
            else
            {
                ImageSource = await convertImageSource(_AddImageNormal);
            }
        }

        private async Task<ImageSource> convertImageSource(string src)
        {
            ImageSource imgSrc = null;
            await Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(
                Windows.UI.Core.CoreDispatcherPriority.Normal, async () =>
            {
                BitmapImage image = new BitmapImage(new Uri(src));
                imgSrc = image;

               
            });
            return imgSrc;
        }
    }
}
