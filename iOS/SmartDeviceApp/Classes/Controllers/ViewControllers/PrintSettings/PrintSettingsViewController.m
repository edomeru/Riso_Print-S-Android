//
//  PrintSettingsViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/3/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsViewController.h"
#import "PrintSettingsTableViewController.h"
#import "PDFFileManager.h"
#import "PrinterManager.h"
#import "PrintDocument.h"
#import "Printer.h"
#import "PreviewSetting.h"
#import "PrintSettingsHelper.h"
#import "UIView+Localization.h"

#define SEGUE_TO_PRINTSETTINGS_TABLE @"PrintSettings-PrintSettingsTable"

@interface PrintSettingsViewController ()

- (void)initialize;
@property (weak, nonatomic) IBOutlet UILabel *printSettingsScreenTitle;

@end

@implementation PrintSettingsViewController

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
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
    {
        self.isFixedSize = NO;
    }
    else
    {
        self.isFixedSize = YES;
    }
    self.slideDirection = SlideRight;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view.
    
    if(self.printerIndex != nil)
    {
       self.printSettingsScreenTitle.localizationId = @"IDS_LBL_DEFAULT_PRINT_SETTINGS";
    }
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    if([segue.identifier isEqualToString:SEGUE_TO_PRINTSETTINGS_TABLE] == YES)
    {
        Printer *printer = nil;
        PreviewSetting *previewSetting = nil;
        UIViewController *destController = [segue.destinationViewController topViewController];
        if(self.printerIndex == nil)
        {
            ((PrintSettingsTableViewController *)destController).previewSetting = [[[PDFFileManager sharedManager] printDocument] previewSetting];
            ((PrintSettingsTableViewController *)destController).printer = [[[PDFFileManager sharedManager] printDocument] printer];
            ((PrintSettingsTableViewController *)destController).isDefaultSettingsMode = NO;
        }
        else
        {
            PrinterManager *printerManager =  [PrinterManager sharedPrinterManager];
            printer = [printerManager getPrinterAtIndex:[self.printerIndex unsignedIntegerValue]];
            ((PrintSettingsTableViewController *)destController).printer = printer;
            previewSetting = [[PreviewSetting alloc] init];
            [PrintSettingsHelper copyPrintSettings:printer.printsetting toPreviewSetting: &previewSetting];
            ((PrintSettingsTableViewController *)destController).previewSetting = previewSetting;
            ((PrintSettingsTableViewController *)destController).isDefaultSettingsMode = YES;
        }
    }
}
@end
