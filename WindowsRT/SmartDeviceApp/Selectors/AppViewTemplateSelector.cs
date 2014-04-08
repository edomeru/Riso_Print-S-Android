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
        public DataTemplate HomePageTemplate { get; set; }
        public DataTemplate PrintPreviewPageTemplate { get; set; }
        public DataTemplate PrintersPageTemplate { get; set; }
        public DataTemplate JobsPageTemplate { get; set; }
        public DataTemplate SettingsPageTemplate { get; set; }
        public DataTemplate HelpPageTemplate { get; set; }
        public DataTemplate LegalPageTemplate { get; set; }
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
                        case AppViewMode.HomePageFullScreen:
                        {
                            _previousTemplate = HomePageTemplate;
                            return HomePageTemplate;
                        }
                        case AppViewMode.PrintPreviewPageFullScreen:
                        {
                            _previousTemplate = PrintPreviewPageTemplate;
                            return PrintPreviewPageTemplate;
                        }
                        case AppViewMode.PrintersPageFullScreen:
                        {
                            _previousTemplate = PrintersPageTemplate;
                            return PrintersPageTemplate;
                        }
                        case AppViewMode.JobsPageFullScreen:
                        {
                            _previousTemplate = JobsPageTemplate;
                            return JobsPageTemplate;
                        }
                        case AppViewMode.SettingsPageFullScreen:
                        {
                            _previousTemplate = SettingsPageTemplate;
                            return SettingsPageTemplate;
                        }
                        case AppViewMode.HelpPageFullScreen:
                        {
                            _previousTemplate = HelpPageTemplate;
                            return HelpPageTemplate;
                        }
                        case AppViewMode.LegalPageFullScreen:
                        {
                            _previousTemplate = LegalPageTemplate;
                            return LegalPageTemplate;
                        }
                    }
                }
            }
            return null;
        }
    }
}
