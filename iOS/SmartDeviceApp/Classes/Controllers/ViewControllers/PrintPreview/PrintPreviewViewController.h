//
//  PrintPreviewViewController.h
//  Tester
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PDFRenderOperation.h"
#import "PrintDocument.h"
#import "PreviewView.h"
#import "RenderCache.h"

@interface PrintPreviewViewController : UIViewController<UIPageViewControllerDataSource, UIPageViewControllerDelegate, PreviewViewDelegate, PrintDocumentDelegate, PDFRenderOperationDelegate, RenderCacheDelegate>

@end
