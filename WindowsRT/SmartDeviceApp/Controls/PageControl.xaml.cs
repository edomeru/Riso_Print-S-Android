using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
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
    public sealed partial class PageControl : UserControl
    {
        /// <summary>
        /// Constructor for PageControl.
        /// </summary>
        public PageControl()
        {
            this.InitializeComponent();
        }

        public static new readonly DependencyProperty VisibilityProperty = 
            DependencyProperty.Register("Visibility", typeof(Visibility), typeof(PageControl), null);

        public static readonly DependencyProperty ImageProperty =
            DependencyProperty.Register("Image", typeof(ImageSource), typeof(PageControl), null);

        public static readonly DependencyProperty IsLoadPageActiveProperty =
            DependencyProperty.Register("IsLoadPageActive", typeof(bool), typeof(PageControl), null);

        public static new readonly DependencyProperty HorizontalAlignmentProperty =
            DependencyProperty.Register("HorizontalAlignment", typeof(HorizontalAlignment), typeof(PageControl), null);

        public static new readonly DependencyProperty MarginProperty =
            DependencyProperty.Register("Margin", typeof(Thickness), typeof(PageControl), null);

        /// <summary>
        /// Visibility property for the control.
        /// </summary>
        public new Visibility Visibility
        {
            get { return (Visibility)GetValue(VisibilityProperty); }
            set { SetValue(VisibilityProperty, value); }
        }

        /// <summary>
        /// Image displayed as the page.
        /// </summary>
        public ImageSource Image
        {
            get { return (ImageSource)GetValue(ImageProperty); }
            set { SetValue(ImageProperty, value); }
        }

        /// <summary>
        /// Flag to check whether to the progress ring is active or not.
        /// </summary>
        public bool IsLoadPageActive
        {
            get { return (bool)GetValue(IsLoadPageActiveProperty); }
            set { SetValue(IsLoadPageActiveProperty, value); }
        }

        /// <summary>
        /// Horizontal alignment of the control.
        /// </summary>
        public new HorizontalAlignment HorizontalAlignment
        {
            get { return (HorizontalAlignment)GetValue(HorizontalAlignmentProperty); }
            set { SetValue(HorizontalAlignmentProperty, value); }
        }

        /// <summary>
        /// Margin property of the control.
        /// </summary>
        public new Thickness Margin
        {
            get { return (Thickness)GetValue(MarginProperty); }
            set { SetValue(MarginProperty, value); }
        }

        /// <summary>
        /// Height of the control.
        /// </summary>
        public new double Height
        {
            get { return (double)GetValue(HeightProperty); }
            set { SetValue(HeightProperty, value); }
        }

        /// <summary>
        /// Image element of the control.
        /// </summary>
        public Image ImageElement
        {
            get { return pageImage; }
        }

        private void pageImage_ImageOpened(object sender, RoutedEventArgs e)
        {
            Messenger.Default.Send<MessageType>(MessageType.RightPageImageUpdated);
        }
    }
}
