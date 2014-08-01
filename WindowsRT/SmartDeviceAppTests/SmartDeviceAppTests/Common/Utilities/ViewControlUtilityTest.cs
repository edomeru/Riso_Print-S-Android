using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Controls;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.Common.Utilities
{
    [TestClass]
    public class ViewControlUtilityTest
    {

        private const string STR_LONG_TEXT = "slightly long long long long long text";
        private const string STR_ELLIPSIS = "...";
        private static double SIZE_MARGIN_NONE = (double)Application.Current.Resources["MARGIN_None"];
        private static double SIZE_MARGIN_DEFAULT = (double)Application.Current.Resources["MARGIN_Default"];
        private static double SIZE_MARGIN_SMALL = (double)Application.Current.Resources["MARGIN_Small"];

        [TestMethod]
        public void Test_GetTextBlockFromParent_NullParent()
        {
            var result = ViewControlUtility.GetTextBlockFromParent(null, null);
            Assert.IsNull(result);
        }

        [UI.UITestMethod]
        public void Test_GetTextBlockFromParent_NullKey()
        {
            var textBlock = new TextBlock();
            var result = ViewControlUtility.GetTextBlockFromParent((UIElement)textBlock, null);
            Assert.IsNull(result);
        }

        [UI.UITestMethod]
        public void Test_GetTextBlockFromParent_TextBlockFoundKey()
        {
            var textBlock = new TextBlock();
            textBlock.Name = "key";
            var result = ViewControlUtility.GetTextBlockFromParent((UIElement)textBlock, textBlock.Name);
            Assert.IsNotNull(result);
            Assert.AreEqual(textBlock, result);
        }

        [UI.UITestMethod]
        public void Test_GetTextBlockFromParent_TextBlockNotFoundKey()
        {
            var textBlock = new TextBlock();
            textBlock.Name = "key";
            var result = ViewControlUtility.GetTextBlockFromParent((UIElement)textBlock, "someOtherKey");
            Assert.IsNull(result);
        }

        [UI.UITestMethod]
        public void Test_GetTextBlockFromParent_NotTextBlock()
        {
            var grid = new Grid();
            grid.Name = "key";
            var result = ViewControlUtility.GetTextBlockFromParent((UIElement)grid, grid.Name);
            Assert.IsNull(result);
        }

        [UI.UITestMethod]
        public void Test_GetTextBlockFromParent_HasChildrenNoTextBlock()
        {
            var grid = new Grid();
            grid.Name = "grid";
            var innerGrid = new Grid();
            innerGrid.Name = "innerGrid";
            grid.Children.Add(innerGrid);
            var result = ViewControlUtility.GetTextBlockFromParent((UIElement)grid, innerGrid.Name);
            Assert.IsNull(result);
        }

        [UI.UITestMethod]
        public void Test_GetTextBlockFromParent_HasChildrenTextBlock()
        {
            var grid = new Grid();
            grid.Name = "grid";
            var textBlock = new TextBlock();
            textBlock.Name = "key";
            grid.Children.Add(textBlock);
            var result = ViewControlUtility.GetTextBlockFromParent((UIElement)grid, textBlock.Name);
            Assert.IsNotNull(result);
            Assert.AreEqual(textBlock, result);
        }

        [UI.UITestMethod]
        public void Test_GetTextBlockFromParent_HasChildrenTextBlockNotFoundKey()
        {
            var grid = new Grid();
            grid.Name = "grid";
            var textBlock = new TextBlock();
            textBlock.Name = "key";
            grid.Children.Add(textBlock);
            var result = ViewControlUtility.GetTextBlockFromParent((UIElement)grid, "someOtherKey");
            Assert.IsNull(result);
        }

        [TestMethod]
        public void Test_GetTextWidthFromTextBlockWithStyle_NullStyle()
        {
            var result = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(null, null);
            Assert.AreEqual(0, result);
        }

        [UI.UITestMethod]
        public void Test_GetTextWidthFromTextBlockWithStyle_NullText()
        {
            var style = new Style { TargetType = typeof(TextBlock) };
            style.Setters.Add(new Setter(TextBlock.FontSizeProperty, 20));
            var result = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(null, style);
            Assert.AreEqual(0, result);
        }

        [UI.UITestMethod]
        public void Test_GetTextWidthFromTextBlockWithStyle_TextNotTrimmed()
        {
            var style = new Style { TargetType = typeof(TextBlock) };
            style.Setters.Add(new Setter(TextBlock.FontSizeProperty, 20));
            var result = ViewControlUtility.GetTextWidthFromTextBlockWithStyle("text", style);
            Assert.AreNotEqual(0, result);
        }

        [UI.UITestMethod]
        public void Test_GetTextWidthFromTextBlockWithStyle_TextTrimmed()
        {
            var style = new Style { TargetType = typeof(TextBlock) };
            style.Setters.Add(new Setter(TextBlock.FontSizeProperty, 20));
            style.Setters.Add(new Setter(TextBlock.WidthProperty, 5)); // Set to smaller, just to make sure
            var result = ViewControlUtility.GetTextWidthFromTextBlockWithStyle(STR_LONG_TEXT, style);
            Assert.AreNotEqual(0, result);
        }

        [TestMethod]
        public void Test_GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth_NullStyle()
        {
            var result = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth("text", null, 1);
            Assert.AreEqual(String.Empty, result);
        }

        [UI.UITestMethod]
        public void Test_GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth_NullText()
        {
            var result = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(null, new Style(), 1);
            Assert.AreEqual(String.Empty, result);
        }
        [UI.UITestMethod]
        public void Test_GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth_EmptyText()
        {
            var result = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth("", new Style(), 1);
            Assert.AreEqual(String.Empty, result);
        }

        [UI.UITestMethod]
        public void Test_GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth_NoWidth()
        {
            var result = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth("text", new Style(), 0);
            Assert.AreEqual(String.Empty, result);
        }

        [UI.UITestMethod]
        public void Test_GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth_InvalidWidth()
        {
            var result = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth("text", new Style(), -1);
            Assert.AreEqual(String.Empty, result);
        }

        [UI.UITestMethod]
        public void Test_GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth_TextTrimmed()
        {
            var style = new Style { TargetType = typeof(TextBlock) };
            style.Setters.Add(new Setter(TextBlock.FontSizeProperty, 20));
            var result = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(STR_LONG_TEXT, style, 100);
            Assert.AreNotEqual(String.Empty, result);
            Assert.AreNotEqual(3, result.Length); // Not only ellipsis
            Assert.IsFalse(result.StartsWith(STR_ELLIPSIS));
            Assert.IsFalse(result.EndsWith(STR_ELLIPSIS));
            Assert.IsTrue(result.Contains(STR_ELLIPSIS)); // In the middle
        }

        [UI.UITestMethod]
        public void Test_GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth_TextTrimmedAll()
        {
            var style = new Style { TargetType = typeof(TextBlock) };
            style.Setters.Add(new Setter(TextBlock.FontSizeProperty, 20));
            var result = ViewControlUtility.GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(STR_LONG_TEXT, style, 10);
            Assert.AreNotEqual(String.Empty, result);
            Assert.AreEqual(3, result.Length); // Only ellipsis
            Assert.IsTrue(result.StartsWith(STR_ELLIPSIS));
            Assert.IsTrue(result.EndsWith(STR_ELLIPSIS));
            Assert.IsTrue(result.Contains(STR_ELLIPSIS));
        }

        [UI.UITestMethod]
        public void Test_GetSeparatorStartPoint_NullSender()
        {
            var result = ViewControlUtility.GetSeparatorStartPoint(null, true, Visibility.Visible);
            Assert.AreEqual(SIZE_MARGIN_NONE, result);
        }

        [UI.UITestMethod]
        public void Test_GetSeparatorStartPoint_NotListItem()
        {
            var result = ViewControlUtility.GetSeparatorStartPoint(new JobListItemControl(), false, Visibility.Visible);
            Assert.AreEqual(SIZE_MARGIN_NONE, result);
        }

        [UI.UITestMethod]
        public void Test_GetSeparatorStartPoint_IconCollapsed()
        {
            var result = ViewControlUtility.GetSeparatorStartPoint(new JobListItemControl(), true, Visibility.Collapsed);
            Assert.AreEqual(SIZE_MARGIN_DEFAULT, result);
        }

        [UI.UITestMethod]
        public void Test_GetSeparatorStartPoint_IconVisible()
        {
            var result = ViewControlUtility.GetSeparatorStartPoint(new JobListItemControl(), true, Visibility.Visible);
            Assert.AreNotEqual(SIZE_MARGIN_NONE, result);
        }

    }
}
