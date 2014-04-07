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

/**
 IBOutlets
 */
@property (nonatomic, weak) IBOutlet UILabel *titleLabel;
@property (nonatomic, weak) IBOutlet UIButton *mainMenuButton;
@property (nonatomic, weak) IBOutlet UIButton *printSettingsButton;
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicator;
@property (nonatomic, weak) IBOutlet UIView *previewArea;
@property (weak, nonatomic) IBOutlet UIView *pageNavArea;
@property (weak, nonatomic) IBOutlet UISlider *pageScroll;
@property (weak, nonatomic) IBOutlet UILabel *pageLabel;
@property (nonatomic, weak) IBOutlet PreviewView *previewView;

/**
 Page view controller
 */
@property (nonatomic, weak) UIPageViewController *pageViewController;

/**
 Document object:
 */
@property (nonatomic, weak) PrintDocument *printDocument;

/**
 Current page
 */
@property (nonatomic) NSInteger currentPage;

/**
 Indicates whether or not the page view controller is currently being animated (page curl)
 */
@property (nonatomic) BOOL pageIsAnimating;

/**
 Number of pages
 */
@property (nonatomic) NSInteger totalPageNum;

/**
 Number of pdf pages per logical page (variations based on 2-up/4-up setting)
 */
@property (nonatomic) NSInteger numPDFPagesPerPage;

/**
 Render queue
 */
@property (atomic, strong) NSOperationQueue *renderQueue;

/**
 List of active render operations
 */
@property (atomic, strong) NSMutableArray *renderArray;

/**
 Cache of pages and rendered images
 */
@property (atomic, strong) RenderCache *renderCache;

/**
 Prepares PDF document
 */
- (void)loadPDF;

/**
 Setup views for preview
 */
- (void)setupPreview;

/**
 Setup page view controller based on spine location
 */
- (void)setupPageviewControllerWithSpineLocation:(UIPageViewControllerSpineLocation) spineLocation navigationOrientation: (UIPageViewControllerNavigationOrientation) navigationOrientation;

/**
 Calculate total page number based on print settings
 */
- (void)computeTotalPageNum;

/**
 Setup page scroll view
 */
- (void)setupPageScroll;

/**
 Setup page label view
 */
- (void)setupPageLabel;

/**
 Updates spine setting of page view controller based on print setting
 @return YES if update is succesful
 */
- (BOOL)applyBindSetting;

/**
 Loads next view controller of the page view controller
 @param index
        Index of current view controller
 @return Next view controller
 */
- (UIViewController *)nextViewController:(NSInteger)index;

/**
 Loads previous view controlller of the page view controller
 @param index
        Index of the current view controller
 @return Previous view controller
 */
- (UIViewController *)previousViewController:(NSInteger)index;

/**
 Loads view controller for the specified index
 @param index
        Index of the view controller to be loaded
 @return View controller for the specified index
 */
- (PDFPageContentViewController *)viewControllerAtIndex:(NSInteger)index;

/**
 Change current page to specified page
 @param pageNum
        Index of the page
 */
- (void)goToPage:(NSInteger)pageNum;

/**
 Action when view unwinds back to print preview
 */
- (IBAction)unwindToPrintPreview:(UIStoryboardSegue *)segue;

/**
 Action when main menu button is tapped
 */
- (IBAction)mainMenuAction:(id)sender;

/**
 Action when print settings button is tapped
 */
- (IBAction)printSettingsAction:(id)sender;

/**
 Action when page scroller thumb is dragged
 */
- (IBAction)dragPageScrollAction:(id)sender;

/**
 Action when page scroller is tapped
 */
- (IBAction)tapPageScrollAction:(id)sender;

@end

@implementation PrintPreviewViewController

#pragma mark - Public Methods
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
    
    self.printSettingsButton.hidden = YES;
    self.previewView.hidden = YES;
    self.previewView.delegate = self;
    self.pageIsAnimating = NO;
    
    NSOperationQueue *renderQueue = [[NSOperationQueue alloc] init];
    renderQueue.maxConcurrentOperationCount = 2;
    renderQueue.name = @"RenderQueue";
    self.renderQueue = renderQueue;
    self.renderArray = [[NSMutableArray alloc] init];
    self.renderCache = [[RenderCache alloc] initWithMaxItemCount:20];
    self.renderCache.delegate = self;
    
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
        kPDFError error = [[PDFFileManager sharedManager] setupDocument];
        if (error == kPDFErrorNone)
        {
            dispatch_async(dispatch_get_main_queue(), ^
            {
                [self setupPreview];
            });
        }
        else
        {
            // TODO: Error message
        }
    });
}

