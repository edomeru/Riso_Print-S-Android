using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Utilities;
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
    public sealed partial class PrinterNameControl : UserControl
    {
        public PrinterNameControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty LeftButtonVisibilityProperty =
            DependencyProperty.Register("LeftButtonVisibility", typeof(Visibility), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty RightButtonVisibilityProperty =
            DependencyProperty.Register("RightButtonVisibility", typeof(Visibility), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty IconVisibilityProperty =
            DependencyProperty.Register("IconVisibility", typeof(Visibility), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty ValueVisibilityProperty =
            DependencyProperty.Register("ValueVisibility", typeof(Visibility), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty ValueContentProperty =
            DependencyProperty.Register("ValueContent", typeof(object), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty IconImageProperty =
            DependencyProperty.Register("IconImage", typeof(ImageSource), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty ValueTextProperty =
            DependencyProperty.Register("ValueText", typeof(string), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty SetFocusProperty =
            DependencyProperty.Register("SetFocus", typeof(bool), typeof(PrinterNameControl),
            new PropertyMetadata(false, FocusChanged));

        public static readonly DependencyProperty IsDefaultProperty =
            DependencyProperty.Register("IsDefault", typeof(bool), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty IsOnlineProperty =
            DependencyProperty.Register("IsOnline", typeof(bool), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty DeleteCommandProperty =
            DependencyProperty.Register("DeleteCommand", typeof(ICommand), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty PrinterIpProperty =
            DependencyProperty.Register("PrinterIp", typeof(string), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty WillBeDeletedProperty =
            DependencyProperty.Register("WillBeDeleted", typeof(bool), typeof(PrinterNameControl), null);

        public static readonly DependencyProperty TextWidthProperty =
            DependencyProperty.Register("TextWidth", typeof(double), typeof(PrinterNameControl), null);

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

        public bool SetFocus
        {
            get { return (bool)GetValue(SetFocusProperty); }
            set { SetValue(SetFocusProperty, value); }
        }


        public bool IsDefault
        {
            get { return (bool)GetValue(IsDefaultProperty); }
            set { SetValue(IsDefaultProperty, value); }
        }

        public bool IsOnline
        {
            get { return (bool)GetValue(IsOnlineProperty); }
            set { SetValue(IsOnlineProperty, value); }
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

        public ICommand DeleteCommand
        {
            get { return (ICommand)GetValue(DeleteCommandProperty); }
            set { 
                SetValue(DeleteCommandProperty, value); 
            }
        }
       
        public string PrinterIp
        {
            get { return (string)GetValue(PrinterIpProperty); }
            set { SetValue(PrinterIpProperty, value); }
        }

        public bool WillBeDeleted
        {
            get { return (bool)GetValue(WillBeDeletedProperty); }
            set { SetValue(WillBeDeletedProperty, value); }
        }

        public double TextWidth
        {
            get { return (double)GetValue(TextWidthProperty); }
            set { SetValue(TextWidthProperty, value); }
        }

        private void deleteButton_Loaded(object sender, RoutedEventArgs e)
        {
            this.deleteButton.AddHandler(PointerPressedEvent, new PointerEventHandler(deleteButton_PointerPressed), true);
            this.deleteButton.AddHandler(PointerCaptureLostEvent, new PointerEventHandler(deleteButton_PointerLost), true);
        }

        private void deleteButton_PointerLost(object sender, PointerRoutedEventArgs e)
        {
            ((ToggleButton)sender).ReleasePointerCapture(e.Pointer);
                WillBeDeleted = !WillBeDeleted;
        }

        private void deleteButton_PointerPressed(object sender, PointerRoutedEventArgs e)
        {
            ((ToggleButton)sender).CapturePointer(e.Pointer);
            WillBeDeleted = !WillBeDeleted;
        }

        private void button_Loaded(object sender, RoutedEventArgs e)
        {
            //compute for text width
            try
            {
                var defaultMargin = (int)((double)Application.Current.Resources["MARGIN_Default"]);

                // Get text width by subtracting widths and margins of visible components
                var groupControlWidth = (int)printerNameControl.ActualWidth;
                if (groupControlWidth <= 0)
                {
                    var parent = (FrameworkElement)printerNameControl.Parent;
                    if (parent != null)
                    {
                        groupControlWidth = (int)parent.ActualWidth;
                        if (groupControlWidth <= 0)
                        {
                            throw new ArgumentException("Zero width element");
                        }
                    }
                    else
                    {
                        throw new ArgumentException("Zero width element");
                    }
                }
                int maxTextWidth = groupControlWidth;

                // Left and right margins
                maxTextWidth -= (defaultMargin * 2);

                var deleteButtonWidth = (int)((double)Application.Current.Resources["SIZE_DeleteButtonWidth_Long"]);
                maxTextWidth -= deleteButtonWidth;
                

                // Image
                maxTextWidth -= ImageConstant.GetIconImageWidth(sender);
                maxTextWidth -= defaultMargin;
                if (maxTextWidth <= 0)
                {
                    TextWidth = 0;
                }
                else
                {
                    TextWidth = maxTextWidth;
                }
            }
            catch (Exception ex)
            {
                LogUtility.LogError(ex);
            }
        }

    }
}
