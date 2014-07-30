﻿//
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

        /// <summary>
        /// Retrieves the TextBlock control from a parent based on its key
        /// </summary>
        /// <param name="parent">reference control</param>
        /// <param name="key">TextBlock key (from xaml)</param>
        /// <returns>TextBlock if found, null otherwise</returns>
        public static TextBlock GetTextBlockFromParent(UIElement parent, string key)
        {
            if (parent.GetType() == typeof(TextBlock) && ((TextBlock)parent).Name == key)
            {
                return (TextBlock)parent;
            }

            TextBlock result = null;
            int count = VisualTreeHelper.GetChildrenCount(parent);
            for (int i = 0; i < count; ++i)
            {
                UIElement child = (UIElement)VisualTreeHelper.GetChild(parent, i);

                TextBlock result2 = GetTextBlockFromParent(child, key);
                if (result2 != null)
                {
                    result = result2;
                    break;
                }
            }
            return result;
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

            if (style != null)
            {
                TextBlock tempTextBlock = new TextBlock(); // Create dummy TextBlock to simulate size
                tempTextBlock.Text = text;
                tempTextBlock.Style = style;

                var tempSize = new Size(10000, 10000); // An arbitray size. We would not expect a very large TextBlock.
                var tempLocation = new Point(0, 0);

                tempTextBlock.Measure(tempSize);
                tempTextBlock.Arrange(new Rect(tempLocation, tempSize));

                width = tempTextBlock.ActualWidth; // ActualWidth is updated after calls from Measure() and Arrange()
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
            if (text == null || style == null || width == 0)
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

            while (leftIndex < text.Length - rightIndex &&  // Both ends don't meet
                    counter < text.Length)  // All characters are traversed
            {
                trimmedText = tempText;

                // Traverse text Left to Right
                destIndex = leftIndex;
                trimmedStrBuilder.Insert(destIndex, text[leftIndex]);
                currTextWidth = GetTextWidthFromTextBlockWithStyle(trimmedStrBuilder.ToString(), style);
                if (currTextWidth < width)
                {
                    ++leftIndex;
                }
                else
                {
                    // Revert insert
                    trimmedStrBuilder.Remove(destIndex, 1);
                }

                // Traverse text Right to Left
                destIndex = trimmedText.Length - rightIndex + 1;
                trimmedStrBuilder.Insert(destIndex, text[text.Length - rightIndex - 1]);
                currTextWidth = GetTextWidthFromTextBlockWithStyle(trimmedStrBuilder.ToString(), style);
                if (currTextWidth < width)
                {
                    ++rightIndex;
                }
                else
                {
                    // Revert insert
                    trimmedStrBuilder.Remove(destIndex, 1);
                }

                ++counter;

                tempText = trimmedStrBuilder.ToString();
            };

            return trimmedText;
        }

    }
}