- (void)setupPreview
{
    self.printDocument = [[PDFFileManager sharedManager] printDocument];
    self.printDocument.delegate = self;
    self.currentPage = 0;
    self.numPDFPagesPerPage = 1;
    self.titleLabel.text = [[PDFFileManager sharedManager] fileName];
    self.printSettingsButton.hidden = NO;
    self.previewView.hidden = NO;
    self.pageNavArea.hidden = NO;
    [self.activityIndicator stopAnimating];
    [self computeTotalPageNum];
    [self setupPageLabel];
    [self setupPageScroll];
    
    // Get aspect ratio and orientation
    CGFloat aspectRatio = [PrintPreviewHelper getAspectRatioForPaperSize:(kPaperSize)self.printDocument.previewSetting.paperSize];
    BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
    kPreviewViewOrientation orientation = kPreviewViewOrientationPortrait;
    if(isLandscape == YES)
    {
        orientation = kPreviewViewOrientationLandscape;
    }
    
    self.previewView.hidden = NO;
    [self.previewView setPreviewWithOrientation:orientation aspectRatio:aspectRatio];
    
    // Create PageViewController
    // Assume left spine
    UIPageViewController *pageViewController = [[UIPageViewController alloc] initWithTransitionStyle:UIPageViewControllerTransitionStylePageCurl navigationOrientation:UIPageViewControllerNavigationOrientationHorizontal options:@{UIPageViewControllerOptionSpineLocationKey: [NSNumber numberWithInteger:UIPageViewControllerSpineLocationMin]}];
    pageViewController.view.translatesAutoresizingMaskIntoConstraints = NO;
    [self addChildViewController:pageViewController];
    [self.previewView.contentView addSubview:pageViewController.view];
    NSDictionary *views = @{@"pageView": pageViewController.view};
    [self.previewView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[pageView]|" options:0 metrics:nil views:views]];
    [self.previewView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[pageView]|" options:0 metrics:nil views:views]];
    pageViewController.dataSource = self;
    pageViewController.delegate = self;
    
    self.currentPage = 0;
    PDFPageContentViewController *initial = [self viewControllerAtIndex:self.currentPage];
    [pageViewController setViewControllers:@[initial] direction:UIPageViewControllerNavigationDirectionForward animated:YES completion:nil];
    
    self.pageViewController = pageViewController;
}

- (void)setupPageviewControllerWithSpineLocation:(UIPageViewControllerSpineLocation) spineLocation navigationOrientation: (UIPageViewControllerNavigationOrientation) navigationOrientation
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
    
    [self goToPage:self.currentPage];
    
    self.pageViewController = pageViewController;
}

- (void)computeTotalPageNum
{
    NSUInteger numPagesPerSheet =[PrintPreviewHelper numberOfPagesPerSheetForPaginationSetting:self.printDocument.previewSetting.imposition];
    self.totalPageNum = self.printDocument.pageCount/numPagesPerSheet;
    if((self.printDocument.pageCount % numPagesPerSheet) > 0)
    {
        self.totalPageNum++;
    }
    if(numPagesPerSheet != self.numPDFPagesPerPage)
    {
        self.currentPage = 0;
    }
    self.numPDFPagesPerPage = numPagesPerSheet;
}

- (void)setupPageScroll
{
    [self.pageScroll setMaximumTrackImage:[[UIImage imageNamed:@"img_slider_maximum"] resizableImageWithCapInsets:UIEdgeInsetsMake(0, 0, 0, 5)]forState: UIControlStateNormal];
    [self.pageScroll setMinimumTrackImage:[[UIImage imageNamed:@"img_slider_minimum"] resizableImageWithCapInsets:UIEdgeInsetsMake(0, 5, 0, 0) ] forState: UIControlStateNormal];
    [self.pageScroll setThumbImage:[UIImage imageNamed:@"img_slider_thumb"] forState: UIControlStateNormal];
    [self.pageScroll setThumbImage:[UIImage imageNamed:@"img_slider_thumb"] forState: UIControlStateHighlighted];
    [self.pageScroll setMinimumValue:1];
    [self.pageScroll setMaximumValue:self.totalPageNum];
    [self.pageScroll setContinuous:NO];
}

- (void)setupPageLabel
{
    NSString *pageString = @"PAGE";
    self.pageLabel.text = [NSString stringWithFormat:@"%@ %ld/%ld", pageString, (long)self.currentPage + 1, (long)self.totalPageNum];
}

- (BOOL)applyBindSetting
{
    UIPageViewControllerSpineLocation spineLocation = UIPageViewControllerSpineLocationMin;
    UIPageViewControllerNavigationOrientation navOrientation = UIPageViewControllerNavigationOrientationHorizontal;
    if(self.printDocument.previewSetting.finishingSide == kFinishingSideRight)
    {
        spineLocation = UIPageViewControllerSpineLocationMax;
    }
    else if(self.printDocument.previewSetting.finishingSide == kFinishingSideTop)
    {
        navOrientation = UIPageViewControllerNavigationOrientationVertical;
    }
    
    if(self.pageViewController!= nil && self.pageViewController.spineLocation == spineLocation && self.pageViewController.navigationOrientation == navOrientation)
    {
        //don't resetup page view controller if spine location and navigationOrientation are still the same
        return NO;
    }
    
    [self setupPageviewControllerWithSpineLocation:spineLocation navigationOrientation:navOrientation];
    return YES;
}

- (UIViewController *)nextViewController:(NSInteger)index
{
    if((self.totalPageNum - 1) == index)
    {
        return nil;
    }
    index++;
    return [self viewControllerAtIndex:index];
}

- (UIViewController *)previousViewController:(NSInteger)index
{
    if(index == 0)
    {
        return nil;
    }
    index--;
    return [self viewControllerAtIndex:index];
}

- (PDFPageContentViewController *)viewControllerAtIndex:(NSInteger)index
{
    // Check if page needs to be created
    RenderCacheItem *renderCacheItem = [self.renderCache itemWithIndex:index];
    if (renderCacheItem == nil)
    {
        renderCacheItem = [[RenderCacheItem alloc] init];
        renderCacheItem.viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];
        renderCacheItem.viewController.pageIndex = index;
        [self.renderCache addItem:renderCacheItem withIndex:index];
    }
    else if (renderCacheItem.image != nil)
    {
        // Use cache if it is available
        if (renderCacheItem.viewController == nil)
        {
            renderCacheItem.viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];
            renderCacheItem.viewController.pageIndex = index;
        }
        renderCacheItem.viewController.image = renderCacheItem.image;
    }
    
    NSMutableArray *activeIndices = [[NSMutableArray alloc] init];
    // Obtain list of pages that is currently being rendered in the background
    for (PDFRenderOperation *renderOperation in self.renderArray)
    {
        [activeIndices addObjectsFromArray:renderOperation.pageIndices];
    }
    
    NSMutableArray *indices = [[NSMutableArray alloc] init];
    // Add current page only if it is not already being rendered in the background or if it is already rendered
    if (renderCacheItem.image == nil && [activeIndices indexOfObject:[NSNumber numberWithUnsignedInteger:index]] == NSNotFound)
    {
        [indices addObject:[NSNumber numberWithUnsignedInteger:index]];
    }
    
    // Add pages -5.. and ..+5 of the current page
    for (int i = 1; i <= 5; i++)
    {
        NSInteger forwardIndex = index + i;
        NSInteger backwardIndex = index - i;
        
        // Add only valid pages and if the page is not already being rendered in the background or if it is already rendered
        if (forwardIndex < self.totalPageNum)
        {
            //RenderCacheItem *forwardCacheItem = [self.renderCache objectForKey:[NSNumber numberWithUnsignedInteger:forwardIndex]];
            RenderCacheItem *forwardCacheItem = [self.renderCache itemWithIndex:forwardIndex];
            if ((forwardCacheItem == nil || forwardCacheItem.image == nil) && [activeIndices indexOfObject:[NSNumber numberWithUnsignedInteger:forwardIndex]] == NSNotFound)
            {
                [indices addObject:[NSNumber numberWithUnsignedInteger:forwardIndex]];
            }
        }
        if (backwardIndex >= 0)
        {
            //RenderCacheItem *backwardCacheItem = [self.renderCache objectForKey:[NSNumber numberWithUnsignedInteger:backwardIndex]];
            RenderCacheItem *backwardCacheItem = [self.renderCache itemWithIndex:backwardIndex];
            if ((backwardCacheItem == nil || backwardCacheItem.image == nil) && [activeIndices indexOfObject:[NSNumber numberWithUnsignedInteger:backwardIndex]] == NSNotFound)
            {
                [indices addObject:[NSNumber numberWithUnsignedInteger:backwardIndex]];
            }
        }
    }
    
    // Create a render operation only if there is a page to render
    if (indices.count > 0)
    {
        BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
        CGSize size = [PrintPreviewHelper getPaperDimensions:(kPaperSize)self.printDocument.previewSetting.paperSize isLandscape:isLandscape];
        PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:indices size:size delegate:self];
        [self.renderArray addObject:renderOperation];
        [self.renderQueue addOperation:renderOperation];
    }
    
    return renderCacheItem.viewController;
}

