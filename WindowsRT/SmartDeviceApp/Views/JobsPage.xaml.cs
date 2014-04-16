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

namespace SmartDeviceApp.Views
{
    public sealed partial class JobsPage : PageBase
    {
        private JobGestureController _gestureController;

        public JobsPage()
        {
            this.InitializeComponent();
            _gestureController = new JobGestureController();
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
            _gestureController.TargetControl = (Grid)sender;
        }

        private void OnJobGesturesGridLoaded(object sender, RoutedEventArgs e)
        {
            _gestureController.Control = (Grid)sender;
        }

        private void OnJobsScrollViewerLoaded(object sender, RoutedEventArgs e)
        {
            _gestureController.ControlReference = (ScrollViewer)sender;
        }
    }
}
