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
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;

namespace SmartDeviceApp.ViewModels
{
    public class AppViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private ICommand _toggleMainMenuPane;
        private ICommand _showPreviewViewFullScreen;
        private ICommand _toggleRightPane;
        private AppViewMode _appViewMode;
        private RightPaneMode _rightPaneMode;

        private DataTemplate _currentPageTemplate;
        

        public AppViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;
            Initialize();
        }

        // TODO: Remove dummy variables and unneeded initialization
        private void Initialize()
        {
            AppViewMode = AppViewMode.PrintPreviewPageFullScreen;
            /*
            //DocumentTitleText = DummyProvider.Instance.PDF_FILENAME;
            DocumentTitleText = DocumentController.Instance.FileName;

            // PageTotal = DummyProvider.Instance.TOTAL_PAGES;
            PageTotal = PrintPreviewController.Instance.PageTotal;
            // PageViewMode = DummyProvider.Instance.PAGE_VIEW_MODE;
            PageViewMode = PrintPreviewController.Instance.PageViewMode;
            GoToPage(0); // Go to first page
             * */
        }

        public DataTemplate CurrentPageTemplate
        {
            get { return _currentPageTemplate; }
            set
            {
                if (_currentPageTemplate != value)
                {
                    _currentPageTemplate = value;
                    RaisePropertyChanged("CurrentPageTemplate");
                }
            }
        }

        #region PANE VISIBILITY

        
        public AppViewMode AppViewMode
        {
            get { return _appViewMode; }
            set
            {
                if (_appViewMode != value)
                {
                    _appViewMode = value;
                    RaisePropertyChanged("AppViewMode");
                }
            }
        }

        public RightPaneMode RightPaneMode
        {
            get { return _rightPaneMode; }
            set
            {
                if (_rightPaneMode != value)
                {
                    _rightPaneMode = value;
                    RaisePropertyChanged("RightPaneMode");
                }
            }
        }

        #endregion
    }
}
