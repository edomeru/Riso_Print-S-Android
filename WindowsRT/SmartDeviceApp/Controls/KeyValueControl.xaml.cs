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
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.Xaml.Navigation;

namespace SmartDeviceApp.Controls
{
    public partial class KeyValueControl : UserControl
    {
        public KeyValueControl()
        {
            this.InitializeComponent();
            RightImage = new BitmapImage(new Uri("ms-appx:///Resources/Images/img_btn_submenu.png"));
        }

        public static readonly DependencyProperty LeftButtonVisibilityProperty =
            DependencyProperty.Register("LeftButtonVisibility", typeof(Visibility), typeof(KeyValueControl), null);

        public static readonly DependencyProperty RightButtonVisibilityProperty =
            DependencyProperty.Register("RightButtonVisibility", typeof(Visibility), typeof(KeyValueControl), null);

        public static readonly DependencyProperty IconVisibilityProperty =
            DependencyProperty.Register("IconVisibility", typeof(Visibility), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueVisibilityProperty =
            DependencyProperty.Register("ValueVisibility", typeof(Visibility), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueContentProperty = 
            DependencyProperty.Register("ValueContent", typeof(object), typeof(KeyValueControl), null);

        public static readonly DependencyProperty IconImageProperty =
            DependencyProperty.Register("IconImage", typeof(ImageSource), typeof(KeyValueControl), null);

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty SeparatorVisibilityProperty =
            DependencyProperty.Register("SeparatorVisibility", typeof(Visibility), typeof(KeyValueControl), null);
        
        public static readonly DependencyProperty SetFocusProperty =
            DependencyProperty.Register("SetFocus", typeof(bool), typeof(KeyValueControl), 
            new PropertyMetadata(false, FocusChanged));

        public static readonly DependencyProperty RightImageProperty =
            DependencyProperty.Register("RightImage", typeof(ImageSource), typeof(KeyValueControl), null);


        public static readonly DependencyProperty BackgroundColorProperty =
            DependencyProperty.Register("BackgroundColor", typeof(SolidColorBrush), typeof(KeyValueControl), null);

        //public int Index
        //{
        //    get { return (int)GetValue(IndexProperty); }
        //    set { SetValue(IndexProperty, value); }
        //}

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

        public string ValueVisibility
        {
            get { return (string)GetValue(ValueVisibilityProperty); }
            set { SetValue(ValueVisibilityProperty, value); }
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

        public string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        public string SeparatorVisibility
        {
            get { return (string)GetValue(SeparatorVisibilityProperty); }
            set { SetValue(SeparatorVisibilityProperty, value); }
        }
        public bool SetFocus
        {
            get { return (bool)GetValue(SetFocusProperty); }
            set { SetValue(SetFocusProperty, value); }
        }

        private static void FocusChanged(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            var targetElement = obj.GetValue(ValueContentProperty) as Control;
            if (targetElement == null || e.NewValue == null || (!((bool)e.NewValue)))
            {
                return;
            }
            targetElement.Focus(FocusState.Programmatic);
            obj.SetValue(SetFocusProperty, false);
        }

        private void ContentPresenter_GotFocus(object sender, RoutedEventArgs e)
        {

        }

        public ImageSource RightImage
        {
            get { return (ImageSource)GetValue(RightImageProperty); }
            set { SetValue(RightImageProperty, value); }
        }

        public SolidColorBrush BackgroundColor
        {
            get { return (SolidColorBrush)GetValue(BackgroundColorProperty); }
            set { SetValue(BackgroundColorProperty, value); }
        }
    }
}
