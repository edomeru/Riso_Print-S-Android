//
//  PrintPreviewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintPreviewViewController.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrinterDetails.h"
#import "Printer.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "PrintPreviewHelper.h"
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

- (void)setupPageviewControllerWithBindSetting;
- (void)setupTotalPageNum;
@end

@interface PrintPreviewControllerTest : GHTestCase

@end


@implementation PrintPreviewControllerTest
{
    NSInteger printerTesDataCount;
    NSString* storyboardId;
    NSURL *testURL;
    NSURL *pdfOriginalFileURL;
    NSURL *previewURL;
    float tolerance;
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
    previewURL = [NSURL URLWithString:[[documentsDir stringByAppendingString: @"/SDAPreview.pdf"] stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
    printerTesDataCount = 5;
    
    tolerance = 0.001f;
}

- (void)setUp
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

- (void)test001_UIViewBinding
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    GHAssertNotNil(viewController.view, @"");
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

- (void)test002_UIViewLoading_NoPreview
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    GHAssertNotNil(viewController.view, @"");
    GHAssertTrue(viewController.previewView.hidden, @"");
    GHAssertTrue(viewController.pageNavArea.hidden, @"");
    GHAssertTrue(viewController.printSettingsButton.hidden, @"");
    GHAssertFalse(viewController.splashView.hidden, @"");
    GHAssertTrue(viewController.activityIndicator.hidden, @"");
}

- (void)test003_UIViewLoading_WithFileForLoad
{
    [[PDFFileManager sharedManager] setFileAvailableForLoad:YES];
    [[PDFFileManager sharedManager] setFileURL:testURL];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
   
    GHAssertTrue(viewController.activityIndicator.isAnimating, @"");
    GHAssertFalse(viewController.activityIndicator.hidden, @"");
}

- (void)test004_UIViewLoading_WithPreview
{
    [[PDFFileManager sharedManager] setFileURL:testURL];
    [[PDFFileManager sharedManager] setupDocument];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    GHAssertFalse(viewController.previewView.hidden, @"");
    GHAssertFalse(viewController.pageNavArea.hidden, @"");
    GHAssertFalse(viewController.printSettingsButton.hidden, @"");
    GHAssertTrue(viewController.splashView.hidden, @"");
    GHAssertFalse(viewController.activityIndicator.isAnimating, @"");
    GHAssertTrue(viewController.activityIndicator.hidden, @"");
}

- (void)test005_setupPageviewControllerWithBindSetting_FinishingSide
{
    [[PDFFileManager sharedManager] setFileURL:testURL];
    [[PDFFileManager sharedManager] setupDocument];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    //change the print document so as not to trigger observer
    PrintDocument *printDocument = [[PrintDocument alloc] init];
    viewController.printDocument = printDocument;
    
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    printDocument.previewSetting = previewSetting;
    
    
    previewSetting.duplex = kDuplexSettingOff;
    previewSetting.booklet = NO;
    previewSetting.finishingSide = kFinishingSideRight;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMax, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationHorizontal, @"");
    GHAssertFalse(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.finishingSide = kFinishingSideTop;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMin, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationVertical, @"");
    GHAssertFalse(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.finishingSide = kFinishingSideLeft;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMin, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationHorizontal, @"");
    GHAssertFalse(viewController.pageViewController.isDoubleSided, @"");
}

- (void)test006_setupPageviewControllerWithBindSetting_Duplex
{
    [[PDFFileManager sharedManager] setFileURL:testURL];
    [[PDFFileManager sharedManager] setupDocument];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    //change the print document so as not to trigger observer
    PrintDocument *printDocument = [[PrintDocument alloc] init];
    viewController.printDocument = printDocument;
    
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    printDocument.previewSetting = previewSetting;
    
    previewSetting.booklet = NO;
    previewSetting.duplex = kDuplexSettingLongEdge;

    previewSetting.finishingSide = kFinishingSideRight;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationHorizontal, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.finishingSide = kFinishingSideTop;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationVertical, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.finishingSide = kFinishingSideLeft;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationHorizontal, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.duplex = kDuplexSettingShortEdge;
    
    previewSetting.finishingSide = kFinishingSideRight;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationHorizontal, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.finishingSide = kFinishingSideTop;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationVertical, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.finishingSide = kFinishingSideLeft;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationHorizontal, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
}

