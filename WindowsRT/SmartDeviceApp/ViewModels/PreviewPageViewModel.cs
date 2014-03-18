using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Models;

namespace SmartDeviceApp.ViewModels
{
    public class PreviewPageViewModel : ViewModelBase
    {
        private bool _isLoaded;

        public PreviewPageViewModel()
        {

        }

        public bool IsLoaded
        {
            get { return _isLoaded; }
            set 
            {
                _isLoaded = value;
                RaisePropertyChanged("IsLoaded"); 
            }
        }
    }
}
