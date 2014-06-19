using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using Windows.Storage;
using SmartDeviceApp.Common.Utilities;
using System.Threading.Tasks;

namespace SmartDeviceAppTests.Common.Utilities
{
    [TestClass]
    public class PreviewPageImageUtilityTest
    {

        [TestMethod]
        public void Test_GetPagesPerSheet_Off()
        {
            int pagesPerSheet = PreviewPageImageUtility.GetPagesPerSheet(0);
            Assert.AreEqual(1, pagesPerSheet);
        }

        [TestMethod]
        public void Test_GetPagesPerSheet_TwoUp()
        {
            int pagesPerSheet = PreviewPageImageUtility.GetPagesPerSheet(1);
            Assert.AreEqual(2, pagesPerSheet);
        }

        [TestMethod]
        public void Test_GetPagesPerSheet_FourUp()
        {
            int pagesPerSheet = PreviewPageImageUtility.GetPagesPerSheet(2);
            Assert.AreEqual(4, pagesPerSheet);
        }

        [TestMethod]
        public void Test_GetPagesPerSheet_NotExists()
        {
            int pagesPerSheet = PreviewPageImageUtility.GetPagesPerSheet(3);
            Assert.AreEqual(1, pagesPerSheet);
        }

    }
}
