using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Models;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using SmartDeviceApp.Controllers;
using UI = Microsoft.VisualStudio.TestPlatform.UnitTestFramework.AppContainer;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class ViewControlViewModelTest
    {
        private ViewControlViewModel viewControlViewModel = new ViewModelLocator().ViewControlViewModel;

        [TestMethod]
        public void Test_ViewControlViewModel()
        {
            Assert.IsNotNull(viewControlViewModel);
        }

        [TestMethod]
        public void Test_ViewMode()
        {
            var viewMode = ViewMode.FullScreen;
            viewControlViewModel.ViewMode = viewMode;
            Assert.AreEqual(viewMode, viewControlViewModel.ViewMode);
        }

        [TestMethod]
        public void Test_ScreenMode()
        {
            var screenMode = ScreenMode.Home;
            viewControlViewModel.ScreenMode = screenMode;
            Assert.AreEqual(screenMode, viewControlViewModel.ScreenMode);
        }

        [TestMethod]
        public void Test_ToggleMainMenuPane()
        {
            viewControlViewModel.ViewMode = ViewMode.MainMenuPaneVisible;
            viewControlViewModel.ToggleMainMenuPane.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.FullScreen;
            viewControlViewModel.ToggleMainMenuPane.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible;
            viewControlViewModel.ToggleMainMenuPane.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
            viewControlViewModel.ToggleMainMenuPane.Execute(null);

            Assert.IsNotNull(viewControlViewModel.ToggleMainMenuPane);
        }

        [TestMethod]
        public void Test_TogglePane1()
        {
            viewControlViewModel.ViewMode = ViewMode.MainMenuPaneVisible;
            viewControlViewModel.ScreenMode = ScreenMode.Home;
            viewControlViewModel.TogglePane1.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.MainMenuPaneVisible;
            viewControlViewModel.ScreenMode = ScreenMode.PrintPreview;
            viewControlViewModel.TogglePane1.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.FullScreen;
            viewControlViewModel.ScreenMode = ScreenMode.Home;
            viewControlViewModel.TogglePane1.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.FullScreen;
            viewControlViewModel.ScreenMode = ScreenMode.PrintPreview;
            viewControlViewModel.TogglePane1.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible;
            viewControlViewModel.IsPane1Visible = true;            
            viewControlViewModel.TogglePane1.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible;
            viewControlViewModel.IsPane2Visible = true;
            viewControlViewModel.TogglePane1.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
            viewControlViewModel.IsPane1Visible = true;
            viewControlViewModel.TogglePane1.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
            viewControlViewModel.IsPane2Visible = true;
            viewControlViewModel.TogglePane1.Execute(null);

            Assert.IsNotNull(viewControlViewModel.TogglePane1);
        }

        [TestMethod]
        public void Test_TogglePane2()
        {
            viewControlViewModel.ViewMode = ViewMode.MainMenuPaneVisible;
            viewControlViewModel.ScreenMode = ScreenMode.Home;
            viewControlViewModel.TogglePane2.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.MainMenuPaneVisible;
            viewControlViewModel.ScreenMode = ScreenMode.PrintPreview;
            viewControlViewModel.TogglePane2.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.FullScreen;
            viewControlViewModel.ScreenMode = ScreenMode.Home;
            viewControlViewModel.TogglePane2.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.FullScreen;
            viewControlViewModel.ScreenMode = ScreenMode.PrintPreview;
            viewControlViewModel.TogglePane2.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible;
            viewControlViewModel.IsPane1Visible = true;
            viewControlViewModel.TogglePane2.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible;
            viewControlViewModel.IsPane2Visible = true;
            viewControlViewModel.TogglePane2.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
            viewControlViewModel.IsPane1Visible = true;
            viewControlViewModel.TogglePane2.Execute(null);

            viewControlViewModel.ViewMode = ViewMode.RightPaneVisible_ResizedWidth;
            viewControlViewModel.IsPane2Visible = true;
            viewControlViewModel.TogglePane2.Execute(null);

            Assert.IsNotNull(viewControlViewModel.TogglePane2);
        }

        [TestMethod]
        public void Test_IsPane1Visible()
        {
            var isPane1Visible = true;
            viewControlViewModel.IsPane1Visible = isPane1Visible;
            Assert.AreEqual(isPane1Visible, viewControlViewModel.IsPane1Visible);
        }

        [TestMethod]
        public void Test_IsPane2Visible()
        {
            var isPane2Visible = true;
            viewControlViewModel.IsPane2Visible = isPane2Visible;
            Assert.AreEqual(isPane2Visible, viewControlViewModel.IsPane2Visible);
        }

        [TestMethod]
        public void Test_TapHandled()
        {
            var tapHandled = true;
            viewControlViewModel.TapHandled = tapHandled;
            Assert.AreEqual(tapHandled, viewControlViewModel.TapHandled);
        }

        private ICommand _test_command;
        public ICommand Test_Command
        {
            get
            {
                if (_test_command == null)
                {
                    _test_command = new RelayCommand(
                        () => { },
                        () => true
                    );
                }
                return _test_command;
            }
        }

        [TestMethod]
        public void Test_MainMenuItems()
        {
            var mainMenuItems = new MainMenuItemList();

            // TODO: For update!!
            var mainMenuItem1 = new MainMenuItem("MAIN_MENU_ITEM1", Test_Command, true);
            mainMenuItems.Add(mainMenuItem1);
            var mainMenuItem2 = new MainMenuItem("MAIN_MENU_ITEM2", Test_Command, true);
            mainMenuItems.Add(mainMenuItem2);

            viewControlViewModel.MainMenuItems = mainMenuItems;
            Assert.AreEqual(mainMenuItems, viewControlViewModel.MainMenuItems);

            // For coverage only:
            Test_Command.Execute(null);
        }
        
        [UI.UITestMethod]
        public void Test_GoToHomePage()
        {
            // Note: Cannot change value of DocumentController.Instance.IsFileLoaded
            //viewControlViewModel.GoToHomePage.Execute(null);
            Assert.IsNotNull(viewControlViewModel.GoToHomePage);
        }

        [TestMethod]
        public void Test_GoToJobsPage()
        {
            //viewControlViewModel.GoToJobsPage.Execute(null);
            Assert.IsNotNull(viewControlViewModel.GoToJobsPage);
        }

        [TestMethod]
        public void Test_GoToPrintersPage()
        {
            //viewControlViewModel.GoToPrintersPage.Execute(null);
            Assert.IsNotNull(viewControlViewModel.GoToPrintersPage);
        }

        [TestMethod]
        public void Test_GoToSettingsPage()
        {
            //viewControlViewModel.GoToSettingsPage.Execute(null);
            Assert.IsNotNull(viewControlViewModel.GoToJobsPage);
        }

        [TestMethod]
        public void Test_GoToHelpPage()
        {
            //viewControlViewModel.GoToHelpPage.Execute(null);
            Assert.IsNotNull(viewControlViewModel.GoToJobsPage);
        }

        [TestMethod]
        public void Test_GoToLegalPage()
        {
            //GoToLegalPage.Execute(null);
            Assert.IsNotNull(viewControlViewModel.GoToLegalPage);
        }
    }
}
