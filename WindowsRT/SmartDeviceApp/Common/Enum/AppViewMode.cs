using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SmartDeviceApp.Common.Enum
{
    public enum AppViewMode
    {
        MainMenuPaneVisible,
        HomePageFullScreen,
        PrintPreviewPageFullScreen,
        PrintersPageFullScreen,
        JobsPageFullScreen,
        SettingsPageFullScreen,
        HelpPageFullScreen,
        LegalPageFullScreen,
        RightPaneVisible,
        RightPaneVisible_ResizedView, // View will be adjusted to fit width of screen
    }
}
