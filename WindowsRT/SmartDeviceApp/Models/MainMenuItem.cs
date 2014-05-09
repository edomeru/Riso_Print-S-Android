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
        
        public MainMenuItem(string text, ICommand command)
        {
            Text = text;
            Command = command;
        }

        public override bool Equals(System.Object obj)
        {
            if (obj == null)
            {
                return false;
            }

            MainMenuItem otherItem = obj as MainMenuItem;
            if ((System.Object)otherItem == null)
            {
                return false;
            }
            return Text == otherItem.Text;
        }

        public bool Equals(MainMenuItem otherItem)
        {
            if ((object)otherItem == null)
            {
                return false;
            }
            return (Text == otherItem.Text);
        }

        public override int GetHashCode()
        {
            return Text.GetHashCode();
        }
    }

    public class MainMenuItemList : ObservableCollection<MainMenuItem>
    {
        public List<MainMenuItem> MainMenuItems { get; set; }
    }
}
