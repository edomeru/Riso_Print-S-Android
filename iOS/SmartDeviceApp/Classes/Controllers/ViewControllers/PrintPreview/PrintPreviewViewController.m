//
//  PrintPreviewViewController.m
//  Tester
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
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
@property (weak, nonatomic) IBOutlet UIView *splashView;
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
 Indicates whether or not the page view controller is currently being animated (page curl)
 */
@property (nonatomic) BOOL pageIsAnimating;

/**
 Number of pages
 */
@property (nonatomic) NSInteger totalPageNum;

/**
 Number of pages needed the needed layout. Includes the padded pages used as book ends.
 */
@property (nonatomic) NSInteger layoutPageNum;

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
- (void)setupPageviewControllerWithSpineLocation:(UIPageViewControllerSpineLocation)spineLocation navigationOrientation:(UIPageViewControllerNavigationOrientation)navigationOrientation;;

- (void)setupPageviewControllerWithBindSetting;

/**
 Calculate total page number based on print settings
 */
- (void)setupTotalPageNum;

/**
 Setup page label view
 */
- (void)setupPageLabel;

/**
 Setup display aspect ratio to match paper size and finishing
 */
- (void)setupDisplayAspectRatio;

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
- (IBAction)dragPageScrollAction:(id)sender withEvent:(UIEvent *)event;

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
    
    self.splashView.hidden = NO;
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
    
    //set theme of UISlider
    [[UISlider appearance] setMaximumTrackImage:[[UIImage imageNamed:@"img_slider_maximum"] resizableImageWithCapInsets:UIEdgeInsetsMake(0, 0, 0, 5)]forState: UIControlStateNormal];
    [[UISlider appearance]  setMinimumTrackImage:[[UIImage imageNamed:@"img_slider_minimum"] resizableImageWithCapInsets:UIEdgeInsetsMake(0, 5, 0, 0) ] forState: UIControlStateNormal];
    [[UISlider appearance]  setThumbImage:[UIImage imageNamed:@"img_slider_thumb"] forState: UIControlStateNormal];
    [[UISlider appearance]  setThumbImage:[UIImage imageNamed:@"img_slider_thumb"] forState: UIControlStateHighlighted];
    
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
    self.titleLabel.text = self.printDocument.name;
    self.printSettingsButton.hidden = NO;
    self.splashView.hidden = YES;
    self.previewView.hidden = NO;
    self.pageNavArea.hidden = NO;
    [self.activityIndicator stopAnimating];
    [self setupTotalPageNum];
    [self setupPageLabel];
    
    self.previewView.hidden = NO;
    [self setupDisplayAspectRatio];

    [self setupPageviewControllerWithBindSetting];
    [self.pageScroll setValue:self.printDocument.currentPage animated:NO];
    [self goToPage:self.printDocument.currentPage];
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
        //total number of pages is the actual number of pdf pages  + additional pages to make number of pages multiple by 4
        self.totalPageNum = self.printDocument.pageCount  +  (4 - self.printDocument.pageCount % 4);
        
        //add two pages for the book ends
        self.layoutPageNum = self.totalPageNum + 2;
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
        
        if(self.printDocument.previewSetting.duplex > kDuplexSettingOff)
        {
            //if duplex and the number of pages is odd, still include the back of the last page, then add 2 pages for book ends
            self.layoutPageNum = self.totalPageNum + self.totalPageNum % 2 + 2;
        }
        else
        {
            self.layoutPageNum = self.totalPageNum; //on other cases, the total page number is the total pages needed for the layout
        }
        [self.pageScroll setMaximumValue:self.totalPageNum];
    }
    //reset current page if it is now way past the computed total page number
    if(self.printDocument.currentPage >= self.totalPageNum)
    {
        self.printDocument.currentPage = 0;
    }
}

