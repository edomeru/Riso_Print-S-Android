using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using Windows.UI.Xaml;
using SmartDeviceApp.Selectors;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Selectors
{
    [TestClass]
    public class PrintSettingsListTemplateSelectorTest
    {
        [UI.UITestMethod]
        public void Test_SelectTemplateCore()
        {
            PrintSettingsListTemplateSelector printSettingsListTemplateSelector = new PrintSettingsListTemplateSelector();
            var printSetting = new PrintSetting();
            printSetting.Type = PrintSettingType.boolean;
            var result = printSettingsListTemplateSelector.SelectTemplate(printSetting, null);
            Assert.AreEqual(printSettingsListTemplateSelector.ListViewItemToggleSwitchTemplate, result);

            printSetting.Type = PrintSettingType.numeric;
            result = printSettingsListTemplateSelector.SelectTemplate(printSetting, null);
            Assert.AreEqual(printSettingsListTemplateSelector.ListViewItemTextBoxTemplate, result);

            printSetting.Type = PrintSettingType.list;
            result = printSettingsListTemplateSelector.SelectTemplate(printSetting, null);
            Assert.AreEqual(printSettingsListTemplateSelector.ListViewItemListTemplate, result);

            printSetting.Type = PrintSettingType.unknown;
            result = printSettingsListTemplateSelector.SelectTemplate(printSetting, null);
            Assert.AreEqual(null, result);
            
            // Test null
            result = printSettingsListTemplateSelector.SelectTemplate("TEST", null);
            Assert.AreEqual(null, result);
        }
    }
}
