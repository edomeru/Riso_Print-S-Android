//
//  PrintPreviewViewController.m
//  Tester
//
//  Created by Seph on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintPreviewViewController.h"
#import "UIViewController+Segue.h"
#import "HomeViewController.h"
#import "PrintSettingsViewController.h"
#import "PDFFileManager.h"
#import "PreviewSetting.h"
#import "PDFPageContentViewController.h"
#import "PrintSettingsHelper.h"
#import "PrintPreviewHelper.h"

#define PREVIEW_MARGIN 10.0f

@interface PrintPreviewViewController ()

// IBOutlets
@property (nonatomic, weak) IBOutlet UILabel *titleLabel;
@property (nonatomic, weak) IBOutlet UIButton *mainMenuButton;
@property (nonatomic, weak) IBOutlet UIButton *printSettingsButton;
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicator;
@property (nonatomic, weak) IBOutlet UIView *previewArea;
@property (weak, nonatomic) IBOutlet UIView *pageNavArea;
@property (weak, nonatomic) IBOutlet UISlider *pageScroll;
@property (weak, nonatomic) IBOutlet UILabel *pageLabel;

// Preview View
@property (nonatomic, weak) IBOutlet PreviewView *previewView;
@property (nonatomic, weak) NSLayoutConstraint *aspectRatioConstraint;

// PageView Controller
@property (nonatomic, weak) UIPageViewController *pageViewController;

// Document object
@property (nonatomic, weak) PrintDocument *printDocument;

// Display
@property (nonatomic) NSInteger currentPage;
@property (nonatomic) BOOL pageIsAnimating;
@property (nonatomic) NSInteger totalPageNum;

// Operations
@property (atomic, strong) NSOperationQueue *renderQueue;
@property (atomic, strong) NSMutableDictionary *renderOperations;
@property (atomic, strong) NSMutableDictionary *renderedViewControllers;

- (void)loadPDF;
- (void)setupPreview;
- (void)setupTotalPageNum;
- (void)setupPageScroll;
- (void)setupPageLabel;
- (void)setupPageviewControllerWithSpineLocation:(UIPageViewControllerSpineLocation)spineLocation navigationOrientation:(UIPageViewControllerNavigationOrientation)navigationOrientation;
- (void)setupPageviewControllerWithBindSetting;
@end

@implementation PrintPreviewViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    
    self.printSettingsButton.hidden = YES;
    self.previewView.hidden = YES;
    self.previewView.delegate = self;
    self.pageIsAnimating = NO;
    
    NSOperationQueue *renderQueue = [[NSOperationQueue alloc] init];
    renderQueue.maxConcurrentOperationCount = 1;
    renderQueue.name = @"RenderQueue";
    self.renderQueue = renderQueue;
    self.renderOperations = [[NSMutableDictionary alloc] init];
    self.renderedViewControllers = [[NSMutableDictionary alloc] init];
    
    if ([[PDFFileManager sharedManager] fileAvailableForLoad])
    {
        [self loadPDF];
    }
    else if([[PDFFileManager sharedManager] fileAvailableForPreview])
    {
        [self setupPreview];
    }
}