- (void)setupPageLabel
{
    NSString *pageString = @"PAGE";
    NSInteger totalSheets = self.totalPageNum;
    NSInteger currentSheet = self.printDocument.currentPage + 1;
    if(self.printDocument.previewSetting.booklet == YES || self.printDocument.previewSetting.duplex != kDuplexSettingOff)
    {
        currentSheet = self.printDocument.currentPage/2 + 1;
        totalSheets = self.totalPageNum/2 + self.totalPageNum % 2;
    }
    self.pageLabel.text = [NSString stringWithFormat:@"%@ %ld/%ld", pageString, (long)currentSheet, (long)totalSheets];
}

- (void)setupPageviewControllerWithBindSetting
{
    //Determine the spine location and navigation direction considering the finishing side and booklet bind setting
    UIPageViewControllerSpineLocation spineLocation = UIPageViewControllerSpineLocationMin;
    UIPageViewControllerNavigationOrientation navOrientation = UIPageViewControllerNavigationOrientationHorizontal;
    
    if(self.printDocument.previewSetting.booklet == YES)
    {
        spineLocation = UIPageViewControllerSpineLocationMid;
        if(self.printDocument.previewSetting.bookletLayout == kBookletLayoutTopToBottom)
        {
            navOrientation = UIPageViewControllerNavigationOrientationVertical;
        }
    }
    else if(self.printDocument.previewSetting.duplex > kDuplexSettingOff)
    {
        spineLocation = UIPageViewControllerSpineLocationMid;
        if(self.printDocument.previewSetting.finishingSide == kFinishingSideTop)
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

- (void)setupDisplayAspectRatio
{
    //ratio is width / height
    CGFloat aspectRatio = [PrintPreviewHelper getAspectRatioForPaperSize:(kPaperSize)self.printDocument.previewSetting.paperSize];
    BOOL isLandscape = [PrintPreviewHelper isPaperLandscapeForPreviewSetting:self.printDocument.previewSetting];
    kPreviewViewOrientation orientation = kPreviewViewOrientationPortrait;
    if(isLandscape == YES)
    {
        orientation = kPreviewViewOrientationLandscape;
    }
    
    //Duplex 2 page view display area computation
    if(self.printDocument.previewSetting.duplex > kDuplexSettingOff && self.printDocument.previewSetting.booklet == NO)
    {
        if((self.printDocument.previewSetting.finishingSide == kFinishingSideTop && isLandscape == NO) ||
           (self.printDocument.previewSetting.finishingSide != kFinishingSideTop && isLandscape == YES))
        {
            aspectRatio/=2; //twice the height for 2 papers, 1 on top of the other
        }
        else
        {
            aspectRatio*= 2; //twice the width for 2 papers side by side
        }
    }
    
    [self.previewView setPreviewWithOrientation:orientation aspectRatio:aspectRatio];
}
- (void)goToPage:(NSInteger)pageIndex
{
    PDFPageContentViewController *current = [self viewControllerAtIndex:pageIndex];
    NSMutableArray *viewControllerArray = [NSMutableArray arrayWithObject:current];
    
    //if Spine is in mid location, must provide 2 controllers always. This occurs for booklet bind setting and duplex
    if(self.pageViewController.spineLocation == UIPageViewControllerSpineLocationMid)
    {
        //the two pages provided are reversed if right side finishing or booklet with right to left layout
        BOOL isReversed = (self.printDocument.previewSetting.finishingSide == kFinishingSideRight ||
                           (self.printDocument.previewSetting.booklet == YES && self.printDocument.previewSetting.bookletLayout == kBookletLayoutRightToLeft));
        
        if(pageIndex == 0)//if first index, provide the last bookend page as the other half
        {
            NSInteger lastPageIndex = self.layoutPageNum - 1;
            PDFPageContentViewController *lastPage = [self viewControllerAtIndex:lastPageIndex];
            if(isReversed == YES)
            {
                [viewControllerArray addObject:lastPage];
            }
            else
            {
                [viewControllerArray insertObject:lastPage atIndex:0];
            }
        }
        else if((pageIndex % 2) == 0) //if page index is even but not the first index, provide the page before it
        {
            PDFPageContentViewController *previous = [self viewControllerAtIndex:pageIndex - 1];
            if(isReversed == YES)
            {
                [viewControllerArray addObject:previous];
                
            }
            else
            {
                [viewControllerArray insertObject:previous atIndex:0];
                
            }
        }
        else //if the page index is odd, provide the page next to it
        {
            PDFPageContentViewController *next = [self viewControllerAtIndex:pageIndex + 1];
            if(isReversed == YES)
            {
                 [viewControllerArray insertObject:next atIndex:0];
            }
            else
            {
                [viewControllerArray addObject:next];
            }
        }
    }
    
    [self.pageViewController setViewControllers:viewControllerArray direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
}

- (UIViewController *)nextViewController:(NSInteger)index
{
    if(self.pageViewController.spineLocation == UIPageViewControllerSpineLocationMid)
    {
        if(index >= (self.layoutPageNum  - 2))//page turn for spine location mid is always by 2, when page shown is second to the last (The front of the last sheet), do not turn;
        {
            return nil;
        }
    }
    if((self.layoutPageNum  - 1) <= index)
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
        if(index== 0)//put the book end as the previous page of the first page
        {
            return [self viewControllerAtIndex:self.layoutPageNum  - 1];
        }
        
        if(index >= (self.layoutPageNum  - 1))//if already the book end page, do not turn
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
    // if the total page num is less than the layout page num, the index must be checked if it is the bookend page index
    // the bookend page indices are the last 2 indices
    if(self.totalPageNum < self.layoutPageNum)
    {
        NSInteger lastIndex = self.layoutPageNum - 1;
        if(index >= (lastIndex - 1))
        {
            //if a bookend page, no need to add to render cache since it is just a PDFPageContentViewController with a transparent view
            PDFPageContentViewController *viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];
            viewController.isBookendPage = YES;
            viewController.pageIndex = index;
            return viewController;
        }
    }
    
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
    
    renderCacheItem.viewController.isBookendPage = NO;
    
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
        
        // For booklet bind, 1 page will only occupy half of the paper. Divide the paper size by 2
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

        PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:indices size:size delegate:self];
        [self.renderArray addObject:renderOperation];
        [self.renderQueue addOperation:renderOperation];
    }
    
    return renderCacheItem.viewController;
}

