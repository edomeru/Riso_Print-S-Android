using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Windows.Input;
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
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Converters;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;
using System.Threading.Tasks;

namespace SmartDeviceApp.Views
{
    public sealed partial class JobsPage : PageBase
    {
        private JobGestureController _gestureController;
        private bool _isJobsGridLoaded;
        private bool _isJobGesturesGridLoaded;
        private bool _isJobsScrollViewerLoaded;
        /// <summary>
        /// Constructor. Initializes UI components and gesture controller.
        /// </summary>
        /// 

        public JobsPage()
        {
            this.InitializeComponent();
            _gestureController = new JobGestureController();
            ViewModel.GestureController = _gestureController;

            if (!ViewModel.IsPrintJobsListEmpty) ViewModel.IsProgressRingActive = true;
        }




        /// <summary>
        /// Gets the data context of this xaml
        /// </summary>
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

        /// <summary>
        /// Command property to be executed when Delete All button is tapped
        /// </summary>
        public ICommand DeleteAllJobsCommand
        {
            get { return (ICommand)GetValue(DeleteAllJobsCommandProperty); }
            set { SetValue(DeleteAllJobsCommandProperty, value); }
        }

        /// <summary>
        /// Command property to be executed when Delete Job button is tapped
        /// </summary>
        public ICommand DeleteJobCommand
        {
            get { return (ICommand)GetValue(DeleteJobCommandProperty); }
            set { SetValue(DeleteJobCommandProperty, value); }
        }

        protected override void OnNavigatedFrom(NavigationEventArgs e)
        {
            ViewModel.OnNavigatedFrom();
            base.OnNavigatedFrom(e);
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

        // Workaround to check if jobs list has finished rendering in UI.
        // Note that loaded event fires even if UI is not finised rendering.
        // This event is triggered for all job groups per printer,
        // whenever the size is changed from 0 to the correct size.
        // Progress ring is set to inactive if the last job group
        // size is initialized.
        private void OnGroupListLoaded(object sender, RoutedEventArgs e)
        {
            // Check if last group
            if (((GroupListControl)sender).SubText == ViewModel.PrintJobsList[ViewModel.PrintJobsList.Count - 1].IpAddress)
            {
                ViewModel.IsProgressRingActive = false;
            }
        }
        private void UILoaded(object sender, RoutedEventArgs e)
        {
       
            _gestureController.collapseAllGroups(null);
            ViewModel.setUpCollapsed();
            
        }

    }
}
