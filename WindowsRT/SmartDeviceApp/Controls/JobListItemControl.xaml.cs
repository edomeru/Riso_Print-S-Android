using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Windows.Input;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using SmartDeviceApp.Models;

namespace SmartDeviceApp.Controls
{
    public sealed partial class JobListItemControl : KeyValueControl
    {
        public JobListItemControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty DeleteJobCommandProperty =
            DependencyProperty.Register("DeleteJobCommand", typeof(ICommand), typeof(JobListItemControl), null);

        public static readonly DependencyProperty DeleteJobCommandParameterProperty =
           DependencyProperty.Register("DeleteJobCommandParameter", typeof(PrintJob), typeof(JobListItemControl), null);
        
        public static readonly DependencyProperty DeleteButtonVisibilityProperty =
            DependencyProperty.Register("DeleteButtonVisibility", typeof(Visibility), typeof(JobListItemControl),
            new PropertyMetadata(Visibility.Collapsed, SetDeleteButtonVisibility));

        public static readonly DependencyProperty DeleteButtonStateProperty =
            DependencyProperty.Register("DeleteButtonState", typeof(Visibility), typeof(JobListItemControl),
            new PropertyMetadata(Visibility.Collapsed, SetVisibility));
        private bool _isVisibilitySet;
        private bool _isLoaded;
        private static void SetVisibility(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            
            if (e.NewValue == null || !(e.NewValue is Visibility)) return;
            var control = (JobListItemControl)obj;
            control._isVisibilitySet = false;
            if (control._isLoaded)
            { 
                control.DeleteButtonVisibility = control.DeleteButtonState;
                if (control.DeleteButtonVisibility == Visibility.Visible)
                {
                    control.VisualState = "Pressed";
                }
                else
                {
                    control.VisualState = "Normal";
                }
                control._isVisibilitySet = true;
            }
        }

        public ICommand DeleteJobCommand
        {
            get { return (ICommand)GetValue(DeleteJobCommandProperty); }
            set { SetValue(DeleteJobCommandProperty, value); }
        }

        public PrintJob DeleteJobCommandParameter
        {
            get { return (PrintJob)GetValue(DeleteJobCommandParameterProperty); }
            set { SetValue(DeleteJobCommandParameterProperty, value); }
        }

        public Visibility DeleteButtonVisibility
        {
            get { return (Visibility)GetValue(DeleteButtonVisibilityProperty); }
            set { SetValue(DeleteButtonVisibilityProperty, value); }
        }

        public Visibility DeleteButtonState
        {
            get { return (Visibility)GetValue(DeleteButtonStateProperty); }
            set { 
                SetValue(DeleteButtonStateProperty, (Visibility)value); 
            }
        }

        private static void SetDeleteButtonVisibility(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is Visibility)) return;
            var control = (JobListItemControl)obj;
            var deleteButton = control.deleteButton;
            var valueText = control.ValueText;
            if ((Visibility)e.NewValue == Visibility.Collapsed)
            {
                VisualStateManager.GoToState(deleteButton, "Collapsed", true);
                control.SetValue(ValueVisibilityProperty, Visibility.Visible);                
            }
            else
            {
                control.deleteButton.Visibility = Visibility.Visible;
                control.SetValue(ValueVisibilityProperty, Visibility.Collapsed);
                VisualStateManager.GoToState(deleteButton, "Visible", true);                
            }            
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            deleteButton.Visibility = Visibility.Visible;
            var control = (JobListItemControl)sender;
            control._isLoaded = true;
            if (!control._isVisibilitySet)
            {
                //Set visibility
                if (control.DataContext != null)
                    ((PrintJob)(control.DataContext)).DeleteButtonVisibility = ((PrintJob)(control.DataContext)).DeleteButtonVisibility;
            }
        }
    }
}
