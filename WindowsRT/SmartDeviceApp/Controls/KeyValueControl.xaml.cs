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
using Windows.UI.Xaml.Media.Imaging;
using SmartDeviceApp.Common.Constants;

namespace SmartDeviceApp.Controls
{
    public partial class KeyValueControl : UserControl
    {
        private bool _isLoaded;
        private bool _isForceReload;

        public KeyValueControl()
        {
            this.InitializeComponent();
            if (!_isLoaded) Loaded += new RoutedEventHandler(OnLoaded);
        }
		
        public static readonly DependencyProperty CommandProperty =
            DependencyProperty.Register("Command", typeof(ICommand), typeof(KeyValueControl), null);
        
        public static readonly DependencyProperty CommandParameterProperty =
            DependencyProperty.Register("CommandParameter", typeof(object), typeof(KeyValueControl), null);
        
        public static readonly DependencyProperty RightButtonVisibilityProperty =
            DependencyProperty.Register("RightButtonVisibility", typeof(Visibility), typeof(KeyValueControl), new PropertyMetadata(Visibility.Visible));

        public static readonly DependencyProperty IconVisibilityProperty =
            DependencyProperty.Register("IconVisibility", typeof(Visibility), typeof(KeyValueControl), new PropertyMetadata(Visibility.Visible));

        public static readonly DependencyProperty ValueVisibilityProperty =
            DependencyProperty.Register("ValueVisibility", typeof(Visibility), typeof(KeyValueControl), new PropertyMetadata(Visibility.Visible));

        public static readonly DependencyProperty ValueContentProperty = 
            DependencyProperty.Register("ValueContent", typeof(object), typeof(KeyValueControl), null);

        public static readonly DependencyProperty IconImageProperty =
            DependencyProperty.Register("IconImage", typeof(ImageSource), typeof(KeyValueControl), null);
			
		public static readonly DependencyProperty RightImageProperty =
            DependencyProperty.Register("RightImage", typeof(ImageSource), typeof(KeyValueControl), null);

        public static readonly DependencyProperty RightDisabledImageProperty =
            DependencyProperty.Register("RightDisabledImage", typeof(ImageSource), typeof(KeyValueControl), null);

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty SubTextProperty =
            DependencyProperty.Register("SubText", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty SubTextVisibilityProperty =
            DependencyProperty.Register("SubTextVisibility", typeof(Visibility), typeof(KeyValueControl), new PropertyMetadata(Visibility.Collapsed));

        public static readonly DependencyProperty KeyTextWidthProperty =
            DependencyProperty.Register("KeyTextWidth", typeof(double), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueTextWidthProperty =
            DependencyProperty.Register("ValueTextWidth", typeof(double), typeof(KeyValueControl), null);

        public static readonly DependencyProperty SeparatorVisibilityProperty =
            DependencyProperty.Register("SeparatorVisibility", typeof(Visibility), typeof(KeyValueControl), new PropertyMetadata(Visibility.Visible));

        public static new readonly DependencyProperty IsEnabledProperty =
            DependencyProperty.Register("IsEnabled", typeof(bool), typeof(KeyValueControl), new PropertyMetadata(true, SetIsEnabled));

        public static readonly DependencyProperty PressedColorProperty =
            DependencyProperty.Register("PressedColor", typeof(SolidColorBrush), typeof(KeyValueControl), null);

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

        public Visibility RightButtonVisibility
        {
            get { return (Visibility)GetValue(RightButtonVisibilityProperty); }
            set { SetValue(RightButtonVisibilityProperty, value); }
        }

        public Visibility IconVisibility
        {
            get { return (Visibility)GetValue(IconVisibilityProperty); }
            set { SetValue(IconVisibilityProperty, value); }
        }

        public Visibility ValueVisibility
        {
            get { return (Visibility)GetValue(ValueVisibilityProperty); }
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

        public string SubText
        {
            get { return (string)GetValue(SubTextProperty); }
            set { SetValue(SubTextProperty, value); }
        }

        public Visibility SubTextVisibility
        {
            get { return (Visibility)GetValue(SubTextVisibilityProperty); }
            set { SetValue(SubTextVisibilityProperty, value); }
        }

        public string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        public double KeyTextWidth
        {
            get { return (double)GetValue(KeyTextWidthProperty); }
            set { SetValue(KeyTextWidthProperty, value); }
        }

        public double ValueTextWidth
        {
            get { return (double)GetValue(ValueTextWidthProperty); }
            set { SetValue(ValueTextWidthProperty, value); }
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

        public SolidColorBrush PressedColor
        {
            get { return (SolidColorBrush)GetValue(PressedColorProperty); }
            set { SetValue(PressedColorProperty, value); }
        }

        private static void SetIsEnabled(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is bool)) return;
            var control = (KeyValueControl)obj;
            if ((bool)e.NewValue)
            {
                VisualStateManager.GoToState(((KeyValueControl)obj).button, "Normal", true);
                control.button.IsEnabled = true;
                if (e.OldValue != e.NewValue)
                {
                    control._isForceReload = true;
                    control.OnLoaded(null, null);
                }
            }
            else
            {
                VisualStateManager.GoToState(((KeyValueControl)obj).button, "Disabled", true);
                control.button.IsEnabled = false;
                if (e.OldValue != e.NewValue)
                {
                    control._isForceReload = true;
                    control.OnLoaded(null, null);
                }
            }
        }
        
        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            if ((_isLoaded && !_isForceReload) || Visibility == Visibility.Collapsed) return;

            var defaultMargin = (int)((double)Application.Current.Resources["MARGIN_Default"]);
            var smallMargin = (int)((double)Application.Current.Resources["MARGIN_Small"]);

            // Get text width by subtracting widths and margins of visible components
            int maxTextWidth = (int)keyValueControl.ActualWidth;
            
            // Left and right margins
            maxTextWidth -= (defaultMargin * 2);
            
            // Icon is visible
            if (IconVisibility == Visibility.Visible)
            {
                var imageWidth = ((BitmapImage)IconImage).PixelWidth;
                if (imageWidth == 0) imageWidth = ImageConstant.GetIconImageWidth(sender);
                maxTextWidth -= imageWidth;
                maxTextWidth -= defaultMargin;
            }
            // RightButton is visible
            if (RightButtonVisibility == Visibility.Visible)
            {
                var rightButtonImageWidth = ((BitmapImage)RightImage).PixelWidth;
                if (rightButtonImageWidth == 0) rightButtonImageWidth = ImageConstant.GetRightButtonImageWidth();
                maxTextWidth -= rightButtonImageWidth;
                maxTextWidth -= defaultMargin;
            }

            // ValueContent is visible
            var valueContent = ValueContent as FrameworkElement;
            if (valueContent != null && valueContent.Visibility == Visibility.Visible)
            {
                maxTextWidth -= (int)valueContent.ActualWidth;
                maxTextWidth -= defaultMargin;
            }

            // Value is visible
            if (ValueVisibility == Visibility.Visible && IsEnabled) // Note: If disabled, value is not visible
            {
                // Space between key and value texts
                maxTextWidth -= smallMargin;
                maxTextWidth /= 2;
                // Set key and value to equal widths
                KeyTextWidth = maxTextWidth;
                ValueTextWidth = maxTextWidth;
            }
            else // only Key is visible
            {
                KeyTextWidth = maxTextWidth;
            }
            _isLoaded = true;
            _isForceReload = false;
        }
    }
}
