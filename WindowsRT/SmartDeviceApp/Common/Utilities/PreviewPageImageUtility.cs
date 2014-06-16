﻿//
//  PreviewPageImageUtility.cs
//  SmartDeviceApp
//
//  Created by a-LINK Group on 2014/03/11.
//  Copyright 2014 RISO KAGAKU CORPORATION. All Rights Reserved.
//
//  Revision History :
//  Date            Author/ID           Ver.
//  ----------------------------------------------------------------------
//

using GalaSoft.MvvmLight.Threading;
using SmartDeviceApp.Common.Constants;
using SmartDeviceApp.Common.Enum;
using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Storage;
using Windows.Storage.Streams;
using Windows.UI.Xaml.Media.Imaging;

namespace SmartDeviceApp.Common.Utilities
{
    public static class PreviewPageImageUtility
    {

        private const string FILE_PATH_RES_IMAGE_STAPLE = "Resources/Images/img_staple.png";
        private const string FILE_PATH_RES_IMAGE_PUNCH = "Resources/Images/img_punch.png";

        /// <summary>
        /// Gets the target size based on paper size
        /// </summary>
        /// <param name="paperSize">paper size</param>
        /// <returns>size</returns>
        public static Size GetPaperSize(int paperSize)
        {
            Size targetSize;
            switch (paperSize)
            {
                case (int)PaperSize.A3:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A3;
                    break;
                case (int)PaperSize.A3W:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A3W;
                    break;
                case (int)PaperSize.A5:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A5;
                    break;
                case (int)PaperSize.A6:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A6;
                    break;
                case (int)PaperSize.B4:
                    targetSize = PrintSettingConstant.PAPER_SIZE_B4;
                    break;
                case (int)PaperSize.B5:
                    targetSize = PrintSettingConstant.PAPER_SIZE_B5;
                    break;
                case (int)PaperSize.B6:
                    targetSize = PrintSettingConstant.PAPER_SIZE_B6;
                    break;
                case (int)PaperSize.Foolscap:
                    targetSize = PrintSettingConstant.PAPER_SIZE_FOOLSCAP;
                    break;
                case (int)PaperSize.Tabloid:
                    targetSize = PrintSettingConstant.PAPER_SIZE_TABLOID;
                    break;
                case (int)PaperSize.Legal:
                    targetSize = PrintSettingConstant.PAPER_SIZE_LEGAL;
                    break;
                case (int)PaperSize.Letter:
                    targetSize = PrintSettingConstant.PAPER_SIZE_LETTER;
                    break;
                case (int)PaperSize.Statement:
                    targetSize = PrintSettingConstant.PAPER_SIZE_STATEMENT;
                    break;
                case (int)PaperSize.A4:
                default:
                    targetSize = PrintSettingConstant.PAPER_SIZE_A4;
                    break;
            }

            return targetSize;
        }

        /// <summary>
        /// Checks if the orientation is portrait based on selected orientation
        /// </summary>
        /// <param name="orientation">orientation</param>
        /// <param name="imposition">imposition</param>
        /// <returns>true when portrait, false otherwise</returns>
        public static bool IsPortrait(int orientation, int? imposition = null)
        {
            bool isPortrait = (orientation == (int)Orientation.Portrait);
            if (imposition != null && imposition == (int)Imposition.TwoUp)
            {
                isPortrait = !isPortrait;
            }
            return isPortrait;
        }

        /// <summary>
        /// Determines the number of pages of a single sheet based on imposition
        /// </summary>
        /// <param name="imposition">imposition type</param>
        /// <returns>number of pages per sheet</returns>
        public static int GetPagesPerSheet(int imposition)
        {
            int pagesPerSheet = 1;
            switch (imposition)
            {
                case (int)Imposition.TwoUp:
                    pagesPerSheet = 2;
                    break;
                case (int)Imposition.FourUp:
                    pagesPerSheet = 4;
                    break;
                case (int)Imposition.Off:
                default:
                    // Do nothing
                    break;
            }
            return pagesPerSheet;
        }

        /// <summary>
        /// Creates a bitmap based on target paper size and orientation
        /// </summary>
        /// <param name="canvasBitmap">canvas bitmap</param>
        public static void FillWhitePageImage(WriteableBitmap canvasBitmap)
        {
            DispatcherHelper.CheckBeginInvokeOnUI(
                () =>
                {
                    WriteableBitmapExtensions.FillRectangle(canvasBitmap, 0, 0, canvasBitmap.PixelWidth,
                        canvasBitmap.PixelHeight, Windows.UI.Colors.White);
                });
        }

        /// <summary>
        /// Retrieves the target preview page image size
        /// </summary>
        /// <param name="paperSize">paper size</param>
        /// <param name="isPortrait">true if portrait, false otherwise</param>
        /// <returns>size of the preview page image</returns>
        public static Size GetPreviewPageImageSize(Size paperSize, bool isPortrait)
        {
            // Get paper size and apply DPI
            double length1 = (paperSize.Width * ImageConstant.FACTOR_MM_TO_IN) * ImageConstant.BASE_DPI;
            double length2 = (paperSize.Height * ImageConstant.FACTOR_MM_TO_IN) * ImageConstant.BASE_DPI;

            Size pageImageSize = new Size();
            // Check orientation
            if (isPortrait)
            {
                pageImageSize.Width = (int)length1;
                pageImageSize.Height = (int)length2;
            }
            else
            {
                pageImageSize.Width = (int)length2;
                pageImageSize.Height = (int)length1;
            }

            return pageImageSize;
        }

