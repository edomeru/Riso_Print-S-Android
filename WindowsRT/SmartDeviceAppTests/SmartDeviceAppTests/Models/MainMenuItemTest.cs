using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Models;

namespace SmartDeviceAppTests.Models
{
    [TestClass]
    public class MainMenuItemTest
    {

        private const string TEXT = "text";

        private MainMenuItem _mainMenuItem;

        private void Initialize()
        {
            _mainMenuItem = new MainMenuItem(TEXT, null, true);
        }

        private void Cleanup()
        {
            _mainMenuItem = null;
        }

        [TestMethod]
        public void Test_GetIsChecked()
        {
            Initialize();

            Assert.IsTrue(_mainMenuItem.IsChecked);

            Cleanup();
        }

        [TestMethod]
        public void TEST_Equals_Object_Null()
        {
            Initialize();

            object target = null;
            bool result = _mainMenuItem.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void TEST_Equals_Object_Other()
        {
            Initialize();

            object target = false;
            bool result = _mainMenuItem.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void TEST_Equals_Object_Same()
        {
            Initialize();

            object target = _mainMenuItem;
            bool result = _mainMenuItem.Equals(target);
            Assert.IsTrue(result);

            Cleanup();
        }

        [TestMethod]
        public void TEST_Equals_Null()
        {
            Initialize();

            MainMenuItem target = null;
            bool result = _mainMenuItem.Equals(target);
            Assert.IsFalse(result);

            Cleanup();
        }

        [TestMethod]
        public void TEST_Equals_Same()
        {
            Initialize();

            MainMenuItem target = _mainMenuItem;
            bool result = _mainMenuItem.Equals(target);
            Assert.IsTrue(result);

            Cleanup();
        }

        [TestMethod]
        public void TEST_GetHashCode()
        {
            Initialize();

            int result = _mainMenuItem.GetHashCode();
            Assert.AreEqual(TEXT.GetHashCode(), result);

            Cleanup();
        }

    }
}
