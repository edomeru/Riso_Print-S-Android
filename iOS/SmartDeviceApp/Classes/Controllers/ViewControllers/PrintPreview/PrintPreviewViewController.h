//
//  PrintPreviewViewController.h
//  Tester
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PrintDocument.h"

@interface PrintPreviewViewController : UIViewController<UIPageViewControllerDataSource, PrintDocumentDelegate>

@end
