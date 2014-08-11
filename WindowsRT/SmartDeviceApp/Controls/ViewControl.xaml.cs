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
using Windows.UI.Xaml.Markup;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Windows.Devices.Sensors;
using Windows.UI.Core;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Converters;

namespace SmartDeviceApp.Controls
{
    [ContentProperty(Name = "Children")]
    public partial class ViewControl : UserControl
    {
        public static readonly DependencyProperty TextProperty =
            DependencyProperty.Register("Text", typeof(string), typeof(ViewControl), null);

        public static readonly DependencyProperty ChildrenProperty = DependencyProperty.Register(
            "Children", typeof(UIElementCollection), typeof(ViewControl), null);

        public static readonly DependencyProperty ChildrenDataContextProperty = 
            DependencyProperty.Register("ChildrenDataContext", typeof(object), typeof(ViewControl), null);

        public static readonly DependencyProperty RightPaneContentProperty =
            DependencyProperty.Register("RightPaneContent", typeof(object), typeof(ViewControl), null);

        public static readonly DependencyProperty RightPaneContentTemplateSelectorProperty =
            DependencyProperty.Register("RightPaneContentTemplateSelector", typeof(DataTemplateSelector), typeof(ViewControl), null);
        
        public static readonly DependencyProperty Button1ImageProperty =
           DependencyProperty.Register("Button1Image", typeof(ImageSource), typeof(ViewControl), null);

        public static readonly DependencyProperty Button1PressedImageProperty =
           DependencyProperty.Register("Button1PressedImage", typeof(ImageSource), typeof(ViewControl), null);
        
        public static readonly DependencyProperty Button2ImageProperty =
           DependencyProperty.Register("Button2Image", typeof(ImageSource), typeof(ViewControl), null);

        public static readonly DependencyProperty Button2PressedImageProperty =
           DependencyProperty.Register("Button2PressedImage", typeof(ImageSource), typeof(ViewControl), null);
        
        public static readonly DependencyProperty Button1VisibilityProperty =
           DependencyProperty.Register("Button1Visibility", typeof(Visibility), typeof(ViewControl), null);
        
        public static readonly DependencyProperty Button2VisibilityProperty =
           DependencyProperty.Register("Button2Visibility", typeof(Visibility), typeof(ViewControl), null);

        private SimpleOrientationSensor _orientationSensor;
        
        /// <summary>
        /// Constructor of ViewControl.
        /// </summary>
        public ViewControl()
        {
            this.InitializeComponent();
            Children = contentGrid.Children;
            Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
            Window.Current.SizeChanged += WindowSizeChanged;
          
            _orientationSensor = SimpleOrientationSensor.GetDefault();
            if (_orientationSensor != null)
            {
                _orientationSensor.OrientationChanged += new TypedEventHandler<SimpleOrientationSensor, SimpleOrientationSensorOrientationChangedEventArgs>(OrientationChanged);
            }

            // Get initial orientation
            ViewModel.ViewOrientation = (Window.Current.Bounds.Width >= Window.Current.Bounds.Height) ?
                ViewOrientation.Landscape : ViewOrientation.Portrait;
        }

        /// <summary>
        /// Data context of the control.
        /// </summary>
        public ViewControlViewModel ViewModel
        {
            get
            {
                return (ViewControlViewModel)DataContext;
            }
        }

        /// <summary>
        /// Text property of the control.
        /// </summary>
        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        /// <summary>
        /// Children of the control.
        /// </summary>
        public UIElementCollection Children
        {
            get { return (UIElementCollection)GetValue(ChildrenProperty); }
            private set { SetValue(ChildrenProperty, value); }
        }

        /// <summary>
        /// Data context of the children of the control.
        /// </summary>
        public object ChildrenDataContext
        {
            get { return (object)GetValue(ChildrenDataContextProperty); }
            set { SetValue(ChildrenDataContextProperty, value); }
        }

        /// <summary>
        /// Content of the right pane.
        /// </summary>
        public object RightPaneContent
        {
            get { return (object)GetValue(RightPaneContentProperty); }
            set { SetValue(RightPaneContentProperty, value); }
        }

