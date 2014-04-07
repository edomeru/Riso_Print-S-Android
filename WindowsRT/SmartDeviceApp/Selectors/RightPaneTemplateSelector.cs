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
    public class RightPaneTemplateSelector : DataTemplateSelector
    {
        public DataTemplate PrintSettingsPaneTemplate { get; set; }
        public DataTemplate AddPrinterPaneTemplate { get; set; }
        public DataTemplate SearchPrinterPaneTemplate { get; set; }
        //private DataTemplate _previousTemplate;

        protected override DataTemplate SelectTemplateCore(object item, DependencyObject container)
        {
            RightPaneMode rightPaneMode;
            if (item != null)
            {
                var success = Enum.TryParse<RightPaneMode>(item.ToString(), out rightPaneMode);
                if (success)
                {
                    //switch (rightPaneMode)
                    //{
                    //    // 4/4 TODO
                    //    //case RightPaneMode.PreviewViewFullScreen:
                    //    //{
                    //    //    _previousTemplate = PrintPreviewPageTemplate;
                    //    //    return PrintPreviewPageTemplate;
                    //    //}
                    //    //case RightPaneMode.PrintersViewFullScreen:
                    //    //{
                    //    //    _previousTemplate = PrintersPageTemplate;
                    //    //    return PrintersPageTemplate;
                    //    //}
                    //}

                    return PrintSettingsPaneTemplate;
                }
            }
            return null;
        }
    }
}
