using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Controllers;
using Windows.UI.Xaml.Controls;
using SmartDeviceApp.Common.Enum;
using GalaSoft.MvvmLight.Messaging;
using Windows.UI.Xaml.Media.Imaging;
using Windows.Foundation;
using SmartDeviceApp.Models;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;
using SmartDeviceApp.Controls;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintPreviewViewModelTest
    {
        public PrintPreviewViewModel GetPrintPreviewViewModel()
        {
            return new ViewModelLocator().PrintPreviewViewModel;
        }

        [UI.UITestMethod]
        public void Test_PrintPreviewViewModel()
        {
            Assert.IsNotNull(GetPrintPreviewViewModel());
        }

        private void Test_EventHandler()
        {
        }

        [UI.UITestMethod]
        public void Test_OnNavigatedTo()
        {
            // Note: Test for coverage only; No tests to assert 
            SmartDeviceApp.Controllers.PrintPreviewController.OnNavigateToEventHandler eventHandler = new PrintPreviewController.OnNavigateToEventHandler(Test_EventHandler);
            GetPrintPreviewViewModel().OnNavigateToEventHandler += eventHandler;
            GetPrintPreviewViewModel().OnNavigatedTo();
        }

        [UI.UITestMethod]
        public void Test_OnNavigatedFrom()
        {
            // Note: Test for coverage only; No tests to assert 
            SmartDeviceApp.Controllers.PrintPreviewController.OnNavigateFromEventHandler eventHandler = new PrintPreviewController.OnNavigateFromEventHandler(Test_EventHandler);
            GetPrintPreviewViewModel().OnNavigateFromEventHandler += eventHandler;
            GetPrintPreviewViewModel().OnNavigatedFrom();
        }

        [UI.UITestMethod]
        public void Test_SetPageAreaGrid()
        {
            //var control = new TwoPageControl();
            //GetPrintPreviewViewModel().SetPageAreaGrid(control);

            {
                // TwoPageControl throws an exception
                // The property 'HorizontalScrollBarVisibility' was not found in type 'Windows.UI.Xaml.Controls.ScrollViewer'
                Assert.Inconclusive("UI Test");
            }
        }

        [UI.UITestMethod]
        public void Test_Cleanup()
        {
            // Note: Test for coverage only; No tests to assert
            GetPrintPreviewViewModel().Cleanup();
        }

        [UI.UITestMethod]
        public void Test_IsLoadLeftPageActive()
        {
            var isLoadPageActive = true;
            GetPrintPreviewViewModel().IsLoadLeftPageActive = isLoadPageActive;
            Assert.IsTrue(GetPrintPreviewViewModel().IsLoadLeftPageActive);
        }

        [UI.UITestMethod]
        public void Test_IsLoadRightPageActive()
        {
            var isLoadPageActive = true;
            GetPrintPreviewViewModel().IsLoadRightPageActive = isLoadPageActive;
            Assert.IsTrue(GetPrintPreviewViewModel().IsLoadRightPageActive);
        }

        [UI.UITestMethod]
        public void Test_SetViewMode()
        {
            // Note: Test for coverage only; No tests to assert
            // Also covers EnablePreviewGestures, EnablePreviewGestures
            new ViewModelLocator().ViewControlViewModel.ScreenMode = ScreenMode.PrintPreview;
            Messenger.Default.Send<ViewMode>(ViewMode.RightPaneVisible_ResizedWidth);
            Messenger.Default.Send<ViewMode>(ViewMode.MainMenuPaneVisible);
            Messenger.Default.Send<ViewMode>(ViewMode.FullScreen);
            Messenger.Default.Send<ViewMode>(ViewMode.RightPaneVisible);
            Messenger.Default.Send<ViewMode>(ViewMode.RightPaneVisible_ResizedWidth);

            new ViewModelLocator().ViewControlViewModel.ScreenMode = ScreenMode.Jobs;
            Messenger.Default.Send<ViewMode>(ViewMode.RightPaneVisible_ResizedWidth);
        }

        [UI.UITestMethod]
        public void Test_DocumentTitleText()
        {
            var documentTitleText = "DOCUMENT_TITLE_TEXT";
            GetPrintPreviewViewModel().DocumentTitleText = documentTitleText;
            Assert.AreEqual(documentTitleText, GetPrintPreviewViewModel().DocumentTitleText);
        }

        [UI.UITestMethod]
        public void Test_RightPageImage()
        {
            //var rightPageImage = new BitmapImage();
            //rightPageImage.UriSource = new Uri("ms-appx:///Resources/Images/RZ1070-page1.jpg");
            var rightPageImage = new WriteableBitmap(1, 1);
            GetPrintPreviewViewModel().RightPageImage = rightPageImage;
            Assert.AreEqual(rightPageImage, GetPrintPreviewViewModel().RightPageImage);
        }

        [UI.UITestMethod]
        public void Test_LeftPageImage()
        {
            //var leftPageImage = new BitmapImage();
            //leftPageImage.UriSource = new Uri("ms-appx:///Resources/Images/RZ1070-page1.jpg");
            var leftPageImage = new WriteableBitmap(1, 1);
            GetPrintPreviewViewModel().LeftPageImage = leftPageImage;
            Assert.AreEqual(leftPageImage, GetPrintPreviewViewModel().LeftPageImage);
        }

        [UI.UITestMethod]
        public void Test_RightPageActualSize()
        {
            var rightPageActualSize = new Size(100, 100);
            GetPrintPreviewViewModel().RightPageActualSize = rightPageActualSize;
            Assert.AreEqual(rightPageActualSize, GetPrintPreviewViewModel().RightPageActualSize);
        }

        [UI.UITestMethod]
        public void Test_LeftPageActualSize()
        {
            var leftPageActualSize = new Size(100, 100);
            GetPrintPreviewViewModel().LeftPageActualSize = leftPageActualSize;
            Assert.AreEqual(leftPageActualSize, GetPrintPreviewViewModel().LeftPageActualSize);
        }

        [UI.UITestMethod]
        public void Test_PageViewMode()
        {
            var pageViewMode = PageViewMode.SinglePageView;
            GetPrintPreviewViewModel().PageViewMode = pageViewMode;
            Assert.AreEqual(pageViewMode, GetPrintPreviewViewModel().PageViewMode);
            pageViewMode = PageViewMode.TwoPageViewHorizontal;
            GetPrintPreviewViewModel().PageViewMode = pageViewMode;
            Assert.AreEqual(pageViewMode, GetPrintPreviewViewModel().PageViewMode);
            pageViewMode = PageViewMode.TwoPageViewVertical;
            GetPrintPreviewViewModel().PageViewMode = pageViewMode;
            Assert.AreEqual(pageViewMode, GetPrintPreviewViewModel().PageViewMode);
        }

        [UI.UITestMethod]
        public void Test_GoToPreviousPage()
        {
            uint pageNumber = 0;
            GetPrintPreviewViewModel().UpdatePageIndexes(pageNumber);
            GetPrintPreviewViewModel().GoToPreviousPage.Execute(null);

            pageNumber = 1;
            GetPrintPreviewViewModel().UpdatePageIndexes(pageNumber);
            GetPrintPreviewViewModel().GoToPreviousPage.Execute(null);
            Assert.IsNotNull(GetPrintPreviewViewModel().GoToPreviousPage);
        }

        [UI.UITestMethod]
        public void Test_GoToNextPage()
        {
            uint pageTotal = 10;
            GetPrintPreviewViewModel().PageTotal = pageTotal;
            uint pageNumber = 0;
            GetPrintPreviewViewModel().UpdatePageIndexes(pageNumber);
            GetPrintPreviewViewModel().GoToNextPage.Execute(null);

            pageNumber = 9;
            GetPrintPreviewViewModel().UpdatePageIndexes(pageNumber);
            GetPrintPreviewViewModel().GoToNextPage.Execute(null);
            Assert.IsNotNull(GetPrintPreviewViewModel().GoToNextPage);
        }

        private void Test_GoToPageEventHandler(int idx)
        {
        }

        [UI.UITestMethod]
        public void Test_GoToPage()
        {
            // Note: Test for coverage only; No tests to assert
            PrintPreviewController.GoToPageEventHandler eventHandler = new PrintPreviewController.GoToPageEventHandler(Test_GoToPageEventHandler);
            GetPrintPreviewViewModel().GoToPageEventHandler += eventHandler;
            uint pageNumber = 0;
            GetPrintPreviewViewModel().GoToPage(pageNumber);
        }

        [UI.UITestMethod]
        public void Test_PageNumber()
        {
            var pageNumber = new PageNumberInfo(0, 10, PageViewMode.SinglePageView);
            GetPrintPreviewViewModel().PageNumber = pageNumber;
            Assert.AreEqual(pageNumber, GetPrintPreviewViewModel().PageNumber);
        }

        [UI.UITestMethod]
        public void Test_SetInitialPageIndex()
        {
            // Note: Test for coverage only; No tests to assert
            uint pageNumber = 0;
            GetPrintPreviewViewModel().SetInitialPageIndex(pageNumber);
        }

        [UI.UITestMethod]
        public void Test_UpdatePageIndexes()
        {
            // Note: Test for coverage only; No tests to assert
            uint pageNumber = 0;
            GetPrintPreviewViewModel().UpdatePageIndexes(pageNumber);
        }

        [UI.UITestMethod]
        public void Test_PageNumberSliderValueChange()
        {
            GetPrintPreviewViewModel().PageNumberSliderValueChange.Execute(null);
            Assert.IsNotNull(GetPrintPreviewViewModel().PageNumberSliderValueChange);
        }

        [UI.UITestMethod]
        public void Test_PageNumberSliderPointerCaptureLost()
        {
            GetPrintPreviewViewModel().PageNumberSliderPointerCaptureLost.Execute(null);
            Assert.IsNotNull(GetPrintPreviewViewModel().PageNumberSliderPointerCaptureLost);
        }

        [UI.UITestMethod]
        public void Test_PageTotal()
        {
            uint pageTotal = 10;
            GetPrintPreviewViewModel().PageTotal = pageTotal;
            Assert.AreEqual(pageTotal, GetPrintPreviewViewModel().PageTotal);
        }

        [UI.UITestMethod]
        public void Test_CurrentPageIndex()
        {
            uint currentPageIndex = 10;
            GetPrintPreviewViewModel().CurrentPageIndex = currentPageIndex;
            Assert.AreEqual(currentPageIndex, GetPrintPreviewViewModel().CurrentPageIndex);
        }

        [UI.UITestMethod]
        public void Test_IsPageNumberSliderEnabled()
        {
            var isPageNumberSliderEnabled = true;
            GetPrintPreviewViewModel().IsPageNumberSliderEnabled = isPageNumberSliderEnabled;
            Assert.AreEqual(isPageNumberSliderEnabled, GetPrintPreviewViewModel().IsPageNumberSliderEnabled);
        }

        [UI.UITestMethod]
        public void Test_IsReverseSwipe()
        {
            var isReverseSwipe = true;
            GetPrintPreviewViewModel().IsReverseSwipe = isReverseSwipe;
            Assert.AreEqual(isReverseSwipe, GetPrintPreviewViewModel().IsReverseSwipe);
        }

        [UI.UITestMethod]
        public void Test_IsHorizontalSwipeEnabled()
        {
            var isHorizontalSwipeEnabled = true;
            GetPrintPreviewViewModel().IsHorizontalSwipeEnabled = isHorizontalSwipeEnabled;
            Assert.AreEqual(isHorizontalSwipeEnabled, GetPrintPreviewViewModel().IsHorizontalSwipeEnabled);
        }
    }
}
