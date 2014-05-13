using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;

using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Converters
{
    [TestClass]
    public class ItemClickedConverterTest
    {
        private ItemClickedConverter itemClickedConverter = new ItemClickedConverter();

        [UI.UITestMethod]
        public void Test_Convert()
        {
            // Test null
            var result = itemClickedConverter.Convert(null, null, null, null);
            Assert.AreEqual(null, result);

            var value = new ItemClickEventArgs();
            result = itemClickedConverter.Convert(value, null, null, null);
            Assert.AreEqual(value.ClickedItem, result);
        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => itemClickedConverter.ConvertBack(null, null, null, null));
        }
    }
}
