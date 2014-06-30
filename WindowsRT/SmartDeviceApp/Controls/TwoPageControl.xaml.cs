//
//  TwoPageControl.xaml.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/02/25.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

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

        public static readonly DependencyProperty RightBackPageImageProperty =
            DependencyProperty.Register("RightBackPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty LoadRightBackPageActiveProperty =
            DependencyProperty.Register("IsLoadRightBackPageActive", typeof(bool), typeof(TwoPageControl), null);

        public static readonly DependencyProperty LeftBackPageImageProperty =
            DependencyProperty.Register("LeftBackPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty IsLoadLeftBackPageActiveProperty =
            DependencyProperty.Register("IsLoadLeftBackPageActive", typeof(bool), typeof(TwoPageControl), null);

        public static readonly DependencyProperty PageAreaGridProperty =
            DependencyProperty.Register("PageAreaGrid", typeof(Grid), typeof(TwoPageControl), null);
        
        public static readonly DependencyProperty RightPageImageProperty =
            DependencyProperty.Register("RightPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty IsLoadRightPageActiveProperty =
            DependencyProperty.Register("IsLoadRightPageActive", typeof(bool), typeof(TwoPageControl), null);

        public static readonly DependencyProperty LeftPageImageProperty =
            DependencyProperty.Register("LeftPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty IsLoadLeftPageActiveProperty =
            DependencyProperty.Register("IsLoadLeftPageActive", typeof(bool), typeof(TwoPageControl), null);

        public static readonly DependencyProperty PageViewModeProperty =
           DependencyProperty.Register("PageViewMode", typeof(PageViewMode), typeof(TwoPageControl),
           new PropertyMetadata(PageViewMode.SinglePageView, new PropertyChangedCallback(SetPageViewMode)));

        public static readonly DependencyProperty PageAreaSizeProperty =
           DependencyProperty.Register("PageAreaSize", typeof(Size), typeof(TwoPageControl), null);

        public ImageSource RightBackPageImage
        {
            get { return (ImageSource)GetValue(RightBackPageImageProperty); }
            set { SetValue(RightBackPageImageProperty, value); }
        }

        public bool IsLoadRightBackPageActive
        {
            get { return (bool)GetValue(LoadRightBackPageActiveProperty); }
            set { SetValue(LoadRightBackPageActiveProperty, value); }
        }

        public ImageSource LeftBackPageImage
        {
            get { return (ImageSource)GetValue(LeftBackPageImageProperty); }
            set { SetValue(LeftBackPageImageProperty, value); }
        }

        public bool IsLoadLeftBackPageActive
        {
            get { return (bool)GetValue(IsLoadLeftBackPageActiveProperty); }
            set { SetValue(IsLoadLeftBackPageActiveProperty, value); }
        }

        public Grid PageAreaGrid
        {
            get { return pageAreaGrid; }
        }

        public ImageSource RightPageImage
        {
            get { return (ImageSource)GetValue(RightPageImageProperty); }
            set { SetValue(RightPageImageProperty, value); }
        }

        public bool IsLoadRightPageActive
        {
            get { return (bool)GetValue(IsLoadRightPageActiveProperty); }
            set { SetValue(IsLoadRightPageActiveProperty, value); }
        }

        public ImageSource LeftPageImage
        {
            get { return (ImageSource)GetValue(LeftPageImageProperty); }
            set { SetValue(LeftPageImageProperty, value); }
        }

        public bool IsLoadLeftPageActive
        {
            get { return (bool)GetValue(IsLoadLeftPageActiveProperty); }
            set { SetValue(IsLoadLeftPageActiveProperty, value); }
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

                    // Dash lines
                    control.horizontalSeparator.Visibility = Visibility.Collapsed;
                    control.verticalSeparator.Visibility = Visibility.Collapsed;

                    break;
                }
                case PageViewMode.TwoPageViewHorizontal:
                {
                    control.leftPage.Visibility = Visibility.Visible;
                    control.leftPageArea.Width = gridLengthFull;
                    control.rightPageArea.Width = gridLengthFull;
                    control.topPage.Visibility = Visibility.Collapsed;
                    control.topPageArea.Height = gridLengthCollapsed;
                    control.bottomPageArea.Height = gridLengthFull;

                    // Dash lines
                    control.horizontalSeparator.Visibility = Visibility.Collapsed;
                    control.verticalSeparator.Visibility = Visibility.Visible;

                    break;
                }
                case PageViewMode.TwoPageViewVertical:
                {
                    control.leftPage.Visibility = Visibility.Collapsed;
                    control.leftPageArea.Width = gridLengthCollapsed;
                    control.rightPageArea.Width = gridLengthFull;
                    control.topPage.Visibility = Visibility.Visible;
                    control.topPageArea.Height = gridLengthFull;
                    control.bottomPageArea.Height = gridLengthFull;
                    
                    // Dash lines
                    control.horizontalSeparator.Visibility = Visibility.Visible;
                    control.verticalSeparator.Visibility = Visibility.Collapsed;

                    break;
                }
            }
        }
    }
}