- (void)goToPage:(NSInteger)pageNum
{
    PDFPageContentViewController *current = [self viewControllerAtIndex:pageNum];
    [self.pageViewController setViewControllers:@[current] direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
}

#pragma mark - UIPageViewControllerDataSource Methods

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

#pragma mark - PDFRenderOperationDelegate Methods

- (void)renderOperation:(PDFRenderOperation *)pdfRenderOperation didFinishRenderingImageForPage:(NSNumber *)pageIndex
{
    UIImage *image = [pdfRenderOperation.images objectForKey:pageIndex];
    //RenderCacheItem *renderCacheItem = [self.renderCache objectForKey:pageIndex];
    RenderCacheItem *renderCacheItem = [self.renderCache itemWithIndex:[pageIndex unsignedIntegerValue]];
    if (renderCacheItem == nil)
    {
        renderCacheItem = [[RenderCacheItem alloc] init];
        //[self.renderCache setObject:renderCacheItem forKey:pageIndex];
        [self.renderCache addItem:renderCacheItem withIndex:[pageIndex unsignedIntegerValue]];
    }
    renderCacheItem.image = image;
    renderCacheItem.viewController.image = image;
}

- (void)renderDidDFinish:(PDFRenderOperation *)pdfRenderOperation
{
    [self.renderArray removeObject:pdfRenderOperation];
}

#pragma mark - PreviewViewDelegate Methods

- (void)previewView:(PreviewView *)previewView didChangeZoomMode:(BOOL)zoomed
{
    [self.pageViewController.view setUserInteractionEnabled:!zoomed];
}

#pragma mark - RenderCacheDelegate Methods

- (void)cache:(NSCache *)cache willEvictObject:(id)obj
{
    RenderCacheItem *renderCacheItem = obj;
    NSLog(@"**Releasing: %d **", renderCacheItem.viewController.pageIndex);
}

- (NSUInteger) currentIndex
{
    return self.currentPage;
}

#pragma mark - PrintDocument Delegate Methods

- (void)previewSettingDidChange
{
    // Cancel all operations
    [self.renderQueue cancelAllOperations];
    [self.renderArray removeAllObjects];
    
    // Clear cache
    [self.renderCache removeAllItems];
    
    // Recompute aspect ratio
    CGFloat aspectRatio = [PrintPreviewHelper getAspectRatioForPaperSize:(kPaperSize)self.printDocument.previewSetting.paperSize];
    BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
    kPreviewViewOrientation orientation = kPreviewViewOrientationPortrait;
    if(isLandscape == YES)
    {
        orientation = kPreviewViewOrientationLandscape;
    }
    [self.previewView setPreviewWithOrientation:orientation aspectRatio:aspectRatio];
    
    [self computeTotalPageNum];
    [self setupPageLabel];
    [self.pageScroll setValue:self.currentPage + 1];
    
    [self applyBindSetting];
    
    [self goToPage:self.currentPage];
}


#pragma mark - IBAction Methods


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

- (IBAction)dragPageScrollAction:(id)sender
{
    UISlider *slider  = (UISlider *) sender;
    NSInteger pageNumber = slider.value;
    self.currentPage = pageNumber - 1;
    [self goToPage:self.currentPage];
    [self setupPageLabel];
}

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
        self.currentPage = self.totalPageNum - 1;
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
