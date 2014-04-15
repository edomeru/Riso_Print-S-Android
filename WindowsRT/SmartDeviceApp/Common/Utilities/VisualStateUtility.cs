using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using SmartDeviceApp.Views;
using SmartDeviceApp.Common.Base;
using SmartDeviceApp.Controls;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Common.Utilities
{
    public class VisualStateUtility : DependencyObject
    {
        #region ViewState

        public static readonly DependencyProperty ViewStateProperty =
            DependencyProperty.RegisterAttached("ViewState", typeof(ViewMode), typeof(VisualStateUtility),
            new PropertyMetadata(null, GoToViewState));

        public static void SetViewState(DependencyObject obj, ViewMode value)
        {
            obj.SetValue(ViewStateProperty, value);
        }

        public static ViewMode GetViewState(DependencyObject obj)
        {
            return (ViewMode)obj.GetValue(ViewStateProperty);
        }

        private static void GoToViewState(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue != null)
            {
                switch ((ViewMode)e.NewValue)
                {
                    case ViewMode.MainMenuPaneVisible:
                        VisualStateManager.GoToState((UserControl)obj, "MainMenuPaneVisibleState", true);
                        break;
                    case ViewMode.FullScreen:
                        VisualStateManager.GoToState((UserControl)obj, "FullScreen", true);
                        break;
                    case ViewMode.RightPaneVisible_ResizedWidth:
                        VisualStateManager.GoToState((UserControl)obj, "RightPaneVisible_ResizedViewState", true);
                        break;
                    case ViewMode.RightPaneVisible:
                        VisualStateManager.GoToState((UserControl)obj, "RightPaneVisibleState", true);
                        break;
                }
            }
        }

        #endregion

        #region PrintSettingsPaneMode

        public static readonly DependencyProperty PrintSettingsPaneStateProperty =
            DependencyProperty.RegisterAttached("PrintSettingsPaneState", typeof(PrintSettingsPaneMode), typeof(VisualStateUtility),
            new PropertyMetadata(null, GoToPrintSettingsPaneState));

        public static void SetPrintSettingsPaneState(DependencyObject obj, PrintSettingsPaneMode value)
        {
            obj.SetValue(PrintSettingsPaneStateProperty, value);
        }

        public static PrintSettingsPaneMode GetPrintSettingsPaneState(DependencyObject obj)
        {
            return (PrintSettingsPaneMode)obj.GetValue(PrintSettingsPaneStateProperty);
        }

        private static void GoToPrintSettingsPaneState(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue != null)
            {
                switch ((PrintSettingsPaneMode)e.NewValue)
                {
                    case PrintSettingsPaneMode.PrintSettings:
                    {
                        VisualStateManager.GoToState((UserControl)obj, "PrintSettingsVisibleState", true);
                        break;
                    }

                    case PrintSettingsPaneMode.PrintSettingOptions:
                    {
                        VisualStateManager.GoToState((UserControl)obj, "PrintSettingOptionsVisibleState", true);
                        break;
                    }
                }
            }
        }

        #endregion
    }
}
