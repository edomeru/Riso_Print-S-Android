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
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

namespace SmartDeviceApp.Controls
{
    public sealed partial class TwoPageControl : UserControl
    {
        public TwoPageControl()
        {
            this.InitializeComponent();
        }

        public static readonly DependencyProperty PageAreaGridProperty =
            DependencyProperty.Register("PageAreaGrid", typeof(Grid), typeof(TwoPageControl), null);
        
        public static readonly DependencyProperty RightPageImageProperty =
            DependencyProperty.Register("RightPageImage", typeof(ImageSource), typeof(TwoPageControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetRightPageImage)));
        
        public static readonly DependencyProperty LeftPageImageProperty =
            DependencyProperty.Register("LeftPageImage", typeof(ImageSource), typeof(TwoPageControl),
            new PropertyMetadata(null, new PropertyChangedCallback(SetLeftPageImage)));

        public static readonly DependencyProperty ViewModeProperty =
           DependencyProperty.Register("ViewMode", typeof(PageViewMode), typeof(TwoPageControl),
           new PropertyMetadata(PageViewMode.SinglePageView, new PropertyChangedCallback(SetViewMode)));

        public Grid PageAreaGrid
        {
            get { return pageAreaGrid; }
        }

        public ImageSource RightPageImage
        {
            get { return (ImageSource)GetValue(RightPageImageProperty); }
            set { SetValue(RightPageImageProperty, value); }
        }

        public ImageSource LeftPageImage
        {
            get { return (ImageSource)GetValue(LeftPageImageProperty); }
            set { SetValue(LeftPageImageProperty, value); }
        }

        public PageViewMode ViewMode
        {
            get { return (PageViewMode)GetValue(ViewModeProperty); }
            set { SetValue(ViewModeProperty, value); }
        }

        private static void SetRightPageImage(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((ImageSource)e.NewValue != null)
            {
                ((TwoPageControl)obj).rightPage.Image = (ImageSource)e.NewValue;
            }
        }

        private static void SetLeftPageImage(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((ImageSource)e.NewValue != null)
            {
                ((TwoPageControl)obj).leftPage.Image = (ImageSource)e.NewValue;
            }
        }

        private static void SetViewMode(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if ((PageViewMode)e.NewValue == PageViewMode.SinglePageView)
            {
                ((TwoPageControl)obj).leftPage.Visibility = Visibility.Collapsed;
                ((TwoPageControl)obj).leftPageArea.Width = new GridLength(0);
                ((TwoPageControl)obj).rightPageArea.Width = new GridLength(1, GridUnitType.Star);
                ((TwoPageControl)obj).rightPage.HorizontalAlignment = HorizontalAlignment.Center;
                ((TwoPageControl)obj).rightPage.Margin = new Thickness(0,0,0,0);
            }
            else if ((PageViewMode)e.NewValue == PageViewMode.TwoPageView)
            {
                ((TwoPageControl)obj).leftPage.Visibility = Visibility.Visible;
                ((TwoPageControl)obj).leftPageArea.Width = new GridLength(1, GridUnitType.Star);
                ((TwoPageControl)obj).rightPageArea.Width = new GridLength(1, GridUnitType.Star);
                ((TwoPageControl)obj).rightPage.HorizontalAlignment = HorizontalAlignment.Left;
                ((TwoPageControl)obj).rightPage.Margin = new Thickness(20, 0, 0, 0);
            }
        }
    }
}
