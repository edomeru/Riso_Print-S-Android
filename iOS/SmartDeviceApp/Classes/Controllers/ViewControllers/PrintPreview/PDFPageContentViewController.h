//
//  PDFPageViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 * Controller for the rendered page.
 * This is the content loaded by the PrintPreviewViewController into
 * its preview area (UIPageViewController).\n
 * The content is essentially an image from the PDF file with the
 * preview settings applied.
 *
 * @see PrintPreviewViewController
 * @see PDFRenderOperation
 */
@interface PDFPageContentViewController : UIViewController

/**
 * Page number.
 * This refers to the page number with the preview settings applied
 * (i.e. Booklet, Imposition,..) rather than the page in the actual
 * PDF file.
 */
@property (nonatomic) NSInteger pageIndex;

/**
 * Rendered image of the PDF page.
 * 
 * @see PDFRenderOperation
 */
@property (nonatomic) UIImage *image;

/**
 * Flag to indicate that the page is used as a bookend.
 */
@property (nonatomic) BOOL isBookendPage;

@end
