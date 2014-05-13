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
    public class PrintSettingsListTemplateSelector : DataTemplateSelector
    {
        public DataTemplate ListViewItemToggleSwitchTemplate { get; set; }
        public DataTemplate ListViewItemTextBoxTemplate { get; set; }
        public DataTemplate ListViewItemListTemplate { get; set; }

        protected override DataTemplate SelectTemplateCore (object item, DependencyObject container)
        {
            PrintSetting printSetting = item as PrintSetting;
            DataTemplate template = null;
            if (printSetting != null)
            {
                switch (printSetting.Type)
                {
                    case PrintSettingType.boolean:
                        template = ListViewItemToggleSwitchTemplate;
                        break;
                    case PrintSettingType.numeric:
                        template = ListViewItemTextBoxTemplate;
                        break;
                    case PrintSettingType.list:
                        template = ListViewItemListTemplate;
                        break;
                    case PrintSettingType.unknown:
                        template = null;
                        break;
                }
            }
            return template;
        }
    }
}
