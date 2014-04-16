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

namespace SmartDeviceApp.Controls
{
    public sealed partial class SidePaneTitleControl : UserControl
    {
        public SidePaneTitleControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty TitleProperty =
            DependencyProperty.Register("Title", typeof(string), typeof(SidePaneTitleControl), null);

        public static readonly DependencyProperty ButtonImageProperty =
            DependencyProperty.Register("ButtonImage", typeof(ImageSource), typeof(SidePaneTitleControl), null);

        public static readonly DependencyProperty ButtonVisibilityProperty =
            DependencyProperty.Register("ButtonVisibility", typeof(Visibility), typeof(SidePaneTitleControl), null);

        public string Title
        {
            get { return (string)GetValue(TitleProperty); }
            set { SetValue(TitleProperty, value); }
        }

        public ImageSource ButtonImage
        {
            get { return (ImageSource)GetValue(ButtonImageProperty); }
            set { SetValue(ButtonImageProperty, value); }
        }

        public Visibility ButtonVisibility
        {
            get { return (Visibility)GetValue(ButtonVisibilityProperty); }
            set { SetValue(ButtonVisibilityProperty, value); }
        }
    }
}
