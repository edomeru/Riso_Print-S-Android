//
//  HomeViewController.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "HomeViewController.h"
#import "PrintPreviewViewController.h"
#import "PrintersIphoneViewController.h"
#import "PrintersIpadViewController.h"
#import "PrintJobHistoryViewController.h"
#import "HelpViewController.h"
#import "LegalViewController.h"
#import "RootViewController.h"
#import "SettingsViewController.h"
#import "UIViewController+Segue.h"

@interface HomeViewController ()

/**
 * Reference to the tap gesture action.
 */
@property (nonatomic, weak) UITapGestureRecognizer *tapRecognizer;

/**
 * Reference to the "Home" menu button.
 */
@property (nonatomic, weak) IBOutlet UIButton *homeButton;

/**
 * Reference to the "Printers" menu button.
 */
@property (nonatomic, weak) IBOutlet UIButton *printersButton;

/**
 * Reference to the "Print Job History" menu button.
 */
@property (nonatomic, weak) IBOutlet UIButton *printJobHistoryButton;

/**
 * Reference to the "Settings" menu button.
 */
@property (nonatomic, weak) IBOutlet UIButton *settingsButton;

/**
 * Reference to the "Help" menu button.
 */
@property (nonatomic, weak) IBOutlet UIButton *helpButton;

/**
 * Reference to the "Legal" menu button.
 */
@property (nonatomic, weak) IBOutlet UIButton *legaButton;

/**
 * Reference to the currently selected menu button.
 * The selected menu button should have its corresponding controller
 * displayed in the center panel.
 */
@property (nonatomic, weak) UIButton *selectedButton;

/**
 * Sets the properties of the SlidingViewController.
 */
- (void)initialize;

/**
 * Changes the background color of the menu button.
 * The menu button has a different background color to indicate 
 * that the menu item is already displayed in the center panel.\n
 *
 * @param item the menu button object
 * @return YES if the menu button was selected, NO if the menu button is already selected
 */
- (BOOL)selectButton:(UIButton *)item;

/**
 * Responds to tapping the "Home" menu button.
 * Displays the PrintPreviewViewController.
 * 
 * @param sender the menu button object
 */
- (IBAction)homeAction:(id)sender;

/**
 * Responds to tapping the "Printers" menu button.
 * Displays either the PrintersIphoneViewController or the PrintersIpadViewController.
 *
 * @param sender the menu button object
 */
- (IBAction)printersAction:(id)sender;

/**
 * Responds to tapping the "Print Job History" menu button.
 * Displays PrintJobHistoryViewController.
 *
 * @param sender the menu button object
 */
- (IBAction)printJobHistoryAction:(id)sender;

/**
 * Responds to tapping the "Settings" menu button.
 * Displays SettingsViewController.
 *
 * @param sender the menu button object
 */
- (IBAction)settingsAction:(id)sender;

/**
 * Responds to tapping the "Help" menu button.
 * Displays HelpViewController.
 *
 * @param sender the menu button object
 */
- (IBAction)helpAction:(id)sender;

/**
 * Responds to tapping the "Legal" menu button.
 * Displays LegalViewController.
 *
 * @param sender the menu button object
 */
- (IBAction)legalAction:(id)sender;

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
    else if (container.mainController.class == [SettingsViewController class])
    {
        self.selectedButton = self.settingsButton;
    }
    else if (container.mainController.class == [HelpViewController class])
    {
        self.selectedButton = self.helpButton;
    }
    else if (container.mainController.class == [LegalViewController class])
    {
        self.selectedButton = self.legaButton;
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
    if ([self selectButton:sender])
    {
        [self performSegueTo:[SettingsViewController class]];
    }
}

- (IBAction)helpAction:(id)sender
{
    if ([self selectButton:sender])
    {
        [self performSegueTo:[HelpViewController class]];
    }
}

- (IBAction)legalAction:(id)sender
{
    if ([self selectButton:sender])
    {
        [self performSegueTo:[LegalViewController class]];
    }
}

@end
