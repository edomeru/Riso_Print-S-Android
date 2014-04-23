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
using SmartDeviceApp.Common.Enum;
using GalaSoft.MvvmLight.Command;

namespace SmartDeviceApp.Controls
{
    public partial class KeyValueControl : UserControl
    {
        public KeyValueControl()
        {
            this.InitializeComponent();
        }
		
        public static readonly DependencyProperty CommandProperty =
            DependencyProperty.Register("Command", typeof(ICommand), typeof(KeyValueControl), null);
        
        public static readonly DependencyProperty CommandParameterProperty =
            DependencyProperty.Register("CommandParameter", typeof(object), typeof(KeyValueControl), null);
        
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
			
		public static readonly DependencyProperty RightImageProperty =
            DependencyProperty.Register("RightImage", typeof(ImageSource), typeof(KeyValueControl), null);

        public static readonly DependencyProperty RightPressedImageProperty =
            DependencyProperty.Register("RightPressedImage", typeof(ImageSource), typeof(KeyValueControl), null);

        public static readonly DependencyProperty RightDisabledImageProperty =
            DependencyProperty.Register("RightDisabledImage", typeof(ImageSource), typeof(KeyValueControl), null);

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty SeparatorVisibilityProperty =
            DependencyProperty.Register("SeparatorVisibility", typeof(Visibility), typeof(KeyValueControl), null);

        public static new readonly DependencyProperty IsEnabledProperty =
            DependencyProperty.Register("IsEnabled", typeof(bool), typeof(KeyValueControl), new PropertyMetadata(true, SetIsEnabled));

        public ICommand Command
        {
            get { return (ICommand)GetValue(CommandProperty); }
            set { SetValue(CommandProperty, value); }
        }

        public object CommandParameter
        {
            get { return (object)GetValue(CommandParameterProperty); }
            set { SetValue(CommandParameterProperty, value); }
        }

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
		
		public ImageSource RightImage
        {
            get { return (ImageSource)GetValue(RightImageProperty); }
            set { SetValue(RightImageProperty, value); }
        }

        public ImageSource RightPressedImage
        {
            get { return (ImageSource)GetValue(RightPressedImageProperty); }
            set { SetValue(RightPressedImageProperty, value); }
        }

        public ImageSource RightDisabledImage
        {
            get { return (ImageSource)GetValue(RightDisabledImageProperty); }
            set { SetValue(RightDisabledImageProperty, value); }
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

        public new bool IsEnabled
        {
            get { return (bool)GetValue(IsEnabledProperty); }
            set { SetValue(IsEnabledProperty, value); }
        }

        private static void SetIsEnabled(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is bool)) return;
            if ((bool)e.NewValue)
            {
                VisualStateManager.GoToState(((KeyValueControl)obj).button, "Normal", true);
                ((KeyValueControl)obj).button.IsEnabled = true;
            }
            else
            {
                VisualStateManager.GoToState(((KeyValueControl)obj).button, "Disabled", true);
                ((KeyValueControl)obj).button.IsEnabled = false;
            }
        }
    }
}
