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
    public class PrintersRightPaneTemplateSelector : DataTemplateSelector
    {
        public DataTemplate AddPrinterPaneTemplate { get; set; }
        public DataTemplate SearchPrinterPaneTemplate { get; set; }
        public DataTemplate PrintSettingsPaneTemplate { get; set; }

        protected override DataTemplate SelectTemplateCore(object item, DependencyObject container)
        {
            PrintersRightPaneMode printersRightPaneMode;
            DataTemplate template = null;
            if (item != null)
            {
                var success = Enum.TryParse<PrintersRightPaneMode>(item.ToString(), out printersRightPaneMode);
                if (success)
                {
                    switch (printersRightPaneMode)
                    {
                        case PrintersRightPaneMode.AddPrinter:
                            template = AddPrinterPaneTemplate;
                            break;
                        case PrintersRightPaneMode.SearchPrinter:
                            template = SearchPrinterPaneTemplate;
                            break;
                        case PrintersRightPaneMode.PrintSettings:
                            template = PrintSettingsPaneTemplate;
                            break;
                    }
                }
            }
            return template;
        }
    }
}
