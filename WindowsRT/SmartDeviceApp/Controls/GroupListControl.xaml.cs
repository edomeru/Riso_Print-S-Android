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
using Windows.UI.Xaml.Media.Imaging;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Controls
{
    public partial class GroupListControl : UserControl
    {
        private bool _isLoaded;
        private bool _isOrientationChanged;

        public GroupListControl()
        {
            this.InitializeComponent();
            if (!_isLoaded) Loaded += new RoutedEventHandler(OnLoaded);
            Messenger.Default.Register<ViewOrientation>(this, (viewOrientation) => ViewOrientationChanged());
        }

        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(GroupListControl), null);

        public static readonly DependencyProperty TextWidthProperty =
            DependencyProperty.Register("TextWidth", typeof(double), typeof(GroupListControl), null);

        public static readonly DependencyProperty SubTextProperty =
            DependencyProperty.Register("SubText", typeof(string), typeof(GroupListControl), null);

        public static readonly DependencyProperty SubTextVisibilityProperty =
            DependencyProperty.Register("SubTextVisibility", typeof(Visibility), typeof(GroupListControl), new PropertyMetadata(Visibility.Collapsed));

        public static new readonly DependencyProperty ContentProperty =
           DependencyProperty.Register("Content", typeof(object), typeof(GroupListControl), null);

        public static readonly DependencyProperty DeleteCommandProperty =
            DependencyProperty.Register("DeleteCommand", typeof(ICommand), typeof(GroupListControl), null);

        public static readonly DependencyProperty DeleteCommandParameterProperty =
           DependencyProperty.Register("DeleteCommandParameter", typeof(object), typeof(GroupListControl), null);

        public static readonly DependencyProperty DeleteButtonVisibilityProperty =
           DependencyProperty.Register("DeleteButtonVisibility", typeof(Visibility), typeof(GroupListControl), new PropertyMetadata(Visibility.Visible));

        public static readonly DependencyProperty PressedHeaderColorProperty =
            DependencyProperty.Register("PressedHeaderColor", typeof(SolidColorBrush), typeof(GroupListControl), null);

        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        public double TextWidth
        {
            get { return (double)GetValue(TextWidthProperty); }
            set { SetValue(TextWidthProperty, value); }
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

        public new object Content
        {
            get { return (object)GetValue(ContentProperty); }
            set { SetValue(ContentProperty, value); }
        }

        public ICommand DeleteCommand
        {
            get { return (ICommand)GetValue(DeleteCommandProperty); }
            set { SetValue(DeleteCommandProperty, value); }
        }

        public object DeleteCommandParameter
        {
            get { return (object)GetValue(DeleteCommandParameterProperty); }
            set { SetValue(DeleteCommandParameterProperty, value); }
        }

        public Visibility DeleteButtonVisibility
        {
            get { return (Visibility)GetValue(DeleteButtonVisibilityProperty); }
            set { SetValue(DeleteButtonVisibilityProperty, value); }
        }

        public SolidColorBrush PressedHeaderColor
        {
            get { return (SolidColorBrush)GetValue(PressedHeaderColorProperty); }
            set { SetValue(PressedHeaderColorProperty, value); }
        }

        public ToggleButton Header
        {
            get { return header; }
        }

        private void ViewOrientationChanged()
        {
            _isOrientationChanged = true;
            OnLoaded(this, null);
        }

        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            try
            {
                if (_isLoaded && !_isOrientationChanged) return;
                var defaultMargin = (int)((double)Application.Current.Resources["MARGIN_Default"]);

                // Get text width by subtracting widths and margins of visible components
                // Workaround because sometimes (when view orientation is changed) 
				// Width != ActualWidth, need to get value of Width when possible
                var groupControlWidth = ((int)groupListControl.Width > 0) ? (int)groupListControl.Width : (int)groupListControl.ActualWidth;
                if (groupControlWidth <= 0)
                {
                    var parent = (FrameworkElement)groupListControl.Parent;
                    if (parent != null)
                    {
                        groupControlWidth = ((int)parent.Width > 0) ? (int)parent.Width : (int)parent.ActualWidth;
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

                // Delete button is visible
                if (DeleteButtonVisibility == Visibility.Visible)
                {
                    var deleteButtonWidth = (int)((double)Application.Current.Resources["SIZE_DeleteButtonWidth_Long"]);
                    maxTextWidth -= deleteButtonWidth;
                }

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
                _isOrientationChanged = false;
            }
            catch (Exception ex)
            {
                LogUtility.LogError(ex);
                _isOrientationChanged = false;
            }
        }
    }
}
