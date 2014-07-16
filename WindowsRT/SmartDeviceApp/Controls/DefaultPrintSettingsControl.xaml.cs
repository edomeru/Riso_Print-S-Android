using SmartDeviceApp.Common;
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

// The User Control item template is documented at http://go.microsoft.com/fwlink/?LinkId=234236

namespace SmartDeviceApp.Controls
{
    public sealed partial class DefaultPrintSettingsControl : UserControl
    {
        /// <summary>
        /// Constructor for DefaultPrintSettingsControl
        /// </summary>
        public DefaultPrintSettingsControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty CommandProperty =
            DependencyProperty.Register("Command", typeof(ICommand), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty CommandParameterProperty =
            DependencyProperty.Register("CommandParameter", typeof(object), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty RightButtonVisibilityProperty =
            DependencyProperty.Register("RightButtonVisibility", typeof(Visibility), typeof(DefaultPrintSettingsControl), new PropertyMetadata(Visibility.Visible));

        public static readonly DependencyProperty IconVisibilityProperty =
            DependencyProperty.Register("IconVisibility", typeof(Visibility), typeof(DefaultPrintSettingsControl), new PropertyMetadata(Visibility.Visible));

        public static readonly DependencyProperty ValueVisibilityProperty =
            DependencyProperty.Register("ValueVisibility", typeof(Visibility), typeof(DefaultPrintSettingsControl), new PropertyMetadata(Visibility.Visible));

        public static readonly DependencyProperty ValueContentProperty =
            DependencyProperty.Register("ValueContent", typeof(object), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty IconImageProperty =
            DependencyProperty.Register("IconImage", typeof(ImageSource), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty RightImageProperty =
            DependencyProperty.Register("RightImage", typeof(ImageSource), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty RightDisabledImageProperty =
            DependencyProperty.Register("RightDisabledImage", typeof(ImageSource), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty SubTextProperty =
            DependencyProperty.Register("SubText", typeof(string), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty SubTextVisibilityProperty =
            DependencyProperty.Register("SubTextVisibility", typeof(Visibility), typeof(DefaultPrintSettingsControl), new PropertyMetadata(Visibility.Collapsed));

        public static readonly DependencyProperty PressedColorProperty =
            DependencyProperty.Register("PressedColor", typeof(SolidColorBrush), typeof(DefaultPrintSettingsControl), null);

        public static readonly DependencyProperty VisualStateProperty =
            DependencyProperty.Register("VisualState", typeof(string), typeof(DefaultPrintSettingsControl),
            new PropertyMetadata("Normal", SetVisualState));

        public static readonly DependencyProperty IsPressedProperty =
            DependencyProperty.Register("IsPressed", typeof(bool), typeof(DefaultPrintSettingsControl), null);

        /// <summary>
        /// Command property that will be executed when this control is tapped.
        /// </summary>
        public ICommand Command
        {
            get { 
                return (ICommand)GetValue(CommandProperty); 
            }
            set { 
                SetValue(CommandProperty, value); 
            }
        }

        /// <summary>
        /// Command parameter for the command to be executed.
        /// </summary>
        public object CommandParameter
        {
            get { return (object)GetValue(CommandParameterProperty); }
            set { SetValue(CommandParameterProperty, value); }
        }

        /// <summary>
        /// Visibility property of the right button
        /// </summary>
        public Visibility RightButtonVisibility
        {
            get { return (Visibility)GetValue(RightButtonVisibilityProperty); }
            set { SetValue(RightButtonVisibilityProperty, value); }
        }

        /// <summary>
        /// Visibility property of the icon
        /// </summary>
        public Visibility IconVisibility
        {
            get { return (Visibility)GetValue(IconVisibilityProperty); }
            set { SetValue(IconVisibilityProperty, value); }
        }

        /// <summary>
        /// ImageSource property for the icon of the control
        /// </summary>
        public ImageSource IconImage
        {
            get { return (ImageSource)GetValue(IconImageProperty); }
            set { SetValue(IconImageProperty, value); }
        }

        /// <summary>
        /// ImageSource property for the right image.
        /// </summary>
        public ImageSource RightImage
        {
            get { return (ImageSource)GetValue(RightImageProperty); }
            set { SetValue(RightImageProperty, value); }
        }

        /// <summary>
        /// String property for the text displayed in the control
        /// </summary>
        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        /// <summary>
        /// Color property for the "Pressed" visual state of this control
        /// </summary>
        public SolidColorBrush PressedColor
        {
            get { return (SolidColorBrush)GetValue(PressedColorProperty); }
            set { SetValue(PressedColorProperty, value); }
        }

        /// <summary>
        /// String property that sets the visual state of this control
        /// </summary>
        public string VisualState
        {
            get { return (string)GetValue(VisualStateProperty); }
            set { SetValue(VisualStateProperty, value); }
        }

        /// <summary>
        /// Flag to check whether this control is in Pressed state
        /// </summary>
        public bool IsPressed
        {
            get { return (bool)GetValue(IsPressedProperty); }
            set { SetValue(IsPressedProperty, value); }
        }


        private static void SetVisualState(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue == null || !(e.NewValue is string)) return;
            var state = e.NewValue.ToString();
            var button = ((DefaultPrintSettingsControl)obj).defaultButton;
            switch (state)
            {
                case "Normal":
                    VisualStateManager.GoToState(button, "Normal", true);
                    ((DefaultPrintSettingsControl)obj).IsPressed = false;
                    break;
                case "Pressed":
                    VisualStateManager.GoToState(button, "Pressed", true);
                    ((DefaultPrintSettingsControl)obj).IsPressed = true;
                    break;
                default:
                    VisualStateManager.GoToState(button, "Normal", true);
                    ((DefaultPrintSettingsControl)obj).IsPressed = false;
                    break;
            }
        }
    }
}
