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

            //Messenger.Default.Register<MessageType>(this, (msg) => getScreenshot(msg));
            ////Messenger.Default.Register<ViewMode>(this, (viewMode) => grabScreenImage(viewMode));
            //rightPage.ImageElement.ImageOpened += setRightPageImageOpened;
            //leftPage.ImageElement.ImageOpened += setLeftPageImageOpened;
            //getScreenImage();

            
        }

        private void setLeftPageImageOpened(object sender, RoutedEventArgs e)
        {
            //_isLeftImageOpened = true;
            System.Diagnostics.Debug.WriteLine("Right Image Opened");
            getScreenImage();

        }

        private void setRightPageImageOpened(object sender, RoutedEventArgs e)
        {
            //_isRightImageOpened = true;
            System.Diagnostics.Debug.WriteLine("Right Image Opened");
            //getScreenImage();

        }

        private async void getScreenImage()
        {
            var rtb = new RenderTargetBitmap();
            await rtb.RenderAsync(PageAreaGrid);
            TransitionImage.Source = rtb;
            await Task.Delay(40000);
        }


        //private void getScreenshot(MessageType msg)
        //{
        //    IsDuplex = false;
        //    if (pageAreaGrid.ActualWidth > rightPage.ActualWidth)
        //    {
        //        IsDuplex = true;
        //    }

        //    if (IsDuplex)
        //    {
        //        //check both
        //        if (msg == MessageType.RightPageImageUpdated)
        //        {
        //            System.Diagnostics.Debug.WriteLine("Duplex");
        //            getScreenImage();
        //        }
        //    }
        //    else
        //    {
        //        if (msg == MessageType.RightPageImageUpdated)
        //        {
        //            System.Diagnostics.Debug.WriteLine("Single");
        //            getScreenImage();

        //            //save image
        //            //var filePicker = new FileSavePicker();
        //            //filePicker.FileTypeChoices.Add("Raw Images", new List<string> { ".raw", ".dat" });
        //            //filePicker.FileTypeChoices.Add(".jpg Image", new List<string> { ".jpg" });
        //            //var file = await filePicker.PickSaveFileAsync();

        //            //var renderTargetBitmap = new RenderTargetBitmap();

        //            //var pixelBuffer = await rtb.GetPixelsAsync();

        //            //using (var stream = await file.OpenAsync(FileAccessMode.ReadWrite))
        //            //{
        //            //    var encoder = await BitmapEncoder.CreateAsync(BitmapEncoder.PngEncoderId, stream);
        //            //    encoder.SetPixelData(
        //            //        BitmapPixelFormat.Bgra8,
        //            //        BitmapAlphaMode.Ignore,
        //            //        (uint)rtb.PixelWidth,
        //            //        (uint)rtb.PixelHeight, 96d, 96d,
        //            //        pixelBuffer.ToArray());

        //            //    await encoder.FlushAsync();
        //            //} 

        //        }
        //    }
        //}

        //private async Task PreloadTransitionGridContentAsync()
        //{
        //    #region Waiting for page 2 content to load
        //    //var bi = Page2SampleContentImage.Source as BitmapImage;

        //    //if (bi.PixelWidth == 0)
        //    //{
        //    //    bi.ImageFailed += (s, e) => new MessageDialog("Need a different sample image.").ShowAsync();
        //    //    bi.ImageOpened += (s, e) => PreloadTransitionGridContentAsync();
        //    //    return;
        //    //}

        //    //if (PageAreaGrid.ActualWidth == 0)
        //    //{
        //    //    SizeChangedEventHandler sizeChangedEventHandler = null;
        //    //    sizeChangedEventHandler = (s, e) =>
        //    //    {
        //    //        PreloadTransitionGridContentAsync();
        //    //        PageAreaGrid.SizeChanged -= sizeChangedEventHandler;
        //    //    };

        //    //    PageAreaGrid.SizeChanged += sizeChangedEventHandler;

        //    //    return;
        //    //}
        //    #endregion

        //    var rtb = new RenderTargetBitmap();
        //    await rtb.RenderAsync(PageAreaGrid);
        //    TransitionImage.Source = rtb;


        //    await Task.Delay(40000);
        //}

        //private bool isCancellationRequested;

        //private enum FlipDirections
        //{
        //    Left,
        //    Right
        //}

        //private FlipDirections flipDirection;
        //private Point manipulationStartPosition;
        //private double rotationCenterX;
        //private double rotationCenterY;
        //private bool _backCurl;
        //private bool _willContinue;

        //private void ManipulationGrid_OnManipulationStarted(object sender, ManipulationStartedRoutedEventArgs e)
        //{
        //    if (TransitionImage.Source == null)
        //    {
        //        CancelManipulation(e);
        //        return;
        //    }

        //    manipulationStartPosition = e.Position;

        //    _backCurl = false;
        //    var startOfBackCurlPosition = 0.0;
        //    if (pageAreaGrid.ActualWidth > rightPage.ActualWidth)
        //    {
        //        startOfBackCurlPosition = ManipulationGrid.ActualWidth * 0.5;
        //    }
        //    else
        //    {
        //        startOfBackCurlPosition = (ManipulationGrid.ActualWidth * 0.25);
        //    }

        //    if (manipulationStartPosition.X < startOfBackCurlPosition)
        //    {
        //        //use page1 clip transition
        //        _backCurl = true;
        //        //Page1ClipTranslateTransform.X = 0;
        //        //TransitionGridClipTranslateTransform.X = -80000;
        //        //TransitionGridContainerTransform.TranslateX = 0;
        //        //TransitionGrid.Opacity = .975;

        //        //var rtb = new RenderTargetBitmap();


        //        ////Page1ClipScaleTransform.ScaleX = -1;
        //        //rtb.RenderAsync(Page1ContentGrid);
        //        //TransitionImage.Source = rtb;
        //        //Page1ClipScaleTransform.ScaleX = 1;

        //        //TrainsitionGridClipScaleTransform.ScaleX = -1;
        //        //TransitionImage.FlowDirection = Windows.UI.Xaml.FlowDirection.RightToLeft;
        //    }

        //    var viewModel = new ViewModelLocator().PrintPreviewViewModel;
        //    if (true)//check if flip left or flip right
        //    {
        //        flipDirection = FlipDirections.Left;
        //        Page2ClipTranslateTransform.X = PageAreaGrid.ActualWidth; //get Page2ClipTranslateTransform and put in XAML.
        //        //Page2.Opacity = 1;
        //        //PageAreaGrid.Opacity = 1;
        //        TransitionGridClipTranslateTransform.X = -80000;
        //        TransitionGridContainerTransform.TranslateX = PageAreaGrid.ActualWidth;
        //        TransitionGrid.Opacity = .975;
        //    }
        //    else
        //    {
        //        if (manipulationStartPosition.X >= this.PageAreaGrid.ActualWidth)// /2 //single view
        //        {
        //            // Can't flip left since there is no page after the current one
        //            CancelManipulation(e);
        //            return;
        //        }

        //        flipDirection = FlipDirections.Right;

        //        //Page1.Opacity = 1;
        //    }
        //}

        //private void ManipulationGrid_OnManipulationDelta(object sender, ManipulationDeltaRoutedEventArgs e)
        //{
        //    if (this.isCancellationRequested)
        //    {
        //        return;
        //    }

        //    if (flipDirection == FlipDirections.Left)
        //    {
        //        var w = this.PageAreaGrid.ActualWidth;
        //        var h = this.PageAreaGrid.ActualHeight;

        //        var tempW = 0.0;
        //        if (pageAreaGrid.ActualWidth > rightPage.ActualWidth)
        //        {
        //            tempW = -w;
        //        }
        //        else
        //        {
        //            tempW = -w * 2;
        //        }
                
        //        var cx = Math.Min(0, Math.Max(e.Position.X - w, tempW));
        //        var cy = e.Cumulative.Translation.Y;
        //        var angle = (Math.Atan2(cx + manipulationStartPosition.Y - w, -cy) * 180 / Math.PI + +90) % 360;

        //        this.rotationCenterX = w + cx / 2;/// 2

        //        if (cy < 0)
        //        {
        //            this.rotationCenterY = h;
        //        }
        //        else
        //        {
        //            this.rotationCenterY = 0;
        //        }

        //        Page2ClipTranslateTransform.X = w + cx / 2;/// 2
        //        Page2ClipTranslateTransform.Y = -40000 + h / 2;/// 2
        //        Page2ClipRotateTransform.CenterX = this.rotationCenterX;
        //        Page2ClipRotateTransform.CenterY = this.rotationCenterY;
        //        Page2ClipRotateTransform.Angle = angle;

        //        TransitionGridClipTranslateTransform.X = -80000 - (cx / 2);/// 2
        //        TransitionGridClipTranslateTransform.Y = -40000 + h / 2;/// 2
        //        TransitionGridClipRotateTransform.CenterX = -cx /2;/// 2
        //        TransitionGridClipRotateTransform.CenterY = this.rotationCenterY;
        //        TransitionGridClipRotateTransform.Angle = -angle;

        //        TransitionGridContainerTransform.TranslateX = w + cx;
        //        TransitionGridContainerTransform.CenterX = -cx / 2;
        //        TransitionGridContainerTransform.CenterY = this.rotationCenterY;
        //        TransitionGridContainerTransform.Rotation = 2 * angle;

                

        //        System.Diagnostics.Debug.WriteLine("w: {0} h: {1} cx: {2} cy: {3} angle: {4} rotationCenterX: {5} rotationCenterY: {6}", w, h, cx, cy,angle, rotationCenterX, rotationCenterY);

        //        System.Diagnostics.Debug.WriteLine("Page2ClipTranslateTransform.X: {0} \nPage2ClipTranslateTransform.Y: {1} \nPage2ClipRotateTransform.CenterX: {2} \nPage2ClipRotateTransform.CenterY: {3} \nPage2ClipRotateTransform.Angle: {4}",
        //            Page2ClipTranslateTransform.X,
        //            Page2ClipTranslateTransform.Y,
        //            Page2ClipRotateTransform.CenterX,
        //            Page2ClipRotateTransform.CenterY,
        //            Page2ClipRotateTransform.Angle);

        //        System.Diagnostics.Debug.WriteLine("TransitionGridClipTranslateTransform.X: {0} \nTransitionGridClipTranslateTransform.Y: {1} \nTransitionGridClipRotateTransform.CenterX: {2} \nTransitionGridClipRotateTransform.CenterY: {3} \nTransitionGridClipRotateTransform.Angle: {4}",
        //            TransitionGridClipTranslateTransform.X,
        //            TransitionGridClipTranslateTransform.Y,
        //            TransitionGridClipRotateTransform.CenterX,
        //            TransitionGridClipRotateTransform.CenterY,
        //            TransitionGridClipRotateTransform.Angle);

        //        System.Diagnostics.Debug.WriteLine("TransitionGridContainerTransform.TranslateX: {0} \nTransitionGridContainerTransform.CenterX: {1} \nTransitionGridClipRotateTransform.Centery: {2} \nTransitionGridContainerTransform.Rotation: {3}",
        //            TransitionGridContainerTransform.TranslateX,
        //            TransitionGridContainerTransform.CenterX,
        //            TransitionGridContainerTransform.CenterY,
        //            TransitionGridContainerTransform.Rotation);
        //    }
        //}

        //private void ManipulationGrid_OnManipulationCompleted(object sender, ManipulationCompletedRoutedEventArgs e)
        //{
        //    if (this.isCancellationRequested)
        //    {
        //        this.isCancellationRequested = false;
        //        return;
        //    }

            

        //    var w = this.PageAreaGrid.ActualWidth;
        //    var h = this.PageAreaGrid.ActualHeight;

        //    System.Diagnostics.Debug.WriteLine("Position X: {0}", e.Position.X);

        //    _willContinue = false;
        //    if (_backCurl)
        //    {
        //        if (e.Position.X > w * 0.25)
        //        {
        //            _willContinue = true;
        //        }
        //    }
        //    else
        //    {
        //        if (e.Position.X < w * 0.75)
        //        {
        //            _willContinue = true;
        //        }
        //    }

        //    var sb = new Storyboard();
        //    if (!_backCurl)
        //    {
        //        var to = 0;
        //        if (_willContinue)
        //        {
        //            if (IsDuplex)
        //                to = (int)(w / 2);
        //            else
        //                to = (int)-w;
        //            System.Diagnostics.Debug.WriteLine("Will continue");
        //        }
        //        else
        //        {
        //            to = (int)w;
        //        }
        //        AddAnimation(sb, Page2ClipTranslateTransform, "X", to);
        //        AddAnimation(sb, Page2ClipRotateTransform, "CenterX", 0);
        //        AddAnimation(sb, Page2ClipRotateTransform, "Angle", 0);

        //        if (_willContinue)
        //        {
        //            if (IsDuplex)
        //                to = (int)(-80000 + (w / 2));
        //            else
        //                to = (int)(-80000 + (w));
        //        }
        //        else
        //        {
        //            to = (int)-80000;
        //        }
        //        AddAnimation(sb, TransitionGridClipTranslateTransform, "X", to);
        //        AddAnimation(sb, TransitionGridClipRotateTransform, "CenterX", 0);
        //        AddAnimation(sb, TransitionGridClipRotateTransform, "Angle", 0);
        //        if (_willContinue)
        //        {
        //            if (IsDuplex)
        //                to = 0;
        //            else
        //                to = (int)-w;
        //        }
        //        else
        //        {
        //            to = (int)w;
        //        }
        //        AddAnimation(sb, TransitionGridContainerTransform, "TranslateX", to);
        //        AddAnimation(sb, TransitionGridContainerTransform, "CenterX", 0);
        //        AddAnimation(sb, TransitionGridContainerTransform, "Rotation", 0);
        //        sb.Begin();
        //        TransitionGrid.Opacity = 0;
        //    }
        //    else
        //    {
        //        AddAnimation(sb, Page1ClipTranslateTransform, "X", 0);
        //        AddAnimation(sb, Page1ClipRotateTransform, "CenterX", w / 2);
        //        AddAnimation(sb, Page1ClipRotateTransform, "Angle", 0);
        //        //Page1.Opacity = 1;
        //        //Page2.Opacity = 0;
        //        var to = 0;
        //        if (_willContinue)
        //        {
        //            to = (int)(-80000);
        //        }
        //        else
        //        {
        //            to = (int)0;
        //        }
        //        AddAnimation(sb, TransitionGridClipTranslateTransform, "X", to);
        //        AddAnimation(sb, TransitionGridClipRotateTransform, "CenterX", w / 2);
        //        AddAnimation(sb, TransitionGridClipRotateTransform, "Angle", 0);

        //        AddAnimation(sb, TransitionGridContainerTransform, "TranslateX", to);
        //        AddAnimation(sb, TransitionGridContainerTransform, "CenterX", w / 2);
        //        AddAnimation(sb, TransitionGridContainerTransform, "Rotation", 0);
        //        sb.Begin();
        //    }
        //}

        //private static void AddAnimation(Storyboard sb, DependencyObject dob, string path, double to)
        //{
        //    var da = new DoubleAnimation();
        //    Storyboard.SetTarget(da, dob);
        //    Storyboard.SetTargetProperty(da, path);
        //    da.To = to;
        //    da.Duration = TimeSpan.FromSeconds(.2);
        //    sb.Children.Add(da);
        //}

        //private void CancelManipulation(ManipulationStartedRoutedEventArgs e)
        //{
        //    this.isCancellationRequested = true;
        //    e.Complete();
        //}

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

        public static readonly DependencyProperty LeftNextPageImageProperty =
            DependencyProperty.Register("LeftNextPageImage", typeof(ImageSource), typeof(TwoPageControl), null);

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

        public ImageSource LeftNextPageImage
        {
            get { return (ImageSource)GetValue(LeftNextPageImageProperty); }
            set { SetValue(LeftNextPageImageProperty, value); }
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

                    // Dash lines
                    control.horizontalSeparator.Visibility = Visibility.Collapsed;
                    control.verticalSeparator.Visibility = Visibility.Collapsed;

                    break;
                }
                case PageViewMode.TwoPageViewHorizontal:
                {
                    control.leftDisplay.Visibility = Visibility.Visible;
                    control.topDisplay.Visibility = Visibility.Collapsed;
                    control.leftDisplayArea.Width = gridLengthFull;
                    control.rightDisplayArea.Width = gridLengthFull;


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



        //private void pageAreaScrollViewer_LayoutUpdated(object sender, object e)
        //{
            
        //}

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

        public Image Image
        {
            get { return TransitionImage; }
        }

        public Image DisplayImage
        {
            get { return TransitionDisplayImage; }
        }

        private void pageAreaGrid_LayoutUpdated(object sender, object e)
        {
            System.Diagnostics.Debug.WriteLine("layoutupdated");
        }

        public void SetLeftDisplayInvisible()
        {
            leftDisplay.Opacity = 0.0;
        }

        public void SetLeftPageInvisible()
        {
            leftPage.Opacity = 0.0;
        }

    }
}
