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


#define PREVIEW_MIN_MARGIN  10.0

@interface PrintPreviewViewController ()
@property (strong, nonatomic) UIPageViewController *pdfPageViewController; //the page view controller
@property (strong, nonatomic) PreviewSetting *previewSetting; //object to hold the preview setting values
@property (weak, nonatomic) IBOutlet UILabel *screenTitle;  //title of the screen set to the title of the previewed pdf
@property (weak, nonatomic) IBOutlet UIView *previewArea; //area where the preview is contained
@property (weak, nonatomic) IBOutlet UIView *pageNavArea; //area where the slider and page number display is shown
@property (weak, nonatomic) IBOutlet UISlider *pageNavigationSlider; //slider used to jump or navigate to pages
@property (weak, nonatomic) IBOutlet UILabel *pageNumberDisplay; //current page number display over total number of pages
@property (weak, nonatomic) IBOutlet UIButton *printSettingButton; //button to navigate to the print settings screen

/*Constraints to adjust the position of slider and page number display in the page navigation area
 when in Phone landscape*/
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *sliderLeftConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *pageLabelTopConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *pageLabelRightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *pageNavAreaHeight;

/*Class Private Methods*/
/**
 Set UI used in print preview to hidden or shown
 **/
-(void) hidePrintPreviewControls: (BOOL) isHidden;
/**
 Initialize Print Preview Settings from default Print settings
 **/
-(void) loadPrintPreviewSettings;
/**
 Compute total number of view pages based on number of PDF pages and imposition setting
 **/
-(void) computeNumViewPages;
/**
 Initialize the page view controller
 **/
-(void) loadPageViewController;
/**
 Set up the page view controller based on preview settings
 **/
-(void) setUpPageViewController;
/**
 Adjust the page view controller frame to ratio of paper height and width based on selected paper size setting
 **/
-(void) setPageSize;
/**
 Set the current page shown by the view controller
 @note This is used for page view controller initialization and in page jumps during slider actions.
 It is NOT used in page turns. 
 The data source methods of the page view controller implements the setting of page for page curl turn
 **/
-(void) setViewControllerToCurrentPage: (NSUInteger) pageIndex;
/**
 Sets up the look and feel of the slider
 **/
-(void) setUpPageNavigationSlider;
/**
 Updates the page number display with the current page index and total number of view pages
 **/
-(void) updatePageNumberDisplay;
/**
 Creates and provide the page content view controller for each page in the page view controller
 **/
-(PDFPageContentViewController *) pageContentViewControllerAtIndex:(NSUInteger) pageIndex;

/*Actions*/
/**
 Action when slider is dragged
 **/
- (IBAction)dragSliderAction:(id)sender;
/**
 Action when slider is tapped
 **/
- (IBAction)tapSliderAction:(id)sender;
@end

