using GalaSoft.MvvmLight.Messaging;
using Microsoft.Xaml.Interactivity;
using SmartDeviceApp.Common.Enum;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading.Tasks;
using Windows.System;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Input;

namespace SmartDeviceApp.Common.Utilities
{
    public class IPAddressTextBoxBehavior : DependencyObject, IBehavior
    {
        private const string REGEX_IPADRESS_CHARS = "[^a-fA-F0-9:.]";
        private const string REGEX_JAPANESE = "/[一-龠]+[ぁ-ゔ]+[ァ-ヴー]+[々〆〤]+/u";
        private const string REGEX_NUMERIC = "[0-9]";
        private const string REGEX_COLON_ALPHA = "[:]";

        private string lastValidText;

        private int lastCaretPosition = 0;

        /// <summary>
        /// Object which this behavior is associated to.
        /// </summary>
        public DependencyObject AssociatedObject { get; private set; }

        /// <summary>
        /// Attaches this behavior to an object.
        /// </summary>
        /// <param name="associatedObject">Object to be associated with this behavior</param>
        public void Attach(DependencyObject associatedObject)
        {
            var textBox = associatedObject as TextBox;
            if (textBox == null)
                throw new ArgumentException(
                    "FilteredTextBoxBehavior can be attached only to TextBox");

            AssociatedObject = associatedObject;

            lastValidText = textBox.Text;
            textBox.KeyDown += OnKeyDown;
            textBox.KeyUp += OnKeyUp;
            textBox.TextChanged += OnTextChanged;

            textBox.IsTextPredictionEnabled = false;

            var inputScope = new InputScope();
            inputScope.Names.Add(new InputScopeName(InputScopeNameValue.Number));
            textBox.InputScope = inputScope;
        }

        /// <summary>
        /// Detaches the object associated with this behavior.
        /// </summary>
        public void Detach()
        {
            var textBox = AssociatedObject as TextBox;
            if (textBox != null)
            {
                textBox.KeyDown -= OnKeyDown;
                textBox.KeyUp -= OnKeyUp;
            }
        }

