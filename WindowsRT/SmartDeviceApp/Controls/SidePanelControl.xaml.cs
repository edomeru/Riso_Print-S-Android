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

        public static readonly DependencyProperty Button1CommandProperty =
            DependencyProperty.Register("Button1Command", typeof(ICommand), typeof(SidePaneControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton1Command)));

        public static readonly DependencyProperty Button1ImageProperty =
            DependencyProperty.Register("Button1ImageSource", typeof(ImageSource), typeof(SidePaneControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetButton1ImageSource)));

        public string Title
        {
            get { return (string)GetValue(TitleProperty); }
            set { SetValue(TitleProperty, value); }
        }

        public ImageSource Button1ImageSource
        {
            get { return (ImageSource)GetValue(Button1ImageProperty); }
            set { SetValue(Button1ImageProperty, value); }
        }

        private static void SetTitle(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ((SidePaneControl)obj).sidePaneTitle.Text = (string)e.NewValue;
        }

        public ICommand Button1Command
        {
            get { return (ICommand)GetValue(Button1CommandProperty); }
            set { SetValue(Button1CommandProperty, value); }
        }

        private static void SetButton1Command(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((ICommand)e.NewValue != null)
            {
                ((SidePaneControl)obj).button1.Command = (ICommand)e.NewValue;
            }
        }

        private static void SetButton1ImageSource(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ImageBrush ib = new ImageBrush();
            ib.ImageSource = (ImageSource)e.NewValue;
            ((SidePaneControl)obj).button1.Background = ib;
        }
    }
}
