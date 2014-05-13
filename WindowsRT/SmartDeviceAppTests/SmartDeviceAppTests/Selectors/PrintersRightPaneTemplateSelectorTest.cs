using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using Windows.UI.Xaml;
using SmartDeviceApp.Selectors;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Selectors
{
    [TestClass]
    public class PrintersRightPaneTemplateSelectorTest
    {
        
        [UI.UITestMethod]
        public void Test_SelectTemplateCore()
        {
            PrintersRightPaneTemplateSelector printersRightPaneTemplateSelector = new PrintersRightPaneTemplateSelector();

            var item = "AddPrinter";
            var result = printersRightPaneTemplateSelector.SelectTemplate(item, null);
            Assert.AreEqual(printersRightPaneTemplateSelector.AddPrinterPaneTemplate, result);

            item = "SearchPrinter";
            result = printersRightPaneTemplateSelector.SelectTemplate(item, null);
            Assert.AreEqual(printersRightPaneTemplateSelector.SearchPrinterPaneTemplate, result);

            item = "PrintSettings";
            result = printersRightPaneTemplateSelector.SelectTemplate(item, null);
            Assert.AreEqual(printersRightPaneTemplateSelector.PrintSettingsPaneTemplate, result);

            // Test null
            item = null;
            result = printersRightPaneTemplateSelector.SelectTemplate(item, null);
            Assert.AreEqual(null, result);
        }
    }
}