#pragma mark - UIPageViewControllerDataSource Methods

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    NSInteger index = ((PDFPageContentViewController *) viewController).pageIndex;
    if (self.pageIsAnimating || index == NSNotFound )
    {
        return nil;
    }
    //if right to left paging, reverse provided pages
    if(self.printDocument.previewSetting.finishingSide == kFinishingSideRight ||
       (self.printDocument.previewSetting.booklet == YES &&
        self.printDocument.previewSetting.bookletLayout == kBookletLayoutRightToLeft))
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
   //if right to left paging, reverse provided pages
    if(self.printDocument.previewSetting.finishingSide == kFinishingSideRight ||
       (self.printDocument.previewSetting.booklet == YES &&
        self.printDocument.previewSetting.bookletLayout == kBookletLayoutRightToLeft))
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
        if(viewController.pageIndex < self.totalPageNum - 1)
        {
            self.printDocument.currentPage = viewController.pageIndex;
        }
        else
        {
            if(viewController.pageIndex == self.layoutPageNum - 1 && self.layoutPageNum != self.totalPageNum)
            {
                self.printDocument.currentPage = 0;
            }
            else
            {
                self.printDocument.currentPage = self.totalPageNum - 1;
            }
        }
        [self setupPageLabel];
        [self.pageScroll setValue:self.printDocument.currentPage + 1];
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
    NSLog(@"**Releasing: %ld **", (long)renderCacheItem.viewController.pageIndex);
}

- (NSUInteger) currentIndex
{
    return self.printDocument.currentPage;
}

#pragma mark - PrintDocument Delegate Methods

