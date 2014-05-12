using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using Windows.Foundation;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class SidePaneLineGeometryStartPointConverterTest
    {
        private SidePaneLineGeometryStartPointConverter sidePaneLineGeometryStartPointConverter = new SidePaneLineGeometryStartPointConverter();

        [TestMethod]
        public void Test_Convert()
        {
            var result = sidePaneLineGeometryStartPointConverter.Convert(0, null, null, null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(new Point(0, 0), result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = sidePaneLineGeometryStartPointConverter.ConvertBack(null, null, null, null);
                Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }

    [TestClass]
    public class SidePaneLineGeometryEndPointConverterTest
    {
        private SidePaneLineGeometryEndPointConverter sidePaneLineGeometryEndPointConverter = new SidePaneLineGeometryEndPointConverter();

        [UITestMethod]
        public void Test_Convert()
        {
            var result = sidePaneLineGeometryEndPointConverter.Convert(0, null, null, null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(new Point(0, Window.Current.Bounds.Height), result);
        }

        [TestMethod]
        public void Test_ConvertBack()
        {
            try
            {
                // Note: Not implemented: Will throw exception
                var result = sidePaneLineGeometryEndPointConverter.ConvertBack(null, null, null, null);
                Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.Fail();
            }
            catch (NotImplementedException)
            {
            }
        }
    }
}
