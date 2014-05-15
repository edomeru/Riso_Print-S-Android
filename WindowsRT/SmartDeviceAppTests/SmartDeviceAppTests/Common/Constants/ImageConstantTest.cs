using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Common.Constants;
using Windows.Foundation;
using SmartDeviceApp.Controls;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;
using Windows.Graphics.Display;

namespace SmartDeviceAppTests.Common.Constant
{
    [TestClass]
    public class ImageConstantTest
    {

        [TestMethod]
        public void Test_GetDpiScaleFactor_NonUIThread()
        {
            double result = ImageConstant.GetDpiScaleFactor();
            Assert.AreEqual(1.0, result);
        }

        [UI.UITestMethod]
        public void Test_GetDpiScaleFactor_UIThread()
        {
            double result = ImageConstant.GetDpiScaleFactor();
            Assert.AreEqual(1.0, result); // Note: Result may be different on other environment
        }

        [UI.UITestMethod]
        public void Test_GetIconImageWidth_JobListItemControl_ResolutionScale100()
        {
            int width = ImageConstant.GetIconImageWidth(new JobListItemControl());
            Assert.AreEqual(34, width);
        }

        [UI.UITestMethod]
        public void Test_GetIconImageWidth_GroupListControl_ResolutionScale100()
        {
            int width = ImageConstant.GetIconImageWidth(new GroupListControl());
            Assert.AreEqual(20, width);
        }

        [UI.UITestMethod]
        public void Test_GetIconImageWidth_KeyValueControl_ResolutionScale100()
        {
            int width = ImageConstant.GetIconImageWidth(new KeyValueControl());
            Assert.AreEqual(20, width);
        }

        [UI.UITestMethod]
        public void Test_GetIconImageWidth_KeyRadioButtonControl_ResolutionScale100()
        {
            int width = ImageConstant.GetIconImageWidth(new KeyRadioButtonControl());
            Assert.AreEqual(20, width);
        }

        [UI.UITestMethod]
        public void Test_GetIconImageWidth_KeyTextBoxControl_ResolutionScale100()
        {
            int width = ImageConstant.GetIconImageWidth(new KeyTextBoxControl());
            Assert.AreEqual(20, width);
        }

        [UI.UITestMethod]
        public void Test_GetIconImageWidth_KeyToggleSwitchControl_ResolutionScale100()
        {
            int width = ImageConstant.GetIconImageWidth(new KeyToggleSwitchControl());
            Assert.AreEqual(20, width);
        }

        [UI.UITestMethod]
        public void Test_GetIconImageWidth_OtherControl()
        {
            int width = ImageConstant.GetIconImageWidth(new KeyPasswordBoxControl());
            Assert.AreEqual(0, width);
        }

        [UI.UITestMethod]
        public void Test_GetRightButtonImageWidth_ResolutionScale100()
        {
            int width = ImageConstant.GetRightButtonImageWidth();
            Assert.AreEqual(14, width);
        }


    }
}
