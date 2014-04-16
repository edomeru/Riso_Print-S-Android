using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Collections.ObjectModel;
using System.Windows.Input;
using GalaSoft.MvvmLight;
using Windows.UI.Xaml;

namespace SmartDeviceApp.Models
{
    public class MainMenuItem : ObservableObject
    {
        public string Text { get; set; }
        public ICommand Command { get; set; }
        public Visibility SeparatorVisibility { get; set; }

        public MainMenuItem(string text, ICommand command, Visibility separatorVisibility)
        {
            Text = text;
            Command = command;
            SeparatorVisibility = separatorVisibility;
        }
    }

    public class MainMenuItemList : ObservableCollection<MainMenuItem>
    {
        public List<MainMenuItem> MainMenuItems { get; set; }
    }
}
