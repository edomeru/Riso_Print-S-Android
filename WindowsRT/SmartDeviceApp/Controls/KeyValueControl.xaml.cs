using SmartDeviceApp.Common.Enum;
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
    public sealed partial class KeyValueControl : UserControl
    {
        public KeyValueControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty LeftButtonVisibilityProperty =
            DependencyProperty.Register("LeftButtonVisibility", typeof(Visibility), typeof(KeyValueControl), null);

        public static readonly DependencyProperty RightButtonVisibilityProperty =
            DependencyProperty.Register("RightButtonVisibility", typeof(Visibility), typeof(KeyValueControl), null);

        public static readonly DependencyProperty IconVisibilityProperty =
            DependencyProperty.Register("IconVisibility", typeof(Visibility), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueContentProperty = 
            DependencyProperty.Register("ValueContent", typeof(object), typeof(KeyValueControl), null);

        public static readonly DependencyProperty IconImageProperty =
            DependencyProperty.Register("IconImage", typeof(ImageSource), typeof(KeyValueControl), null);

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(KeyValueControl), null);

        public string LeftButtonVisibility
        {
            get { return (string)GetValue(LeftButtonVisibilityProperty); }
            set { SetValue(LeftButtonVisibilityProperty, value); }
        }

        public string RightButtonVisibility
        {
            get { return (string)GetValue(RightButtonVisibilityProperty); }
            set { SetValue(RightButtonVisibilityProperty, value); }
        }

        public string IconVisibility
        {
            get { return (string)GetValue(IconVisibilityProperty); }
            set { SetValue(IconVisibilityProperty, value); }
        }

        public object ValueContent
        {
            get { return (object)GetValue(ValueContentProperty); }
            set { SetValue(ValueContentProperty, value); }
        }

        public ImageSource IconImage
        {
            get { return (ImageSource)GetValue(IconImageProperty); }
            set { SetValue(IconImageProperty, value); }
        }

        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }
    }
}
