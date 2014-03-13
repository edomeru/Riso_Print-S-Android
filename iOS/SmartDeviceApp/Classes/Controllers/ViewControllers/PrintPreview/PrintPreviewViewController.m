//
//  PrintPreviewViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintPreviewViewController.h"
#import "RootViewController.h"
#import "SlideSegue.h"
#import "HomeViewController.h"
#import "PrintSettingsViewController.h"
#import "UIViewController+Segue.h"
#import "PDFFileManager.h"
#import "PDFPageContentViewController.h"
#import "PDFPageView.h"
#import "DefaultView.h"
#import "PrintPreviewHelper.h"

@interface PrintPreviewViewController ()
@property (strong, nonatomic) UIPageViewController *pdfPageViewController;
@property (strong, nonatomic) PreviewSetting *previewSetting;
@property (weak, nonatomic) IBOutlet UIView *previewArea;
@end

@implementation PrintPreviewViewController
{
    CGPDFDocumentRef __pdfDocument;
    NSUInteger __numPDFPages;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        // Custom initialization
        __pdfDocument = nil;
        __numPDFPages = 0;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    BOOL previewModeOn = ((RootViewController*)self.parentViewController).isPrintPreviewMode;
    if(previewModeOn == YES)
    {
        [self loadPrintPreview];
    }
    
}
//detect rotation
- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    //resize page view controller
    [self setPageSize];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void) loadPrintPreview
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    __pdfDocument = manager.pdfDocument;
    __numPDFPages = CGPDFDocumentGetNumberOfPages(__pdfDocument);
    
    [self loadPrintPreviewSettings];
    [self setUpPageViewController];
}

-(void) loadPrintPreviewSettings
{
    if(self.previewSetting == nil)
    {
        self.previewSetting = [[PreviewSetting alloc] init];
    }
    
    //TODO get from print settings
    self.previewSetting.duplex = NO;
    self.previewSetting.pagination = 0;
    self.previewSetting.paperSize = 0;
    self.previewSetting.colorMode = 0;
    self.previewSetting.bind = 0;
}

-(void) setUpPageViewController
{
    [self reloadPageViewController];
    
    [self setPageSize];
    
    [self.pdfPageViewController.view setClipsToBounds:true];
    
    //set view controllers
    UIViewController *firstPageViewController = [self pageContentViewControllerAtIndex:0];
    NSMutableArray *initialViewControllers = [@[firstPageViewController] mutableCopy];
    
    if(self.previewSetting.duplex)
    {
        //[self.pdfPageViewController setDoubleSided:YES];
        [initialViewControllers addObject:[self pageContentViewControllerAtIndex:1]];
    }
    
    [self.pdfPageViewController setViewControllers:initialViewControllers direction:UIPageViewControllerNavigationDirectionForward animated:YES completion:nil];
}


-(PDFPageContentViewController *) pageContentViewControllerAtIndex:(NSUInteger) index
{
    PDFPageContentViewController *controller = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];

    controller.delegate = self;
    controller.pageIndex = index;
    return controller;
}

#pragma mark Apply print settings to page view controller methods
-(void) reloadPageViewController
{
    if(self.pdfPageViewController != nil)
    {
        [self.pdfPageViewController.view removeFromSuperview];
        [self.pdfPageViewController removeFromParentViewController];
    }
    
    UIPageViewControllerSpineLocation spineLocation = getSpineLocation(_previewSetting.bind, _previewSetting.duplex,
                                                                       _previewSetting.bookletBinding);
    
    UIPageViewControllerNavigationOrientation navigationOrientation = getNavigationOrientation(_previewSetting.bind);
    
    NSDictionary *options = [NSDictionary dictionaryWithObject: [NSNumber numberWithInteger:spineLocation] forKey: UIPageViewControllerOptionSpineLocationKey];

    self.pdfPageViewController = [[UIPageViewController alloc]
                                initWithTransitionStyle: UIPageViewControllerTransitionStylePageCurl
                                navigationOrientation: navigationOrientation
                                options:options];
    
    
    self.pdfPageViewController.dataSource = self;
    self.pdfPageViewController.delegate =self;
    
    //add ui page controller to view
    [self addChildViewController:_pdfPageViewController];
    [self.previewArea addSubview:self.pdfPageViewController.view];
    [self.pdfPageViewController didMoveToParentViewController:self];
}


