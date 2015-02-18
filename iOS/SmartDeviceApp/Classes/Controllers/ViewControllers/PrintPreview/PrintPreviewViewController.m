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
#import "AlertHelper.h"
#import "PrinterManager.h"
#import "Printer.h"
#define PREVIEW_MARGIN 10.0f

@interface PrintPreviewViewController ()

/**
 * Reference to the label displaying the PDF's filename.
 */
@property (nonatomic, weak) IBOutlet UILabel *titleLabel;

/**
 * Reference to the main menu button on the header.
 */
@property (nonatomic, weak) IBOutlet UIButton *mainMenuButton;

/**
 * Reference to the print settings button on the header.
 */
@property (nonatomic, weak) IBOutlet UIButton *printSettingsButton;

/**
 * Reference to the animated loading indicator in the preview area.
 * This is displayed while the page is not yet available for display.
 */
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicator;

/**
 * Reference to the area where the preview is displayed.
 */
@property (nonatomic, weak) IBOutlet UIView *previewArea;

/**
 * Reference to the area where the page slider is displayed.
 */
@property (weak, nonatomic) IBOutlet UIView *pageNavArea;

/**
 * Reference to the page slider.
 */
@property (weak, nonatomic) IBOutlet UISlider *pageScroll;

/**
 * Reference to the label displaying the "current page / total pages".
 */
@property (weak, nonatomic) IBOutlet UILabel *pageLabel;

/**
 * Reference to the splash screen.
 */
@property (weak, nonatomic) IBOutlet UIView *splashView;

/**
 * Reference to the actual print preview.
 */
@property (nonatomic, weak) IBOutlet PreviewView *previewView;

/**
 * Reference to the height constraint of the {@link pageNavArea}.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *pageNavAreaHeightConstraint;

/**
 * Reference to the left constraint of the {@link pageScroll}.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *pageScrollLeftConstraint;

/**
 * Reference to the vertical constraint of the {@link pageScroll}.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *pageScrollVerticalCenterConstraint;

/**
 * Reference to the top constraint of the {@link pageLabel}.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *pageLabelTopConstraint;

/**
 * Reference to the right constraint of the {@link pageLabel}.
 */
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *pageLabelRightConstraint;

/**
 * Reference to the UIPageViewController which controls the page-turning with curl display and animation.
 */
@property (nonatomic, weak) UIPageViewController *pageViewController;

/**
 * Reference to the PDF document object held by PDFFileManager.
 */
@property (nonatomic, weak) PrintDocument *printDocument;

/**
 * Flag that indicates whether or not {@link pageViewController} is currently being animated (page curl).
 */
@property (nonatomic) BOOL pageIsAnimating;

/**
 * Stores the total number of pages to be displayed.
 * This is needed in the {@link pageLabel} and is dependent on the current preview settings.
 */
@property (nonatomic) NSInteger totalPageNum;

/**
 * Stores the total number of pages needed for the layout. 
 * This includes the padded pages used as book ends.
 */
@property (nonatomic) NSInteger layoutPageNum;

/**
 * Queue for the PDFRenderOperation objects.
 */
@property (atomic, strong) NSOperationQueue *renderQueue;

/**
 * List of all active PDFRenderOperation objects.
 */
@property (atomic, strong) NSMutableArray *renderArray;

/**
 * Cache for all the rendered pages.
 */
@property (atomic, strong) RenderCache *renderCache;

/**
 * Requests the PDFFileManager to prepare the document object.
 * If there is no error ({@link kPDFErrorNone}), {@link setupPreview} is called after.\n
 * If there is an error, an error message is displayed instead after and the preview area remains empty.
 */
- (void)loadPDF;

/**
 * Sets-up the screen for displaying the first page of the preview.
 * The following steps are performed:
 *  - retrieves the PDF document object from PDFFileManager
 *  - displays the title of the PDF
 *  - displays the page slider area
 *  - applies the current preview settings
 *  - displays the first page
 */
- (void)setupPreview;

/**
 * Sets-up the spine location of the {@link pageViewController}.
 * This depends on the current preview settings.
 *
 * @param spineLocation the location of the spine
 * @param navigationOrientation the direction of page-turning
 */
