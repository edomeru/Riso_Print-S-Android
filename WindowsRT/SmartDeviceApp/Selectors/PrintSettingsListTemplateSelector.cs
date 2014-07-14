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
        /// <summary>
        /// Gets and sets the data template for print setting with toggle switch
        /// </summary>
        public DataTemplate ListViewItemToggleSwitchTemplate { get; set; }
        /// <summary>
        /// Gets and sets the data template for print setting with textbox
        /// </summary>
        public DataTemplate ListViewItemTextBoxTemplate { get; set; }
        /// <summary>
        /// Gets and sets the data template for print setting with password text box
        /// </summary>
        public DataTemplate ListViewItemPasswordBoxTemplate { get; set; }
        /// <summary>
        /// Gets and sets the data template for print setting
        /// </summary>
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
                    case PrintSettingType.password:
                        template = ListViewItemPasswordBoxTemplate;
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
