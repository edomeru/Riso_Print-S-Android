//
//  PDFPageViewController.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PDFPageContentViewController : UIViewController

/**
 Page number
 */
@property (nonatomic) NSInteger pageIndex;

/**
 Rendered image of the PDF page
 */
@property (nonatomic) UIImage *image;

/**
 Flag to indicate that page is used as book end
 */
@property (nonatomic) BOOL isBookendPage;

@end
