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
using SmartDeviceApp.Common.Enum;

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
            var control = (TwoPageControl)obj;
            var gridLengthCollapsed = new GridLength(0);
            var gridLengthFull = new GridLength(1, GridUnitType.Star);
            var gridWidth = new GridLength(control.PageAreaSize.Width);
            var gridHeight = new GridLength(control.PageAreaSize.Height);

            switch((PageViewMode)e.NewValue)
            {
                case PageViewMode.SinglePageView:
                {
                    control.leftPage.Visibility = Visibility.Collapsed;
                    control.leftPageArea.Width = gridLengthCollapsed;
                    control.rightPageArea.Width = gridLengthFull;
                    control.topPage.Visibility = Visibility.Collapsed;
                    control.topPageArea.Height = gridLengthCollapsed;
                    control.bottomPageArea.Height = gridLengthFull;
                    break;
                }
                case PageViewMode.TwoPageViewHorizontal:
                {
                    control.leftPage.Visibility = Visibility.Visible;
                    control.leftPageArea.Width = gridWidth;
                    control.rightPageArea.Width = gridWidth;
                    control.topPage.Visibility = Visibility.Collapsed;
                    control.topPageArea.Height = gridLengthCollapsed;
                    control.bottomPageArea.Height = gridLengthFull;
                    break;
                }
                case PageViewMode.TwoPageViewVertical:
                {
                    control.leftPage.Visibility = Visibility.Collapsed;
                    control.leftPageArea.Width = gridLengthCollapsed;
                    control.rightPageArea.Width = gridLengthFull;
                    control.topPage.Visibility = Visibility.Visible;          
                    control.topPageArea.Height = gridHeight;
                    control.bottomPageArea.Height = gridHeight;                    
                    break;
                }
            }
        }
    }
}
