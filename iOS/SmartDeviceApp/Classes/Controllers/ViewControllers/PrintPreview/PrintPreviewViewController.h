//
//  PrintPreviewViewController.h
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PDFPageContentViewController.h"

@interface PrintPreviewViewController : UIViewController <PDFPageContentViewControllerDatasource, UIPageViewControllerDataSource, UIPageViewControllerDelegate, UIScrollViewDelegate>

/**
 Method to invoke loading of print preview elements in the screen
 **/
- (void) loadPrintPreview;

@property (weak, nonatomic) IBOutlet UIButton *mainMenuButton;
@property (weak, nonatomic) IBOutlet UIButton *printSettingsButton;

@end
