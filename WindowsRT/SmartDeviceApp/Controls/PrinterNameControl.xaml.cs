using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;
using System;
using System.Windows.Input;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;

// The User Control item template is documented at http://go.microsoft.com/fwlink/?LinkId=234236

namespace SmartDeviceApp.Controls
{
    public sealed partial class PrinterNameControl : UserControl
    {
        /// <summary>
        /// Constructor for PrinterNameControl
        /// </summary>
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

        /// <summary>
        /// Determines the visibility of the left button
        /// </summary>
        public string LeftButtonVisibility
        {
            get { return (string)GetValue(LeftButtonVisibilityProperty); }
            set { SetValue(LeftButtonVisibilityProperty, value); }
        }

        /// <summary>
        /// Determines the visibility of the right button
        /// </summary>
        public string RightButtonVisibility
        {
            get { return (string)GetValue(RightButtonVisibilityProperty); }
            set { SetValue(RightButtonVisibilityProperty, value); }
        }

        /// <summary>
        /// Determines the visibility of the icon
        /// </summary>
        public string IconVisibility
        {
            get { return (string)GetValue(IconVisibilityProperty); }
            set { SetValue(IconVisibilityProperty, value); }
        }

        /// <summary>
        /// Determines the visibility of the Value text
        /// </summary>
        public string ValueVisibility
        {
            get { return (string)GetValue(ValueVisibilityProperty); }
            set { SetValue(ValueVisibilityProperty, value); }
        }

        /// <summary>
        /// Holds the content to be displayed in this control
        /// </summary>
        public object ValueContent
        {
            get { return (object)GetValue(ValueContentProperty); }
            set { SetValue(ValueContentProperty, value); }
        }

        /// <summary>
        /// ImageSource for the Icon
        /// </summary>
        public ImageSource IconImage
        {
            get { return (ImageSource)GetValue(IconImageProperty); }
            set { SetValue(IconImageProperty, value); }
        }

        /// <summary>
        /// Text property for the control
        /// </summary>
        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        /// <summary>
        /// Text property that will be displayed as value for this control.
        /// </summary>
        public string ValueText
        {
            get { return (string)GetValue(ValueTextProperty); }
            set { SetValue(ValueTextProperty, value); }
        }

        /// <summary>
        /// Flag to check the focus state of the control
        /// </summary>
        public bool SetFocus
        {
            get { return (bool)GetValue(SetFocusProperty); }
            set { SetValue(SetFocusProperty, value); }
        }

        /// <summary>
        /// Flag to check if the printer in the control is default printer
        /// </summary>
        public bool IsDefault
        {
            get { return (bool)GetValue(IsDefaultProperty); }
            set { SetValue(IsDefaultProperty, value); }
        }

        /// <summary>
        /// Flag to check if the printer in the control is online
        /// </summary>
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

        /// <summary>
        /// Delete command that will be executed when the delete button is tapped.
        /// </summary>
        public ICommand DeleteCommand
        {
            get { return (ICommand)GetValue(DeleteCommandProperty); }
            set { 
                SetValue(DeleteCommandProperty, value); 
            }
        }
       
        /// <summary>
        /// Ip address of the printer in this control.
        /// </summary>
        public string PrinterIp
        {
            get { return (string)GetValue(PrinterIpProperty); }
            set { SetValue(PrinterIpProperty, value); }
        }

        /// <summary>
        /// Flag to check if current printer selected will be deleted.
        /// </summary>
        public bool WillBeDeleted
        {
            get { return (bool)GetValue(WillBeDeletedProperty); }
            set { SetValue(WillBeDeletedProperty, value); }
        }

        /// <summary>
        /// This determines if the text will be truncated
        /// </summary>
        public double TextWidth
        {
            get { return (double)GetValue(TextWidthProperty); }
            set { SetValue(TextWidthProperty, value); }
        }

        private void OnSizeChanged(object sender, SizeChangedEventArgs e)
        {
            if (sender != null && sender is PrinterNameControl)
            {
                resizeTextWidth();

                // Update the displayed text based on new width, not the Text property
                key.Text = (string)new PrinterNameMiddleTrimmedTextConverter().Convert(Text, null, TextWidth, null);
            }
        }

        private void resizeTextWidth()
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
                maxTextWidth -= ImageConstant.GetIconImageWidth(this);
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
