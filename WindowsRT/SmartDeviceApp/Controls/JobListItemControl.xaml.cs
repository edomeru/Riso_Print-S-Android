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

        private static void SetDeleteButtonVisibility(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is Visibility)) return;
            var control = (JobListItemControl)obj;
            var deleteButton = control.deleteButton;
            var valueText = control.ValueText;
            if ((Visibility)e.NewValue == Visibility.Collapsed)
            {
                deleteButton.Visibility = Visibility.Collapsed;
                control.SetValue(ValueVisibilityProperty, Visibility.Visible);
            }
            else
            {
                deleteButton.Visibility = Visibility.Visible;
                control.SetValue(ValueVisibilityProperty, Visibility.Collapsed);
            }            
        }
    }
}
