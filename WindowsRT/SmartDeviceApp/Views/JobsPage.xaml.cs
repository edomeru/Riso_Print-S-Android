using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using System.Windows.Input;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceApp.Views
{
    public sealed partial class JobsPage : PageBase
    {
        private JobGestureController _gestureController;
        private bool _isJobsGridLoaded;
        private bool _isJobGesturesGridLoaded;
        private bool _isJobsScrollViewerLoaded;

        public JobsPage()
        {
            this.InitializeComponent();
            _gestureController = new JobGestureController();
            ViewModel.GestureController = _gestureController;
        }

        public JobsViewModel ViewModel
        {
            get
            {
                return (JobsViewModel)DataContext;
            }
        }

        public static readonly DependencyProperty DeleteAllJobsCommandProperty =
            DependencyProperty.Register("DeleteAllJobsCommand", typeof(ICommand), typeof(JobsPage), null);

        public static readonly DependencyProperty DeleteJobCommandProperty =
            DependencyProperty.Register("DeleteJobCommand", typeof(ICommand), typeof(JobsPage), null);

        public ICommand DeleteAllJobsCommand
        {
            get { return (ICommand)GetValue(DeleteAllJobsCommandProperty); }
            set { SetValue(DeleteAllJobsCommandProperty, value); }
        }

        public ICommand DeleteJobCommand
        {
            get { return (ICommand)GetValue(DeleteJobCommandProperty); }
            set { SetValue(DeleteJobCommandProperty, value); }
        }

        private void OnJobsGridLoaded(object sender, RoutedEventArgs e)
        {
            if (!_isJobsGridLoaded)
            {
                _gestureController.TargetControl = (Grid)sender;
                _isJobsGridLoaded = true;
            }
        }

        private void OnJobGesturesGridLoaded(object sender, RoutedEventArgs e)
        {
            if (!_isJobGesturesGridLoaded)
            {
                _gestureController.Control = (Grid)sender;
                _isJobGesturesGridLoaded = true;
            }
        }

        private void OnJobsScrollViewerLoaded(object sender, RoutedEventArgs e)
        {
            if (!_isJobsScrollViewerLoaded)
            {
                _gestureController.ControlReference = (ScrollViewer)sender;
                _isJobsScrollViewerLoaded = true;
            }
        }
    }
}