- (void)setupPageviewControllerWithSpineLocation:(UIPageViewControllerSpineLocation)spineLocation navigationOrientation:(UIPageViewControllerNavigationOrientation)navigationOrientation;;

/**
 * Sets-up the binding side of the {@link pageViewController}.
 * This depends on the current preview settings.
 */
- (void)setupPageviewControllerWithBindSetting;

/**
 * Calculates the total pages based on the preview settings.
 */
- (void)setupTotalPageNum;

/**
 * Sets-up the {@link pageLabel}.
 */
- (void)setupPageLabel;

/**
 * Sets-up the display aspect ratio to match the paper size and finishing settings.
 */
- (void)setupDisplayAspectRatio;

/**
 * Loads the next UIViewController of the {@link pageViewController}.
 * This is used when turning the pages forward.
 *
 * @param index index of current UIViewController
 * @return the next UIViewController
 */
- (UIViewController *)nextViewController:(NSInteger)index;

/**
 * Loads the previous UIViewController of the {@link pageViewController}.
 * This is used when turning the pages backward.
 *
 * @param index index of current UIViewController
 * @return the previous UIViewController
 */
- (UIViewController *)previousViewController:(NSInteger)index;

/**
 * Loads the actual PDFPageContentViewController at the specified page.
 * This is used when turning pages.\n
 * If the required page is not yet rendered, it is added to the queue and the 
 * loading indicator is displayed.\n
 * If the required page is already rendered, it is retrieved instead from the
 * cache of rendered pages.
 *
 * @param index page to display
 * @return the PDFPageContentViewController at the specified page
 */
- (PDFPageContentViewController *)viewControllerAtIndex:(NSInteger)index;

/**
 * Loads the specified page to the {@link pageViewController}.
 * @param pageNum page to display
 */
- (void)goToPage:(NSInteger)pageNum;

/**
 * Unwind segue back to the "Print Preview" screen.
 * Called when transitioning back to the "Print Preview" screen from the
 * "Print Settings" screen or from the Main Menu panel.
 *
 * @param sender the segue object
 */
- (IBAction)unwindToPrintPreview:(UIStoryboardSegue *)segue;

/**
 * Responds to pressing the main menu button in the header.
 * Displays the Main Menu panel.
 *
 * @param sender the button object
 */
- (IBAction)mainMenuAction:(id)sender;

/**
 * Responds to the print settings button press.
 * Displays the "Print Settings" screen.
 *
 * @param sender the button object
 */
- (IBAction)printSettingsAction:(id)sender;

/**
 * Responds to dragging the thumb in the page slider.
 * 
 * @param sender the page slider object
 * @param event the drag touch event
 */
- (IBAction)dragPageScrollAction:(id)sender withEvent:(UIEvent *)event;

/**
 * Responds to tapping anywhere on the page slider.
 *
 * @param sender the page slider object
 */
- (IBAction)tapPageScrollAction:(id)sender;

/**
 * Checks if the specified setting can be previewed.
 * 
 * @param settingKey the defined string name of the preview setting
 * @return YES if previewable, NO otherwise
 */
- (BOOL)isNonPreviewableSetting:(NSString *)settingKey;

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
    self.renderCache = [[RenderCache alloc] initWithMaxItemCount:11];
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
        
        // Printer check
        //check if printer of the printdocument has already been deleted from DB
        if (self.printDocument.printer == nil || self.printDocument.printer.managedObjectContext == nil)
        {
            self.printDocument.printer = [[PrinterManager sharedPrinterManager] getDefaultPrinter];
        }
    }
}

