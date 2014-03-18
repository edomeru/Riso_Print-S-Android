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
        public PageControl()
        {
            this.InitializeComponent();
        }

        public static new readonly DependencyProperty VisibilityProperty = 
            DependencyProperty.Register("Visibility", typeof(Visibility), typeof(PageControl), 
            new PropertyMetadata(Visibility.Visible, new PropertyChangedCallback(SetVisibility)));

        public static readonly DependencyProperty ImageProperty =
            DependencyProperty.Register("Image", typeof(ImageSource), typeof(PageControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetImage)));

        public static new readonly DependencyProperty HorizontalAlignmentProperty =
            DependencyProperty.Register("HorizontalAlignment", typeof(HorizontalAlignment), typeof(PageControl),
            new PropertyMetadata(HorizontalAlignment.Center, new PropertyChangedCallback(SetHorizontalAlignment)));

        public static new readonly DependencyProperty MarginProperty =
            DependencyProperty.Register("Margin", typeof(Thickness), typeof(PageControl),
            new PropertyMetadata(new Thickness(0,0,0,0), new PropertyChangedCallback(SetMargin)));

        public new Visibility Visibility
        {
            get { return (Visibility)GetValue(VisibilityProperty); }
            set { SetValue(VisibilityProperty, value); }
        }

        public ImageSource Image
        {
            get { return (ImageSource)GetValue(ImageProperty); }
            set { SetValue(ImageProperty, value); }
        }

        public new HorizontalAlignment HorizontalAlignment
        {
            get { return (HorizontalAlignment)GetValue(HorizontalAlignmentProperty); }
            set { SetValue(HorizontalAlignmentProperty, value); }
        }

        public new Thickness Margin
        {
            get { return (Thickness)GetValue(MarginProperty); }
            set { SetValue(MarginProperty, value); }
        }

        public new double Height
        {
            get { return (double)GetValue(HeightProperty); }
            set { SetValue(HeightProperty, value); }
        }

        #region PRIVATE METHODS

        private static void SetVisibility(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((Visibility)e.NewValue == Visibility.Visible)
            {
                ((PageControl)obj).pageGrid.Visibility = Visibility.Visible;
            }
            else 
            {
                ((PageControl)obj).pageGrid.Visibility = Visibility.Collapsed;
            }
        }

        private static void SetImage(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((BitmapImage)e.NewValue != null)
            {
                ((PageControl)obj).pageImage.Source = (ImageSource)e.NewValue;
            }
        }

        private static void SetHorizontalAlignment(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ((PageControl)obj).pageGrid.HorizontalAlignment = (HorizontalAlignment)e.NewValue;
        }

        private static void SetMargin(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            ((PageControl)obj).pageGrid.Margin = (Thickness)e.NewValue;
        }

        #endregion
    }
}