-(void) setPageSize
{
    NSLog(@"set page size %f %f", self.previewArea.frame.size.height,self.previewArea.frame.size.width);
    float heightToWidthRatio = getHeightToWidthRatio(self.previewSetting.paperSize);
    BOOL isLandscape = isPaperLandscape(self.previewSetting.pagination);

    
    CGFloat horizontalMargin = 10;
    CGFloat verticalMargin = horizontalMargin;
    
    CGFloat width = self.previewArea.frame.size.width - (horizontalMargin * 2);
    CGFloat height = self.previewArea.frame.size.height - (verticalMargin * 2);
    CGFloat temp = 0;
    
    
    if(isLandscape)
    {
        temp = width / heightToWidthRatio;
        verticalMargin += (height -temp)/2;
        height = temp;
    }
    else
    {
        temp = width * heightToWidthRatio;
        
        if(temp > height)
        {
            temp = height / heightToWidthRatio;
            horizontalMargin += (width -temp)/2;
            width = temp;
        }
        else
        {
            verticalMargin += (height - temp)/2;
            height = temp;
        }
    }
    
    NSLog(@"Pageview controller height = %f. width = %f", height, width);    
    [self.pdfPageViewController.view setFrame: CGRectMake(horizontalMargin, verticalMargin, width, height)];
}


#pragma mark PageViewController Datasource
- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    NSUInteger index = ((PDFPageContentViewController*) viewController).pageIndex;
    
    UIViewController * controller = nil;
    if(self.pdfPageViewController.spineLocation == UIPageViewControllerSpineLocationMax)
    {
        controller = [self goToNextPage:index];
    }
    else
    {
        controller = [self goToPreviousPage:index];
    }
    return controller;
}

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController
{
    
    NSUInteger index = ((PDFPageContentViewController*) viewController).pageIndex;
    UIViewController * controller = nil;
    if(self.pdfPageViewController.spineLocation == UIPageViewControllerSpineLocationMax)
    {
        controller = [self goToPreviousPage:index];
    }
    else
    {
        controller = [self goToNextPage:index];
    }
    return controller;
}

-(UIViewController *) goToNextPage:(NSUInteger) index
{
    if (index == NSNotFound)
    {
        return nil;
    }
    
    index++;
    NSUInteger numPagesPerSheet = getNumberOfPagesPerSheet(self.previewSetting.pagination);
    NSUInteger numViewPages = __numPDFPages/numPagesPerSheet;
    
    if((__numPDFPages % numPagesPerSheet) > 0)
    {
        numViewPages++;
    }
    
    if ((index + 1) > numViewPages)
    {
        return nil;
    }
    return [self pageContentViewControllerAtIndex:index];
}

-(UIViewController *) goToPreviousPage:(NSUInteger) index
{
    if ((index == 0) || (index == NSNotFound)) {
        return nil;
    }
    
    index--;
    return [self pageContentViewControllerAtIndex:index];
}

#pragma mark PageViewController Delegate
- (UIPageViewControllerSpineLocation) pageViewController:(UIPageViewController *)pageViewController spineLocationForInterfaceOrientation:(UIInterfaceOrientation)orientation
{

    return getSpineLocation(self.previewSetting.bind, self.previewSetting.duplex, self.previewSetting.bookletBinding);
    
}

#pragma mark Page View Content Delegate
-(CGPDFPageRef) getPDFPage:(NSUInteger)pageIndex withPageOffset:(NSUInteger)pageOffset
{
    NSUInteger numPagesPerSheet = getNumberOfPagesPerSheet(self.previewSetting.pagination);
    NSUInteger actualPage = (pageIndex * numPagesPerSheet) + 1;
    NSLog(@" pageIndex %d", pageIndex);
    if(actualPage + pageOffset > __numPDFPages)
    {
        return nil;
    }

    CGPDFPageRef pdfPage = CGPDFDocumentGetPage(__pdfDocument, actualPage + pageOffset);
    return pdfPage;
}

-(PreviewSetting *) getPreviewSetting
{
    return self.previewSetting;
}


#pragma mark -
#pragma mark IBActions
- (IBAction)mainMenuAction:(id)sender
{
    [self performSegueTo:[HomeViewController class]];
}

- (IBAction)printSettingsAction:(id)sender
{
    [self performSegueTo:[PrintSettingsViewController class]];
}

- (IBAction)unwindToPrintPreview:(UIStoryboardSegue *)segue
{
}

@end
