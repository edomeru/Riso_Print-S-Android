//
//  PDFPageViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
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


@end
