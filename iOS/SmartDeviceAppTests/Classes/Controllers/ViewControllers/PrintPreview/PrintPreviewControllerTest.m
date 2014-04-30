//
//  PrintPreviewControllerTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 4/30/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintPreviewViewController.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "Printer.h"
#import "PrintDocument.h"


@interface PrintPreviewViewController (Test)


@property (nonatomic, weak) IBOutlet UILabel *titleLabel;
@property (nonatomic, weak) IBOutlet UIButton *mainMenuButton;
@property (nonatomic, weak) IBOutlet UIButton *printSettingsButton;
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicator;
@property (nonatomic, weak) IBOutlet UIView *previewArea;
@property (weak, nonatomic) IBOutlet UIView *pageNavArea;
@property (weak, nonatomic) IBOutlet UISlider *pageScroll;
@property (weak, nonatomic) IBOutlet UILabel *pageLabel;
@property (weak, nonatomic) IBOutlet UIView *splashView;
@property (nonatomic, weak) IBOutlet PreviewView *previewView;


@property (nonatomic, weak) UIPageViewController *pageViewController;
@property (nonatomic, weak) PrintDocument *printDocument;
@property (nonatomic) BOOL pageIsAnimating;
@property (nonatomic) NSInteger totalPageNum;
@property (nonatomic) NSInteger layoutPageNum;
@property (atomic, strong) NSOperationQueue *renderQueue;
@property (atomic, strong) NSMutableArray *renderArray;
@property (atomic, strong) RenderCache *renderCache;

- (void)loadPDF;
- (void)setupPreview;
- (void)setupPageviewControllerWithSpineLocation:(UIPageViewControllerSpineLocation)spineLocation navigationOrientation:(UIPageViewControllerNavigationOrientation)navigationOrientation;;

- (void)setupPageviewControllerWithBindSetting;
- (void)setupTotalPageNum;
- (void)setupPageLabel;
- (void)setupDisplayAspectRatio;
- (UIViewController *)nextViewController:(NSInteger)index;
- (UIViewController *)previousViewController:(NSInteger)index;
- (PDFPageContentViewController *)viewControllerAtIndex:(NSInteger)index;
- (void)goToPage:(NSInteger)pageNum;
- (IBAction)unwindToPrintPreview:(UIStoryboardSegue *)segue;
- (IBAction)mainMenuAction:(id)sender;
- (IBAction)printSettingsAction:(id)sender;
- (IBAction)dragPageScrollAction:(id)sender withEvent:(UIEvent *)event;
- (IBAction)tapPageScrollAction:(id)sender;
@end

@interface PrintPreviewControllerTest : GHTestCase

@end


@implementation PrintPreviewControllerTest
{
    NSInteger printerTesDataCount;
    NSString* storyboardId;
    NSURL *testURL;
    NSURL *pdfOriginalFileURL;
}

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)setUpClass
{
    storyboardId =@"PrintPreviewViewController";
    
    pdfOriginalFileURL= [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    NSString *testFilePath = [documentsDir stringByAppendingString: [NSString stringWithFormat:@"/%@",[pdfOriginalFileURL.path lastPathComponent]]];
    
    testURL = [NSURL URLWithString:[testFilePath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    printerTesDataCount = 5;
}

- (void) setUp
{
    NSError *error;
    [[NSFileManager defaultManager] copyItemAtPath:[pdfOriginalFileURL path] toPath: [testURL path] error:&error];
    
    //setup managers to init with data
    //add test printer data
    for(int i = 0; i < printerTesDataCount; i++)
    {
        PrinterDetails *pd = [[PrinterDetails alloc] init];
        pd.name = [NSString stringWithFormat:@"Printer %d", i];
        pd.ip = [NSString stringWithFormat:@"192.168.2.%d", i];
        [[PrinterManager sharedPrinterManager] registerPrinter:pd];
    }
    //create test default printer
    Printer *printer = [[PrinterManager sharedPrinterManager] getPrinterAtIndex:0];
    [[PrinterManager sharedPrinterManager] registerDefaultPrinter:printer];
    NSURL *dummyURL = [testURL URLByDeletingPathExtension];
    [[PDFFileManager sharedManager] setFileAvailableForLoad:NO];
    [[PDFFileManager sharedManager] setFileURL:dummyURL];
    [[PDFFileManager sharedManager] setupDocument];
}

-(void)tearDown
{
    while([[PrinterManager sharedPrinterManager] countSavedPrinters] > 0)
    {
        [[PrinterManager sharedPrinterManager] deletePrinterAtIndex:0];
    }
}

- (void)test_001_UIViewBinding
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    [viewController view];
    GHAssertNotNil(viewController.titleLabel, @"");
    GHAssertNotNil(viewController.mainMenuButton, @"");
    GHAssertNotNil(viewController.printSettingsButton, @"");
    GHAssertNotNil(viewController.previewArea, @"");
    GHAssertNotNil(viewController.pageNavArea, @"");
    GHAssertNotNil(viewController.pageScroll, @"");
    GHAssertNotNil(viewController.pageLabel, @"");
    GHAssertNotNil(viewController.splashView, @"");
    GHAssertNotNil(viewController.previewView, @"");
}

- (void)test_001_UIViewLoading_NoPreview
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    [viewController view];
    GHAssertTrue(viewController.previewView.hidden, @"");
    GHAssertTrue(viewController.pageNavArea.hidden, @"");
    GHAssertTrue(viewController.printSettingsButton.hidden, @"");
    GHAssertFalse(viewController.splashView.hidden, @"");
    GHAssertTrue(viewController.activityIndicator.hidden, @"");
}

- (void)test_001_UIViewLoading_WithFileForLoad
{
    [[PDFFileManager sharedManager] setFileAvailableForLoad:YES];
    [[PDFFileManager sharedManager] setFileURL:testURL];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    [viewController view];
    
   
    GHAssertTrue(viewController.activityIndicator.isAnimating, @"");
    GHAssertFalse(viewController.activityIndicator.hidden, @"");
}

- (void)test_001_UIViewLoading_WithPreview
{
    [[PDFFileManager sharedManager] setFileURL:testURL];
    [[PDFFileManager sharedManager] setupDocument];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    [viewController view];
    
    GHAssertFalse(viewController.previewView.hidden, @"");
    GHAssertFalse(viewController.pageNavArea.hidden, @"");
    GHAssertFalse(viewController.printSettingsButton.hidden, @"");
    GHAssertTrue(viewController.splashView.hidden, @"");
    GHAssertFalse(viewController.activityIndicator.isAnimating, @"");
    GHAssertTrue(viewController.activityIndicator.hidden, @"");
}
@end
