using SmartDeviceApp.Converters;
using System;
using System.Collections.Generic;
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

namespace SmartDeviceApp.Controls
{
    public sealed partial class MessageProgressBarControl : UserControl
    {
        /// <summary>
        /// Constructor for the MessageProgressBarControl.
        /// </summary>
        /// <param name="content">Content of the control.</param>
        /// <param name="title">Title of the control.</param>
        public MessageProgressBarControl(string content, string title = default(string))
        {
            this.InitializeComponent();
            var conv = new ResourceStringToTextConverter();
            if (title == null)
            {
                TitleVisibility = Visibility.Collapsed;
                titleText.Height = 0;
                titleText.Margin = new Thickness(0);
            }
            else
            {
                TitleVisibility = Visibility.Visible;
                Title = conv.Convert(title, null, null, null).ToString();
            }
            Content = conv.Convert(content, null, null, null).ToString();
        }

        public static readonly DependencyProperty TitleProperty =
            DependencyProperty.Register("Title", typeof(string), typeof(MessageProgressBarControl), null);

        public static readonly DependencyProperty TitleVisibilityProperty =
            DependencyProperty.Register("TitleVisibility", typeof(Visibility), typeof(MessageProgressBarControl), null);

        public static readonly DependencyProperty ContentProperty =
           DependencyProperty.Register("Content", typeof(string), typeof(MessageProgressBarControl), null);

        public static readonly DependencyProperty ProgressValueProperty =
            DependencyProperty.Register("ProgressValue", typeof(double), typeof(MessageProgressBarControl), null);

        public static readonly DependencyProperty CancelCommandProperty =
           DependencyProperty.Register("CancelCommand", typeof(ICommand), typeof(MessageProgressBarControl), null);

        /// <summary>
        /// Title of the control.
        /// </summary>
        public string Title
        {
            get { return (string)GetValue(TitleProperty); }
            set { SetValue(TitleProperty, value); }
        }

        /// <summary>
        /// Visibility property for the Title.
        /// </summary>
        public Visibility TitleVisibility
        {
            get { return (Visibility)GetValue(TitleVisibilityProperty); }
            set { SetValue(TitleVisibilityProperty, value); }
        }

        /// <summary>
        /// Content of the control.
        /// </summary>
        public string Content
        {
            get { return (string)GetValue(ContentProperty); }
            set { SetValue(ContentProperty, value); }
        }

        /// <summary>
        /// Progress value of the progress bar.
        /// </summary>
        public double ProgressValue
        {
            get { return (double)GetValue(ProgressValueProperty); }
            set { SetValue(ProgressValueProperty, value); }
        }

        /// <summary>
        /// Cancels the current progress.
        /// </summary>
        public ICommand CancelCommand
        {
            get { return (ICommand)GetValue(CancelCommandProperty); }
            set { SetValue(CancelCommandProperty, value); }
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            overlay.Width = Window.Current.Bounds.Width;
            overlay.Height = Window.Current.Bounds.Height;                
        }
    }
}
