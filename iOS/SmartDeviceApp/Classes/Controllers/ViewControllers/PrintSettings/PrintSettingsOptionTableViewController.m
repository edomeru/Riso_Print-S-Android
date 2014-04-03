//
//  PrintSettingsOptionTableViewController.m
//  SmartDeviceApp
//
//  Created by Seph on 3/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PrintSettingsOptionTableViewController.h"
#import "PrintSettingsOptionsHeaderCell.h"
#import "PrintSettingsOptionsItemCell.h"
#import "UIView+Localization.h"
#import "PDFFileManager.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"

#define OPTIONS_HEADER_CELL @"OptionsHeaderCell"
#define OPTIONS_ITEM_CELL @"OptionsItemCell"

@interface PrintSettingsOptionTableViewController ()

@property (nonatomic, weak) PrintDocument* printDocument;
@property (nonatomic) NSInteger selectedIndex;
@property (nonatomic, strong) NSString *key;

@end

@implementation PrintSettingsOptionTableViewController

- (id)initWithStyle:(UITableViewStyle)style
{
    self = [super initWithStyle:style];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
 
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    
    self.printDocument = [[PDFFileManager sharedManager] printDocument];
    self.key = [self.setting objectForKey:@"name"];
    self.selectedIndex = [[self.printDocument.previewSetting valueForKey:self.key] integerValue];
    
    // Add empty footer
    UIView *footer = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 1.0f, 20.0f)];
    footer.backgroundColor = [UIColor clearColor];
    self.tableView.tableFooterView = footer;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    // Return the number of rows in the section.
    return [[self.setting objectForKey:@"option"] count] + 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    UITableViewCell *cell;
    
    if (indexPath.row == 0)
    {
        PrintSettingsOptionsHeaderCell *headerCell = [tableView dequeueReusableCellWithIdentifier:OPTIONS_HEADER_CELL forIndexPath:indexPath];
        headerCell.settingLabel.localizationId = [self.setting objectForKey:@"text"];
        headerCell.selectionStyle = UITableViewCellSelectionStyleNone;
        cell = headerCell;
    }
    else
    {
        PrintSettingsOptionsItemCell *itemCell = [tableView dequeueReusableCellWithIdentifier:OPTIONS_ITEM_CELL forIndexPath:indexPath];
        NSArray *options = [self.setting objectForKey:@"option"];
        itemCell.optionLabel.localizationId = [[options objectAtIndex:indexPath.row - 1] objectForKey:@"content-body"];
        if ((indexPath.row - 1) == self.selectedIndex)
        {
            [tableView selectRowAtIndexPath:indexPath animated:NO scrollPosition:UITableViewScrollPositionNone];
        }
        itemCell.separator.hidden = NO;
        if (indexPath.row == options.count)
        {
            itemCell.separator.hidden = YES;
        }
        cell = itemCell;
    }
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (indexPath.row == 0)
    {
        [tableView selectRowAtIndexPath:[NSIndexPath indexPathForRow:self.selectedIndex + 1 inSection:0] animated:NO scrollPosition:UITableViewScrollPositionNone];
        return;
    }
    
    int index = indexPath.row - 1;
    if (index != self.selectedIndex)
    {
        self.selectedIndex = index;
        [self.printDocument.previewSetting setValue:[NSNumber numberWithInteger:self.selectedIndex] forKey:self.key];
    }
}

- (IBAction)backButtonAction:(id)sender
{
    [self.navigationController popToRootViewControllerAnimated:YES];
}


/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }   
    else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath
{
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath
{
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Navigation

// In a story board-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender
{
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}

 */

@end