        /// <summary>
        /// Right pane template selector.
        /// </summary>
        public DataTemplateSelector RightPaneContentTemplateSelector
        {
            get { return (DataTemplateSelector)GetValue(RightPaneContentTemplateSelectorProperty); }
            set { SetValue(RightPaneContentTemplateSelectorProperty, value); }
        }

        /// <summary>
        /// Imagesource for button1.
        /// </summary>
        public ImageSource Button1Image
        {
            get { return (ImageSource)GetValue(Button1ImageProperty); }
            set { SetValue(Button1ImageProperty, value); }
        }

        /// <summary>
        /// Imagesource for the pressed state of button1.
        /// </summary>
        public ImageSource Button1PressedImage
        {
            get { return (ImageSource)GetValue(Button1PressedImageProperty); }
            set { SetValue(Button1PressedImageProperty, value); }
        }

        /// <summary>
        /// Imagesource for button2.
        /// </summary>
        public ImageSource Button2Image
        {
            get { return (ImageSource)GetValue(Button2ImageProperty); }
            set { SetValue(Button2ImageProperty, value); }
        }

        /// <summary>
        /// Imagesource for the pressed state of button2.
        /// </summary>
        public ImageSource Button2PressedImage
        {
            get { return (ImageSource)GetValue(Button2PressedImageProperty); }
            set { SetValue(Button2PressedImageProperty, value); }
        }

        /// <summary>
        /// Visibility property for button1.
        /// </summary>
        public Visibility Button1Visibility
        {
            get { return (Visibility)GetValue(Button1VisibilityProperty); }
            set { SetValue(Button1VisibilityProperty, value); }
        }

        /// <summary>
        /// Visibility property for button2.
        /// </summary>
        public Visibility Button2Visibility
        {
            get { return (Visibility)GetValue(Button2VisibilityProperty); }
            set { SetValue(Button2VisibilityProperty, value); }
        }

        /// <summary>
        /// Width property for button1.
        /// </summary>
        public double Button1Width { get; private set; }

        /// <summary>
        /// Width property for mainMenuButton
        /// </summary>
        public double MainMenuButtonWidth { get; private set; }

        /// <summary>
        /// On loaded event
        /// </summary>
        /// <param name="sender">sender</param>
        /// <param name="e">event argument</param>
        private void OnLoaded(object sender, RoutedEventArgs e)
        {
            ResizeTitleTextWidth(ViewModel.ViewMode);
        }

        /// <summary>
        /// Handle view mode updates
        /// </summary>
        /// <param name="viewMode">current view mode</param>
        private void SetViewMode(ViewMode viewMode)
        {
            if (viewMode == ViewMode.FullScreen)
            {
                mainMenuButton.IsChecked = false;
                if (button1.Visibility == Visibility.Visible) button1.IsChecked = false;
                if (button2.Visibility == Visibility.Visible) button2.IsChecked = false;
            }

            if (viewMode != ViewMode.Unknown)
            {
                ResizeTitleTextWidth(viewMode);
            }
        }

