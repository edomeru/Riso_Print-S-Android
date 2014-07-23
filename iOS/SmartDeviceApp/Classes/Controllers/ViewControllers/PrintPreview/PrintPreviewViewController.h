//
//  PrintPreviewViewController.h
//  Tester
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PDFRenderOperation.h"
#import "PrintDocument.h"
#import "PreviewView.h"
#import "RenderCache.h"

/**
 * Controller for the "Print Preview" screen (phone and tablet).
 */
@interface PrintPreviewViewController : UIViewController<UIPageViewControllerDataSource, UIPageViewControllerDelegate, PreviewViewDelegate, PrintDocumentDelegate, PDFRenderOperationDelegate, RenderCacheDelegate>

@end
