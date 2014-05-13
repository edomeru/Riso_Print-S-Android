using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SmartDeviceApp.Controllers;
using SmartDeviceApp.Models;
using SmartDeviceApp.ViewModels;
using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Foundation;

namespace SmartDeviceAppTests.ViewModels
{
    [TestClass]
    public class SearchPrinterViewModelTest
    {
        SearchPrinterViewModel viewModel = new ViewModelLocator().SearchPrinterViewModel;

        [TestMethod]
        public void Test_SearchPrinterViewModel()
        {
            Assert.IsNotNull(viewModel);
        }

        [TestMethod]
        public async void Test_SearchPrinterViewModel_GetSetPrinterSearchList()
        {
            ObservableCollection<PrinterSearchItem> tempList = new ObservableCollection<PrinterSearchItem>();

            PrinterSearchItem p = new PrinterSearchItem();
            await ExecuteOnUIThread(() =>
            {
                p.Ip_address = "192.168.0.1";
                p.Name = "test";
                p.IsInPrinterList = false;
                tempList.Add(p);
            });
            viewModel.PrinterSearchList = tempList;
            ObservableCollection<PrinterSearchItem> tempList2 = viewModel.PrinterSearchList;

            Assert.AreEqual(tempList, tempList2);


        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_GetSetWillRefresh()
        {
            viewModel.WillRefresh = true;
            bool willRefreshTest = viewModel.WillRefresh;

            Assert.AreEqual(true, willRefreshTest);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_SetRefreshState()
        {
            viewModel.SetStateRefreshState();

            Assert.AreEqual(true, viewModel.WillRefresh);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_PrinterSearchItemSelectedFail()
        {
            PrinterController.Instance.Initialize();
            PrinterSearchItem p = new PrinterSearchItem();
            p.Ip_address = "19.2.2222.2";

            viewModel.PrinterSearchItemSelected.Execute(p);
            Assert.IsNotNull(viewModel.PrinterSearchItemSelected);
        }

        [TestMethod]
        public void Test_SearchPrinterViewModel_PrinterSearchItemSelectedSuccess()
        {
            PrinterController.Instance.Initialize();
            PrinterSearchItem p = new PrinterSearchItem();
            p.Ip_address = "19.2.2.2";

            viewModel.PrinterSearchItemSelected.Execute(p);
            Assert.IsNotNull(viewModel.PrinterSearchItemSelected);
        }





        public static IAsyncAction ExecuteOnUIThread(Windows.UI.Core.DispatchedHandler action)
        {
            return Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, action);
        }
    }
}
