//
//  PrintSettingsPrinterListViewController.m
//  RISOSmartPrint
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsPrinterListViewController.h"
#import "UIColor+Theme.h"

@interface PrintSettingsPrinterListViewController ()

/** 
 * Reference to the header at the top of the PrintSettingsPrinterListViewController.
 */
@property (weak, nonatomic) IBOutlet UIView *printerListHeader;

/**
 * Responds to tapping the {@link printerListHeader}.
 * Transitions back to "Print Settings" screen.
 */
- (IBAction)pressPrinterHeaderCellAction:(id)sender;

@end

@implementation PrintSettingsPrinterListViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}
- (IBAction)pressPrinterHeaderCellAction:(id)sender
{
    
    UILongPressGestureRecognizer *press = (UILongPressGestureRecognizer *) sender;
    if(press.state == UIGestureRecognizerStateBegan)
    {
        [self.printerListHeader setBackgroundColor: [UIColor purple1ThemeColor]];
    }
    else
    {
        if(CGRectContainsPoint(self.printerListHeader.frame, [press locationInView:self.printerListHeader]) == NO)
        {
            [self.printerListHeader setBackgroundColor: [UIColor blackThemeColor]];
        }
        else if(press.state == UIGestureRecognizerStateEnded)
        {
            [self.navigationController popToRootViewControllerAnimated:YES];
            [self.printerListHeader setBackgroundColor: [UIColor blackThemeColor]];
        }
    }

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

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
