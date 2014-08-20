//
//  ViewControlUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/07/29.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using SmartDeviceApp.Common.Constants;
using System;
using System.Text;
using Windows.Foundation;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Media;

namespace SmartDeviceApp.Common.Utilities
{
    public static class ViewControlUtility
    {

        private const string STR_ELLIPSIS = "...";

        private static double SIZE_MARGIN_NONE = (double)Application.Current.Resources["MARGIN_None"];
        private static double SIZE_MARGIN_DEFAULT = (double)Application.Current.Resources["MARGIN_Default"];
        private static double SIZE_MARGIN_SMALL = (double)Application.Current.Resources["MARGIN_Small"];

        /// <summary>
        /// Retrieves a control from a parent based on its key
        /// </summary>
        /// <typeparam name="T">target control type</typeparam>
        /// <param name="parent">reference control</param>
        /// <param name="key">key (from xaml)</param>
        /// <returns>the control if found, null otherwise</returns>
        public static object GetControlFromParent<T>(UIElement parent, string key)
        {
            if (parent == null || string.IsNullOrEmpty(key))
            {
                return null;
            }

            if (parent.GetType() == typeof(T) && ((FrameworkElement)parent).Name == key)
            {
                return parent;
            }

            int count = VisualTreeHelper.GetChildrenCount(parent);
            for (int i = 0; i < count; ++i)
            {
                UIElement child = (UIElement)VisualTreeHelper.GetChild(parent, i);

                object result = GetControlFromParent<T>(child, key);
                if (result != null)
                {
                    return result;
                }
            }

            return null;
        }

        /// <summary>
        /// Computes the text width based on TextBox style
        /// </summary>
        /// <param name="text">text</param>
        /// <param name="style">style</param>
        /// <returns>total text width based on TextBox style</returns>
        public static double GetTextWidthFromTextBlockWithStyle(string text, Style style)
        {
            double width = 0;

            if (style != null && !string.IsNullOrEmpty(text))
            {
                TextBlock tempTextBlock = new TextBlock(); // Create dummy TextBlock to simulate size
                tempTextBlock.Text = text;
                tempTextBlock.Style = style;

                var tempSize = new Size(10000, 10000); // An arbitray size. We would not expect a very large TextBlock.
                var tempLocation = new Point(0, 0);

                tempTextBlock.Measure(tempSize);
                tempTextBlock.Arrange(new Rect(tempLocation, tempSize));

                width = tempTextBlock.ActualWidth; // ActualWidth is updated after calls from Measure() and Arrange()
                tempTextBlock = null;
            }

            return width;
        }

        /// <summary>
        /// Applies text trimming into the middle part based on specified TextBlock style and width
        /// </summary>
        /// <param name="text">text</param>
        /// <param name="style">desired style</param>
        /// <param name="width">desired width</param>
        /// <returns>middle trimmed text</returns>
        public static String GetMiddleTrimmedTextFromTextBlockWithStyleAndWidth(string text,
            Style style, double width)
        {
            if (string.IsNullOrEmpty(text) || style == null || width <= 0)
            {
                return String.Empty;
            }

            double currTextWidth = 0;
            int destIndex = 0;
            int counter = 0;
            int leftIndex = 0;
            int rightIndex = 0;

            StringBuilder trimmedStrBuilder = new StringBuilder(STR_ELLIPSIS);
            String trimmedText = trimmedStrBuilder.ToString();
            String tempText = trimmedText;

            currTextWidth = GetTextWidthFromTextBlockWithStyle(trimmedStrBuilder.ToString(), style);

            while (leftIndex < text.Length - rightIndex - 1 &&  // Both ends don't meet
                   counter < text.Length && // All characters are traversed
                   currTextWidth < width)   // Currently trimmed text is within desired width
            {
                trimmedText = tempText;

                bool isAddedCharToLeft = false;

                // Traverse text Left to Right
                destIndex = leftIndex;
                trimmedStrBuilder.Insert(destIndex, text[leftIndex]);
                currTextWidth = GetTextWidthFromTextBlockWithStyle(trimmedStrBuilder.ToString(), style);
                if (currTextWidth < width)
                {
                    ++leftIndex;
                    isAddedCharToLeft = true;
                }
                else
                {
                    trimmedStrBuilder.Remove(destIndex, 1);  // Revert insert
                }

                // Traverse text Right to Left
                if (trimmedStrBuilder.ToString().StartsWith(STR_ELLIPSIS))
                {
                    destIndex = trimmedText.Length - rightIndex - 1; // There are no characters added on the left side
                }
                else
                {
                    destIndex = trimmedText.Length - rightIndex +
                        ((isAddedCharToLeft) ? 1 : 0); // Update position depending when a character is added on the left
                }
                trimmedStrBuilder.Insert(destIndex, text[text.Length - rightIndex - 1]);
                currTextWidth = GetTextWidthFromTextBlockWithStyle(trimmedStrBuilder.ToString(), style);
                if (currTextWidth < width)
                {
                    ++rightIndex;
                }
                else
                {
                    trimmedStrBuilder.Remove(destIndex, 1);  // Revert insert
                }

                ++counter;

                tempText = trimmedStrBuilder.ToString();
                currTextWidth = GetTextWidthFromTextBlockWithStyle(tempText, style);
            };

            return trimmedText;
        }

        /// <summary>
        /// Computes the separator start point based on control type and left icon visibility
        /// </summary>
        /// <param name="sender">target control</param>
        /// <param name="isListItem">true when is a list item, false otherwise</param>
        /// <param name="iconVisibility">left icon visibility</param>
        /// <returns>separator start point</returns>
        public static double GetSeparatorStartPoint(object sender, bool isListItem, Visibility iconVisibility)
        {
            double separatorStartPoint = SIZE_MARGIN_NONE;

            if (sender != null && isListItem)
            {
                if (iconVisibility == Visibility.Visible)
                {
                    var imageWidth = ImageConstant.GetIconImageWidth(sender, true);
                    separatorStartPoint = imageWidth + (SIZE_MARGIN_DEFAULT * 2);
                }
                else
                {
                    separatorStartPoint = SIZE_MARGIN_DEFAULT;
                }
            }

            return separatorStartPoint;
        }

    }
}
