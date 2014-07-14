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
        /// <summary>
        /// Gets and sets the data template for Add Printer Pane
        /// </summary>
        public DataTemplate AddPrinterPaneTemplate { get; set; }
        /// <summary>
        /// Gets and sets the data template for Search Printer Pane
        /// </summary>
        public DataTemplate SearchPrinterPaneTemplate { get; set; }
        /// <summary>
        /// Gets and sets the data template for Default Print Settings Pane
        /// </summary>
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
