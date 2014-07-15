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
using GalaSoft.MvvmLight.Messaging;
using Windows.UI.Xaml.Controls;

namespace SmartDeviceApp.ViewModels
{
    public class LegalViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        /// <summary>
        /// LegalViewModel class constructor
        /// </summary>
        /// <param name="dataService">data service</param>
        /// <param name="navigationService">navigation service</param>
        public LegalViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Messenger.Default.Register<ViewMode>(this, (viewMode) => EnableMode(viewMode));
        }

        /// <summary>
        /// Enables/disables gestures in Legal Screen
        /// </summary>
        /// <param name="viewMode"></param>
        private void EnableMode(ViewMode viewMode)
        {
            if (viewMode == ViewMode.FullScreen)
            {
                if (LegalGestureGrid != null)
                {
                    LegalGestureGrid.Visibility = Windows.UI.Xaml.Visibility.Collapsed;
                }
            }
            else
            {
                if (LegalGestureGrid != null)
                {
                    LegalGestureGrid.Visibility = Windows.UI.Xaml.Visibility.Visible;
                }
            }
        }

        /// <summary>
        /// Grid control for enabling/disabling gestures on Home Screen
        /// </summary>
        public Grid LegalGestureGrid
        {
            get;
            set;
        }

    }
}
