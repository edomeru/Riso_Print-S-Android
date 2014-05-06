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
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Common.Enum;

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
        
        public ViewControl()
        {
            this.InitializeComponent();
            Children = contentGrid.Children;
            Messenger.Default.Register<ViewMode>(this, (viewMode) => SetViewMode(viewMode));
        }

        public ViewControlViewModel ViewModel
        {
            get
            {
                return (ViewControlViewModel)DataContext;
            }
        }

        public string Text
        {
            get { return (string)GetValue(TextProperty); }
            set { SetValue(TextProperty, value); }
        }

        public UIElementCollection Children
        {
            get { return (UIElementCollection)GetValue(ChildrenProperty); }
            private set { SetValue(ChildrenProperty, value); }
        }

        public object ChildrenDataContext
        {
            get { return (object)GetValue(ChildrenDataContextProperty); }
            set { SetValue(ChildrenDataContextProperty, value); }
        }

        public object RightPaneContent
        {
            get { return (object)GetValue(RightPaneContentProperty); }
            set { SetValue(RightPaneContentProperty, value); }
        }

        public DataTemplateSelector RightPaneContentTemplateSelector
        {
            get { return (DataTemplateSelector)GetValue(RightPaneContentTemplateSelectorProperty); }
            set { SetValue(RightPaneContentTemplateSelectorProperty, value); }
        }

        public ImageSource Button1Image
        {
            get { return (ImageSource)GetValue(Button1ImageProperty); }
            set { SetValue(Button1ImageProperty, value); }
        }

        public ImageSource Button1PressedImage
        {
            get { return (ImageSource)GetValue(Button1PressedImageProperty); }
            set { SetValue(Button1PressedImageProperty, value); }
        }

        public ImageSource Button2Image
        {
            get { return (ImageSource)GetValue(Button2ImageProperty); }
            set { SetValue(Button2ImageProperty, value); }
        }

        public ImageSource Button2PressedImage
        {
            get { return (ImageSource)GetValue(Button2PressedImageProperty); }
            set { SetValue(Button2PressedImageProperty, value); }
        }

        public Visibility Button1Visibility
        {
            get { return (Visibility)GetValue(Button1VisibilityProperty); }
            set { SetValue(Button1VisibilityProperty, value); }
        }

        public Visibility Button2Visibility
        {
            get { return (Visibility)GetValue(Button2VisibilityProperty); }
            set { SetValue(Button2VisibilityProperty, value); }
        }

        private void SetViewMode(ViewMode viewMode)
        {
            if (viewMode == ViewMode.FullScreen)
            {
                mainMenuButton.IsChecked = false;
                if (button1.Visibility == Visibility.Visible) button1.IsChecked = false;
                if (button2.Visibility == Visibility.Visible) button2.IsChecked = false;
            }
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
        }

        private void OnButton2Tapped(object sender, TappedRoutedEventArgs e)
        {
            e.Handled = true;
        }
    }
}
