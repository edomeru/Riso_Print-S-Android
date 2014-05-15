using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Models;

namespace SmartDeviceAppTests.Models
{
    [TestClass]
    public class PreviewSettingsTest
    {

        private const string TEXT = "sample";
        private PrintSettingOption _printSettingOption1;
        private PrintSettingOption _printSettingOption2;
        private PrintSetting _printSetting;

        private void Initialize()
        {
            _printSettingOption1 = new PrintSettingOption();
            _printSettingOption1.Text = TEXT;
            _printSettingOption1.Index = 0;
            _printSettingOption1.IsEnabled = true;

            _printSettingOption2 = new PrintSettingOption();
            _printSettingOption2.Text = TEXT;
            _printSettingOption2.Index = 1;
            _printSettingOption2.IsEnabled = true;

            _printSetting = new PrintSetting();
            _printSetting.Name = "name";
            _printSetting.Text = TEXT;
            _printSetting.Icon = "icon";
            _printSetting.Type = SmartDeviceApp.Common.Enum.PrintSettingType.list;
            _printSetting.Value = 1;
            _printSetting.SelectedOption = _printSettingOption1;
            _printSetting.Default = 0;
            _printSetting.Options = new List<PrintSettingOption>();
            _printSetting.Options.Add(_printSettingOption1);
            _printSetting.Options.Add(_printSettingOption2);
            _printSetting.IsEnabled = true;
            _printSetting.IsValueDisplayed = true;
        }

        private void Cleanup()
        {
            _printSettingOption1 = null;
            _printSettingOption2 = null;
            _printSetting = null;
        }

        [TestMethod]
        public void Test_PrintSettingOption_Equals_Object_Null()
        {
            Initialize();

            object target = null;
            bool result = _printSettingOption1.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSettingOption_Equals_Object_Other()
        {
            Initialize();

            object target = false;
            bool result = _printSettingOption1.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSettingOption_Equals_Object_Same()
        {
            Initialize();

            object target = _printSettingOption1;
            bool result = _printSettingOption1.Equals(target);
            Assert.IsTrue(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSettingOption_Equals_Null()
        {
            Initialize();

            PrintSettingOption target = null;
            bool result = _printSettingOption1.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSettingOption_Equals_Same()
        {
            Initialize();

            PrintSettingOption target = _printSettingOption1;
            bool result = _printSettingOption1.Equals(target);
            Assert.IsTrue(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSettingOption_GetHashCode()
        {
            Initialize();

            int result = _printSettingOption1.GetHashCode();
            Assert.AreEqual(TEXT.GetHashCode(), result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_SetSelectedOption()
        {
            Initialize();

            _printSetting.SelectedOption = _printSettingOption2;
            // Note: nothing to assert

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_GetSelectedOption_Invalid()
        {
            Initialize();

            _printSetting.Value = 3;
            PrintSettingOption result = _printSetting.SelectedOption;
            Assert.IsNull(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_GetSelectedOption_Valid()
        {
            Initialize();

            PrintSettingOption result = _printSetting.SelectedOption;
            Assert.AreEqual(_printSettingOption1, result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_GetIsEnabled()
        {
            Initialize();

            Assert.IsTrue(_printSetting.IsEnabled);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_GetIsValueDisplayed()
        {
            Initialize();

            Assert.IsTrue(_printSetting.IsValueDisplayed);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_Equals_Object_Null()
        {
            Initialize();

            object target = null;
            bool result = _printSetting.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_Equals_Object_Other()
        {
            Initialize();

            object target = false;
            bool result = _printSetting.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_Equals_Object_Same()
        {
            Initialize();

            object target = _printSetting;
            bool result = _printSetting.Equals(target);
            Assert.IsTrue(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_Equals_Null()
        {
            Initialize();

            PrintSetting target = null;
            bool result = _printSetting.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_Equals_Same()
        {
            Initialize();

            PrintSetting target = _printSetting;
            bool result = _printSetting.Equals(target);
            Assert.IsTrue(result);

            Cleanup();
        }

        [TestMethod]
        public void Test_PrintSetting_GetHashCode()
        {
            Initialize();

            int result = _printSetting.GetHashCode();
            Assert.AreEqual(TEXT.GetHashCode(), result);

            Cleanup();
        }

    }
}
