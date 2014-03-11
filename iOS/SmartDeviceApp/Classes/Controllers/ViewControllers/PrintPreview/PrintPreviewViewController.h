//
//  PrintPreviewViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PDFPageContentViewController.h"

@interface PrintPreviewViewController : UIViewController <PDFPageViewContentDelegate, UIPageViewControllerDataSource, UIPageViewControllerDelegate>

- (void) loadPrintPreview;

@end
