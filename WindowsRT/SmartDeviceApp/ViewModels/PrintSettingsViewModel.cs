using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Threading.Tasks;
using System.Xml.Serialization;
using System.Xml.Linq;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.ApplicationModel;
using GalaSoft.MvvmLight;
using SmartDeviceApp.Models;
using SmartDeviceApp.Common.Utilities;
using SmartDeviceApp.Controls;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media.Imaging;
using Windows.UI.Xaml;
using System.Collections.ObjectModel;
using System.Windows.Input;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Messaging;
using SmartDeviceApp.Common.Enum;
using Windows.UI.Xaml.Data;
using System.ComponentModel;

namespace SmartDeviceApp.ViewModels
{
    public class PrintSettingsViewModel : ViewModelBase
    {
        private readonly IDataService _dataService;
        private readonly INavigationService _navigationService;

        private PrintSettingList _printSettingsList;
        private ICommand _selectPrintSetting;
        private PrintSetting _selectedPrintSetting;

        public PrintSettingsViewModel(IDataService dataService, INavigationService navigationService)
        {
            _dataService = dataService;
            _navigationService = navigationService;

            Initialize();
        }
        
        public PrintSettingList PrintSettingsList
        {
            get { return _printSettingsList; }
            set
            {
                if (_printSettingsList != value)
                {
                    _printSettingsList = value;
                    RaisePropertyChanged("PrintSettingsList");
                }
            }
        }

        public PrintSetting SelectedPrintSetting
        {
            get { return _selectedPrintSetting; }
            set
            {
                if (_selectedPrintSetting != value)
                {
                    _selectedPrintSetting = value;
                    RaisePropertyChanged("SelectedPrintSetting");
                }
            }
        }

        public ICommand SelectPrintSetting
        {
            get
            {
                if (_selectPrintSetting == null)
                {
                    _selectPrintSetting = new RelayCommand<PrintSetting>(
                        (printSetting) => SelectPrintSettingExecute(printSetting),
                        (printSetting) => true
                    );
                }
                return _selectPrintSetting;
            }
        }

        private void Initialize()
        {
            string xmlPath = Path.Combine(Package.Current.InstalledLocation.Path, "Assets/printsettings.xml");
            XDocument data = XDocument.Load(xmlPath);
            
            var printSettingsData = from groupData in data.Descendants("group")
                select new PrintSettingGroup
                {
                    Name = (string)groupData.Attribute("name"),
                    Text = (string)groupData.Attribute("text"),
                    PrintSettings = 
                    (
                        from settingData in groupData.Elements("setting")
                            select new PrintSetting
                            {
                                Name = (string)settingData.Attribute("name"),
                                Text = (string)settingData.Attribute("text"),
                                Icon = (string)settingData.Attribute("icon"),
                                Type = (PrintSettingType)Enum.Parse(typeof(PrintSettingType), (string)settingData.Attribute("type")),
                                Options = 
                                (
                                    from optionData in settingData.Elements("option")
                                        select new PrintSettingOption
                                        {
                                            Text = (string)optionData.Value
                                        }).ToList<PrintSettingOption>()
                            }).ToList<PrintSetting>()
                };

            PrintSettingsList = new PrintSettingList();
            var tempList = printSettingsData.Cast<PrintSettingGroup>().ToList<PrintSettingGroup>();
            foreach (PrintSettingGroup group in tempList)
            {
                PrintSettingsList.Add(group);
            }
        }

        private void SelectPrintSettingExecute(PrintSetting printSetting)
        {
            Messenger.Default.Send<PrintSetting>(printSetting);
            Messenger.Default.Send<RightPaneMode>(RightPaneMode.PrintSettingOptionsVisible);
			SelectedPrintSetting = printSetting;
        }
    }
}