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

        /// <summary>
        /// Constructor. Initializes component.
        /// </summary>
        public GroupListControl()
        {
            this.InitializeComponent();
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

        public static readonly DependencyProperty IsCollapsedProperty =
            DependencyProperty.Register("IsCollapsed", typeof(bool), typeof(GroupListControl), null);

        public static readonly DependencyProperty DeleteButtonVisualStateProperty =
            DependencyProperty.Register("DeleteButtonVisualState", typeof(string), typeof(GroupListControl),
            new PropertyMetadata("DeleteNormal", SetDeleteButtonVisualState));

        /// <summary>
        /// Text property. This is displayed in the control next to the +/- button.
        /// </summary>
        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        /// <summary>
        /// Width of the text displayed in the control.
        /// </summary>
        public double TextWidth
        {
            get { return (double)GetValue(TextWidthProperty); }
            set { SetValue(TextWidthProperty, value); }
        }

        /// <summary>
        /// Subtext property displayed below the Text property.
        /// </summary>
        public string SubText
        {
            get { return (string)GetValue(SubTextProperty); }
            set { SetValue(SubTextProperty, value); }
        }

        /// <summary>
        /// Visibility property for Subtext.
        /// </summary>
        public Visibility SubTextVisibility
        {
            get { return (Visibility)GetValue(SubTextVisibilityProperty); }
            set { SetValue(SubTextVisibilityProperty, value); }
        }

        /// <summary>
        /// Content property of the control.
        /// </summary>
        public new object Content
        {
            get { return (object)GetValue(ContentProperty); }
            set { SetValue(ContentProperty, value); }
        }

        /// <summary>
        /// Command to be executed when Delete button is tapped.
        /// </summary>
        public ICommand DeleteCommand
        {
            get { return (ICommand)GetValue(DeleteCommandProperty); }
            set { SetValue(DeleteCommandProperty, value); }
        }

        /// <summary>
        /// Command parameter for the Delete command.
        /// </summary>
        public object DeleteCommandParameter
        {
            get { return (object)GetValue(DeleteCommandParameterProperty); }
            set { SetValue(DeleteCommandParameterProperty, value); }
        }

        /// <summary>
        /// Visibility property for the Delete button.
        /// </summary>
        public Visibility DeleteButtonVisibility
        {
            get { return (Visibility)GetValue(DeleteButtonVisibilityProperty); }
            set { SetValue(DeleteButtonVisibilityProperty, value); }
        }

        /// <summary>
        /// Color property for the "pressed" state of the control.
        /// </summary>
        public SolidColorBrush PressedHeaderColor
        {
            get { return (SolidColorBrush)GetValue(PressedHeaderColorProperty); }
            set { SetValue(PressedHeaderColorProperty, value); }
        }

        /// <summary>
        /// Flag for checking whether the Group List is collapsed or not.
        /// </summary>
        public bool IsCollapsed
        {
            get { return (bool)GetValue(IsCollapsedProperty); }
            set { SetValue(IsCollapsedProperty, value); }
        }

        /// <summary>
        /// Visual state of the delete control.
        /// </summary>
        public string DeleteButtonVisualState
        {
            get { return (string)GetValue(DeleteButtonVisualStateProperty); }
            set { SetValue(DeleteButtonVisualStateProperty, value); }
        }

        /// <summary>
        /// ToggleButton element of the control.
        /// </summary>
        public ToggleButton Header
        {
            get { return header; }
        }

        /// <summary>
        /// Delete button element of the control.
        /// </summary>
        public Button DeleteButton
        {
            get { return (Button)ViewControlUtility.GetControlFromParent<Button>((UIElement)header, "deleteButton"); }
        }

        private void OnSizeChanged(object sender, SizeChangedEventArgs e)
        {
            try
            {
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
                            return;
                        }
                    }
                    else
                    {
                        return;
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

                // Update delete button's VisualState
                UpdateDeleteButtonVisualState((GroupListControl)sender, DeleteButtonVisualState);
            }
            catch (Exception ex)
            {
                LogUtility.LogError(ex);
            }
        }

        private static void SetDeleteButtonVisualState(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            UpdateDeleteButtonVisualState((GroupListControl)obj, e.NewValue.ToString());
        }

        private static void UpdateDeleteButtonVisualState(GroupListControl groupListControl, string state)
        {
            var deleteButton = (Button)ViewControlUtility.GetControlFromParent<Button>(
                (UIElement)groupListControl.Header, "deleteButton"); // "deleteButton" as defined in GroupListControl.xaml
            if (deleteButton != null)
            {
                switch (state)
                {
                    case "DeleteNormal":
                    case "DeletePressed":
                        VisualStateManager.GoToState(deleteButton, state, true);
                        break;
                    default:
                        VisualStateManager.GoToState(deleteButton, "DeleteNormal", true);
                        break;
                }
            }
        }

        private void OnUnloaded(object sender, RoutedEventArgs e)
        {
            // Fix for multiple callback firing for DependencyPropertyChanged - http://stackoverflow.com/a/19305798
            this.ClearValue(GroupListControl.DeleteButtonVisualStateProperty);
        }

    }
}
