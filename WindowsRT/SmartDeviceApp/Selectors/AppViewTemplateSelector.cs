using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceApp.Selectors
{
    public class AppViewTemplateSelector : DataTemplateSelector
    {
        public DataTemplate PrintPreviewPageTemplate { get; set; }
        public DataTemplate PrintersPageTemplate { get; set; }
        private DataTemplate _previousTemplate;

        protected override DataTemplate SelectTemplateCore(object item, DependencyObject container)
        {
            AppViewMode appViewMode;
            if (item != null)
            {
                var success = Enum.TryParse<AppViewMode>(item.ToString(), out appViewMode);
                if (success)
                {
                    switch (appViewMode)
                    {
                        case AppViewMode.MainMenuPaneVisible:
                        case AppViewMode.RightPaneVisible:
                        case AppViewMode.RightPaneVisible_ResizedView:
                        {
                            if (_previousTemplate != null)
                            {
                                return _previousTemplate;
                            }
                            return null; // TODO: Make sure this is not possible!!!
                        }
                        case AppViewMode.PreviewViewFullScreen:
                        {
                            _previousTemplate = PrintPreviewPageTemplate;
                            return PrintPreviewPageTemplate;
                        }
                        case AppViewMode.PrintersViewFullScreen:
                        {
                            _previousTemplate = PrintersPageTemplate;
                            return PrintersPageTemplate;
                        }
                    }
                }
            }
            return null;
        }
    }
}
