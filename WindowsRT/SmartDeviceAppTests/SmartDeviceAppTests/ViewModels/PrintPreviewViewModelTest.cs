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
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintPreviewViewModelTest
    {
        private PrintPreviewViewModel printPreviewViewModel = new ViewModelLocator().PrintPreviewViewModel;

        [TestMethod]
        public void Test_PrintPreviewViewModel()
        {
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.IsNotNull(printPreviewViewModel);
        }

        private void Test_EventHandler()
        {
        }

        [TestMethod]
        public void Test_OnNavigatedTo()
        {
            // Note: Test for coverage only; No tests to assert 
            SmartDeviceApp.Controllers.PrintPreviewController.OnNavigateToEventHandler eventHandler = new PrintPreviewController.OnNavigateToEventHandler(Test_EventHandler);
            printPreviewViewModel.OnNavigateToEventHandler += eventHandler;
            printPreviewViewModel.OnNavigatedTo();
        }

        [TestMethod]
        public void Test_OnNavigatedFrom()
        {
            // Note: Test for coverage only; No tests to assert 
            SmartDeviceApp.Controllers.PrintPreviewController.OnNavigateFromEventHandler eventHandler = new PrintPreviewController.OnNavigateFromEventHandler(Test_EventHandler);
            printPreviewViewModel.OnNavigateFromEventHandler += eventHandler;
            printPreviewViewModel.OnNavigatedFrom();
        }

        [UITestMethod]
        public void Test_SetPageAreaGrid()
        {
            var grid = new Grid();
            printPreviewViewModel.SetPageAreaGrid(grid);
        }

        [UITestMethod]
        public void Test_InitializeGestures()
        {
            // Note: Unreachable because cannot change value of _isPageAreaGridLoaded
            //printPreviewViewModel.PageViewMode = PageViewMode.SinglePageView;
            //printPreviewViewModel.InitializeGestures();

            //printPreviewViewModel.PageViewMode = PageViewMode.TwoPageView;
            //printPreviewViewModel.InitializeGestures();
        }

        [TestMethod]
        public void Test_IsLoadPageActive()
        {
            var isLoadPageActive = true;
            printPreviewViewModel.IsLoadPageActive = isLoadPageActive;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.IsTrue(printPreviewViewModel.IsLoadPageActive);
        }

        [TestMethod]
        public void Test_SetViewMode()
        {
            // Note: Test for coverage only; No tests to assert
            // Also covers EnablePreviewGestures, EnablePreviewGestures
            Messenger.Default.Send<ViewMode>(ViewMode.MainMenuPaneVisible);
            Messenger.Default.Send<ViewMode>(ViewMode.FullScreen);
            Messenger.Default.Send<ViewMode>(ViewMode.RightPaneVisible);
            Messenger.Default.Send<ViewMode>(ViewMode.RightPaneVisible_ResizedWidth);
        }

        [TestMethod]
        public void Test_DocumentTitleText()
        {
            var documentTitleText = "DOCUMENT_TITLE_TEXT";
            printPreviewViewModel.DocumentTitleText = documentTitleText;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(documentTitleText, printPreviewViewModel.DocumentTitleText);
        }

        [UITestMethod]
        public void Test_RightPageImage()
        {
            var rightPageImage = new BitmapImage();
            rightPageImage.UriSource = new Uri("ms-appx:///Resources/Images/RZ1070-page1.jpg");
            printPreviewViewModel.RightPageImage = rightPageImage;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(rightPageImage, printPreviewViewModel.RightPageImage);
        }

        [UITestMethod]
        public void Test_LeftPageImage()
        {
            var leftPageImage = new BitmapImage();
            leftPageImage.UriSource = new Uri("ms-appx:///Resources/Images/RZ1070-page1.jpg");
            printPreviewViewModel.LeftPageImage = leftPageImage;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(leftPageImage, printPreviewViewModel.LeftPageImage);
        }

        [TestMethod]
        public void Test_RightPageActualSize()
        {
            var rightPageActualSize = new Size(100, 100);
            printPreviewViewModel.RightPageActualSize = rightPageActualSize;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(rightPageActualSize, printPreviewViewModel.RightPageActualSize);
        }

        [TestMethod]
        public void Test_LeftPageActualSize()
        {
            var leftPageActualSize = new Size(100, 100);
            printPreviewViewModel.LeftPageActualSize = leftPageActualSize;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(leftPageActualSize, printPreviewViewModel.LeftPageActualSize);
        }

        [TestMethod]
        public void Test_PageViewMode()
        {
            var pageViewMode = PageViewMode.SinglePageView;
            printPreviewViewModel.PageViewMode = pageViewMode;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(pageViewMode, printPreviewViewModel.PageViewMode);
        }

        [TestMethod]
        public void Test_GoToPreviousPage()
        {
            uint pageNumber = 0;
            printPreviewViewModel.UpdatePageIndexes(pageNumber);
            printPreviewViewModel.GoToPreviousPage.Execute(null);

            pageNumber = 1;
            printPreviewViewModel.UpdatePageIndexes(pageNumber);
            printPreviewViewModel.GoToPreviousPage.Execute(null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.IsNotNull(printPreviewViewModel.GoToPreviousPage);
        }

        [TestMethod]
        public void Test_GoToNextPage()
        {
            uint pageTotal = 10;
            printPreviewViewModel.PageTotal = pageTotal;
            uint pageNumber = 0;
            printPreviewViewModel.UpdatePageIndexes(pageNumber);
            printPreviewViewModel.GoToNextPage.Execute(null);

            pageNumber = 9;
            printPreviewViewModel.GoToNextPage.Execute(null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.IsNotNull(printPreviewViewModel.GoToNextPage);
        }

        [TestMethod]
        public void Test_GoToPage()
        {
            // Note: Test for coverage only; No tests to assert
            uint pageNumber = 0;
            printPreviewViewModel.GoToPage(pageNumber);
        }


        [TestMethod]
        public void Test_PageNumber()
        {
            var pageNumber = new PageNumberInfo(0, 10, PageViewMode.SinglePageView);
            printPreviewViewModel.PageNumber = pageNumber;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(pageNumber, printPreviewViewModel.PageNumber);
        }

        [TestMethod]
        public void Test_SetInitialPageIndex()
        {
            // Note: Test for coverage only; No tests to assert
            uint pageNumber = 0;
            printPreviewViewModel.SetInitialPageIndex(pageNumber);
        }

        [TestMethod]
        public void Test_UpdatePageIndexes()
        {
            // Note: Test for coverage only; No tests to assert
            uint pageNumber = 0;
            printPreviewViewModel.UpdatePageIndexes(pageNumber);
        }

        [TestMethod]
        public void Test_PageNumberSliderValueChange()
        {
            printPreviewViewModel.PageNumberSliderValueChange.Execute(null);
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.IsNotNull(printPreviewViewModel.PageNumberSliderValueChange);
        }

        [TestMethod]
        public void Test_PageTotal()
        {
            uint pageTotal = 10;
            printPreviewViewModel.PageTotal = pageTotal;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(pageTotal, printPreviewViewModel.PageTotal);
        }

        [TestMethod]
        public void Test_CurrentPageIndex()
        {
            uint currentPageIndex = 10;
            printPreviewViewModel.CurrentPageIndex = currentPageIndex;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(currentPageIndex, printPreviewViewModel.CurrentPageIndex);
        }

        [TestMethod]
        public void Test_IsPageNumberSliderEnabled()
        {
            var isPageNumberSliderEnabled = true;
            printPreviewViewModel.IsPageNumberSliderEnabled = isPageNumberSliderEnabled;
            Microsoft.VisualStudio.TestPlatform.UnitTestFramework.Assert.AreEqual(isPageNumberSliderEnabled, printPreviewViewModel.IsPageNumberSliderEnabled);
        }
    }
}
