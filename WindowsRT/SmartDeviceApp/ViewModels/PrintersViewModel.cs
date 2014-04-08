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

namespace SmartDeviceApp.ViewModels
{
    public class PrintersViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _toggleMainMenuPane;
        private AppViewModel _appViewModel;

        public PrintersViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            _appViewModel = new ViewModelLocator().AppViewModel;
        }

        #region PANE VISIBILITY

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
            AppViewMode appViewMode = AppViewMode.PrintersPageFullScreen;
            switch (_appViewModel.AppViewMode)
            {
                case AppViewMode.MainMenuPaneVisible:
                    {
                        appViewMode = AppViewMode.PrintPreviewPageFullScreen;
                        break;
                    }

                case AppViewMode.PrintersPageFullScreen:
                    {
                        appViewMode = AppViewMode.MainMenuPaneVisible;
                        break;
                    }
            }
            _appViewModel.AppViewMode = appViewMode;
        }

        #endregion
    }
}
