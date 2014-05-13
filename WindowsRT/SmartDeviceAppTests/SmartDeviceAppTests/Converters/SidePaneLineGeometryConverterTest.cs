using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using Windows.Foundation;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Converters
{
    [TestClass]
    public class SidePaneLineGeometryStartPointConverterTest
    {
        private SidePaneLineGeometryStartPointConverter sidePaneLineGeometryStartPointConverter = new SidePaneLineGeometryStartPointConverter();

        [TestMethod]
        public void Test_Convert()
        {
            var result = sidePaneLineGeometryStartPointConverter.Convert(0, null, null, null);
            Assert.AreEqual(new Point(0, 0), result);
        }

        [TestMethod]
        
        public void Test_ConvertBack()
        {
            // Note: Not implemented: Will throw exception
            Assert.ThrowsException<NotImplementedException>(() => sidePaneLineGeometryStartPointConverter.ConvertBack(null, null, null, null));
        }
    }

    [TestClass]
    public class SidePaneLineGeometryEndPointConverterTest
    {
        private SidePaneLineGeometryEndPointConverter sidePaneLineGeometryEndPointConverter = new SidePaneLineGeometryEndPointConverter();

        [UI.UITestMethod]
        public void Test_Convert()
        {
            var result = sidePaneLineGeometryEndPointConverter.Convert(0, null, null, null);
            Assert.AreEqual(new Point(0, Window.Current.Bounds.Height), result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => sidePaneLineGeometryEndPointConverter.ConvertBack(null, null, null, null));
        }
    }
}