        /// <summary>
        /// Puts an image into the canvas
        /// </summary>
        /// <param name="canvasBitmap">canvas</param>
        /// <param name="canvasSize">canvas size</param>
        /// <param name="overlayBitmap">overlay image</param>
        /// <param name="overlaySize">overlay size</param>
        /// <param name="enableScaleToFit">true when fit to scale, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        public static void OverlayImage(WriteableBitmap canvasBitmap, Size canvasSize,
            WriteableBitmap overlayBitmap, Size overlaySize, bool enableScaleToFit,
            CancellationTokenSource cancellationToken)
        {
            PreviewPageImageUtility.FillWhitePageImage(canvasBitmap);

            if (enableScaleToFit)
            {
                ScaleImageToFit(canvasBitmap, canvasSize, overlayBitmap, overlaySize,
                    cancellationToken);
            }
            else
            {
                // Determine logical page size if cropping is needed
                // If not cropped, logical page just fits into paper
                int cropWidth = (int)canvasSize.Width;
                if (canvasSize.Width > overlaySize.Width)
                {
                    cropWidth = (int)overlaySize.Width;
                }
                int cropHeight = (int)canvasSize.Height;
                if (canvasSize.Height > overlaySize.Height)
                {
                    cropHeight = (int)overlaySize.Height;
                }

                // Source and destination rectangle are the same since
                // logical page is cropped using the rectangle and put as in into the paper
                Rect rect = new Rect(0, 0, cropWidth, cropHeight);

                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                // Place image into paper
                DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        WriteableBitmapExtensions.Blit(canvasBitmap, rect, overlayBitmap, rect);
                    });

            }
        }

        /// <summary>
        /// Applies imposition (uses selected imposition order).
        /// Imposition images are assumed to be applied with selected paper size and orientation.
        /// The page images are assumed to be in order based on logical page index.
        /// </summary>
        /// <param name="canvasBitmap">canvas</param>
        /// <param name="canvasSize">canvas size</param>
        /// <param name="overlayImages">overlay image</param>
        /// <param name="overlaySize">overlay size</param>
        /// <param name="orientation">orientation</param>
        /// <param name="imposition">imposition</param>
        /// <param name="impositionOrder">imposition order</param>
        /// <param name="scaleToFit">scaleToFit</param>
        /// <param name="isPortrait">sets the new orientation based on imposition; true if portrait, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        public static void OverlayImagesForImposition(WriteableBitmap canvasBitmap, Size canvasSize,
            List<WriteableBitmap> overlayImages, Size overlaySize, int orientation, int imposition,
            int impositionOrder, bool scaleToFit, out bool isPortrait, CancellationTokenSource cancellationToken)
        {
            // Determine final orientation based on imposition
            int pagesPerSheet = GetPagesPerSheet(imposition);
            isPortrait = IsPortrait(orientation, imposition);

            // Compute number of pages per row and column
            int pagesPerRow = 0;
            int pagesPerColumn = 0;
            if (isPortrait)
            {
                pagesPerColumn = (int)Math.Sqrt(pagesPerSheet);
                pagesPerRow = pagesPerSheet / pagesPerColumn;
            }
            else
            {
                pagesPerRow = (int)Math.Sqrt(pagesPerSheet);
                pagesPerColumn = pagesPerSheet / pagesPerRow;
            }

            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            // Compute page area size and margin
            double marginPaper = PrintSettingConstant.MARGIN_IMPOSITION_EDGE * ImageConstant.BASE_DPI;
            double marginBetweenPages = PrintSettingConstant.MARGIN_IMPOSITION_BETWEEN_PAGES * ImageConstant.BASE_DPI;
            Size impositionPageAreaSize = GetImpositionSinglePageAreaSize(canvasSize,
                pagesPerRow, pagesPerColumn, marginBetweenPages, marginPaper);

            // Set initial positions
            double initialOffsetX = 0;
            double initialOffsetY = 0;
            if (impositionOrder == (int)ImpositionOrder.FourUpUpperRightToBottom ||
                impositionOrder == (int)ImpositionOrder.FourUpUpperRightToLeft ||
                impositionOrder == (int)ImpositionOrder.TwoUpRightToLeft)
            {
                initialOffsetX = (marginBetweenPages * (pagesPerColumn - 1)) +
                    (impositionPageAreaSize.Width * (pagesPerColumn - 1));
            }
            if (impositionOrder == (int)ImpositionOrder.TwoUpRightToLeft &&
                isPortrait)
            {
                initialOffsetY = (marginBetweenPages * (pagesPerRow - 1)) +
                    (impositionPageAreaSize.Height * (pagesPerRow - 1));
            }

            FillWhitePageImage(canvasBitmap);

            // Loop each imposition page
            int impositionPageIndex = 0;
            double pageImageOffsetX = initialOffsetX;
            double pageImageOffsetY = initialOffsetY;
            foreach (WriteableBitmap impositionPageBitmap in overlayImages)
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                // Put imposition page image in center of imposition page area
                double x = marginPaper + pageImageOffsetX;
                double y = marginPaper + pageImageOffsetY;

                Rect destRect = new Rect();
                destRect.X = x;
                destRect.Y = y;
                if (scaleToFit)
                {
                    Size scaledSize = GetScaledSize(impositionPageAreaSize, overlaySize);
                    destRect.X += (impositionPageAreaSize.Width - scaledSize.Width) / 2;
                    destRect.Y += (impositionPageAreaSize.Height - scaledSize.Height) / 2;
                    destRect.Width = scaledSize.Width;
                    destRect.Height = scaledSize.Height;
                }
                else
                {
                    if (imposition == (int)Imposition.FourUp)
                    {
                        destRect.Width = overlaySize.Width * 0.5;
                        destRect.Height = overlaySize.Height * 0.5;
                    }
                    else if (imposition == (int)Imposition.TwoUp)
                    {
                        destRect.Width = overlaySize.Width * 0.75;
                        destRect.Height = overlaySize.Height * 0.75;
                    }
                }
                Rect srcRect = new Rect(0, 0, overlaySize.Width, overlaySize.Height);

                DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        WriteableBitmapExtensions.Blit(canvasBitmap, destRect, impositionPageBitmap, srcRect);
                    });

                // Update offset/postion based on direction
                if (impositionOrder == (int)ImpositionOrder.TwoUpLeftToRight ||
                    impositionOrder == (int)ImpositionOrder.FourUpUpperLeftToRight)
                {
                    // Upper left to right
                    pageImageOffsetX += marginBetweenPages + impositionPageAreaSize.Width;
                    if (((impositionPageIndex + 1) % pagesPerColumn) == 0)
                    {
                        pageImageOffsetX = initialOffsetX;
                        pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                    }
                }
                else if (impositionOrder == (int)ImpositionOrder.TwoUpRightToLeft &&
                    isPortrait)
                {
                    // Lower left to right
                    pageImageOffsetX -= marginBetweenPages + impositionPageAreaSize.Width;
                    if (((impositionPageIndex + 1) % pagesPerColumn) == 0)
                    {
                        pageImageOffsetX = initialOffsetX;
                        pageImageOffsetY -= marginBetweenPages + impositionPageAreaSize.Height;
                    }
                }
                else if (impositionOrder == (int)ImpositionOrder.FourUpUpperLeftToBottom)
                {
                    // Upper left to bottom
                    pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                    if (((impositionPageIndex + 1) % pagesPerRow) == 0)
                    {
                        pageImageOffsetY = initialOffsetY;
                        pageImageOffsetX += marginBetweenPages + impositionPageAreaSize.Width;
                    }
                }
                else if (impositionOrder == (int)ImpositionOrder.FourUpUpperRightToBottom)
                {
                    // Upper right to bottom
                    pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                    if (((impositionPageIndex + 1) % pagesPerRow) == 0)
                    {
                        pageImageOffsetY = initialOffsetY;
                        pageImageOffsetX -= marginBetweenPages + impositionPageAreaSize.Width;
                    }
                }
                else if ((impositionOrder == (int)ImpositionOrder.TwoUpRightToLeft && !isPortrait) ||
                    impositionOrder == (int)ImpositionOrder.FourUpUpperRightToLeft)
                {
                    // Upper right to left
                    pageImageOffsetX -= marginBetweenPages + impositionPageAreaSize.Width;
                    if (((impositionPageIndex + 1) % pagesPerColumn) == 0)
                    {
                        pageImageOffsetX = initialOffsetX;
                        pageImageOffsetY += marginBetweenPages + impositionPageAreaSize.Height;
                    }
                }

                ++impositionPageIndex;
            }
        }

        /// <summary>
        /// Changes the bitmap to grayscale
        /// </summary>
        /// <param name="canvasBitmap">canvas</param>
        /// <param name="cancellationToken">cancellation token</param>
        public static void GrayscalePageImage(WriteableBitmap canvasBitmap,
            CancellationTokenSource cancellationToken)
        {
            DispatcherHelper.CheckBeginInvokeOnUI(
                () =>
                {
                    byte[] pixelBytes = WriteableBitmapExtensions.ToByteArray(canvasBitmap);

                    // From http://social.msdn.microsoft.com/Forums/windowsapps/en-US/5ff10c14-51d4-4760-afe6-091624adc532/sample-code-for-making-a-bitmapimage-grayscale
                    for (int i = 0; i < pixelBytes.Length; i += 4)
                    {
                        if (cancellationToken.IsCancellationRequested)
                        {
                            return;
                        }

                        double b = (double)pixelBytes[i] / 255.0;
                        double g = (double)pixelBytes[i + 1] / 255.0;
                        double r = (double)pixelBytes[i + 2] / 255.0;
                        //byte a = pixelBytes[i + 3];

                        // Altered color factor to be equal
                        double bwPixel = (0.3 * r + 0.59 * g + 0.11 * b) * 255;
                        byte bwPixelByte = Convert.ToByte(bwPixel);

                        pixelBytes[i] = bwPixelByte;
                        pixelBytes[i + 1] = bwPixelByte;
                        pixelBytes[i + 2] = bwPixelByte;
                        //pixelBytes[i + 3] = a;
                    }

                    // Copy new pixels to bitmap
                    WriteableBitmapExtensions.FromByteArray(canvasBitmap, pixelBytes);
                });
        }

        /// <summary>
        /// Applies duplex into image with staple and punch as needed
        /// </summary>
        /// <param name="canvasBitmap">canvas</param>
        /// <param name="canvasSize">canvas size</param>
        /// <param name="duplexType">duplex</param>
        /// <param name="finishingSide">finishing side</param>
        /// <param name="punch">punch</param>
        /// <param name="enabledPunchFour">true when punch4 is enabled, false when punch3 is enabled</param>
        /// <param name="staple">staple</param>
        /// <param name="isPortrait">true when portrait, false, otherwise</param>
        /// <param name="isRightSide">true when page is on right side, false otherwise</param>
        /// <param name="isBackSide">true if for backside (duplex), false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        /// <returns>task</returns>
        public static async Task FormatPageImageForDuplex(WriteableBitmap canvasBitmap, Size canvasSize,
            int duplexType, int finishingSide, int punch, bool enabledPunchFour, int staple,
            bool isPortrait, bool isRightSide, bool isBackSide,
            CancellationTokenSource cancellationToken)
        {
            // Rotate image if needed
            if (!isRightSide || isBackSide)
            {
                if ((duplexType == (int)Duplex.LongEdge && !isPortrait) ||
                    (duplexType == (int)Duplex.ShortEdge && isPortrait))
                {
                    DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        PreviewPageImageUtility.OverlayImage(canvasBitmap, canvasSize,
                            WriteableBitmapExtensions.Rotate(canvasBitmap, 180), canvasSize, false, cancellationToken);
                    });
                }

                // Change the side of the staple if letf or right
                if (finishingSide == (int)FinishingSide.Left)
                {
                    finishingSide = (int)FinishingSide.Right;
                }
                else if (finishingSide == (int)FinishingSide.Right)
                {
                    finishingSide = (int)FinishingSide.Left;
                }
            }

            // Apply punch
            if (punch != (int)Punch.Off)
            {
                await OverlayPunch(canvasBitmap, punch, enabledPunchFour, finishingSide, cancellationToken);
            }

            // Apply staple
            if (staple != (int)Staple.Off)
            {
                await OverlayStaple(canvasBitmap, staple, finishingSide, false, false, cancellationToken);
            }
        }

        /// <summary>
        /// Applies booklet settings into a single page image
        /// </summary>
        /// <param name="canvasBitmap">canvas</param>
        /// <param name="bookletFinishing">booklet finishing</param>
        /// <param name="isPortrait">true when portrait, false otherwise</param>
        /// <param name="isRightSide">true when page is on right side, false otherwise</param>
        /// <param name="isBackSide">true if for backside (booklet), false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        /// <returns>task</returns>
        public static async Task FormatPageImageForBooklet(WriteableBitmap canvasBitmap, int bookletFinishing,
            bool isPortrait, bool isRightSide, bool isBackSide, CancellationTokenSource cancellationToken)
        {
            // Determine finishing side
            int bindingSide;
            if (isPortrait)
            {
                if ((isRightSide && isBackSide) || (!isRightSide && !isBackSide))
                {
                    bindingSide = (int)FinishingSide.Right;
                }
                else
                {
                    bindingSide = (int)FinishingSide.Left;
                }
            }
            else
            {
                if ((isRightSide && isBackSide) || (!isRightSide && !isBackSide))
                {
                    bindingSide = -1; // Out of range number to denote bottom
                }
                else
                {
                    bindingSide = (int)FinishingSide.Top;
                }
            }

            // Determine booklet type
            bool applyStaple = (bookletFinishing == (int)BookletFinishing.FoldAndStaple);

            // Apply staple at the edge based on finishing side
            if (applyStaple)
            {
                await OverlayStaple(canvasBitmap, 0, bindingSide, true, isRightSide, cancellationToken);
            }
        }

        // TODO: Does not handle thread here!!!
        /// <summary>
        /// Adds staple wire image into target page image specifying the booklet setting.
        /// </summary>
        /// <param name="canvasBitmap">canvas</param>
        /// <param name="staple">staple</param>
        /// <param name="finishingSide">position of staple</param>
        /// <param name="isBooklet">true when booklet is on, false otherwise</param>
        /// <param name="isRightSide">true when page is on right side, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        /// <returns>task</returns>
        public static async Task OverlayStaple(WriteableBitmap canvasBitmap, int staple,
            int finishingSide, bool isBooklet, bool isRightSide, CancellationTokenSource cancellationToken)
        {
            // Get staple image
            WriteableBitmap stapleBitmap = new WriteableBitmap(1, 1); // Size doesn't matter here yet
            StorageFile stapleFile = await StorageFileUtility.GetFileFromAppResource(FILE_PATH_RES_IMAGE_STAPLE);
            using (IRandomAccessStream raStream = await stapleFile.OpenReadAsync())
            {
                // Put staple image to a bitmap
                stapleBitmap = await WriteableBitmapExtensions.FromStream(null, raStream);
            }

            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            double targetScaleFactor =
                (double)(PrintSettingConstant.STAPLE_CROWN_LENGTH * ImageConstant.BASE_DPI)
                / stapleBitmap.PixelWidth;
            // Scale the staple image
            WriteableBitmap scaledStapleBitmap = WriteableBitmapExtensions.Resize(stapleBitmap,
                (int)(stapleBitmap.PixelWidth * targetScaleFactor),
                (int)(stapleBitmap.PixelHeight * targetScaleFactor),
                WriteableBitmapExtensions.Interpolation.Bilinear);

            if (isBooklet)
            {
                // Determine finishing side
                if (finishingSide == (int)FinishingSide.Top)
                {
                    OverlayRotateStaple(canvasBitmap, scaledStapleBitmap, 0, false, false,
                        canvasBitmap.PixelWidth, true, 0.25, 0, true, cancellationToken);
                    OverlayRotateStaple(canvasBitmap, scaledStapleBitmap, 0, true, false,
                        canvasBitmap.PixelWidth, true, 0.75, 0, true, cancellationToken);
                }
                else if (finishingSide == (int)FinishingSide.Left)
                {
                    OverlayRotateStaple(canvasBitmap, scaledStapleBitmap, 90, false, false,
                        canvasBitmap.PixelHeight, false, 0.25, 0, true, cancellationToken);
                    OverlayRotateStaple(canvasBitmap, scaledStapleBitmap, 90, false, true,
                        canvasBitmap.PixelHeight, false, 0.75, 0, true, cancellationToken);
                }
                else if (finishingSide == (int)FinishingSide.Right)
                {
                    OverlayRotateStaple(canvasBitmap, scaledStapleBitmap, 90, true, false,
                            canvasBitmap.PixelHeight, false, 0.25, 0, true, cancellationToken);
                    OverlayRotateStaple(canvasBitmap, scaledStapleBitmap, 90, true, true,
                        canvasBitmap.PixelHeight, false, 0.75, 0, true, cancellationToken);
                }
                else
                {
                    OverlayRotateStaple(canvasBitmap, scaledStapleBitmap, 0, false, true,
                        canvasBitmap.PixelWidth, true, 0.25, 0, true, cancellationToken);
                    OverlayRotateStaple(canvasBitmap, scaledStapleBitmap, 0, true, true,
                        canvasBitmap.PixelWidth, true, 0.75, 0, true, cancellationToken);
                }
            }
            else  // if (isBooklet)
            {
                // Determine finishing side
                if (finishingSide == (int)FinishingSide.Top)
                {
                    if (staple == (int)Staple.OneUpperLeft)
                    {
                        OverlayCornerStaple(canvasBitmap, scaledStapleBitmap, 135, false, false, cancellationToken);
                    }
                    else if (staple == (int)Staple.OneUpperRight)
                    {
                        OverlayCornerStaple(canvasBitmap, scaledStapleBitmap, 45, true, false, cancellationToken);
                    }
                    else if (staple == (int)Staple.Two)
                    {
                        OverlaySideStaple(canvasBitmap, scaledStapleBitmap, 0, false, false,
                            canvasBitmap.PixelWidth, true, 0.25, cancellationToken);
                        OverlaySideStaple(canvasBitmap, scaledStapleBitmap, 0, true, false,
                            canvasBitmap.PixelWidth, true, 0.75, cancellationToken);
                    }
                }
                else if (finishingSide == (int)FinishingSide.Left)
                {
                    if (staple == (int)Staple.One)
                    {
                        OverlayCornerStaple(canvasBitmap, scaledStapleBitmap, 135, false, false, cancellationToken);
                    }
                    else if (staple == (int)Staple.Two)
                    {
                        OverlaySideStaple(canvasBitmap, scaledStapleBitmap, 90, false, false,
                            canvasBitmap.PixelHeight, false, 0.25, cancellationToken);
                        OverlaySideStaple(canvasBitmap, scaledStapleBitmap, 90, false, true,
                            canvasBitmap.PixelHeight, false, 0.75, cancellationToken);
                    }
                }
                else if (finishingSide == (int)FinishingSide.Right)
                {
                    if (staple == (int)Staple.One)
                    {
                        OverlayCornerStaple(canvasBitmap, scaledStapleBitmap, 45, true, false, cancellationToken);
                    }
                    else if (staple == (int)Staple.Two)
                    {
                        OverlaySideStaple(canvasBitmap, scaledStapleBitmap, 270, true, false,
                            canvasBitmap.PixelHeight, false, 0.25, cancellationToken);
                        OverlaySideStaple(canvasBitmap, scaledStapleBitmap, 270, true, true,
                            canvasBitmap.PixelHeight, false, 0.75, cancellationToken);
                    }
                }
            } // if (isBooklet)
        }

        // TODO: Does not handle thread here!!!
        /// <summary>
        /// Adds punch hole image into page image
        /// </summary>
        /// <param name="canvasBitmap">canvas</param>
        /// <param name="punch">punch</param>
        /// <param name="enabledPunchFour">true when punch4 is enabled, false when punch3 is enabled</param>
        /// <param name="finishingSide">finishing side</param>
        /// <param name="cancellationToken">cancellation token</param>
        /// <returns>task</returns>
        public static async Task OverlayPunch(WriteableBitmap canvasBitmap, int punch,
            bool enabledPunchFour, int finishingSide, CancellationTokenSource cancellationToken)
        {
            int holeCount = GetPunchHoleCount(punch, enabledPunchFour);

            // Get punch image
            WriteableBitmap punchBitmap = new WriteableBitmap(1, 1); // Size doesn't matter here yet
            StorageFile stapleFile = await StorageFileUtility.GetFileFromAppResource(FILE_PATH_RES_IMAGE_PUNCH);
            using (IRandomAccessStream raStream = await stapleFile.OpenReadAsync())
            {
                // Put staple image to a bitmap
                punchBitmap = await WriteableBitmapExtensions.FromStream(null, raStream);
            }

            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            double targetScaleFactor =
                (double)(PrintSettingConstant.PUNCH_HOLE_DIAMETER * ImageConstant.BASE_DPI)
                / punchBitmap.PixelWidth;
            // Scale the staple image
            WriteableBitmap scaledPunchBitmap = WriteableBitmapExtensions.Resize(punchBitmap,
                (int)(punchBitmap.PixelWidth * targetScaleFactor),
                (int)(punchBitmap.PixelHeight * targetScaleFactor),
                WriteableBitmapExtensions.Interpolation.Bilinear);

            // Determine punch
            double diameterPunch = PrintSettingConstant.PUNCH_HOLE_DIAMETER * ImageConstant.BASE_DPI;
            double marginPunch = PrintSettingConstant.MARGIN_PUNCH * ImageConstant.BASE_DPI;
            double distanceBetweenHoles = GetDistanceBetweenHoles(enabledPunchFour, punch);
            if (finishingSide == (int)FinishingSide.Top)
            {
                double startPos = GetPunchStartPosition(canvasBitmap.PixelWidth, true, holeCount,
                    diameterPunch, marginPunch, distanceBetweenHoles);
                OverlayScalePunch(canvasBitmap, scaledPunchBitmap, holeCount, startPos, false, true,
                    diameterPunch, marginPunch, distanceBetweenHoles, cancellationToken);
            }
            else if (finishingSide == (int)FinishingSide.Left)
            {
                double startPos = GetPunchStartPosition(canvasBitmap.PixelHeight, false, holeCount,
                    diameterPunch, marginPunch, distanceBetweenHoles);
                OverlayScalePunch(canvasBitmap, scaledPunchBitmap, holeCount, startPos, false, false,
                    diameterPunch, marginPunch, distanceBetweenHoles, cancellationToken);
            }
            else if (finishingSide == (int)FinishingSide.Right)
            {
                double startPos = GetPunchStartPosition(canvasBitmap.PixelHeight, false, holeCount,
                    diameterPunch, marginPunch, distanceBetweenHoles);
                OverlayScalePunch(canvasBitmap, scaledPunchBitmap, holeCount, startPos, true, false,
                    diameterPunch, marginPunch, distanceBetweenHoles, cancellationToken);
            }
        }

        /// <summary>
        /// Adds dash lines at the edge of an image
        /// </summary>
        /// <param name="canvasBitmap">canvas</param>
        /// <param name="canvasSize">canvas size</param>
        /// <param name="isRightSide">true when page is on right side, false otherwise</param>
        /// <param name="isPortrait">true when portrait, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        public static void OverlayDashLineToEdge(WriteableBitmap canvasBitmap, Size canvasSize,
            bool isRightEdge, bool isPortrait, CancellationTokenSource cancellationToken)
        {
            int point = 0;

            if (isPortrait)
            {
                if (isRightEdge) // for left page
                {
                    do
                    {
                        if (cancellationToken.IsCancellationRequested)
                        {
                            return;
                        }

                        Point p1 = new Point(canvasSize.Width - 1, point);
                        Point p2 = new Point(canvasSize.Width - 1, point +
                            PrintSettingConstant.DASH_LINE_LENGTH);
                        point += PrintSettingConstant.DASH_LINE_LENGTH * 2;
                        DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        WriteableBitmapExtensions.DrawLine(canvasBitmap, (int)p1.X, (int)p1.Y,
                            (int)p2.X, (int)p2.Y, Windows.UI.Colors.Gray);
                    });
                    } while (point < canvasSize.Height);
                }
                else // for right page
                {
                    do
                    {
                        if (cancellationToken.IsCancellationRequested)
                        {
                            return;
                        }

                        Point p1 = new Point(0, point);
                        Point p2 = new Point(0, point + PrintSettingConstant.DASH_LINE_LENGTH);
                        point += PrintSettingConstant.DASH_LINE_LENGTH * 2;
                        DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        WriteableBitmapExtensions.DrawLine(canvasBitmap, (int)p1.X, (int)p1.Y,
                            (int)p2.X, (int)p2.Y, Windows.UI.Colors.Gray);
                    });
                    } while (point < canvasSize.Height);
                }
            }
            else // for landscape
            {
                if (isRightEdge) // for left page
                {
                    do
                    {
                        if (cancellationToken.IsCancellationRequested)
                        {
                            return;
                        }

                        Point p1 = new Point(point, canvasSize.Height - 1);
                        Point p2 = new Point(point + PrintSettingConstant.DASH_LINE_LENGTH,
                            canvasSize.Height - 1);
                        point += PrintSettingConstant.DASH_LINE_LENGTH * 2;
                        DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        WriteableBitmapExtensions.DrawLine(canvasBitmap, (int)p1.X, (int)p1.Y,
                            (int)p2.X, (int)p2.Y, Windows.UI.Colors.Gray);
                    });
                    } while (point < canvasSize.Width);
                }
                else // for right page
                {
                    do
                    {
                        if (cancellationToken.IsCancellationRequested)
                        {
                            return;
                        }

                        Point p1 = new Point(point, 0);
                        Point p2 = new Point(point + PrintSettingConstant.DASH_LINE_LENGTH, 0);
                        point += PrintSettingConstant.DASH_LINE_LENGTH * 2;
                        DispatcherHelper.CheckBeginInvokeOnUI(
                    () =>
                    {
                        WriteableBitmapExtensions.DrawLine(canvasBitmap, (int)p1.X, (int)p1.Y,
                            (int)p2.X, (int)p2.Y, Windows.UI.Colors.Gray);
                    });
                    } while (point < canvasSize.Width);
                }
            }
        }

        #region Private Methods

        /// <summary>
        /// Computes the imposition area size for a single page
        /// </summary>
        /// <param name="canvasSize">canvas size</param>
        /// <param name="numRows">number of rows based on imposition</param>
        /// <param name="numColumns">number of columns based on imposition</param>
        /// <param name="marginBetween">margin between pages (in pixels)</param>
        /// <param name="marginOuter">margin of the preview page image (in pixels)</param>
        /// <returns>size of a page for imposition</returns>
        /// <returns>size of a page for imposition</returns>
        private static Size GetImpositionSinglePageAreaSize(Size canvasSize,
            int numRows, int numColumns, double marginBetween, double marginOuter)
        {
            Size pageAreaSize = new Size();
            if (canvasSize.Width > 0 && canvasSize.Height > 0 && numRows > 0 && numColumns > 0)
            {
                pageAreaSize.Width = (canvasSize.Width - (marginBetween * (numColumns - 1)) - (marginOuter * 2))
                    / numColumns;
                pageAreaSize.Height = (canvasSize.Width - (marginBetween * (numRows - 1)) - (marginOuter * 2))
                    / numRows;
            }
            return pageAreaSize;
        }

        /// <summary>
        /// Gets the number of punch holes based on punch type
        /// </summary>
        /// <param name="punch">punch type</param>
        /// <returns>number of punch holes</returns>
        private static int GetPunchHoleCount(int punch, bool enabledPunchFour)
        {
            int numberOfHoles = 0;
            switch (punch)
            {
                case (int)Punch.TwoHoles:
                    numberOfHoles = 2;
                    break;
                case (int)Punch.FourHoles:
                    numberOfHoles = 4;
                    if (!enabledPunchFour)
                    {
                        numberOfHoles = 3;
                    }
                    break;
                case (int)Punch.Off:
                default:
                    // Do nothing
                    break;
            }

            return numberOfHoles;
        }

        /// <summary>
        /// Computes the distance between punch holes based on number of punches
        /// </summary>
        /// <param name="enabledPunchFour">true when punch4 is enabled, false when punch3 is enabled</param>
        /// <param name="punch">punch type</param>
        /// <returns>distance</returns>
        private static double GetDistanceBetweenHoles(bool enabledPunchFour, int punch)
        {
            double distance = 0;
            switch (punch)
            {
                case (int)Punch.TwoHoles:
                    distance = PrintSettingConstant.PUNCH_BETWEEN_TWO_HOLES_DISTANCE;
                    break;
                case (int)Punch.FourHoles:
                    distance = (enabledPunchFour) ?
                        PrintSettingConstant.PUNCH_BETWEEN_FOUR_HOLES_DISTANCE :
                        PrintSettingConstant.PUNCH_BETWEEN_THREE_HOLES_DISTANCE;
                    break;
                case (int)Punch.Off:
                default:
                    // Do nothing
                    break;
            }

            return distance * ImageConstant.BASE_DPI;
        }

        /// <summary>
        /// Computes the starting position of the punch hole image
        /// </summary>
        /// <param name="edgeLength">length of page image edge where punch will be placed</param>
        /// <param name="isAlongXAxis">direction of punch holes</param>
        /// <param name="holeCount">number of punch holes</param>
        /// <param name="diameterPunch">size of punch hole</param>
        /// <param name="marginPunch">margin of punch hole against edge of page image</param>
        /// <param name="distanceBetweenHoles">distance between punch holes</param>
        /// <returns>starting position of the first punch hole</returns>
        private static double GetPunchStartPosition(double edgeLength, bool isAlongXAxis, int holeCount,
            double diameterPunch, double marginPunch, double distanceBetweenHoles)
        {
            double startPos = (edgeLength - (holeCount * diameterPunch) -
                                ((holeCount - 1) * distanceBetweenHoles)) / 2;
            return startPos;
        }

        /// <summary>
        /// Scales the logical page image into the preview page image
        /// </summary>
        /// <param name="canvasBitmap">canvas bitmap</param>
        /// <param name="canvasSize">canvas size</param>
        /// <param name="overlayBitmap">overlay image</param>
        /// <param name="overlaySize">overlay size</param>
        /// <param name="cancellationToken">cancellation token</param>
        private static void ScaleImageToFit(WriteableBitmap canvasBitmap, Size canvasSize,
            WriteableBitmap overlayBitmap, Size overlaySize, CancellationTokenSource cancellationToken)
        {
            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            Size scaledSize = GetScaledSize(canvasSize, overlaySize);

            // Compute position in preview page image
            Rect srcRect = new Rect(0, 0, overlaySize.Width, overlaySize.Height);
            Rect destRect = new Rect(
                (canvasSize.Width - scaledSize.Width) / 2,    // Puts the image to the center X
                (canvasSize.Height - scaledSize.Height) / 2,  // Puts the image to the center Y
                scaledSize.Width, scaledSize.Height);

            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            DispatcherHelper.CheckBeginInvokeOnUI(
            () =>
            {
                WriteableBitmapExtensions.Blit(canvasBitmap, destRect, overlayBitmap, srcRect);
            });
        }

        /// <summary>
        /// Computes the scaling size
        /// </summary>
        /// <param name="canvasSize">canvas size</param>
        /// <param name="overlaySize">overlay size</param>
        /// <returns>scaled size</returns>
        private static Size GetScaledSize(Size canvasSize, Size overlaySize)
        {
            double scaleX = canvasSize.Width / overlaySize.Width;
            double scaleY = canvasSize.Height / overlaySize.Height;
            double targetScaleFactor = (scaleX < scaleY) ? scaleX : scaleY;

            return new Size(overlaySize.Width * targetScaleFactor, overlaySize.Height * targetScaleFactor);
        }

        /// <summary>
        /// Adds a staple image. Requires that the staple image is already scaled.
        /// For single staple and non-booklet only.
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleBitmap">staple image; required to be scaled beforehand</param>
        /// <param name="angle">angle for rotation</param>
        /// <param name="isXEnd">true when staple is to be placed near the end along X-axis</param>
        /// <param name="isYEnd">true when staple is to be placed near the end along Y-axis</param>
        /// <param name="cancellationToken">cancellation token</param>
        private static void OverlayCornerStaple(WriteableBitmap canvasBitmap, WriteableBitmap stapleBitmap,
            int angle, bool isXEnd, bool isYEnd, CancellationTokenSource cancellationToken)
        {
            OverlayRotateStaple(canvasBitmap, stapleBitmap, angle, isXEnd, isYEnd, 0, false, 0,
                PrintSettingConstant.MARGIN_STAPLE * ImageConstant.BASE_DPI, false, cancellationToken);
        }

        /// <summary>
        /// Adds a staple image. Requires that the staple image is already scaled.
        /// For double staple and non-booklet only.
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleBitmap">staple image; required to be scaled beforehand</param>
        /// <param name="angle">angle for rotation</param>
        /// <param name="isXEnd">true when staple is to be placed near the end along X-axis</param>
        /// <param name="isYEnd">true when staple is to be placed near the end along Y-axis</param>
        /// <param name="edgeLength">length of page image edge where staples will be placed; used with dual staple</param>
        /// <param name="isAlongXAxis">location of punch holes; used with dual staple</param>
        /// <param name="positionPercentage">relative location from edge length; used with dual staple</param>
        /// <param name="cancellationToken">cancellation token</param>
        private static void OverlaySideStaple(WriteableBitmap canvasBitmap, WriteableBitmap stapleBitmap,
            int angle, bool isXEnd, bool isYEnd, int edgeLength, bool isAlongXAxis,
            double positionPercentage, CancellationTokenSource cancellationToken)
        {
            OverlayRotateStaple(canvasBitmap, stapleBitmap, angle, isXEnd, isYEnd, edgeLength, isAlongXAxis,
                positionPercentage, PrintSettingConstant.MARGIN_STAPLE * ImageConstant.BASE_DPI, false, cancellationToken);
        }

        /// <summary>
        /// Adds a staple image. Requires that the staple image is already scaled.
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="stapleBitmap">staple image; required to be scaled beforehand</param>
        /// <param name="angle">angle for rotation</param>
        /// <param name="isXEnd">true when staple is to be placed near the end along X-axis</param>
        /// <param name="isYEnd">true when staple is to be placed near the end along Y-axis</param>
        /// <param name="edgeLength">length of page image edge where staples will be placed; used with dual staple</param>
        /// <param name="isAlongXAxis">location of punch holes; used with dual staple</param>
        /// <param name="positionPercentage">relative location from edge length; used with dual staple</param>
        /// <param name="marginStaple">margin from edge</param>
        /// <param name="isBooklet">true when applied with booklet, false otherwise</param>
        /// <param name="cancellationToken">cancellation token</param>
        private static void OverlayRotateStaple(WriteableBitmap canvasBitmap, WriteableBitmap stapleBitmap,
            int angle, bool isXEnd, bool isYEnd, int edgeLength, bool isAlongXAxis,
            double positionPercentage, double marginStaple, bool isBooklet, CancellationTokenSource cancellationToken)
        {
            // Rotate
            WriteableBitmap rotatedStapleBitmap = stapleBitmap;
            if (angle > 0)
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                rotatedStapleBitmap = WriteableBitmapExtensions.RotateFree(stapleBitmap, angle, false);
            }

            double destXOrigin;
            if (positionPercentage > 0 && isAlongXAxis)
            {
                destXOrigin = (edgeLength * positionPercentage) - (rotatedStapleBitmap.PixelWidth / 2);
            }
            else if (isXEnd && isBooklet)
            {
                destXOrigin = canvasBitmap.PixelWidth - (rotatedStapleBitmap.PixelWidth / 2) - marginStaple;
            }
            else if (isXEnd && !isBooklet)
            {
                destXOrigin = canvasBitmap.PixelWidth - rotatedStapleBitmap.PixelWidth - marginStaple;
            }
            else if (!isXEnd && isBooklet)
            {
                destXOrigin = 0 - (rotatedStapleBitmap.PixelWidth / 2);
            }
            else
            {
                destXOrigin = marginStaple;
            }

            double destYOrigin;
            if (positionPercentage > 0 && !isAlongXAxis)
            {
                destYOrigin = (edgeLength * positionPercentage) - (rotatedStapleBitmap.PixelHeight / 2);
            }
            else if (isYEnd && isBooklet)
            {
                destYOrigin = canvasBitmap.PixelHeight - (rotatedStapleBitmap.PixelHeight / 2) - marginStaple;
            }
            else if (isYEnd && !isBooklet)
            {
                destYOrigin = canvasBitmap.PixelHeight - rotatedStapleBitmap.PixelHeight - marginStaple;
            }
            else if (!isYEnd && isBooklet)
            {
                destYOrigin = 0 - (rotatedStapleBitmap.PixelHeight / 2);
            }
            else
            {
                destYOrigin = marginStaple;
            }

            Rect destRect = new Rect(destXOrigin, destYOrigin, rotatedStapleBitmap.PixelWidth,
                rotatedStapleBitmap.PixelHeight);
            Rect srcRect = new Rect(0, 0, rotatedStapleBitmap.PixelWidth, rotatedStapleBitmap.PixelHeight);

            if (cancellationToken.IsCancellationRequested)
            {
                return;
            }

            WriteableBitmapExtensions.Blit(canvasBitmap, destRect, rotatedStapleBitmap, srcRect);
        }

        /// <summary>
        /// Adds punch hole images
        /// </summary>
        /// <param name="canvasBitmap">destination image</param>
        /// <param name="punchBitmap">punch hole image</param>
        /// <param name="holeCount">number of punch holes</param>
        /// <param name="startPos">starting position</param>
        /// <param name="isXEnd">true when punch holes are to be placed near the end along X-axis</param>
        /// <param name="isAlongXAxis">true when punch holes are to be placed horizontally</param>
        /// <param name="diameterPunch">size of punch hole</param>
        /// <param name="marginPunch">margin of punch hole against edge of page image</param>
        /// <param name="distanceBetweenHoles">distance between punch holes</param>
        /// <param name="cancellationToken">cancellation token</param>
        private static void OverlayScalePunch(WriteableBitmap canvasBitmap, WriteableBitmap punchBitmap,
            int holeCount, double startPos, bool isXEnd, bool isAlongXAxis, double diameterPunch,
            double marginPunch, double distanceBetweenHoles, CancellationTokenSource cancellationToken)
        {
            double endMarginPunch = (isXEnd) ? canvasBitmap.PixelWidth - diameterPunch - marginPunch : marginPunch;

            double currPos = startPos;
            for (int index = 0; index < holeCount; ++index, currPos += diameterPunch + distanceBetweenHoles)
            {
                if (cancellationToken.IsCancellationRequested)
                {
                    return;
                }

                // Do not put punch hole image when it is outside the page image size
                if (currPos < 0 || (isAlongXAxis && currPos > canvasBitmap.PixelWidth) ||
                    (!isAlongXAxis && currPos > canvasBitmap.PixelHeight))
                {
                    continue;
                }

                double destXOrigin = (isAlongXAxis) ? currPos : endMarginPunch;
                double destYOrigin = (isAlongXAxis) ? marginPunch : currPos;
                Rect destRect = new Rect(destXOrigin, destYOrigin, punchBitmap.PixelWidth,
                    punchBitmap.PixelHeight);
                Rect srcRect = new Rect(0, 0, punchBitmap.PixelWidth, punchBitmap.PixelHeight);
                WriteableBitmapExtensions.Blit(canvasBitmap, destRect, punchBitmap, srcRect);
            }
        }

        #endregion Private Methods

    }
}
