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
using Windows.UI.Xaml.Markup;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

namespace SmartDeviceApp.Controls
{
    public sealed partial class SidePaneControl : UserControl
    {
        public SidePaneControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty TitleProperty =
            DependencyProperty.Register("Title", typeof(string), typeof(SidePaneControl),
            new PropertyMetadata(String.Empty, new PropertyChangedCallback(SetTitle)));

        public static readonly DependencyProperty Button2CommandProperty =
            DependencyProperty.Register("Button2Command", typeof(ICommand), typeof(SidePaneControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton2Command)));

        public string Title
        {
            get { return (string)GetValue(TitleProperty); }
            set { SetValue(TitleProperty, value); }
        }

        private static void SetTitle(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ((SidePaneControl)obj).sidePaneTitle.Text = (string)e.NewValue;
        }

        public ICommand Button2Command
        {
            get { return (ICommand)GetValue(Button2CommandProperty); }
            set { SetValue(Button2CommandProperty, value); }
        }

        private static void SetButton2Command(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((ICommand)e.NewValue != null)
            {
                ((SidePaneControl)obj).button2.Command = (ICommand)e.NewValue;
            }
        }
    }
}
