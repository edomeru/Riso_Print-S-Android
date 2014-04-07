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
        PreviewViewFullScreen,
        PrintersViewFullScreen,
        SettingsViewFullScreen,
        JobsViewFullScreen,
        RightPaneVisible,
        RightPaneVisible_ResizedView, // View will be adjusted to fit width of screen
    }
}
