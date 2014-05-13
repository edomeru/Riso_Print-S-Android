using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Converters;
using Windows.UI.Xaml;
using SmartDeviceApp.Models;
using GalaSoft.MvvmLight.Command;
using System.Windows.Input;
using SmartDeviceApp.ViewModels;

namespace SmartDeviceAppTests.Converters
{
    [TestClass]
    public class MainMenuItemToSeparatorVisibilityConverterTest
    {
        private MainMenuItemToSeparatorVisibilityConverter mainMenuItemToSeparatorVisibilityConverter = new MainMenuItemToSeparatorVisibilityConverter();

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
        public void Test_Convert()
        {
            // Test null
            var result = mainMenuItemToSeparatorVisibilityConverter.Convert(null, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // Test wrong type
            result = mainMenuItemToSeparatorVisibilityConverter.Convert("TEST", null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            var mainMenuItem1 = new MainMenuItem("MAIN_MENU_ITEM1", Test_Command, true);
            var mainMenuItem2 = new MainMenuItem("MAIN_MENU_ITEM2", Test_Command, true);
            var mainMenuItems = new MainMenuItemList();
            mainMenuItems.Add(mainMenuItem1);
            mainMenuItems.Add(mainMenuItem2);
            new ViewModelLocator().ViewControlViewModel.MainMenuItems = mainMenuItems;

            var value = mainMenuItem1;
            result = mainMenuItemToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Visible, result);

            value = mainMenuItem2;
            result = mainMenuItemToSeparatorVisibilityConverter.Convert(value, null, null, null);
            Assert.AreEqual(Visibility.Collapsed, result);

            // For coverage only
            Test_Command.Execute(null);

        }

        [TestMethod]        
        public void Test_ConvertBack()
        {
            Assert.ThrowsException<NotImplementedException>(() => mainMenuItemToSeparatorVisibilityConverter.ConvertBack(null, null, null, null));
        }
    }
}