- (void)dealloc
{
    if (self.printDocument.delegate == self)
    {
        self.printDocument.delegate = nil;
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Private Methods

- (void)loadPDF
{
    [self.activityIndicator startAnimating];
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^
    {
        [[PDFFileManager sharedManager] loadFile];
        dispatch_async(dispatch_get_main_queue(), ^
        {
            self.printDocument = [[PDFFileManager sharedManager] printDocument];
            self.printDocument.delegate = self;
            self.currentPage = 0;
            self.titleLabel.text = [[PDFFileManager sharedManager] fileName];
            self.printSettingsButton.hidden = NO;
            self.previewView.hidden = NO;
            self.pageNavArea.hidden = NO;
            [self.activityIndicator stopAnimating];
            [self setupTotalPageNum];
            [self setupPreview];
            [self setupPageLabel];
            [self setupPageScroll];
        });
    });
}

- (void)setupPreview
{
    // *Assume portrait
    // Get aspect ratio
    CGFloat aspectRatio = [PrintPreviewHelper getAspectRatioForPaperSize:(kPaperSize)self.printDocument.previewSetting.paperSize];

    BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
    kPreviewViewOrientation orientation = kPreviewViewOrientationPortrait;
    if(isLandscape == YES)
    {
        orientation = kPreviewViewOrientationLandscape;
    }
    
    self.previewView.hidden = NO;
    [self.previewView setPreviewWithOrientation:orientation aspectRatio:aspectRatio];

    self.currentPage = 0;
    [self setupPageviewControllerWithBindSetting];
    [self goToPage:self.currentPage];
}

- (void)setupPageviewControllerWithSpineLocation:(UIPageViewControllerSpineLocation)spineLocation navigationOrientation:(UIPageViewControllerNavigationOrientation)navigationOrientation
{
    if(self.pageViewController != nil && self.pageViewController.view.superview != nil)
    {
        [self.pageViewController.view removeFromSuperview];
        [self.pageViewController removeFromParentViewController];
    }
    
    UIPageViewController *pageViewController = [[UIPageViewController alloc] initWithTransitionStyle:UIPageViewControllerTransitionStylePageCurl navigationOrientation:navigationOrientation options:@{UIPageViewControllerOptionSpineLocationKey: [NSNumber numberWithInteger:spineLocation]}];
    pageViewController.view.translatesAutoresizingMaskIntoConstraints = NO;
    [self addChildViewController:pageViewController];
    [self.previewView.contentView addSubview:pageViewController.view];
    
    NSDictionary *views = @{@"pageView": pageViewController.view};
    [self.previewView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[pageView]|" options:0 metrics:nil views:views]];
    [self.previewView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[pageView]|" options:0 metrics:nil views:views]];
    pageViewController.dataSource = self;
    pageViewController.delegate = self;
    
    self.pageViewController = pageViewController;
}

- (void)setupTotalPageNum
{
    if(self.printDocument.previewSetting.booklet == YES)
    {
        //booklet number of pages is always a multiple of 4 (1 paper folded in half = 2 leaves * 2 sides per leaf = 4 pages)
        //total number of pages is the actual number of pdf pages  + padding pages to make number of pages multiple by 4
        self.totalPageNum = self.printDocument.pageCount  +  self.printDocument.pageCount % 4;
        [self.pageScroll setMaximumValue:self.totalPageNum - 1]; //the last page that can be visited for the slider should be the second to the last page  which is the front page of the last sheet
    }
    else
    {
        //consider imposition setting for the total number of pages of the page view controller
        NSUInteger numPagesPerSheet =[PrintPreviewHelper getNumberOfPagesPerSheetForImpostionSetting:self.printDocument.previewSetting.imposition];
        self.totalPageNum = self.printDocument.pageCount/numPagesPerSheet;
        if((self.printDocument.pageCount % numPagesPerSheet) > 0)
        {
            self.totalPageNum++;
        }
        [self.pageScroll setMaximumValue:self.totalPageNum];
    }
    //reset current page if it is now way past the computed total page number
    if(self.currentPage >= self.totalPageNum)
    {
        self.currentPage = 0;
    }
}

- (void)setupPageScroll
{
    [self.pageScroll setMaximumTrackImage:[[UIImage imageNamed:@"img_slider_maximum"] resizableImageWithCapInsets:UIEdgeInsetsMake(0, 0, 0, 5)]forState: UIControlStateNormal];
    [self.pageScroll setMinimumTrackImage:[[UIImage imageNamed:@"img_slider_minimum"] resizableImageWithCapInsets:UIEdgeInsetsMake(0, 5, 0, 0) ] forState: UIControlStateNormal];
    [self.pageScroll setThumbImage:[UIImage imageNamed:@"img_slider_thumb"] forState: UIControlStateNormal];
    [self.pageScroll setThumbImage:[UIImage imageNamed:@"img_slider_thumb"] forState: UIControlStateHighlighted];
    [self.pageScroll setMinimumValue:1];
    [self.pageScroll setContinuous:NO];
}

- (void)setupPageLabel
{
    NSString *pageString = @"PAGE";
    self.pageLabel.text = [NSString stringWithFormat:@"%@ %ld/%ld", pageString, (long)self.currentPage + 1, (long)self.totalPageNum];
}

- (void)setupPageviewControllerWithBindSetting
{
    //Determine the spine location and navigation direction considering the finishing side and booklet bind setting
    UIPageViewControllerSpineLocation spineLocation = UIPageViewControllerSpineLocationMin;
    UIPageViewControllerNavigationOrientation navOrientation = UIPageViewControllerNavigationOrientationHorizontal;
    
    if(self.printDocument.previewSetting.booklet == YES)
    {
        spineLocation = UIPageViewControllerSpineLocationMid;
        if(self.printDocument.previewSetting.orientation == kOrientationLandscape)
        {
            navOrientation = UIPageViewControllerNavigationOrientationVertical;
        }
    }
    else if(self.printDocument.previewSetting.finishingSide == kFinishingSideRight)
    {
        spineLocation = UIPageViewControllerSpineLocationMax;
    }
    else if(self.printDocument.previewSetting.finishingSide == kFinishingSideTop)
    {
        navOrientation = UIPageViewControllerNavigationOrientationVertical;
    }

    //set-up page view controller if initial or if the spine location and navigation direction changed
    //don't resetup page view controller is existing and the spine location and navigationOrientation are still the same
    if(!(self.pageViewController!= nil && self.pageViewController.spineLocation == spineLocation && self.pageViewController.navigationOrientation == navOrientation))
    {
        [self setupPageviewControllerWithSpineLocation:spineLocation navigationOrientation:navOrientation];
    }
    
    //apply double sided setting for duplex and booklet bind setting
    if(self.printDocument.previewSetting.duplex > kDuplexSettingOff || self.printDocument.previewSetting.booklet == YES)
    {
        [self.pageViewController setDoubleSided:YES];
    }
    else
    {
        [self.pageViewController setDoubleSided:NO];
    }
}

- (void)goToPage:(NSInteger)pageIndex
{
    PDFPageContentViewController *current = [self viewControllerAtIndex:pageIndex];
    NSMutableArray *viewControllerArray = [NSMutableArray arrayWithObject:current];
    
    //if Spine is in mid location, must provide 2 controllers always. This occurs for booklet bind setting
    if(self.pageViewController.spineLocation == UIPageViewControllerSpineLocationMid)
    {
        if(pageIndex == 0)//if first index, provide last page(back cover) as the left side page of the first page (front cover)
        {
            NSInteger lastPageIndex = self.totalPageNum - 1;
            PDFPageContentViewController *lastPage = [self viewControllerAtIndex:lastPageIndex];
            [viewControllerArray insertObject:lastPage atIndex:0];
        }
        else if((pageIndex % 2) == 0) //if page index is even but not the first index, provide the page before it
        {
            PDFPageContentViewController *previous = [self viewControllerAtIndex:pageIndex - 1];
            [viewControllerArray insertObject:previous atIndex:0];
        }
        else // if the page index is odd, provide the page next to it
        {
            PDFPageContentViewController *next = [self viewControllerAtIndex:pageIndex + 1];
            [viewControllerArray addObject:next];
        }
    }
    
    [self.pageViewController setViewControllers:viewControllerArray direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
}

#pragma mark - UIPageViewControllerDataSource

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    NSInteger index = ((PDFPageContentViewController *) viewController).pageIndex;
    if (self.pageIsAnimating || index == NSNotFound )
    {
        return nil;
    }
    if(pageViewController.spineLocation == UIPageViewControllerSpineLocationMax)
    {
        return [self nextViewController:index];
    }
    return [self previousViewController:index];
}

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController
{
    NSInteger index = ((PDFPageContentViewController *) viewController).pageIndex;
    if (self.pageIsAnimating || index == NSNotFound )
    {
        return nil;
    }
    if(pageViewController.spineLocation == UIPageViewControllerSpineLocationMax)
    {
        return [self previousViewController:index];
    }
    return [self nextViewController:index];
}

- (UIViewController *)nextViewController:(NSInteger)index
{
    if(self.pageViewController.spineLocation == UIPageViewControllerSpineLocationMid)
    {
        if(index >= (self.totalPageNum - 2))//page turn for spine location mid is always by 2, when page shown is second to the last (The front of the last sheet), do not turn;
        {
            return nil;
        }
    }
    if((self.totalPageNum - 1) <= index)
    {
        return nil;
    }
    index++;
    return [self viewControllerAtIndex:index];
}

- (UIViewController *)previousViewController:(NSInteger)index
{
    if(self.pageViewController.spineLocation == UIPageViewControllerSpineLocationMid)
    {
        if(index== 0)//put the last page (back cover) at left side of the first page (front cover)
        {
            return [self viewControllerAtIndex:self.totalPageNum - 1];
        }
        
        if(index == (self.totalPageNum - 1))// the last page (back cover) should not flip back to the previous pages since it is placed beside the first page (front cover)
        {
            return nil;
        }
    }
    
    if(index == 0)
    {
        return nil;
    }
    index--;
    return [self viewControllerAtIndex:index];
}

- (PDFPageContentViewController *)viewControllerAtIndex:(NSInteger)index
{
    NSNumber *pageIndexKey = [NSNumber numberWithInteger:index];
    
    // Check if page needs to be created
    PDFPageContentViewController *viewController = [self.renderedViewControllers objectForKey:pageIndexKey];
    if (viewController == nil)
    {
        viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];
        viewController.pageIndex = index;
        
        // TODO: add buffer size limit
        [self.renderedViewControllers setObject:viewController forKey:pageIndexKey];
    }
    
    // Do not create a render operation if one is already started
    PDFRenderOperation *existingRenderOperation = [self.renderOperations objectForKey:pageIndexKey];
    if (existingRenderOperation != nil)
    {
        return viewController;
    }
    
    BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
    // Get paper size
    CGSize size = [PrintPreviewHelper getPaperDimensions:(kPaperSize)self.printDocument.previewSetting.paperSize isLandscape:isLandscape];
    //for booklet bind, 1 page will only occupy half of the paper. Divide the paper size by 2
    if(self.printDocument.previewSetting.booklet == YES)
    {
        if(isLandscape == YES) // if paper is in landscape, paper fold runs along the width else it runs along the height
        {
            size.width /= 2;
        }
        else
        {
            size.height/= 2;
        }
    }
    // Create render operation
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndex:index size:size delegate:self];
    [self.renderOperations setObject:renderOperation forKey:pageIndexKey];
    [self.renderQueue addOperation:renderOperation];
    
    return viewController;
}

- (void)pageViewController:(UIPageViewController *)pageViewController willTransitionToViewControllers:(NSArray *)pendingViewControllers
{
    self.pageIsAnimating = YES;
}

- (void)pageViewController:(UIPageViewController *)pageViewController didFinishAnimating:(BOOL)finished previousViewControllers:(NSArray *)previousViewControllers transitionCompleted:(BOOL)completed
{
    if (completed || finished)
    {
        self.pageIsAnimating = NO;
    }
    
    if(completed == YES)
    {
        PDFPageContentViewController *viewController = (PDFPageContentViewController *)[pageViewController.viewControllers lastObject];
        self.currentPage = viewController.pageIndex;
        [self setupPageLabel];
        [self.pageScroll setValue:self.currentPage + 1];
    }
}

#pragma mark -
- (void)renderDidDFinish:(PDFRenderOperation *)pdfRenderOperation
{
    NSNumber *pageIndexKey = [NSNumber numberWithInteger:pdfRenderOperation.pageIndex];
    [self.renderOperations removeObjectForKey:pageIndexKey];
    PDFPageContentViewController *viewController = [self.renderedViewControllers objectForKey:pageIndexKey];
    [viewController setImage:pdfRenderOperation.image];
}

#pragma mark - 

- (void)previewView:(PreviewView *)previewView didChangeZoomMode:(BOOL)zoomed
{
    [self.pageViewController.view setUserInteractionEnabled:!zoomed];
}

#pragma mark -

- (void)previewSettingDidChange
{
    // Destroy cache
    [self.renderedViewControllers removeAllObjects];
    
    // Cancel all operations
    [self.renderQueue cancelAllOperations];
    [self.renderOperations removeAllObjects];
    
    // Recompute aspect ratio
    CGFloat aspectRatio = [PrintPreviewHelper getAspectRatioForPaperSize:(kPaperSize)self.printDocument.previewSetting.paperSize];
    BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
    kPreviewViewOrientation orientation = kPreviewViewOrientationPortrait;
    if(isLandscape == YES)
    {
        orientation = kPreviewViewOrientationLandscape;
    }
    [self.previewView setPreviewWithOrientation:orientation aspectRatio:aspectRatio];
    
    //recompute total page number
    [self setupTotalPageNum];
    
    //set page label and page scroll based on the new total page number
    [self setupPageLabel];
    [self.pageScroll setValue:self.currentPage + 1];
    
    //reset-up page view controller with new bind setting
    [self setupPageviewControllerWithBindSetting];
    
    [self goToPage:self.currentPage];
}

#pragma mark - IBActions

- (IBAction)mainMenuAction:(id)sender
{
    [self.mainMenuButton setEnabled:NO];
    [self performSegueTo:[HomeViewController class]];
}

- (IBAction)printSettingsAction:(id)sender
{
    [self.printSettingsButton setEnabled:NO];
    [self performSegueTo:[PrintSettingsViewController class]];
}

- (IBAction)unwindToPrintPreview:(UIStoryboardSegue *)segue
{
    UIViewController* sourceViewController = [segue sourceViewController];
    
    if ([sourceViewController isKindOfClass:[HomeViewController class]])
    {
        [self.mainMenuButton setEnabled:YES];
    }
    else if ([sourceViewController isKindOfClass:[PrintSettingsViewController class]])
    {
        [self.printSettingsButton setEnabled:YES];
    }
}

/*Action when slider is dragged*/
- (IBAction)dragPageScrollAction:(id)sender
{
    UISlider *slider  = (UISlider *) sender;
    NSInteger pageNumber = slider.value;
    
    //update the current page  and page number label
    self.currentPage = pageNumber - 1;
    [self goToPage:self.currentPage];
    [self setupPageLabel];
}

/*Action when slider is tapped*/
- (IBAction)tapPageScrollAction:(id)sender
{
    UIGestureRecognizer *tap =  (UIGestureRecognizer *)sender;
    //get point in slider where it is tapped
    CGPoint point = [tap locationInView:self.pageScroll];
    //Get how many percent of the total slider length is the distance of the tapped point from the start point of the slider
    CGFloat scrollPercentage = point.x/self.pageScroll.bounds.size.width;
    
    //multiply the the percentage with the total number of pages in view to get the current page index;
    NSInteger pageNumber = (self.totalPageNum * scrollPercentage);
    if(pageNumber <= 0)
    {
        self.currentPage = 0;
    }
    else if(pageNumber > self.totalPageNum)
    {
        // the last viewable page for 2 page view or mid spine location is the front page of last sheet (second to the last page)
        if(self.pageViewController.spineLocation == UIPageViewControllerSpineLocationMid)
        {
            self.currentPage = self.totalPageNum - 2;
        }
        else
        {
            self.currentPage = self.totalPageNum - 1;
        }
    }
    else
    {
        self.currentPage = pageNumber - 1;
    }
    
    //update page in view, page number label. slider thumb position
    [self goToPage:self.currentPage];
    [self setupPageLabel];
    [self.pageScroll setValue:self.currentPage+1];
}

@end
