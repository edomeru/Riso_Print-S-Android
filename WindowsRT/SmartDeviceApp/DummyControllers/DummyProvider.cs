using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Data.Pdf;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI.Xaml.Media.Imaging;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Enum;
using System.Diagnostics;
using Windows.Foundation;
using SmartDeviceApp.Common.Utilities;

namespace SmartDeviceApp.DummyControllers
{
    public class DummyProvider
    {
        #region CONSTANTS

        public uint TOTAL_PAGES = 12;
        public PageViewMode PAGE_VIEW_MODE = PageViewMode.SinglePageView;
        public string PDF_FILENAME = "RZ1070.pdf";

        #endregion

        private static DummyProvider _instance;

        private DummyProvider()
        {
        }

        public static DummyProvider Instance
        {
            get
            {
                if (_instance == null)
                {
                    _instance = new DummyProvider();
                   
                }
                return _instance;
            }
        }

        public void LoadPageImage(uint idx)
        {
            var filename = "RZ1070-page" + (idx + 1).ToString() + ".jpg";
            BitmapImage pageImage = new BitmapImage();
            pageImage.UriSource = new Uri("ms-appx:///Resources/Dummy/" + filename);
            Messenger.Default.Send<DummyPageMessage>(new DummyPageMessage(pageImage, new Size(2480, 3508)));
        }

        public async Task<StorageFile> GetSamplePdf()
        {
            Windows.ApplicationModel.Package package = Windows.ApplicationModel.Package.Current;
            Windows.Storage.StorageFolder installedLocation = package.InstalledLocation;

            String output = String.Format("Installed Location: {0}", installedLocation.Path);

            StorageFile samplePdf = await StorageFileUtility.GetFileFromAppResource("Resources/Dummy/RZ1070.pdf");
            return samplePdf;
        }
    }

    public class DummyPageMessage
    {
        private BitmapImage _pageImage;
        private Size _actualSize;

        public DummyPageMessage(BitmapImage pageImage, Size actualSize)
        {
            _pageImage = pageImage;
            _actualSize = actualSize;
        }

        public BitmapImage PageImage
        {
            get { return _pageImage; }
            set { _pageImage = value; }
        }

        public Size ActualSize
        {
            get { return _actualSize; }
            set { _actualSize = value; }
        }
    }
}
