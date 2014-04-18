//
//  HomeViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "HomeViewController.h"
#import "PrintPreviewViewController.h"
#import "PrintersIphoneViewController.h"
#import "PrintersIpadViewController.h"
#import "PrintJobHistoryViewController.h"
#import "RootViewController.h"
#import "SettingsViewController.h"
#import "UIViewController+Segue.h"

@interface HomeViewController ()

- (void)initialize;

@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;
@property (nonatomic, weak) IBOutlet UIButton *homeButton;
@property (nonatomic, weak) IBOutlet UIButton *printersButton;
@property (nonatomic, weak) IBOutlet UIButton *printJobHistoryButton;
@property (nonatomic, weak) IBOutlet UIButton *settingsButton;
@property (nonatomic, weak) IBOutlet UIButton *helpButton;
@property (nonatomic, weak) IBOutlet UIButton *legaButton;

@property (nonatomic, weak) UIButton *selectedButton;
- (BOOL)selectButton:(UIButton *)item;

@end

@implementation HomeViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
    self = [super initWithCoder:aDecoder];
    if (self)
    {
        [self initialize];
    }
    return self;
}

- (void)initialize
{
    self.slideDirection = SlideLeft;
    self.isFixedSize = YES;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    
    // Set selected button
    RootViewController *container = (RootViewController *)self.parentViewController;
    if (container.mainController.class == [PrintPreviewViewController class])
    {
        self.selectedButton = self.homeButton;
    }
    else if (container.mainController.class == [PrintersIphoneViewController class] || container.mainController.class == [PrintersIpadViewController class])
    {
        self.selectedButton = self.printersButton;
    }
    else if (container.mainController.class == [PrintJobHistoryViewController class])
    {
        self.selectedButton = self.printJobHistoryButton;
    }
    
    self.selectedButton.selected = YES;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (BOOL)selectButton:(UIButton *)item
{
    if (item.selected)
    {
        return NO;
    }
    
    // Select new item
    item.selected = YES;
    
    // Disable current selection
    self.selectedButton.selected = NO;
    
    return YES;
}

#pragma mark -
#pragma mark IBActions
- (IBAction)homeAction:(id)sender
{
    if([self selectButton:sender])
    {
        [self performSegueTo:[PrintPreviewViewController class]];
    }
}

- (IBAction)printersAction:(id)sender
{
    if([self selectButton:sender])
    {
        if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
        {
            [self performSegueTo:[PrintersIphoneViewController class]];
        }
        else
        {
            [self performSegueTo:[PrintersIpadViewController class]];
        }
    }
}

- (IBAction)printJobHistoryAction:(id)sender
{
    if ([self selectButton:sender])
    {
        [self performSegueTo:[PrintJobHistoryViewController class]];
    }
}

- (IBAction)settingsAction:(id)sender
{
    [self performSegueTo:[SettingsViewController class]];
}

- (IBAction)helpAction:(id)sender
{
}

- (IBAction)legalAction:(id)sender
{
}

@end
