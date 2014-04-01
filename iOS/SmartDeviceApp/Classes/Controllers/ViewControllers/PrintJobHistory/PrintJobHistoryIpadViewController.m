//
//  PrintJobHistoryIpadViewController.m
//  SmartDeviceApp
//
//  Created by Gino Mempin on 3/27/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintJobHistoryIpadViewController.h"
#import "PrintJobHistoryIpadLayout.h"

@interface PrintJobHistoryIpadViewController ()

@property (weak, nonatomic) IBOutlet PrintJobHistoryIpadLayout* ipadLayout;

@end

@implementation PrintJobHistoryIpadViewController

#pragma mark - Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    self.ipadLayout.delegate = self;
    [self.ipadLayout setupForOrientation:self.interfaceOrientation];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

#pragma mark - PrintJobHistoryIpadLayoutDelegate

- (CGSize)sizeForGroupAtIndexPath:(NSIndexPath*)indexPath
{
    return [super computeSizeForGroupAtIndexPath:indexPath];
}

#pragma mark - Rotation

- (void)willRotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration
{
    [self.ipadLayout setupForOrientation:toInterfaceOrientation];
}

@end
