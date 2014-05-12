using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class ItemClickedConverterTest
    {
        private ItemClickedConverter itemClickedConverter = new ItemClickedConverter();

        [UITestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = itemClickedConverter.Convert(null, null, null, null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(null, result);

            var value = new ItemClickEventArgs();
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(value.ClickedItem, result);
        }

        [UITestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = itemClickedConverter.ConvertBack(null, null, null, null);
                Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