@implementation PrintPreviewViewController
{
    CGPDFDocumentRef __pdfDocument; //PDF document object reference
    NSUInteger __numPDFPages; // number of pages in the PDF document
    NSUInteger __currentIndex; //current view page index (not pertaining to pdf page number)
    NSUInteger __numViewPages; // number of view pages, NOT pdf pages
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
    /*If view is already in preview mode, show print preview*/
    BOOL previewModeOn = ((RootViewController*)self.parentViewController).isPrintPreviewMode;
    if(previewModeOn == YES)
    {
        [self loadPrintPreview];
    }
    else
    {
        //TODO: Show replacement image for blank home screen
        //hide UI related to preview
        [self hidePrintPreviewControls:YES];
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*Overriden to handle device rotation*/
- (void)didRotateFromInterfaceOrientation:(UIInterfaceOrientation)fromInterfaceOrientation
{
    [super didRotateFromInterfaceOrientation:fromInterfaceOrientation];
    //adjust position of slider and page label for phone when in landscape
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
    {
        if(UIInterfaceOrientationIsLandscape(self.interfaceOrientation) == YES)
        {
            self.pageNavAreaHeight.constant = 40;
            //adjust
            self.pageLabelTopConstraint.constant = 20 ;
            self.pageLabelRightConstraint.constant =  self.previewArea.frame.size.width - 100;
            self.self.sliderLeftConstraint.constant = 120;
        }
        else
        {
            //Original values
            self.pageNavAreaHeight.constant = 60;
            self.pageLabelRightConstraint.constant  = 20;
            self.pageLabelTopConstraint.constant = 40;
            self.sliderLeftConstraint.constant = 20;
        }
    }
    //adjust the page view controller frame size
    [self setPageSize];
}

#pragma mark - Class Private Methods

-(void) hidePrintPreviewControls: (BOOL) isHidden
{
    [self.printSettingButton setHidden:isHidden];
    [self.pageNavArea setHidden: isHidden];
}

- (void) loadPrintPreview
{
    //get PDF document
    PDFFileManager *manager = [PDFFileManager sharedManager];
    __pdfDocument = manager.pdfDocument;
    if(__pdfDocument == nil)
    {
        NSLog(@"document is nil");
        return;
    }
    //Get number of PDF pages
    __numPDFPages = CGPDFDocumentGetNumberOfPages(__pdfDocument);
    //Set current index to first page
    __currentIndex = 0;
    
    //set screen title to file name of PDF
    NSString *screenTitle = [[[manager pdfURL] pathComponents] lastObject];
    [self.screenTitle setText:screenTitle];
    
    //unhide print preview controls
    [self hidePrintPreviewControls:NO];
    
    //get the initial preview settings
    [self loadPrintPreviewSettings];
    
    //compute number of pages in view taking into account imposition
    [self computeNumViewPages];
    
    //setup the page view controller
    [self loadPageViewController];
    
    //setup the page navigation slider
    [self setUpPageNavigationSlider];
}

-(void) loadPrintPreviewSettings
{
     self.previewSetting = [[PreviewSetting alloc] init];
    
    //TODO: get from print settings
    self.previewSetting.duplex = DUPLEX_OFF;
    self.previewSetting.pagination = PAGINATION_OFF;
    self.previewSetting.paperSize = PAPERSIZE_A4;
    self.previewSetting.colorMode = COLORMODE_AUTO;
    self.previewSetting.bind = BIND_LEFT;
    self.previewSetting.orientation = ORIENTATION_PORTRAIT;
    self.previewSetting.isBookletBind = NO;
    self.previewSetting.isScaleToFit = YES;
}

-(void) computeNumViewPages
{
    //Number of view pages is number of PDF pages divided by number of pages in sheet with respect to the pagination setting
    NSUInteger numPagesPerSheet = getNumberOfPagesPerSheet(self.previewSetting.pagination);
    __numViewPages = __numPDFPages/numPagesPerSheet;
    
    if((__numPDFPages % numPagesPerSheet) > 0)
    {
        __numViewPages++;
    }
    
    //update slider maximum value and page number label after computation of number of pages
    [self.pageNavigationSlider setMaximumValue: __numViewPages];
    [self updatePageNumberDisplay];
}

-(void) loadPageViewController
{
    [self setUpPageViewController];
    [self setPageSize];
}

-(void) setUpPageViewController
{
    //remove from view if already existing
    if(self.pdfPageViewController != nil)
    {
        [self.pdfPageViewController.view removeFromSuperview];
        [self.pdfPageViewController removeFromParentViewController];
    }
    
    //consider bind setting for spine location and navigation orientation
    UIPageViewControllerSpineLocation spineLocation = getSpineLocation(self.previewSetting.bind, self.previewSetting.duplex, self.previewSetting.isBookletBind);
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
    
    //set the current page of the page view controller
    [self setViewControllerToCurrentPage:__currentIndex];
    
    //add ui page controller to view
    [self addChildViewController:_pdfPageViewController];
    [self.previewArea addSubview:self.pdfPageViewController.view];
    [self.pdfPageViewController didMoveToParentViewController:self];
}


-(void) setPageSize
{
    //get ratio of width and height based on paper size
    float heightToWidthRatio = getHeightToWidthRatio(self.previewSetting.paperSize);
    
    //check if paper should be in landscape orientation
    BOOL isLandscape = isPaperLandscape(self.previewSetting);
    
    //set margins
    CGFloat horizontalMargin = PREVIEW_MIN_MARGIN;
    CGFloat verticalMargin = horizontalMargin;
    
    //intial width and height is dimension of superview minus the margins
    CGFloat width = self.previewArea.frame.size.width - (horizontalMargin * 2);
    CGFloat height = self.previewArea.frame.size.height - (verticalMargin * 2);
    CGFloat temp = 0;
    
    //if paper is in landscape. set the width to be the height
    if(isLandscape == YES)
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


-(void) setViewControllerToCurrentPage: (NSUInteger) pageIndex
{
    //set initial view controllers to show
    UIViewController *firstPageViewController = [self pageContentViewControllerAtIndex:pageIndex];
    NSMutableArray *initialViewControllers = [@[firstPageViewController] mutableCopy];
    
    if(self.previewSetting.duplex > DUPLEX_OFF || self.previewSetting.isBookletBind == YES)
    {
        [self.pdfPageViewController setDoubleSided:YES];
        [initialViewControllers addObject:[self pageContentViewControllerAtIndex:pageIndex + 1]];
    }
  
    [self.pdfPageViewController setViewControllers:initialViewControllers direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
}


-(void) setUpPageNavigationSlider
{
    [self.pageNavigationSlider setMinimumValue: 1];
    [self.pageNavigationSlider setValue:__currentIndex];
    [self.pageNavigationSlider setMinimumTrackImage:[UIImage imageNamed:@"SliderMinimum.png"] forState:UIControlStateNormal];
    [self.pageNavigationSlider setMaximumTrackImage:[UIImage imageNamed:@"SliderMaximum.png"] forState:UIControlStateNormal];
    [self.pageNavigationSlider setThumbImage:[UIImage imageNamed:@"SliderThumb.png"] forState:UIControlStateNormal];
    [self.pageNavigationSlider setThumbImage:[UIImage imageNamed:@"SliderThumb.png"] forState:UIControlStateHighlighted];
}


-(void) updatePageNumberDisplay
{
    self.pageNumberDisplay.text = [NSString stringWithFormat:@"PAGE %lu/%lu", (unsigned long)(__currentIndex + 1), (unsigned long)__numViewPages];
}


-(PDFPageContentViewController *) pageContentViewControllerAtIndex:(NSUInteger) pageIndex
{
    PDFPageContentViewController *controller = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];
    
    controller.datasource = self;
    controller.pageIndex = pageIndex;
    return controller;
}


#pragma mark UIPageViewController Datasource methods

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    NSUInteger index = ((PDFPageContentViewController*) viewController).pageIndex;
    
    UIViewController * controller = nil;
    //page navigation is reversed for right bind
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
    //page navigation is reversed for right bind
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

/*Helper methods for the UIPageViewController datasource methods*/
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


#pragma mark - UIPageViewController Delegate methods

/*Returns the spine location based on an orientation*/
- (UIPageViewControllerSpineLocation) pageViewController:(UIPageViewController *)pageViewController spineLocationForInterfaceOrientation:(UIInterfaceOrientation)orientation
{
    return getSpineLocation(self.previewSetting.bind, self.previewSetting.duplex, self.previewSetting.isBookletBind);
}

/*Detect the page turns.*/
- (void)pageViewController:(UIPageViewController *)pageViewController didFinishAnimating:(BOOL)finished previousViewControllers:(NSArray *)previousViewControllers transitionCompleted:(BOOL)completed
{
    if(completed == YES)
    {
        //if page is turned completely, update the page number label and the slider position
        PDFPageContentViewController *viewController = (PDFPageContentViewController *)[pageViewController.viewControllers lastObject];
        __currentIndex = viewController.pageIndex;
        [self updatePageNumberDisplay];
        [self.pageNavigationSlider setValue:__currentIndex];
    }
}


#pragma mark - PDFPageViewContentControllerDatasource methods

/*Returns the PDF page according to the page index of the page view controller. Takes into consideration imposition/pagination.*/
-(CGPDFPageRef) getPDFPage:(NSUInteger)pageIndex withPageOffset:(NSUInteger)pageOffset
{
    NSUInteger numPagesPerSheet = getNumberOfPagesPerSheet(self.previewSetting.pagination);
    NSUInteger actualPDFPageNum = (pageIndex * numPagesPerSheet) + 1; // the actual PDF page number of the first PDF page in the sheet
    //the pageOffset is the Nth page in the sheet. To get Nth PDF page in a sheet, add the offset to the actual pdf page number of the first page in the sheet
    if((actualPDFPageNum + pageOffset) > __numPDFPages)
    {
        return nil;
    }

    CGPDFPageRef pdfPage = CGPDFDocumentGetPage(__pdfDocument, actualPDFPageNum + pageOffset);
    return pdfPage;
}

/*Returns the preview setting object*/
-(PreviewSetting *) getPreviewSetting
{
    return self.previewSetting;
}


#pragma mark - IBActions

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

/*Action when slider is dragged*/
- (IBAction)dragSliderAction:(id)sender
{
    UISlider *slider  = (UISlider *) sender;
    NSInteger pageNumber = slider.value;
    __currentIndex = pageNumber - 1;
    [self setViewControllerToCurrentPage: __currentIndex];
    [self updatePageNumberDisplay];
}

/*Action when slider is tapped*/
- (IBAction)tapSliderAction:(id)sender
{
    UIGestureRecognizer *tap =  (UIGestureRecognizer *)sender;
    //get point in slider where it is tapped
    CGPoint point = [tap locationInView:self.pageNavigationSlider];
    //Get how many percent of the total slider length is the distance of the tapped point from the start point of the slider
    CGFloat sliderPercentage = point.x/self.pageNavigationSlider.bounds.size.width;
    //multiply the the percentage with the total number of pages in view to get the current page index;
    __currentIndex = (__numViewPages * sliderPercentage)-1;;
    
    //update page in view, page number label. slider thumb position
    [self setViewControllerToCurrentPage:__currentIndex];
    [self updatePageNumberDisplay];
    [self.pageNavigationSlider setValue:__currentIndex];
}

@end