- (BOOL)previewSettingDidChange:(NSString *)keyChanged
{
    if([self isNonPreviewableSetting:keyChanged] == YES)
    {
        return NO;
    }
    
    // Cancel all operations
    [self.renderQueue cancelAllOperations];
    [self.renderArray removeAllObjects];
    
    // Clear cache
    [self.renderCache removeAllItems];
    
    // Recompute aspect ratio
    [self setupDisplayAspectRatio];
    
    //recompute total page number
    [self setupTotalPageNum];
    
    //set page label and page scroll based on the new total page number
    [self setupPageLabel];
    [self.pageScroll setValue:self.printDocument.currentPage + 1];
    
    //reset-up page view controller with new bind setting
    [self setupPageviewControllerWithBindSetting];
    
    [self goToPage:self.printDocument.currentPage];
    
    return YES;
}

- (BOOL)isNonPreviewableSetting:(NSString *)settingKey
{
    NSArray *nonPreviewSettingKeys = [NSArray arrayWithObjects:
                                        KEY_COPIES,
                                        KEY_OUTPUT_TRAY,
                                        KEY_PAPER_TYPE,
                                        KEY_SORT,
                                        KEY_INPUT_TRAY,
                                        nil];
    return [nonPreviewSettingKeys containsObject:settingKey];
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

- (IBAction)dragPageScrollAction:(id)sender withEvent:(UIEvent *)event
{
    UISlider *slider = sender;
    NSInteger pageNumber = (NSInteger)(slider.value + 0.5f);
    
    //update the current page  and page number label
    // if double sided and page is at the back (even pages)  - always navigate to the next front page
    if(self.pageViewController.isDoubleSided == YES && (pageNumber % 2) == 0)
    {
        //go to the next front page
        if(pageNumber < self.totalPageNum)
        {
            pageNumber++;
        }
        else //if there are no more front pages, go the previous front page
        {
            pageNumber--;
        }
    }
    
    self.printDocument.currentPage = pageNumber - 1;
    [self setupPageLabel];
    
    UITouch *touch = [event.allTouches anyObject];
    if (touch.phase == UITouchPhaseCancelled || touch.phase == UITouchPhaseEnded)
    {
        [slider setValue:pageNumber animated:YES];
        [self goToPage:self.printDocument.currentPage];
    }
}

- (IBAction)tapPageScrollAction:(id)sender
{
    UIGestureRecognizer *tap =  (UIGestureRecognizer *)sender;
    //get point in slider where it is tapped
    CGPoint point = [tap locationInView:self.pageScroll];
    //Get how many percent of the total slider length is the distance of the tapped point from the start point of the slider
    CGFloat scrollPercentage = point.x/self.pageScroll.bounds.size.width;
    
    //multiply the the percentage with the total number of pages in view to get the current page index;
    NSInteger pageNumber = (self.totalPageNum * scrollPercentage + 1.5f);
   
    // if double sided and page is at the back (even pages) - always navigate to the next front page
    if(self.pageViewController.isDoubleSided == YES && (pageNumber % 2)== 0)
    {
        if(pageNumber < self.totalPageNum)
        {
            pageNumber++;
        }
        else
        {
            pageNumber--;
        }
    }
    
    if(pageNumber <= 0)
    {
        pageNumber = 1;
    }
    else if(pageNumber > self.totalPageNum)
    {
        if(self.pageViewController.isDoubleSided == YES && (self.totalPageNum % 2 == 0)) // double sided with even number of pages, give the front page of the last sheet
        {
            pageNumber = self.totalPageNum - 1;
        }
        else
        {
           pageNumber =  self.totalPageNum; // give the last page
        }
    }
    
    self.printDocument.currentPage = pageNumber - 1;
    //update page in view, page number label. slider thumb position
    [self goToPage:self.printDocument.currentPage];
    [self setupPageLabel];
    
    [self.pageScroll setValue:pageNumber animated:YES];
}

@end
