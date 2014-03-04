//
//  SlidingViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/4/14.
//  Copyright (c) 2014 aLink. All rights reserved.
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
    if (self) {
        _slideDirection = SlideLeft;
        _isAnimating = NO;
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

- (IBAction)handleTap:(UITapGestureRecognizer *)gestureRecognizer
{
    // Unwind
    if (self.slideDirection == SlideLeft)
    {
        [self performSegueWithIdentifier:UnwindLeftId sender:self];
    }
    else
    {
        [self performSegueWithIdentifier:UnwindRightId sender:self];
    }
}

- (IBAction)handlePan:(UIPanGestureRecognizer *)gestureRecognizer
{
    if (self.isAnimating)
    {
        return;
    }
    
    if (gestureRecognizer.state != UIGestureRecognizerStateEnded)
    {
        if (self.slideDirection == SlideRight)
        {
            self.container.rightSlidingConstraint.constant -= [gestureRecognizer translationInView:self.container.view].x;
        }
        else
        {
            self.container.leftSlidingConstraint.constant += [gestureRecognizer translationInView:self.container.view].x;
        }
        self.container.leftMainConstraint.constant += [gestureRecognizer translationInView:self.container.view].x;
        [self.container.view layoutIfNeeded];
    }
    else
    {
        CGRect mainFrame = self.container.mainView.frame;
        if (fabs(mainFrame.origin.x) <= mainFrame.size.width * SlideRangeFactor)
        {
            self.isAnimating =YES;
            if (self.slideDirection == SlideLeft)
            {
                [self performSegueWithIdentifier:UnwindLeftId sender:self];
            }
            else
            {
                [self performSegueWithIdentifier:UnwindRightId sender:self];
            }
        }
        else
        {
            if (self.slideDirection == SlideLeft)
            {
                self.container.leftMainConstraint.constant = self.container.leftSlidingView.frame.size.width;
                self.container.leftSlidingConstraint.constant = 0;
            } else
            {
                self.container.leftMainConstraint.constant = -self.container.rightSlidingView.frame.size.width;
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