        /// <summary>
        /// Resizes the title text width based on screen size
        /// </summary>
        /// <param name="viewMode">current view mode</param>
        private void ResizeTitleTextWidth(ViewMode viewMode)
        {
            if (Visibility == Visibility.Collapsed) return;

            // Adjust widths
            var defaultMargin = (double)Application.Current.Resources["MARGIN_Default"];
            var rightPaneWidth = (double)Application.Current.Resources["SIZE_SidePaneWidthWithBorder"];

            var maxTextWidth = (int)Window.Current.Bounds.Width;

            // Left and right margins
            maxTextWidth -= ((int)defaultMargin * 2);

            // Main menu button is always visible
            {
                MainMenuButtonWidth = (int)mainMenuButton.ActualWidth;
                if (MainMenuButtonWidth == 0) MainMenuButtonWidth = ImageConstant.GetIconImageWidth(this, true);
                maxTextWidth -= (int)MainMenuButtonWidth;
                maxTextWidth -= (int)defaultMargin;
            }
            // Button1 is visible
            if (Button1Visibility == Visibility.Visible)
            {
                Button1Width = (int)button1.ActualWidth;
                if (Button1Width == 0) Button1Width = ImageConstant.GetIconImageWidth(this, true);
                maxTextWidth -= (int)Button1Width;
                maxTextWidth -= (int)defaultMargin;
            }
            // Button2 is visible
            if (Button2Visibility == Visibility.Visible)
            {
                var imageWidth = (int)button2.ActualWidth;
                if (imageWidth == 0) imageWidth = ImageConstant.GetIconImageWidth(this, true);
                maxTextWidth -= imageWidth;
                maxTextWidth -= (int)defaultMargin;
            }

            // RightPaneVisible_ResizeWidth view mode
            if (viewMode == ViewMode.RightPaneVisible_ResizedWidth)
            {
                maxTextWidth -= (int)rightPaneWidth;
            }

            viewTitle.Width = maxTextWidth;
            viewTitle.MaxWidth = maxTextWidth;
        }

        /// <summary>
        /// Show fullscreen when area outside side panes are tapped
        /// </summary>
        private void OnViewRootTapped(object sender, TappedRoutedEventArgs e)
        {
            switch (ViewModel.ViewMode)
            {
                case ViewMode.MainMenuPaneVisible:
                    {
                        ViewModel.ViewMode = ViewMode.FullScreen;
                        break;
                    }

                case ViewMode.FullScreen:
                    {
                        // Do nothing
                        break;
                    }

                case ViewMode.RightPaneVisible:
                case ViewMode.RightPaneVisible_ResizedWidth:
                    {
                        if (!ViewModel.TapHandled)
                        {
                            ViewModel.ViewMode = ViewMode.FullScreen;
                            ViewModel.IsPane1Visible = false;
                            ViewModel.IsPane2Visible = false;
                        }
                        else
                        {
                            ViewModel.TapHandled = false;
                        }
                        break;
                    }
            }
        }

        private void OnMainMenuButtonTapped(object sender, TappedRoutedEventArgs e)
        {
            e.Handled = true;
        }

        private void OnButton1Tapped(object sender, TappedRoutedEventArgs e)
        {
            e.Handled = true;
            if (ViewModel.ScreenMode == ScreenMode.PrintPreview && !(new ViewModelLocator().PrintSettingsPaneViewModel.IsEnabled))
            {
                ((ToggleButton)sender).IsChecked = false;
            }
        }

        private void OnButton2Tapped(object sender, TappedRoutedEventArgs e)
        {
            e.Handled = true;
        }

        /// <summary>
        /// Triggered when the window is snapped, reduced to a partial screen view by another app,
        /// or changed to full screen from a partial screen view
        /// </summary>
        private void WindowSizeChanged(object sender, Windows.UI.Core.WindowSizeChangedEventArgs e)
        {
            // Workaround to reset the view mode to force recalculation 
            // of the view root width in ResizedViewWidthConverter
            var prevViewMode = ViewModel.ViewMode;
            ViewModel.ViewMode = ViewMode.Unknown;
            ViewModel.ViewMode = prevViewMode;
        }

        async private void OrientationChanged(object sender, SimpleOrientationSensorOrientationChangedEventArgs e)
        {
            await Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {
                DisplayOrientation(e.Orientation);
            });
        }

        private void DisplayOrientation(SimpleOrientation orientation)
        {        
            var viewOrientation = ViewModel.ViewOrientation; // Initialize to previous value
            switch (orientation)
            {
                case SimpleOrientation.NotRotated:
                case SimpleOrientation.Rotated180DegreesCounterclockwise:
                    viewOrientation = ViewOrientation.Landscape;
                    break;
                case SimpleOrientation.Rotated90DegreesCounterclockwise:
                case SimpleOrientation.Rotated270DegreesCounterclockwise:
                    viewOrientation = ViewOrientation.Portrait;
                    break;
                default:
                    break;
            }
            ViewModel.ViewOrientation = viewOrientation;
        }
    }
}
