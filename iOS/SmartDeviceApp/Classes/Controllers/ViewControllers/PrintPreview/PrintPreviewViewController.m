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

@interface PrintPreviewViewController ()
@property (strong, nonatomic) UIPageViewController *pdfPageViewController;
@property (strong, nonatomic) PreviewSetting *previewSettings;
@property (weak, nonatomic) IBOutlet DefaultView *contentArea;

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
    if(self.previewSettings == nil)
    {
        self.previewSettings = [[PreviewSetting alloc] init];
    }
    
    //TODO get from print settings
    self.previewSettings.duplex = YES;
}

-(void) setUpPageViewController
{
    [self reloadPageViewController];
    
    //TODO get initial page size
    [self setPageSize];
    
    [self.pdfPageViewController.view setClipsToBounds:true];
    
    //set view controllers
    UIViewController *firstPageViewController = [self pageContentViewControllerAtIndex:0];
    NSMutableArray *initialViewControllers = [@[firstPageViewController] mutableCopy];
    
    if(self.previewSettings.duplex)
    {
        [self.pdfPageViewController setDoubleSided:YES];
    }
    
    [self.pdfPageViewController setViewControllers:initialViewControllers direction:UIPageViewControllerNavigationDirectionForward animated:YES completion:nil];
}


-(PDFPageContentViewController *) pageContentViewControllerAtIndex:(NSUInteger) index
{
    PDFPageContentViewController *controller = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];
    NSLog(@"get page view controller");
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

    self.pdfPageViewController = [[UIPageViewController alloc] initWithTransitionStyle:UIPageViewControllerTransitionStylePageCurl navigationOrientation: UIPageViewControllerNavigationOrientationHorizontal options:nil];
    
    self.pdfPageViewController.dataSource = self;
    self.pdfPageViewController.delegate =self;
    
    //add ui page controller to view
    [self addChildViewController:_pdfPageViewController];
    [self.contentArea addSubview:self.pdfPageViewController.view];
    [self.pdfPageViewController didMoveToParentViewController:self];
}


-(void) setPageSize
{
    float heightToWidthRatio = (297.0/210.0); //Hardcode A4;
    
    //TODO take into account paper size and pagination
    CGFloat margin = 10;
    CGFloat width = self.contentArea.frame.size.width - (margin * 2); //fix width

    CGFloat height = width * heightToWidthRatio;
    
    NSLog(@"height = %f. width = %f", height, width);
    
    self.pdfPageViewController.view.frame = CGRectMake(margin, margin, width, height);
}


#pragma mark PageViewController Datasource
- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    NSUInteger index = ((PDFPageContentViewController*) viewController).pageIndex;
    
    if ((index == 0) || (index == NSNotFound)) {
        return nil;
    }
    
    index--;
    return [self pageContentViewControllerAtIndex:index];
}

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController
{
    
    NSUInteger index = ((PDFPageContentViewController*) viewController).pageIndex;
    
    if (index == NSNotFound) {
        return nil;
    }

    index++;
    NSUInteger numViewPages = __numPDFPages; //TODO take into account pagination
    if ((index + 1) > numViewPages)
    {
        return nil;
    }
    return [self pageContentViewControllerAtIndex:index];
}


- (UIPageViewControllerSpineLocation) pageViewController:(UIPageViewController *)pageViewController spineLocationForInterfaceOrientation:(UIInterfaceOrientation)orientation
{
    return UIPageViewControllerSpineLocationMin;
    
}

#pragma mark Page View Content Delegate
-(CGPDFPageRef) getPDFPage:(NSUInteger)pageIndex withPageOffset:(NSUInteger)pageOffset
{
    NSUInteger actualPage = pageIndex + 1; //TODO add multiply by number of pages
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
    return self.previewSettings;
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
