﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;

namespace SmartDeviceAppTests.Models
{
    [TestClass]
    public class PageNumberInfoTest
    {

        [TestMethod]
        public void Test_PageNumberInfoTest()
        {
            PageNumberInfo pageNumberInfo = new PageNumberInfo(0, 10, PageViewMode.SinglePageView);

            pageNumberInfo.PageIndex = 1;
            pageNumberInfo.PageTotal = 6;
            pageNumberInfo.PageViewMode = PageViewMode.TwoPageView;

            // Note: nothing to sssert
        }

    }
}