        private void OnKeyUp(object sender, Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        {
            if (e.Key == VirtualKey.Shift)
                IsShiftPressed = false;

            if (e.Key == VirtualKey.Enter)
            {
                Messenger.Default.Send<MessageType>(MessageType.AddPrinter);

                //workaround to hide softkeyboard after printer search
                //since we don't have direct control over the keyboard
                ((TextBox)sender).IsEnabled = false;
                ((TextBox)sender).IsEnabled = true;
            }
        }

        private void OnKeyDown(object sender, Windows.UI.Xaml.Input.KeyRoutedEventArgs e)
        {
            int keyValue = (int)e.Key;
            System.Diagnostics.Debug.WriteLine(e.Key);

            if (e.Key == VirtualKey.Shift)
                IsShiftPressed = true;

            // do something
            e.Handled = false;
            System.Diagnostics.Debug.WriteLine("Accepted");
        }

        /// <summary>
        /// Flag to check whether Shift key is pressed
        /// </summary>
        public bool IsShiftPressed
        {
            get;
            set;
        }

        private void OnTextChanged(object sender, TextChangedEventArgs e)
        {
            var textBox = AssociatedObject as TextBox;
            bool hasHiragana = false;
            bool doesNotContain = false;

            string newText = "";
            if (textBox != null)
            {
                //get the last added character(s)
                newText = textBox.Text;

                if (String.CompareOrdinal(lastValidText, newText) < 0) 
                //if (!Regex.IsMatch(newText, REGEX_JAPANESE))
                {
                    if (!newText.Contains(lastValidText)) // checks if the new text in textbox is replaced by hiragana thus different from the lastValidText
                    {
                        doesNotContain = true;
                    }
                    string text;
                    hasHiragana = convert(newText, out text);
                    newText = text;
                    if (doesNotContain && hasHiragana) 
                    {
                        if (!newText.Contains(lastValidText))
                        {
                            if (newText.Length < 3) // Japanese IME replaces the text with at most two hiragana equivalent. If the new text's length < 3, then it may have replaced the whole last valid text.
                            {

                                if (lastCaretPosition >= lastValidText.Length)
                                {
                                    newText = lastValidText + newText;
                                    lastCaretPosition = newText.Length;
                                }
                                else
                                {
                                    newText = lastValidText.Insert(lastCaretPosition, newText);
                                    lastCaretPosition += newText.Length;
                                }
                            }
                        }
                        else //contains
                        {
                            if (newText.Equals("ee") || newText.Equals("aa")) //special handling
                            {
                                newText = lastValidText + newText;
                                lastCaretPosition = newText.Length;
                            }
                        }
                    }

                    if (doesNotContain && !hasHiragana) // if newText does not contain lastValidText but has no hiragana
                    {
                        
                        //check if it is just a removed zero
                        if (!checkForRemovedZeroes(newText, lastValidText))
                        {
                         
                            if (lastCaretPosition >= lastValidText.Length)
                            {
                                newText = lastValidText + newText;
                                lastCaretPosition = newText.Length;
                            }
                            else
                            {
                                newText = lastValidText.Insert(lastCaretPosition, newText);
                                lastCaretPosition += newText.Length;
                            }

                        }
                    }
                    
                    //var caretPosition = textBox.SelectionStart;
                    textBox.Text = newText;
                    textBox.SelectionStart = lastCaretPosition + 1;
                    //textBox.SelectionStart = (caretPosition > 0) ? caretPosition : 0;
                    
                }
                else
                {
                    if (newText.Equals("bb") || newText.Equals("cc") || newText.Equals("dd")  // case handling the event when IME replaces the text in the textbox with these characters.
                        || newText.Equals("ff") || newText.Equals("::") 
                        || (newText.Length == 2 && (Regex.IsMatch(newText, REGEX_NUMERIC) 
                        || Regex.IsMatch(newText, REGEX_COLON_ALPHA))))
                    
                    {
                        if (!Regex.IsMatch(newText, REGEX_JAPANESE))
                        {
                            string text;
                            convert(newText, out text);
                            newText = text;
                        }
                        if (lastCaretPosition >= lastValidText.Length)
                        {
                            newText = lastValidText + newText;
                            lastCaretPosition += newText.Length;
                        }
                        else
                        {
                            newText = lastValidText.Insert(lastCaretPosition, newText);
                        }
                        textBox.Text = newText;
                        textBox.SelectionStart = lastCaretPosition + 1;
                    }
                }

                lastCaretPosition = textBox.SelectionStart;

            }


            // check if valid IP Address characters
            if (textBox != null && !string.IsNullOrWhiteSpace(REGEX_IPADRESS_CHARS))
            {
                if (!Regex.IsMatch(textBox.Text, REGEX_IPADRESS_CHARS))
                {
                    // The text matches the regular expression.
                    lastValidText = newText;
                }
                else
                {
                    // The text doesn't match the regular expression.
                    // Restore the last valid value.
                    var caretPosition = textBox.SelectionStart;
                    textBox.Text = lastValidText;
                    textBox.SelectionStart = (caretPosition > 0) ? caretPosition - 1 : 0;
                }
            }
        }

        private bool checkForRemovedZeroes(string newText, string lastValidText)
        {
            
            if (newText.Length < lastValidText.Length)
            {
                int i = 0;
                foreach (char c in newText)
                {
                    if (c != lastValidText.ElementAt(i))
                    {
                        if (lastValidText.ElementAt(i) == '0')
                        {
                            return true;
                        }
                    }
                    i++;
                }
            }

            return false;
            
        }

        private bool convert(string text, out string newText)
        {
            string convertedString = "";
            bool hasHiragana = false;
            newText = text;
            //replace hiragana characters
            if (newText.Contains("あ")) // a
            {
                newText = Regex.Replace(newText, "あ", "a");
                lastCaretPosition++;
                hasHiragana = true;
            }

            if (newText.Contains("え")) // e
            {
                newText = Regex.Replace(newText, "え", "e");
                lastCaretPosition++;
                hasHiragana = true;
            }

            if (newText.Contains("ば")) // ba
            {
                newText = Regex.Replace(newText, "ば", "ba");
                lastCaretPosition++;
                hasHiragana = true;
                if (newText.Contains("っ")) // bba
                {
                    newText = Regex.Replace(newText, "っ", "b");
                    lastCaretPosition++;
                }
            }

            if (newText.Contains("べ")) // be
            {
                newText = Regex.Replace(newText, "べ", "be");
                lastCaretPosition++;
                hasHiragana = true;
                if (newText.Contains("っ")) // bbe
                {
                    newText = Regex.Replace(newText, "っ", "b");
                    lastCaretPosition++;
                }
            }

            if (newText.Contains("か")) // ca
            {
                newText = Regex.Replace(newText, "か", "ca");
                lastCaretPosition++;
                hasHiragana = true;
                if (newText.Contains("っ")) // cca
                {
                    newText = Regex.Replace(newText, "っ", "c");
                    lastCaretPosition++;
                }
            }

            if (newText.Contains("せ")) // ce
            {
                newText = Regex.Replace(newText, "せ", "ce");
                lastCaretPosition++;
                hasHiragana = true;
                if (newText.Contains("っ")) // cce
                {
                    newText = Regex.Replace(newText, "っ", "c");
                    lastCaretPosition++;
                }
            }

            if (newText.Contains("だ")) // da
            {
                newText = Regex.Replace(newText, "だ", "da");
                lastCaretPosition++;
                hasHiragana = true;
                if (newText.Contains("っ")) // dda
                {
                    newText = Regex.Replace(newText, "っ", "d");
                    lastCaretPosition++;
                }
            }

            if (newText.Contains("で")) // de
            {
                newText = Regex.Replace(newText, "で", "de");
                lastCaretPosition++;
                hasHiragana = true;
                if (newText.Contains("っ")) // dde
                {
                    newText = Regex.Replace(newText, "っ", "d");
                    lastCaretPosition++;
                }
            }

            if (newText.Contains("ふぁ")) // fa
            {
                newText = Regex.Replace(newText, "ふぁ", "fa");
                lastCaretPosition++;
                hasHiragana = true;
                if (newText.Contains("っ")) // ffa
                {
                    newText = Regex.Replace(newText, "っ", "f");
                    lastCaretPosition++;
                }
            }

            if (newText.Contains("ふぇ")) // fe
            {
                newText = Regex.Replace(newText, "ふぇ", "fe");
                lastCaretPosition++;
                hasHiragana = true;
                if (newText.Contains("っ")) // ffe
                {
                    newText = Regex.Replace(newText, "っ", "f");
                    lastCaretPosition++;
                }
            }

            return hasHiragana;
        }
    }
}