-(void)viewDidLayoutSubviews
{
    [super viewDidLayoutSubviews];
    //if phone and landscape, adjust the position of the page label and page scroll
    if(UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone && self.pageNavArea.isHidden == NO)
    {
        if(UIInterfaceOrientationIsPortrait(self.interfaceOrientation) ==UIInterfaceOrientationPortrait)
        {
            self.pageNavAreaHeightConstraint.constant = 50;
            self.pageScrollLeftConstraint.constant = 20;
            self.pageLabelTopConstraint.constant = 30;
            self.pageLabelRightConstraint.constant = 0;
            self.pageScrollVerticalCenterConstraint.constant = 8;
        }
        else
        {
            CGFloat screenWidth = self.view.frame.size.width;
            
            self.pageNavAreaHeightConstraint.constant = 30;
            self.pageScrollLeftConstraint.constant = screenWidth * 0.25f;
            self.pageLabelRightConstraint.constant = screenWidth * 0.75f;
            self.pageLabelTopConstraint.constant = 10;
            self.pageScrollVerticalCenterConstraint.constant = 0;
        }
    }
    [self.view layoutIfNeeded];
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
            kAlertResult result;
            if (error == kPDFErrorPrintingNotAllowed)
            {
                result = kAlertResultErrFileDoesNotAllowPrinting;
            }
            else if (error == kPDFErrorLocked)
            {
                result = kAlertResultErrFileHasOpenPassword;
            }
            else
            {
                result = kAlertResultFileCannotBeOpened;
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [AlertHelper displayResult:result withTitle:kAlertTitleDefault withDetails:nil];
                [self.activityIndicator stopAnimating];
            });
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
    [self.pageScroll setValue:self.printDocument.currentPage + 1 animated:NO];
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
    [self.previewView setPageContentView:pageViewController.view];
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
        self.totalPageNum = self.printDocument.pageCount;
        int oddPages = self.printDocument.pageCount % 4;
        if (oddPages > 0)
        {
            self.totalPageNum += 4 - oddPages;
        }
        
        //add two pages for the book ends
        self.layoutPageNum = self.totalPageNum + 2;
        [self.pageScroll setMaximumValue:self.totalPageNum]; //the last page that can be visited for the slider should be the second to the last page  which is the front page of the last sheet
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
            [self.pageScroll setMaximumValue:self.layoutPageNum - 2];
        }
        else
        {
            self.layoutPageNum = self.totalPageNum; //on other cases, the total page number is the total pages needed for the layout
            [self.pageScroll setMaximumValue:self.totalPageNum];
        }
    }
    //reset current page if it is now way past the computed total page number
    if(self.printDocument.currentPage >= self.totalPageNum)
    {
        self.printDocument.currentPage = 0;
    }
}

- (void)setupPageLabel
{
    NSInteger totalSheets = self.totalPageNum;
    NSInteger currentSheet = self.printDocument.currentPage + 1;
    if((self.totalPageNum % 2) >  0 &&
       (self.printDocument.previewSetting.booklet == YES || self.printDocument.previewSetting.duplex != kDuplexSettingOff)) {
        totalSheets++;
    }

    self.pageLabel.text = [NSString stringWithFormat:NSLocalizedString(IDS_LBL_PAGE_DISPLAYED, @""), (long)currentSheet, (long)totalSheets];
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
        if (self.printDocument.previewSetting.finishingSide ==kFinishingSideTop)
        {
            orientation = kPreviewViewOrientationPortrait;
            if (isLandscape == YES)
            {
                aspectRatio = 0.5f / aspectRatio;
            }
            else
            {
                aspectRatio = 0.5f * aspectRatio;
            }
        }
        else
        {
            orientation = kPreviewViewOrientationLandscape;
            if (isLandscape == YES)
            {
                aspectRatio = 0.5f * aspectRatio;
            }
            else
            {
                aspectRatio = 0.5f / aspectRatio;
            }
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
                           (self.printDocument.previewSetting.booklet == YES && self.printDocument.previewSetting.bookletLayout == kBookletLayoutReverse));
        
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

        CGFloat aspectRatio = size.width / size.height;
        CGSize pageRenderSize = [[UIScreen mainScreen] bounds].size;
        if (UIInterfaceOrientationIsLandscape(self.interfaceOrientation) == YES)
        {
            pageRenderSize = CGSizeMake(pageRenderSize.height, pageRenderSize.width);
        }
        pageRenderSize.width = pageRenderSize.height * aspectRatio;
        
        PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:indices size:pageRenderSize delegate:self];
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
        self.printDocument.previewSetting.bookletLayout == kBookletLayoutReverse))
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
        self.printDocument.previewSetting.bookletLayout == kBookletLayoutReverse))
    {
        return [self previousViewController:index];
    }
    return [self nextViewController:index];
}


