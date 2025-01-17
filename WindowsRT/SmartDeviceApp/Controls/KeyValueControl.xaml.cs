﻿using System;
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
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.Controls
{
    public partial class KeyValueControl : UserControl
    {
        private bool _isLoaded;

        /// <summary>
        /// Constructor for KeyValueControl.
        /// </summary>
        public KeyValueControl()
        {
            this.InitializeComponent();
            if (!_isLoaded) Loaded += new RoutedEventHandler(OnLoaded);
        }

        public static readonly DependencyProperty CommandProperty =
            DependencyProperty.Register("Command", typeof(ICommand), typeof(KeyValueControl), null);
        
        public static readonly DependencyProperty CommandParameterProperty =
            DependencyProperty.Register("CommandParameter", typeof(object), typeof(KeyValueControl), null);

        public static readonly DependencyProperty IsListItemProperty =
            DependencyProperty.Register("IsListItem", typeof(bool), typeof(KeyValueControl), null);

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

        public static readonly DependencyProperty KeyPressedColorProperty =
            DependencyProperty.Register("KeyPressedColor", typeof(SolidColorBrush), typeof(KeyValueControl), 
            new PropertyMetadata((SolidColorBrush)Application.Current.Resources["BRUSH_Gray4"]));

        public static readonly DependencyProperty SubTextProperty =
            DependencyProperty.Register("SubText", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty SubTextVisibilityProperty =
            DependencyProperty.Register("SubTextVisibility", typeof(Visibility), typeof(KeyValueControl), new PropertyMetadata(Visibility.Collapsed));

        public static readonly DependencyProperty KeyTextWidthProperty =
            DependencyProperty.Register("KeyTextWidth", typeof(double), typeof(KeyValueControl), null);

        public static readonly DependencyProperty KeyTextStyleProperty =
            DependencyProperty.Register("KeyTextStyle", typeof(Style), typeof(KeyValueControl),
            new PropertyMetadata((Style)Application.Current.Resources["STYLE_TextKey"]));

        public static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueTextWidthProperty =
            DependencyProperty.Register("ValueTextWidth", typeof(double), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueTextStyleProperty =
            DependencyProperty.Register("ValueTextStyle", typeof(Style), typeof(KeyValueControl), 
            new PropertyMetadata((Style)Application.Current.Resources["STYLE_TextValue"]));

        public static readonly DependencyProperty ValueSubTextProperty =
            DependencyProperty.Register("ValueSubText", typeof(string), typeof(KeyValueControl), null);

        public static readonly DependencyProperty ValueSubTextVisibilityProperty =
            DependencyProperty.Register("ValueSubTextVisibility", typeof(Visibility), typeof(KeyValueControl), new PropertyMetadata(Visibility.Collapsed));

        public static readonly DependencyProperty SeparatorVisibilityProperty =
            DependencyProperty.Register("SeparatorVisibility", typeof(Visibility), typeof(KeyValueControl), new PropertyMetadata(Visibility.Visible));

        public static readonly DependencyProperty SeparatorStartPointProperty =
            DependencyProperty.Register("SeparatorStartPoint", typeof(double), typeof(KeyValueControl), new PropertyMetadata(0.0));

        public static new readonly DependencyProperty IsEnabledProperty =
            DependencyProperty.Register("IsEnabled", typeof(bool), typeof(KeyValueControl), new PropertyMetadata(true, SetIsEnabled));

        public static readonly DependencyProperty PressedColorProperty =
            DependencyProperty.Register("PressedColor", typeof(SolidColorBrush), typeof(KeyValueControl), null);

        public static readonly DependencyProperty VisualStateProperty =
            DependencyProperty.Register("VisualState", typeof(string), typeof(KeyValueControl),
            new PropertyMetadata("Normal", SetVisualState));

        public static readonly DependencyProperty IsOnLoadedEnabledProperty =
            DependencyProperty.Register("IsOnLoadedEnabled", typeof(bool), typeof(KeyValueControl),
            new PropertyMetadata(true));

        /// <summary>
        /// Command property of the KeyValueControl.
        /// </summary>
        public ICommand Command
        {
            get { return (ICommand)GetValue(CommandProperty); }
            set { SetValue(CommandProperty, value); }
        }

        /// <summary>
        /// Command parameter for the KeyValueControl.
        /// </summary>
        public object CommandParameter
        {
            get { return (object)GetValue(CommandParameterProperty); }
            set { SetValue(CommandParameterProperty, value); }
        }

        /// <summary>
        /// Flag to check whether the control is a list item.
        /// </summary>
        public bool IsListItem
        {
            get { return (bool)GetValue(IsListItemProperty); }
            set { SetValue(IsListItemProperty, value); }
        }

        /// <summary>
        /// Visibility property for the right button.
        /// </summary>
        public Visibility RightButtonVisibility
        {
            get { return (Visibility)GetValue(RightButtonVisibilityProperty); }
            set { SetValue(RightButtonVisibilityProperty, value); }
        }

        /// <summary>
        /// Visibility property for the icon.
        /// </summary>
        public Visibility IconVisibility
        {
            get { return (Visibility)GetValue(IconVisibilityProperty); }
            set { SetValue(IconVisibilityProperty, value); }
        }

        /// <summary>
        /// Visibility property for the Value element.
        /// </summary>
        public Visibility ValueVisibility
        {
            get { return (Visibility)GetValue(ValueVisibilityProperty); }
            set { SetValue(ValueVisibilityProperty, value); }
        }

        /// <summary>
        /// Visibility property for the subtext.
        /// </summary>
        public Visibility ValueSubTextVisibility
        {
            get { return (Visibility)GetValue(ValueSubTextVisibilityProperty); }
            set { SetValue(ValueSubTextVisibilityProperty, value); }
        }

        /// <summary>
        /// Content of the control.
        /// </summary>
        public object ValueContent
        {
            get { return (object)GetValue(ValueContentProperty); }
            set { SetValue(ValueContentProperty, value); }
        }

        /// <summary>
        /// Text style for the text of the control.
        /// </summary>
        public Style KeyTextStyle
        {
            get { return (Style)GetValue(KeyTextStyleProperty); }
            set { SetValue(KeyTextStyleProperty, value); }
        }

        /// <summary>
        /// Text style for the text of the control.
        /// </summary>
        public Style ValueTextStyle
        {
            get { return (Style)GetValue(ValueTextStyleProperty); }
            set { SetValue(ValueTextStyleProperty, value); }
        }

        /// <summary>
        /// Imagesource for the icon.
        /// </summary>
        public ImageSource IconImage
        {
            get { return (ImageSource)GetValue(IconImageProperty); }
            set { SetValue(IconImageProperty, value); }
        }

        /// <summary>
        /// Imagesource for the right image.
        /// </summary>
        public ImageSource RightImage
        {
            get { return (ImageSource)GetValue(RightImageProperty); }
            set { SetValue(RightImageProperty, value); }
        }

        /// <summary>
        /// Imagesource for the right image when it is disabled.
        /// </summary>
        public ImageSource RightDisabledImage
        {
            get { return (ImageSource)GetValue(RightDisabledImageProperty); }
            set { SetValue(RightDisabledImageProperty, value); }
        }

        /// <summary>
        /// Key text property of the control.
        /// </summary>
        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        /// <summary>
        /// Color property used for the "pressed" state of the control.
        /// </summary>
        public SolidColorBrush KeyPressedColor
        {
            get { return (SolidColorBrush)GetValue(KeyPressedColorProperty); }
            set { SetValue(KeyPressedColorProperty, value); }
        }

        /// <summary>
        /// Subtext property displayed under the Text property.
        /// </summary>
        public string SubText
        {
            get { return (string)GetValue(SubTextProperty); }
            set { SetValue(SubTextProperty, value); }
        }

        /// <summary>
        /// Visibility property for the subtext.
        /// </summary>
        public Visibility SubTextVisibility
        {
            get { return (Visibility)GetValue(SubTextVisibilityProperty); }
            set { SetValue(SubTextVisibilityProperty, value); }
        }

        /// <summary>
        /// Text displaying the value of the key text.
        /// </summary>
        public string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        /// <summary>
        /// Subtext for the ValueText.
        /// </summary>
        public string ValueSubText
        {
            get { return (string)GetValue(ValueSubTextProperty); }
            set { SetValue(ValueSubTextProperty, value); }
        }

        /// <summary>
        /// Width of the Key Text.
        /// </summary>
        public double KeyTextWidth
        {
            get { return (double)GetValue(KeyTextWidthProperty); }
            set { SetValue(KeyTextWidthProperty, value); }
        }

        /// <summary>
        /// Width of the Value Text.
        /// </summary>
        public double ValueTextWidth
        {
            get { return (double)GetValue(ValueTextWidthProperty); }
            set { SetValue(ValueTextWidthProperty, value); }
        }

        /// <summary>
        /// Visibility for the separator.
        /// </summary>
        public Visibility SeparatorVisibility
        {
            get { return (Visibility)GetValue(SeparatorVisibilityProperty); }
            set { SetValue(SeparatorVisibilityProperty, value); }
        }

        /// <summary>
        /// Start point of the separator line.
        /// </summary>
        public double SeparatorStartPoint
        {
            get { return (double)GetValue(SeparatorStartPointProperty); }
            set { SetValue(SeparatorStartPointProperty, value); }
        }

        /// <summary>
        /// Flag to check whether the control is enabled.
        /// </summary>
        public new bool IsEnabled
        {
            get { return (bool)GetValue(IsEnabledProperty); }
            set { SetValue(IsEnabledProperty, value); }
        }

        /// <summary>
        /// Color for the "pressed" state of the control.
        /// </summary>
        public SolidColorBrush PressedColor
        {
            get { return (SolidColorBrush)GetValue(PressedColorProperty); }
            set { SetValue(PressedColorProperty, value); }
        }

        /// <summary>
        /// Visual state of the control.
        /// </summary>
        public string VisualState
        {
            get { return (string)GetValue(VisualStateProperty); }
            set { SetValue(VisualStateProperty, value); }
        }

        /// <summary>
        /// Flag to check whether the function OnLoaded is enabled.
        /// </summary>
        public bool IsOnLoadedEnabled
        {
            get { return (bool)GetValue(IsOnLoadedEnabledProperty); }
            set { SetValue(IsOnLoadedEnabledProperty, value); }
        }

        private static void SetIsEnabled(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is bool)) return;
            var control = (KeyValueControl)obj;
            if ((bool)e.NewValue)
            {
                VisualStateManager.GoToState(control.button, "Normal", true);
                control.button.IsEnabled = true;
            }
            else
            {
                VisualStateManager.GoToState(control.button, "Disabled", true);
                control.button.IsEnabled = false;
            }
        }
        
        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            try
            {
                if (!IsOnLoadedEnabled || _isLoaded || Visibility == Visibility.Collapsed) return;

                // Set key styles
                if (SubTextVisibility == Visibility.Visible)
                {
                    KeyTextStyle = (Style)Application.Current.Resources["STYLE_TextKeyWithSubText"];
                }
                else
                {
                    KeyTextStyle = (Style)Application.Current.Resources["STYLE_TextKey"];
                }

                // Set value styles
                if (ValueSubTextVisibility == Visibility.Visible)
                {
                    ValueTextStyle = (Style)Application.Current.Resources["STYLE_TextValueWithSubText"];
                }
                else
                {
                    ValueTextStyle = (Style)Application.Current.Resources["STYLE_TextValue"];
                }

                // Adjust widths
                var noMargin = (double)Application.Current.Resources["MARGIN_None"];
                var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
                var smallMargin = (double)Application.Current.Resources["MARGIN_Small"];

                // Get text width by subtracting widths and margins of visible components
                var keyValueControlWidth = (int)keyValueControl.ActualWidth;
                if (keyValueControlWidth <= 0)
                {
                    var parent = (FrameworkElement)keyValueControl.Parent;
                    if (parent != null)
                    {
                        keyValueControlWidth = (int)parent.ActualWidth;
                        if (keyValueControlWidth <= 0)
                        {
                            throw new ArgumentException("Zero width element");
                        }
                    }
                    else
                    {
                        throw new ArgumentException("Zero width element");
                    }
                }
                int maxTextWidth = keyValueControlWidth;

                // Left and right margins
                maxTextWidth -= ((int)defaultMargin * 2);

                // Icon is visible
                if (IconVisibility == Visibility.Visible)
                {
                    var imageWidth = ((BitmapImage)IconImage).PixelWidth;
                    if (imageWidth == 0) imageWidth = ImageConstant.GetIconImageWidth(sender);
                    maxTextWidth -= imageWidth;
                    maxTextWidth -= (int)defaultMargin;
                }
                // RightButton is visible
                if (RightButtonVisibility == Visibility.Visible)
                {
                    var rightButtonImageWidth = ((BitmapImage)RightImage).PixelWidth;
                    if (rightButtonImageWidth == 0) rightButtonImageWidth = ImageConstant.GetRightButtonImageWidth(sender);
                    maxTextWidth -= rightButtonImageWidth;
                    maxTextWidth -= (int)defaultMargin;
                }

                // ValueContent is visible
                var valueContent = ValueContent as FrameworkElement;
                if (valueContent != null && valueContent.Visibility == Visibility.Visible)
                {
                    maxTextWidth -= (int)valueContent.ActualWidth;
                    maxTextWidth -= (int)defaultMargin;
                }

                // Value is visible
                if (ValueVisibility == Visibility.Visible && IsEnabled) // Note: If disabled, value is not visible
                {
                    // Space between key and value texts
                    maxTextWidth -= (int)smallMargin;
                    maxTextWidth /= 2;
                    // Set key and value to equal widths
                    KeyTextWidth = maxTextWidth;

                    ValueTextWidth = maxTextWidth;
                }
                else // only Key is visible
                {
                    KeyTextWidth = maxTextWidth;
                }

                // Set separator start point
                if (SeparatorVisibility == Visibility.Visible)
                {
                    SeparatorStartPoint = ViewControlUtility.GetSeparatorStartPoint(sender,
                                                                    IsListItem, IconVisibility);
                }

                _isLoaded = true;
            }
            catch (Exception ex)
            {
                LogUtility.LogError(ex);
            }
        }

        private static void SetVisualState(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is string)) return;
            var state = e.NewValue.ToString();
            var button = ((KeyValueControl)obj).button;
            switch (state)
            {
                case "Normal":
                    VisualStateManager.GoToState(button, "Normal", true);
                    break;
                case "Pressed":
                    VisualStateManager.GoToState(button, "Pressed", true);
                    break;
                case "Disabled":
                    VisualStateManager.GoToState(button, "Disabled", true);
                    break;    
                default:
                    VisualStateManager.GoToState(button, "Normal", true);
                    break;
            }
        }
    }
}
