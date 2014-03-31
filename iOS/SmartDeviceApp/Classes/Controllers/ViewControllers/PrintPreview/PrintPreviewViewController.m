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
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "PDFPageContentViewController.h"
#import "PrintSettingsHelper.h"

#define PREVIEW_MARGIN 10.0f

@interface PrintPreviewViewController ()

// IBOutlets
@property (nonatomic, weak) IBOutlet UILabel *titleLabel;
@property (nonatomic, weak) IBOutlet UIButton *mainMenuButton;
@property (nonatomic, weak) IBOutlet UIButton *printSettingsButton;
@property (nonatomic, weak) IBOutlet UIActivityIndicatorView *activityIndicator;
@property (nonatomic, weak) IBOutlet UIView *previewArea;

// Preview View
@property (nonatomic, weak) IBOutlet UIView *previewView;
@property (nonatomic, weak) NSLayoutConstraint *aspectRatioConstraint;

// PageView Controller
@property (nonatomic, weak) UIPageViewController *pageViewController;

// Document object
@property (nonatomic, weak) PrintDocument *printDocument;

// Display
@property (nonatomic) NSInteger currentPage;

- (void)loadPDF;
- (void)setupPreview;

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
    
    self.printSettingsButton.hidden = NO;
    self.previewView.hidden = YES;
    
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
            [self setupPreview];
        });
    });
}

- (void)setupPreview
{
    // *Assume portrait
    
    // Get aspect ratio
    CGFloat aspectRatio = 8.5f / 11.0f;
    
    self.previewView.hidden = NO;
    self.previewView.translatesAutoresizingMaskIntoConstraints = NO;
    
    // Create margin constraints
    NSDictionary *metrics = @{@"margin": @PREVIEW_MARGIN};
    NSDictionary *views = @{@"previewView": self.previewView};
    NSArray *hMarginConstraints = [NSLayoutConstraint constraintsWithVisualFormat:@"H:|-(>=margin)-[previewView]-(>=margin)-|" options:0 metrics:metrics views:views];
    NSArray *vMarginConstraints = [NSLayoutConstraint constraintsWithVisualFormat:@"V:|-(>=margin)-[previewView]-(>=margin)-|" options:0 metrics:metrics views:views];
    
    // Create max dimension constraints
    NSLayoutConstraint *maxWidthConstraint = [NSLayoutConstraint constraintWithItem:self.previewView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self.previewArea attribute:NSLayoutAttributeWidth multiplier:1.0f constant:-20.0f];
    [maxWidthConstraint setPriority:UILayoutPriorityDefaultLow];
    NSLayoutConstraint *maxHeighConstraint = [NSLayoutConstraint constraintWithItem:self.previewView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self.previewArea attribute:NSLayoutAttributeHeight multiplier:1.0f constant:-20.0f];
    [maxHeighConstraint setPriority:UILayoutPriorityDefaultLow];
    
    // Create alignment constraints
    NSLayoutConstraint *xAlignConstraint = [NSLayoutConstraint constraintWithItem:self.previewView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self.previewArea attribute:NSLayoutAttributeCenterX multiplier:1.0f constant:0.0f];
    [xAlignConstraint setPriority:UILayoutPriorityRequired];
    [self.previewArea addConstraint:xAlignConstraint];
    NSLayoutConstraint *yAlignConstraint = [NSLayoutConstraint constraintWithItem:self.previewView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.previewArea attribute:NSLayoutAttributeCenterY multiplier:1.0f constant:0.0f];
    [yAlignConstraint setPriority:UILayoutPriorityRequired];
    
    // Create aspect ratio constraints
    NSLayoutConstraint *aspectRatioConstraint = [NSLayoutConstraint constraintWithItem:self.previewView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self.previewView attribute:NSLayoutAttributeHeight multiplier:aspectRatio constant:0.0f];
    [aspectRatioConstraint setPriority:UILayoutPriorityRequired];
    
    // Add constraints
    [self.previewArea addConstraints:hMarginConstraints];
    [self.previewArea addConstraints:vMarginConstraints];
    [self.previewArea addConstraints:@[maxWidthConstraint, maxHeighConstraint, xAlignConstraint, yAlignConstraint]];
    [self.previewView addConstraint:aspectRatioConstraint];
    self.aspectRatioConstraint = aspectRatioConstraint;
    
    // Apply constraints
    [self.previewArea layoutIfNeeded];
    
    // Create PageViewController
    // Assume left spine
    UIPageViewController *pageViewController = [[UIPageViewController alloc] initWithTransitionStyle:UIPageViewControllerTransitionStylePageCurl navigationOrientation:UIPageViewControllerNavigationOrientationHorizontal options:@{UIPageViewControllerOptionSpineLocationKey: [NSNumber numberWithInteger:UIPageViewControllerSpineLocationMin]}];
    pageViewController.view.translatesAutoresizingMaskIntoConstraints = NO;
    [self addChildViewController:pageViewController];
    [self.previewView addSubview:pageViewController.view];
    views = @{@"pageView": pageViewController.view};
    [self.previewView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|[pageView]|" options:0 metrics:nil views:views]];
    [self.previewView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|[pageView]|" options:0 metrics:nil views:views]];
    pageViewController.dataSource = self;
    
    self.currentPage = 0;
    PDFPageContentViewController *initial = [self viewControllerAtIndex:self.currentPage];
    [pageViewController setViewControllers:@[initial] direction:UIPageViewControllerNavigationDirectionForward animated:YES completion:nil];
    
    self.pageViewController = pageViewController;
}

#pragma mark - UIPageViewControllerDataSource

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerBeforeViewController:(UIViewController *)viewController
{
    NSInteger index = ((PDFPageContentViewController *) viewController).pageIndex;
    if (index == 0 || index == NSNotFound)
    {
        return nil;
    }
    index--;
    return [self viewControllerAtIndex:index];
}

- (UIViewController *)pageViewController:(UIPageViewController *)pageViewController viewControllerAfterViewController:(UIViewController *)viewController
{
    NSInteger index = ((PDFPageContentViewController *) viewController).pageIndex;
    if (index == self.printDocument.pageCount - 1 || index == NSNotFound)
    {
        return nil;
    }
    index++;
    return [self viewControllerAtIndex:index];
}

- (PDFPageContentViewController *)viewControllerAtIndex:(NSInteger)index
{
    PDFPageContentViewController *viewController = [self.storyboard instantiateViewControllerWithIdentifier:@"PDFPageContentViewController"];
    viewController.pageIndex = index;
    return viewController;
}

#pragma mark -

- (void)previewSettingDidChange
{
    PDFPageContentViewController *current = [self viewControllerAtIndex:self.currentPage];
    [self.pageViewController setViewControllers:@[current] direction:UIPageViewControllerNavigationDirectionForward animated:NO completion:nil];
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

@end