- (void)pageViewController:(UIPageViewController *)pageViewController willTransitionToViewControllers:(NSArray *)pendingViewControllers
{
    self.pageIsAnimating = YES;
    self.pageScroll.userInteractionEnabled = NO;
}

- (void)pageViewController:(UIPageViewController *)pageViewController didFinishAnimating:(BOOL)finished previousViewControllers:(NSArray *)previousViewControllers transitionCompleted:(BOOL)completed
{
    if (completed || finished)
    {
        self.pageIsAnimating = NO;
        self.pageScroll.userInteractionEnabled = YES;
    }
    
    if(completed == YES)
    {
        PDFPageContentViewController *viewController = (PDFPageContentViewController *)[pageViewController.viewControllers lastObject];
        if (self.pageViewController.isDoubleSided) {
            if (viewController.pageIndex == (self.layoutPageNum - 1))
            {
                self.printDocument.currentPage = 0;
            } else {
                if (self.printDocument.previewSetting.finishingSide == kFinishingSideRight ||
                        self.printDocument.previewSetting.bookletLayout == kBookletLayoutReverse) {
                    self.printDocument.currentPage = MAX(viewController.pageIndex, 0);
                } else {
                    self.printDocument.currentPage = MAX(viewController.pageIndex - 1, 0);
                }
            }
        } else {
            self.printDocument.currentPage =  viewController.pageIndex;
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
                                        KEY_BOOKLET_FINISH,
                                        KEY_STAPLE,
                                        KEY_PUNCH,
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
    if ([[PrinterManager sharedPrinterManager] countSavedPrinters] == 0)
    {
        [AlertHelper displayResult:kAlertResultErrNoPrinterSelected
                         withTitle:kAlertTitlePrintPreview
                       withDetails:nil];
    }
    //if getDefaultPrinter returns nil, there is an error in db request
    else if([[PrinterManager sharedPrinterManager] getDefaultPrinter] == nil)
    {
        [AlertHelper displayResult:kAlertResultErrDB
                         withTitle:kAlertTitlePrintPreview
                       withDetails:nil];
    }
    else
    {
        [self.printSettingsButton setEnabled:NO];
        [self performSegueTo:[PrintSettingsViewController class]];
    }
}

- (IBAction)dragPageScrollAction:(id)sender withEvent:(UIEvent *)event
{
    UISlider *slider = sender;
    NSInteger pageNumber = (NSInteger)(slider.value + 0.5f);
    
    //update the current page  and page number label
    // if double sided and page is at the back (even pages)  - always navigate to the next front page
    if(self.pageViewController.isDoubleSided == YES && (pageNumber % 2) > 0)
    {
        if (pageNumber != slider.maximumValue && pageNumber != 1) {
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
    UISlider *slider =(UISlider *)tap.view;
    //get point in slider where it is tapped
    CGPoint point = [tap locationInView:self.pageScroll];
    point.x = MAX(0.0f, point.x);
    point.x = MIN(self.pageScroll.bounds.size.width, point.x);
    //Get how many percent of the total slider length is the distance of the tapped point from the start point of the slider
    CGFloat scrollPercentage = point.x/self.pageScroll.bounds.size.width;
    
    //multiply the the percentage with the total number of pages in view to get the current page index;
    NSInteger pageNumber = (self.totalPageNum * scrollPercentage + 1.5f);
    pageNumber = MIN(self.totalPageNum, pageNumber);
   
    // if double sided and page is at the back (even pages) - always navigate to the next front page
    if(self.pageViewController.isDoubleSided == YES && (pageNumber % 2) > 0)
    {
        if (pageNumber != slider.maximumValue && pageNumber != 1) {
            pageNumber--;
        }
    }
    
    self.printDocument.currentPage = pageNumber - 1;
    //update page in view, page number label. slider thumb position
    [self goToPage:self.printDocument.currentPage];
    [self setupPageLabel];
    
    [self.pageScroll setValue:pageNumber animated:YES];
}

@end
