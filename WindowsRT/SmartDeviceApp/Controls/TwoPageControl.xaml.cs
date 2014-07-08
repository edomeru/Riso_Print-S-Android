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
using System.Threading.Tasks;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.Popups;
using Windows.UI.Xaml.Media.Animation;
using SmartDeviceApp.ViewModels;
using Windows.Storage;
using Windows.Storage.Pickers;
using Windows.Graphics.Imaging;
using GalaSoft.MvvmLight.Messaging;

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

        public static readonly DependencyProperty DrawingSurfaceProperty =
            DependencyProperty.Register("DrawingSurface", typeof(ContentControl), typeof(TwoPageControl), null);
        public static readonly DependencyProperty LeftPageImage2Property =
            DependencyProperty.Register("LeftPageImage2", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty IsDuplexProperty =
            DependencyProperty.Register("IsDuplex", typeof(bool), typeof(TwoPageControl), null);

        public static readonly DependencyProperty RightNextPageImageProperty =
            DependencyProperty.Register("RightNextPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty IsLoadRightNextPageActiveProperty =
            DependencyProperty.Register("IsLoadRightNextPageActive", typeof(bool), typeof(TwoPageControl), null);

        public static readonly DependencyProperty LeftNextPageImageProperty =
            DependencyProperty.Register("LeftNextPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

        public static readonly DependencyProperty IsLoadLeftNextPageActiveProperty =
            DependencyProperty.Register("IsLoadLeftNextPageActive", typeof(bool), typeof(TwoPageControl), null);

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

        public ImageSource LeftPageImage2
        {
            get { return (ImageSource)GetValue(LeftPageImage2Property); }
            set { SetValue(LeftPageImage2Property, value); }
        }

        public bool IsDuplex
        {
            get { return (bool)GetValue(IsDuplexProperty); }
            set { SetValue(IsDuplexProperty, value); }
        }

        public ImageSource RightNextPageImage
        {
            get { return (ImageSource)GetValue(RightNextPageImageProperty); }
            set { SetValue(RightNextPageImageProperty, value); }
        }

        public bool IsLoadRightNextPageActive
        {
            get { return (bool)GetValue(IsLoadRightNextPageActiveProperty); }
            set { SetValue(IsLoadRightNextPageActiveProperty, value); }
        }

        public ImageSource LeftNextPageImage
        {
            get { return (ImageSource)GetValue(LeftNextPageImageProperty); }
            set { SetValue(LeftNextPageImageProperty, value); }
        }

        public bool IsLoadLeftNextPageActive
        {
            get { return (bool)GetValue(IsLoadLeftNextPageActiveProperty); }
            set { SetValue(IsLoadLeftNextPageActiveProperty, value); }
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
                    control.leftDisplay.Visibility = Visibility.Collapsed;
                    control.leftDisplayArea.Width = gridLengthCollapsed;
                    control.rightDisplayArea.Width = gridLengthFull;
                    control.topDisplay.Visibility = Visibility.Collapsed;
                    control.topDisplayArea.Height = gridLengthCollapsed;
                    control.bottomDisplayArea.Height = gridLengthFull;

                    control.leftPage.Visibility = Visibility.Collapsed;
                    control.leftPageArea.Width = gridLengthCollapsed;
                    control.rightPageArea.Width = gridLengthFull;
                    control.topPage.Visibility = Visibility.Collapsed;
                    control.topPageArea.Height = gridLengthCollapsed;
                    control.bottomPageArea.Height = gridLengthFull;

                    control.leftTrans.Visibility = Visibility.Collapsed;
                    control.leftTransitionArea.Width = gridLengthCollapsed;
                    control.rightTransitionArea.Width = gridLengthFull;
                    control.topTrans.Visibility = Visibility.Collapsed;
                    control.topTransitionArea.Height = gridLengthCollapsed;
                    control.bottomTransitionArea.Height = gridLengthFull;

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

                    control.leftDisplay.Visibility = Visibility.Visible;
                    control.leftDisplayArea.Width = gridLengthFull;
                    control.rightDisplayArea.Width = gridLengthFull;
                    control.topDisplay.Visibility = Visibility.Collapsed;
                    control.topDisplayArea.Height = gridLengthCollapsed;
                    control.bottomDisplayArea.Height = gridLengthFull;

                    control.leftTrans.Visibility = Visibility.Visible;
                    control.leftTransitionArea.Width = gridLengthFull;
                    control.rightTransitionArea.Width = gridLengthFull;
                    control.topTrans.Visibility = Visibility.Collapsed;
                    control.topTransitionArea.Height = gridLengthCollapsed;
                    control.bottomTransitionArea.Height = gridLengthFull;

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

                    control.leftDisplay.Visibility = Visibility.Collapsed;
                    control.leftDisplayArea.Width = gridLengthCollapsed;
                    control.rightDisplayArea.Width = gridLengthFull;
                    control.topDisplay.Visibility = Visibility.Visible;
                    control.topDisplayArea.Height = gridLengthFull;
                    control.bottomDisplayArea.Height = gridLengthFull;

                    control.leftTrans.Visibility = Visibility.Collapsed;
                    control.leftTransitionArea.Width = gridLengthCollapsed;
                    control.rightTransitionArea.Width = gridLengthFull;
                    control.topTrans.Visibility = Visibility.Visible;
                    control.topTransitionArea.Height = gridLengthFull;
                    control.bottomTransitionArea.Height = gridLengthFull;
                    
                    // Dash lines
                    control.horizontalSeparator.Visibility = Visibility.Visible;
                    control.verticalSeparator.Visibility = Visibility.Collapsed;

                    break;
                }
            }
        }

        public Grid DisplayAreaGrid
        {
            get { return displayAreaGrid; }
        }

        public Grid TransitionGrid
        {
            get { return transitionGrid; }
        }

        public Grid ManipulationGrid
        {
            get { return manipulationGrid; }
        }

        public TranslateTransform Page1TranslateTransform
        {
            get { return Page1ClipTranslateTransform; }
        }

        public RotateTransform Page1RotateTransform
        {
            get { return Page1ClipRotateTransform; }
        }

        public TranslateTransform Page2TranslateTransform
        {
            get { return Page2ClipTranslateTransform; }
        }

        public RotateTransform Page2RotateTransform
        {
            get { return Page2ClipRotateTransform; }
        }

        public TranslateTransform TransitionTranslateTransform
        {
            get { return TransitionGridClipTranslateTransform; }
        }

        public RotateTransform TransitionRotateTransform
        {
            get { return TransitionGridClipRotateTransform; }
        }

        public CompositeTransform TransitionContainerTransform
        {
            get { return TransitionGridContainerTransform; }
        }

    }
}