- (void)test007_setupPageviewControllerWithBindSetting_Booklet
{
    [[PDFFileManager sharedManager] setFileURL:testURL];
    [[PDFFileManager sharedManager] setupDocument];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    //change the print document so as not to trigger observer
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:previewURL name:[testURL lastPathComponent]];
    viewController.printDocument = printDocument;
    
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    printDocument.previewSetting = previewSetting;
    
    previewSetting.duplex = kDuplexSettingShortEdge;
    previewSetting.booklet = YES;
    previewSetting.finishingSide = kFinishingSideLeft;
    
    previewSetting.bookletLayout = kBookletLayoutLeftToRight;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationHorizontal, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.bookletLayout = kBookletLayoutTopToBottom;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationVertical, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
    
    previewSetting.bookletLayout = kBookletLayoutRightToLeft;
    [viewController setupPageviewControllerWithBindSetting];
    GHAssertNotNil(viewController.pageViewController, @"");
    GHAssertEquals(viewController.pageViewController.spineLocation, UIPageViewControllerSpineLocationMid, @"");
    GHAssertEquals(viewController.pageViewController.navigationOrientation, UIPageViewControllerNavigationOrientationHorizontal, @"");
    GHAssertTrue(viewController.pageViewController.isDoubleSided, @"");
    
}

- (void)test008_setupTotalPageNum_Normal
{
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    [self performTotalPageNumTest:testURL withPreviewSetting:previewSetting];
}

- (void)test008_setupTotalPageNum_Imposition
{
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    previewSetting.imposition = kImposition2Pages;
    [self performTotalPageNumTest:testURL withPreviewSetting:previewSetting];
}

- (void)test008_setupTotalPageNum_Duplex
{
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    previewSetting.duplex = kDuplexSettingLongEdge;
    [self performTotalPageNumTest:testURL withPreviewSetting:previewSetting];
}

- (void)test008_setupTotalPageNum_Booklet
{
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    previewSetting.booklet = YES;
    [self performTotalPageNumTest:testURL withPreviewSetting:previewSetting];
}

- (void) performTotalPageNumTest:(NSURL *) pdfURL withPreviewSetting: (PreviewSetting *)previewSetting
{

    [[PDFFileManager sharedManager] setFileURL:pdfURL];
    [[PDFFileManager sharedManager] setupDocument];
    
    CGPDFDocumentRef pdfDocumentRef = CGPDFDocumentCreateWithURL((__bridge CFURLRef)previewURL);
    NSUInteger pageCount = CGPDFDocumentGetNumberOfPages(pdfDocumentRef);
    CGPDFDocumentRelease(pdfDocumentRef);

    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    //change the print document so as not to trigger observer
    PrintDocument *printDocument = [[PrintDocument alloc] initWithURL:previewURL name:[testURL lastPathComponent]];
    viewController.printDocument = printDocument;
    
    printDocument.previewSetting = previewSetting;
    [viewController setupTotalPageNum];
    
    NSInteger expectedPageNum = pageCount;
    if(previewSetting.booklet == YES)
    {
        expectedPageNum = pageCount + 4 - (pageCount - ((pageCount / 4) * 4));
    }
    else if(previewSetting.duplex != kDuplexSettingOff)
    {
        expectedPageNum = pageCount + pageCount % 2;
    }
    else if(previewSetting.imposition == kImposition2Pages)
    {
        expectedPageNum = pageCount/2 + (((pageCount % 2) > 0)? 1: 0);
    }
    else if(previewSetting.imposition == kImposition4pages)
    {
        expectedPageNum = pageCount/2 + (((pageCount % 4) > 0)? 1: 0);
    }
    
    GHAssertEquals(viewController.totalPageNum, expectedPageNum, @"");
}

- (void)test009_previewSettingDidChange_Booklet
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    [manager setFileURL:testURL];
    [manager setupDocument];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    
    manager.printDocument.previewSetting.booklet = YES;
    //TODO: Add checks

    
    manager.printDocument.previewSetting.bookletLayout = kBookletLayoutRightToLeft;
    //TODO: Add checks
    
    manager.printDocument.previewSetting.bookletLayout = kBookletLayoutTopToBottom;
    //TODO: Add checks
    
}

- (void)test009_previewSettingDidChange_Duplex
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    [manager setFileURL:testURL];
    [manager setupDocument];
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintPreviewViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.view, @"");
    
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    //TODO: Add checks
    
    
    manager.printDocument.previewSetting.orientation = kOrientationLandscape;
    //TODO: Add checks
    

    manager.printDocument.previewSetting.finishingSide = kFinishingSideTop;
    //TODO: Add Checks
    
    
    manager.printDocument.previewSetting.orientation = kOrientationPortrait;
    
    
}
@end
