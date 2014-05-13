using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.ViewModels;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;
using SmartDeviceApp.Controllers;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class PrintSettingsViewModelTest
    {
        private PrintSettingsViewModel printSettingsViewModel = new ViewModelLocator().PrintSettingsViewModel;

        [TestMethod]
        public void Test_PrintSettingsViewModel()
        {
            Assert.IsNotNull(printSettingsViewModel);
        }

        [TestMethod]
        public void Test_PrinterId()
        {
            printSettingsViewModel.PrinterId = 1;
            Assert.AreEqual(1, printSettingsViewModel.PrinterId);
        }

        [TestMethod]
        public void Test_PrinterName()
        {
            printSettingsViewModel.PrinterName = "TEST_PRINTER";
            Assert.AreEqual("TEST_PRINTER", printSettingsViewModel.PrinterName);
        }

        [TestMethod]
        public void Test_PrinterIpAddress()
        {
            printSettingsViewModel.PrinterIpAddress = "192.168.1.1";
            Assert.AreEqual("192.168.1.1", printSettingsViewModel.PrinterIpAddress);
        }

        [TestMethod]
        public void Test_IsPrintPreview()
        {
            printSettingsViewModel.IsPrintPreview = true;
            Assert.IsTrue(printSettingsViewModel.IsPrintPreview);
        }

        private void Test_PrintEventHandler()
        {
        }

        [TestMethod]
        public void Test_PrintCommand()
        {
            printSettingsViewModel.PrinterId = -1;
            printSettingsViewModel.PrintCommand.Execute(null);
            Assert.IsNotNull(printSettingsViewModel.PrintCommand);

            printSettingsViewModel.PrinterId = 0;
            PrintPreviewController.PrintEventHandler eventHandler = new PrintPreviewController.PrintEventHandler(Test_PrintEventHandler);
            printSettingsViewModel.ExecutePrintEventHandler += eventHandler;
            printSettingsViewModel.PrintCommand.Execute(null);
            Assert.IsNotNull(printSettingsViewModel.PrintCommand);
        }

        [TestMethod]
        public void Test_ListPrintersCommand()
        {
            printSettingsViewModel.IsPrintPreview = true;
            printSettingsViewModel.ListPrintersCommand.Execute(null);
            Assert.IsNotNull(printSettingsViewModel.ListPrintersCommand);
        }

        [TestMethod]
        public void Test_PrintSettingsList()
        {
            var printSetting1 = new PrintSetting();
            printSetting1.Text = "PRINT_SETTING1_TEXT";
            var printSetting2 = new PrintSetting();
            printSetting2.Text = "PRINT_SETTING2_TEXT";
            var printSettings = new List<PrintSetting>();
            printSettings.Add(printSetting1);
            printSettings.Add(printSetting2);
            var group = new PrintSettingGroup();
            group.Name = "GROUP_NAME";
            group.Text = "GROUP_TEXT";
            group.PrintSettings = printSettings;
            var groups = new List<PrintSettingGroup>();
            groups.Add(group);
            var printSettingList = new PrintSettingList();
            printSettingList.Groups = groups;
            printSettingsViewModel.PrintSettingsList = printSettingList;
            Assert.AreEqual(printSettingList, printSettingsViewModel.PrintSettingsList);
        }

        [TestMethod]
        public void Test_SelectedPrintSetting()
        {
            var selectedPrintSetting = new PrintSetting();
            selectedPrintSetting.Text = "SELECTED_PRINT_SETTING_TEXT";
            printSettingsViewModel.SelectedPrintSetting = selectedPrintSetting;
            Assert.AreEqual(selectedPrintSetting, printSettingsViewModel.SelectedPrintSetting);
        }

        [TestMethod]
        public void Test_SelectPrintSetting()
        {
            var selectedPrintSetting = new PrintSetting();
            selectedPrintSetting.Type = PrintSettingType.boolean;
            printSettingsViewModel.SelectPrintSetting.Execute(selectedPrintSetting);

            selectedPrintSetting.Type = PrintSettingType.numeric;
            printSettingsViewModel.SelectPrintSetting.Execute(selectedPrintSetting);

            selectedPrintSetting.Type = PrintSettingType.list;
            printSettingsViewModel.SelectPrintSetting.Execute(selectedPrintSetting);

            selectedPrintSetting.Type = PrintSettingType.unknown;
            printSettingsViewModel.SelectPrintSetting.Execute(selectedPrintSetting);
            Assert.IsNotNull(printSettingsViewModel.SelectPrintSetting);
        }

        private void Test_PinCodeValueChangedEventHandler(string pin)
        {
        }
        
        [TestMethod]
        public void Test_AuthenticationLoginPinCode()
        {
            PrintPreviewController.PinCodeValueChangedEventHandler eventHandler = new PrintPreviewController.PinCodeValueChangedEventHandler(Test_PinCodeValueChangedEventHandler);
            printSettingsViewModel.PinCodeValueChangedEventHandler += eventHandler;

            var pinCode = "AUTHENTICATION_PIN_CODE";
            printSettingsViewModel.AuthenticationLoginPinCode = pinCode;
            Assert.AreEqual(pinCode, printSettingsViewModel.AuthenticationLoginPinCode);
        }
    }
}
