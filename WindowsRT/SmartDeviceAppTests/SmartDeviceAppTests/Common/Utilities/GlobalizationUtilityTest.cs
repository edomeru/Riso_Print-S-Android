using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceAppTests.Common.Utilities
{
    [TestClass]
    public class GlobalizationUtilityTest
    {

        [TestMethod]
        public void Test_IsJapaneseLocale()
        {
            bool result = GlobalizationUtility.IsJapaneseLocale();
            Assert.IsFalse(result);
        }

    }
}
