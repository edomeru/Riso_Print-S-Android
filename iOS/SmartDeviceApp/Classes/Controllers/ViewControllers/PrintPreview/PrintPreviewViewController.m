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
@property (weak, nonatomic) IBOutlet UISlider *pageNavigationSlider;
@property (weak, nonatomic) IBOutlet UILabel *screenTitle;
@property (weak, nonatomic) IBOutlet UILabel *pageNumberDisplay;
@end

@implementation PrintPreviewViewController
{
    CGPDFDocumentRef __pdfDocument;
    NSUInteger __numPDFPages;
    NSUInteger __currentIndex;
    NSUInteger __numViewPages;
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

//intial loading of print preview
- (void) loadPrintPreview
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    __pdfDocument = manager.pdfDocument;
    __numPDFPages = CGPDFDocumentGetNumberOfPages(__pdfDocument);
    __currentIndex = 0;
    NSString *screenTitle = [[[manager pdfURL] pathComponents] lastObject];
    
    [self.screenTitle setText:screenTitle];

    [self loadPrintPreviewSettings];
    [self computeNumViewPages];
    [self setUpPageViewController];
    [self setUpPageNavigationSlider];
    [self updatePageNumberLabel];
}


//retrieval of initial settings for preview
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
    self.previewSetting.bookletBinding = 0;
}

//compute the total number of pages in the view controller taking into account pagination
-(void) computeNumViewPages
{    NSUInteger numPagesPerSheet = getNumberOfPagesPerSheet(self.previewSetting.pagination);
    __numViewPages = __numPDFPages/numPagesPerSheet;
    
    if((__numPDFPages % numPagesPerSheet) > 0)
    {
        __numViewPages++;
    }
}

//initial set up of the page view controller
-(void) setUpPageViewController
{
    [self loadPageViewController];
    [self setPageSize];
    [self setViewToPage:__currentIndex];
}

//creates the page view controller and adds to view
-(void) loadPageViewController
{
    //remove from view if already existing
    if(self.pdfPageViewController != nil)
    {
        [self.pdfPageViewController.view removeFromSuperview];
        [self.pdfPageViewController removeFromParentViewController];
    }
    
    //consider bind setting for spine location and navigation orientation
    UIPageViewControllerSpineLocation spineLocation = getSpineLocation(self.previewSetting.bind, self.previewSetting.duplex,self.previewSetting.bookletBinding);
    UIPageViewControllerNavigationOrientation navigationOrientation = getNavigationOrientation(self.previewSetting.bind);
    NSDictionary *options = [NSDictionary dictionaryWithObject: [NSNumber numberWithInteger:spineLocation] forKey: UIPageViewControllerOptionSpineLocationKey];
    
    //create page view controller
    self.pdfPageViewController = [[UIPageViewController alloc]
                                  initWithTransitionStyle: UIPageViewControllerTransitionStylePageCurl
                                  navigationOrientation: navigationOrientation
                                  options:options];
    
    [self.pdfPageViewController.view setClipsToBounds:true];
    
    //set self as delegate and datasource
    self.pdfPageViewController.dataSource = self;
    self.pdfPageViewController.delegate =self;
    
    //add ui page controller to view
    [self addChildViewController:_pdfPageViewController];
    [self.previewArea addSubview:self.pdfPageViewController.view];
    [self.pdfPageViewController didMoveToParentViewController:self];
}

//sets the size of the page view controller based on the ratio of the paper size
-(void) setPageSize
{
    //get ratio of width and height based on paper size
    float heightToWidthRatio = getHeightToWidthRatio(self.previewSetting.paperSize);
    //check if paper should be in landscape orientation
    BOOL isLandscape = isPaperLandscape(self.previewSetting.pagination);
    
    if(self.previewSetting.bookletBinding > 0 && self.previewSetting.bind != BIND_TOP)
    {
        isLandscape = YES;
    }
    
    //set margins
    CGFloat horizontalMargin = 10;
    CGFloat verticalMargin = horizontalMargin;
    
    //intial width and height is dimension of superview minus the margins
    CGFloat width = self.previewArea.frame.size.width - (horizontalMargin * 2);
    CGFloat height = self.previewArea.frame.size.height - (verticalMargin * 2);
    CGFloat temp = 0;
    
    //if paper is in landscape. set the width to be the height
    if(isLandscape)
    {
        temp = width / heightToWidthRatio;
        verticalMargin += (height -temp)/2;
        height = temp;
    }
    else
    {
        //first set width as constant and adjust height using ratio
        temp = width * heightToWidthRatio;
        
        //if height is more than initial height (superview height - margins), set the height to constant and adjust the width instead
        if(temp > height)
        {
            temp = height / heightToWidthRatio;
            horizontalMargin += (width -temp)/2; //adjust margins according to the new width to center page view controller in superview
            width = temp;
        }
        else
        {
            verticalMargin += (height - temp)/2; //adjust margins according to the new height to center page view controller in superview
            height = temp;
        }
    }
    
    NSLog(@"Pageview controller height = %f. width = %f", height, width);
    //set the page controller view frame to new dimensions
    [self.pdfPageViewController.view setFrame: CGRectMake(horizontalMargin, verticalMargin, width, height)];
}

