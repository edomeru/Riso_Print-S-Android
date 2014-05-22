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
            DependencyProperty.Register("RightPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty LeftPageImageProperty =
            DependencyProperty.Register("LeftPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty PageViewModeProperty =
           DependencyProperty.Register("PageViewMode", typeof(PageViewMode), typeof(TwoPageControl),
           new PropertyMetadata(PageViewMode.SinglePageView, new PropertyChangedCallback(SetPageViewMode)));

        public static readonly DependencyProperty PageAreaSizeProperty =
           DependencyProperty.Register("PageAreaSize", typeof(Size), typeof(TwoPageControl), null);

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
		
        public PageViewMode PageViewMode
        {
            get { return (PageViewMode)GetValue(PageViewModeProperty); }
            set { SetValue(PageViewModeProperty, value); }
        }

        public Size PageAreaSize
        {
            get { return (Size)GetValue(PageAreaSizeProperty); }
            set { SetValue(PageAreaSizeProperty, value); }
        }

        private static void SetPageViewMode(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            switch((PageViewMode)e.NewValue)
            {
                case PageViewMode.SinglePageView:
                {
                    ((TwoPageControl)obj).leftPage.Visibility = Visibility.Collapsed;
                    ((TwoPageControl)obj).leftPageArea.Width = new GridLength(0);
                    ((TwoPageControl)obj).topPage.Visibility = Visibility.Collapsed;
                    ((TwoPageControl)obj).topPageArea.Height = new GridLength(0);
                    ((TwoPageControl)obj).rightPageArea.Width = new GridLength(1, GridUnitType.Star);
                    ((TwoPageControl)obj).rightPage.Margin = new Thickness(0,0,0,0);
                    break;
                }
                case PageViewMode.TwoPageViewHorizontal:
                {
                    ((TwoPageControl)obj).leftPage.Visibility = Visibility.Visible;
                    var gridLength = new GridLength(((TwoPageControl)obj).PageAreaSize.Width);
                    ((TwoPageControl)obj).leftPageArea.Width = gridLength;
                    ((TwoPageControl)obj).rightPageArea.Width = gridLength;
                    ((TwoPageControl)obj).topPage.Visibility = Visibility.Collapsed;
                    ((TwoPageControl)obj).topPageArea.Height = new GridLength(0);
                    break;
                }
                case PageViewMode.TwoPageViewVertical:
                {
                    ((TwoPageControl)obj).topPage.Visibility = Visibility.Visible;
                    var gridLength = new GridLength(((TwoPageControl)obj).PageAreaSize.Height);
                    ((TwoPageControl)obj).topPageArea.Height = gridLength;
                    ((TwoPageControl)obj).bottomPageArea.Height = gridLength;
                    ((TwoPageControl)obj).leftPage.Visibility = Visibility.Collapsed;
                    ((TwoPageControl)obj).leftPageArea.Width = new GridLength(0);
                    break;
                }
            }
        }
    }
}
