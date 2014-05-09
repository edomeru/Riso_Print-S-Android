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

        public string Title
        {
            get { return (string)GetValue(TitleProperty); }
            set { SetValue(TitleProperty, value); }
        }

        public Visibility TitleVisibility
        {
            get { return (Visibility)GetValue(TitleVisibilityProperty); }
            set { SetValue(TitleVisibilityProperty, value); }
        }

        public string Content
        {
            get { return (string)GetValue(ContentProperty); }
            set { SetValue(ContentProperty, value); }
        }

        public double ProgressValue
        {
            get { return (double)GetValue(ProgressValueProperty); }
            set { SetValue(ProgressValueProperty, value); }
        }

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
