//
//  SlidingViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "SlidingViewController.h"
#import "RootViewController.h"

NSString *UnwindLeftId = @"UnwindLeft";
NSString *UnwindRightId = @"UnwindRight";

const CGFloat MinPanVelocity = 700.0f;
const CGFloat MaxPanVelocity = 1000.0f;
const CGFloat SlideRangeFactor = 2.0f / 3.0f;
const CGFloat _AnimationDuration = 0.3f;

@interface SlidingViewController ()

@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;
@property (nonatomic, weak) UIPanGestureRecognizer *panRecognizer;
@property (nonatomic, weak) RootViewController *container;
@property (atomic) BOOL isAnimating;

@end

@implementation SlidingViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        _slideDirection = SlideLeft;
        _isAnimating = NO;
        _isFixedSize = YES;
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        _slideDirection = SlideLeft;
        _isAnimating = NO;
        _isFixedSize = YES;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)didMoveToParentViewController:(UIViewController *)parent
{
    [super didMoveToParentViewController:parent];
    self.container = (RootViewController *) parent;
    
    // Slide enter
    if (self.container != nil)
    {
        UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
        [self.container.mainView addGestureRecognizer:tapRecognizer];
        self.tapRecognizer = tapRecognizer;
        
        UIPanGestureRecognizer *panRecognizer = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePan:)];
        [self.container.mainView addGestureRecognizer:panRecognizer];
        self.panRecognizer = panRecognizer;
    }
    else
    {
        [self.tapRecognizer.view removeGestureRecognizer:self.tapRecognizer];
        [self.panRecognizer.view removeGestureRecognizer:self.panRecognizer];
    }
}

- (void)close
{
    // Unwind
    NSString *className = NSStringFromClass([self.container.mainController class]);
    NSError *error;
    NSRegularExpression *pattern = [[NSRegularExpression alloc] initWithPattern:@"ViewController$" options:0 error:&error];
    NSString *destination = [pattern stringByReplacingMatchesInString:className options:0 range:NSMakeRange(0, [className length]) withTemplate:@""];
    [self performSegueWithIdentifier:[NSString stringWithFormat:@"UnwindTo%@", destination] sender:nil];
}

- (IBAction)handleTap:(UITapGestureRecognizer *)gestureRecognizer
{
    [self close];
}

- (IBAction)handlePan:(UIPanGestureRecognizer *)gestureRecognizer
{
    if (self.isAnimating)
    {
        return;
    }
    
    CGPoint translation = [gestureRecognizer translationInView:self.container.view];
    if (gestureRecognizer.state != UIGestureRecognizerStateEnded)
    {
        // Do panning
        if (self.slideDirection == SlideLeft)
        {
            CGFloat leftMainConstraint = self.container.leftMainConstraint.constant + translation.x;
            CGFloat rightMainConstraint = self.container.rightMainConstraint.constant - translation.x;
            CGFloat sideConstraint = self.container.leftSlidingConstraint.constant + translation.x;
            
            CGRect slidingFrame = self.container.leftSlidingView.frame;
            if (self.isFixedSize == YES)
            {
                self.container.rightMainConstraint.constant = MIN(0, MAX(-slidingFrame.size.width, rightMainConstraint));
            }
            self.container.leftMainConstraint.constant = MAX(0, MIN(slidingFrame.size.width, leftMainConstraint));
            self.container.leftSlidingConstraint.constant = MIN(0, MAX(-slidingFrame.size.width, sideConstraint));
        }
        else
        {
            CGFloat leftMainConstraint = self.container.leftMainConstraint.constant + translation.x;
            CGFloat rightMainConstraint = self.container.rightMainConstraint.constant - translation.x;
            CGFloat sideConstraint = self.container.rightSlidingConstraint.constant - translation.x;
            
            CGRect slidingFrame = self.container.rightSlidingView.frame;
            if (self.isFixedSize == YES)
            {
                self.container.leftMainConstraint.constant = MIN(0, MAX(-slidingFrame.size.width, leftMainConstraint));
            }
            self.container.rightMainConstraint.constant = MAX(0, MIN(slidingFrame.size.width, rightMainConstraint));
            self.container.rightSlidingConstraint.constant = MIN(0, MAX(-self.container.rightSlidingView.frame.size.width, sideConstraint));
        }
        [self.container.view layoutIfNeeded];
    }
    else
    {
        if ((self.slideDirection == SlideLeft && fabs(self.container.leftSlidingConstraint.constant) > self.container.leftSlidingView.frame.size.width / 3.0f) ||
            (self.slideDirection == SlideRight && fabs(self.container.rightSlidingConstraint.constant) > self.container.rightSlidingView.frame.size.width / 3.0f))
        {
            // Start segue if slide threshold is reached
            self.isAnimating =YES;
            [self close];
        }
        else
        {
            // Snap back if slide threshold is not reached
            if (self.slideDirection == SlideLeft)
            {
                CGRect slidingFrame = self.container.leftSlidingView.frame;
                if (self.isFixedSize == YES)
                {
                    self.container.rightMainConstraint.constant = -slidingFrame.size.width;
                }
                self.container.leftMainConstraint.constant = slidingFrame.size.width;
                self.container.leftSlidingConstraint.constant = 0;
            } else
            {
                CGRect slidingFrame = self.container.rightSlidingView.frame;
                if (self.isFixedSize == YES)
                {
                    self.container.leftMainConstraint.constant = -slidingFrame.size.width;
                }
                self.container.rightMainConstraint.constant = slidingFrame.size.width;
                self.container.rightSlidingConstraint.constant = 0;
            }
            
            [UIView animateWithDuration:_AnimationDuration delay:0 options:UIViewAnimationOptionCurveEaseOut animations:^
             {
                 [self.container.view layoutIfNeeded];
                 self.isAnimating = YES;
             }completion:^(BOOL finished)
             {
                 self.isAnimating = NO;
             }];
        }
    }
    
    [gestureRecognizer setTranslation:CGPointZero inView:self.container.view];
}

@end