//set the page view controller to a specific page
-(void) setViewToPage: (NSUInteger) pageIndex
{

    //set initial view controllers to show
    UIViewController *firstPageViewController = [self pageContentViewControllerAtIndex:pageIndex];
    NSMutableArray *initialViewControllers = [@[firstPageViewController] mutableCopy];
    
    if(self.previewSetting.duplex == YES || self.previewSetting.bookletBinding > 0)
    {
        [self.pdfPageViewController setDoubleSided:YES];
        [initialViewControllers addObject:[self pageContentViewControllerAtIndex:pageIndex + 1]];
    }
    
    [self.pdfPageViewController setViewControllers:initialViewControllers direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
}

//set up the slider
-(void) setUpPageNavigationSlider
{
    [self.pageNavigationSlider setValue:__currentIndex];
    [self.pageNavigationSlider setMaximumValue: __numViewPages];
}

//update the page number label
-(void) updatePageNumberLabel
{
    self.pageNumberDisplay.text = [NSString stringWithFormat:@"PAGE %d/%d",(__currentIndex + 1), __numViewPages];
}

//provider of view controllers per uipageview controller page
-(PDFPageContentViewController *) pageContentViewControllerAtIndex:(NSUInteger) index
{
    PDFPageContentViewController *controller = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];
    
    controller.delegate = self;
    controller.pageIndex = index;
    return controller;
}

#pragma mark UIPageViewController Datasource methods

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

//helper methods for the UIPageViewController datasource methods
-(UIViewController *) goToNextPage:(NSUInteger) index
{
    if (index == NSNotFound)
    {
        return nil;
    }
    
    index++;
    
    
    if ((index + 1) > __numViewPages)
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

#pragma mark UIPageViewController Delegate methods
- (UIPageViewControllerSpineLocation) pageViewController:(UIPageViewController *)pageViewController spineLocationForInterfaceOrientation:(UIInterfaceOrientation)orientation
{

    return getSpineLocation(self.previewSetting.bind, self.previewSetting.duplex, self.previewSetting.bookletBinding);
    
}
//detect page turns
- (void)pageViewController:(UIPageViewController *)pageViewController didFinishAnimating:(BOOL)finished previousViewControllers:(NSArray *)previousViewControllers transitionCompleted:(BOOL)completed
{
    if(completed == YES)
    {
        PDFPageContentViewController *viewController = (PDFPageContentViewController *)[pageViewController.viewControllers lastObject];
        __currentIndex = viewController.pageIndex;
        [self updatePageNumberLabel];
        [self.pageNavigationSlider setValue:__currentIndex];
    }
}

//delegate of the controller of the page content
#pragma mark PDF Page View Content Controller Delegate methods
//gets the pdf page according to the page index of the page view controller. Takes into consideration pagination.
-(CGPDFPageRef) getPDFPage:(NSUInteger)pageIndex withPageOffset:(NSUInteger)pageOffset
{
    NSUInteger numPagesPerSheet = getNumberOfPagesPerSheet(self.previewSetting.pagination);
    NSUInteger actualPDFPageNum = (pageIndex * numPagesPerSheet) + 1; // the actual starting PDF Page num based on the page index
    if((actualPDFPageNum + pageOffset) > __numPDFPages)
    {
        return nil;
    }

    CGPDFPageRef pdfPage = CGPDFDocumentGetPage(__pdfDocument, actualPDFPageNum + pageOffset);
    return pdfPage;
}

//gets the preview setting object
-(PreviewSetting *) getPreviewSetting
{
    return self.previewSetting;
}

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

- (IBAction)sliderChanged:(id)sender
{
    UISlider *slider  = (UISlider *) sender;
    NSInteger pageIndex = slider.value;
    __currentIndex = pageIndex;
    [self setViewToPage: pageIndex];
    [self updatePageNumberLabel];
}
- (IBAction)tapSlider:(id)sender
{
    
    UIGestureRecognizer *tap =  (UIGestureRecognizer *)sender;
    CGPoint point = [tap locationInView:self.pageNavigationSlider];
    CGFloat sliderPortion = point.x/self.pageNavigationSlider.bounds.size.width;
    __currentIndex = __numViewPages * sliderPortion;
    //update page in view, page number label. slider position
    [self setViewToPage:__currentIndex];
    [self updatePageNumberLabel];
    [self.pageNavigationSlider setValue:__currentIndex];
}

@end
