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
        /// <summary>
        /// Text label of a menu item
        /// </summary>
        public string Text { get; set; }

        /// <summary>
        /// Command associated to the menu item
        /// </summary>
        public ICommand Command { get; set; }

        private bool isChecked;
        
        /// <summary>
        /// Constructor for MainMenuItem
        /// </summary>
        /// <param name="text"></param>
        /// <param name="command"></param>
        /// <param name="isChecked"></param>
        public MainMenuItem(string text, ICommand command, bool isChecked)
        {
            Text = text;
            Command = command;
            IsChecked = isChecked;
        }

        /// <summary>
        /// Denotes if a menu item is currently selected
        /// </summary>
        public bool IsChecked
        {
            get { return isChecked; }
            set
            {
                if (isChecked != value)
                {
                    isChecked = value;
                    RaisePropertyChanged("IsChecked");
                }
            }
        }

        /// <summary>
        /// Determines whether the specified object is equal to the current object.
        /// </summary>
        /// <param name="obj">object</param>
        /// <returns>true when equal, false otherwise</returns>
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

        /// <summary>
        /// Determines whether the specified MainMenuItem object is equal to the current MainMenuItem object.
        /// </summary>
        /// <param name="otherItem">main menu item</param>
        /// <returns>true when equal, false otherwise</returns>
        public bool Equals(MainMenuItem otherItem)
        {
            if ((object)otherItem == null)
            {
                return false;
            }
            return (Text == otherItem.Text);
        }

        /// <summary>
        /// Hash function
        /// </summary>
        /// <returns>hash code</returns>
        public override int GetHashCode()
        {
            return Text.GetHashCode();
        }
    }

    public class MainMenuItemList : ObservableCollection<MainMenuItem>
    {
        /// <summary>
        /// A collection of main menu items
        /// </summary>
        public List<MainMenuItem> MainMenuItems { get; set; }
    }
}
