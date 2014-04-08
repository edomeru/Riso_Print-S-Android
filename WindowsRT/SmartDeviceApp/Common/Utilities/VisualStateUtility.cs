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
        #region AppViewState

        public static readonly DependencyProperty AppViewStateProperty =
            DependencyProperty.RegisterAttached("AppViewState", typeof(AppViewMode), typeof(VisualStateUtility),
            new PropertyMetadata(null, GoToViewState));

        public static void SetAppViewState(DependencyObject obj, AppViewMode value)
        {
            obj.SetValue(AppViewStateProperty, value);
        }

        public static AppViewMode GetAppViewState(DependencyObject obj)
        {
            return (AppViewMode)obj.GetValue(AppViewStateProperty);
        }

        private static void GoToViewState(DependencyObject obj, DependencyPropertyChangedEventArgs e)
        {
            if (e.NewValue != null)
            {
                switch ((AppViewMode)e.NewValue)
                {
                    case AppViewMode.MainMenuPaneVisible:
                    {
                        VisualStateManager.GoToState((PageBase)obj, "MainMenuPaneVisibleState", true);
                        break;
                    }

                    case AppViewMode.PrintPreviewPageFullScreen:
                    case AppViewMode.PrintersPageFullScreen:
                    case AppViewMode.JobsPageFullScreen:
                    case AppViewMode.SettingsPageFullScreen:
                    {
                        VisualStateManager.GoToState((PageBase)obj, "FullScreenState", true);
                        break;
                    }
                    case AppViewMode.RightPaneVisible:
                    {
                        VisualStateManager.GoToState((PageBase)obj, "RightPaneVisibleState", true);
                        break;
                    }
                    case AppViewMode.RightPaneVisible_ResizedView:
                    {
                        VisualStateManager.GoToState((PageBase)obj, "RightPaneVisible_ResizedViewState", true);
                        break;
                    }
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
