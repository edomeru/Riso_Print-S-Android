//
//  PrintSettingsOptionTableViewControllerTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PrintSettingsOptionTableViewController.h"
#import "PrintSettingsOptionsHeaderCell.h"
#import "PrintSettingsOptionsItemCell.h"
#import "PreviewSetting.h"
#import "PrintSettingsHelper.h"
#import "PrintPreviewHelper.h"

#define OPTIONS_HEADER_CELL_ID @"OptionsHeaderCell"
#define OPTIONS_ITEM_CELL_ID @"OptionsItemCell"


@interface PrintSettingsOptionTableViewController (Test)
@property (nonatomic) NSInteger selectedIndex;
@property (nonatomic, strong) NSString *key;
@property (nonatomic,  strong) NSMutableArray *options;
@property (nonatomic,  strong) NSMutableArray *optionValues;
-(BOOL) isApplicableOption:(NSString *)option;
@end

@interface PrintSettingsOptionsItemCell(Test)
@property (nonatomic, weak) IBOutlet UIImageView *radioImageView;
@end

@interface PrintSettingsOptionTableViewControllerTest : GHTestCase

@end


@implementation PrintSettingsOptionTableViewControllerTest
{
    NSString* storyboardId;
}

- (BOOL)shouldRunOnMainThread
{
    return YES;
}

- (void)setUpClass
{
    storyboardId =@"PrintSettingsOptionTableViewController";
}

- (void)test001_UIViewBinding
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsOptionTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    GHAssertNotNil(viewController.tableView, @"");
    
    PrintSettingsOptionsHeaderCell *headerCell = (PrintSettingsOptionsHeaderCell *)[viewController.tableView dequeueReusableCellWithIdentifier: OPTIONS_HEADER_CELL_ID];
    GHAssertNotNil(headerCell, @"");
    GHAssertNotNil(headerCell.settingLabel, @"");
    
    PrintSettingsOptionsItemCell *itemCell = (PrintSettingsOptionsItemCell *)[viewController.tableView dequeueReusableCellWithIdentifier: OPTIONS_ITEM_CELL_ID];
    
    GHAssertNotNil(itemCell, @"");
    GHAssertNotNil(itemCell.optionLabel, @"");
    GHAssertNotNil(itemCell.separator, @"");
    GHAssertNotNil(itemCell.radioImageView, @"");

}

-(void) test002_UIViewLoading
{
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSDictionary *setting = [self getSettingsForKey:KEY_FINISHING_SIDE];
    
    previewSetting.finishingSide = kFinishingSideRight;
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsOptionTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    viewController.previewSetting = previewSetting;
    viewController.setting = setting;
    
    [viewController view];
    
    NSInteger expectedSelectedIndex = 2;
    
    GHAssertEquals(expectedSelectedIndex, viewController.selectedIndex, @"");
    NSUInteger rowCount = [viewController.tableView numberOfRowsInSection:0];
    for(NSUInteger i = 1; i < rowCount; i++)
    {
       PrintSettingsOptionsItemCell *cell = (PrintSettingsOptionsItemCell *)[viewController.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:0]];
        if(viewController.selectedIndex == (i - 1))
        {
            GHAssertTrue(cell.isSelected, @"");
            GHAssertTrue(cell.radioImageView.isHighlighted, @"");
        }
        else
        {
            GHAssertFalse(cell.isSelected, @"");
            GHAssertFalse(cell.radioImageView.isHighlighted, @"");
        }
        
        if(i == rowCount - 1)
        {
            GHAssertTrue(cell.separator.hidden, @"");
        }
        else
        {
            GHAssertFalse(cell.separator.hidden, @"");
        }
    }
}

-(void)test003_UIViewLoading_NotAllApplicableOption
{
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSDictionary *setting = [self getSettingsForKey:@"staple"];
    
    previewSetting.finishingSide = kFinishingSideLeft;
    previewSetting.staple = kStapleType1Pos;
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsOptionTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    viewController.previewSetting = previewSetting;
    viewController.setting = setting;
    
    [viewController view];
    
    //TODO add asserts
    
}

-(void)test004_isApplicableOption_Staple
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsOptionTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    viewController.previewSetting = previewSetting;
    viewController.key = KEY_STAPLE;
    
    NSDictionary *setting = [self getSettingsForKey:KEY_STAPLE];
    NSArray *options = [setting objectForKey:@"options"];
    
    viewController.previewSetting.finishingSide = kFinishingSideTop;
    for(NSDictionary *option in options)
    {
        NSString *optionString = [option objectForKey:@"content-body"];
        BOOL retVal = [viewController isApplicableOption:optionString];
        if([optionString isEqualToString:@"ids_lbl_staple_1"])
        {
            GHAssertFalse(retVal, @"");
        }
        else
        {
            GHAssertTrue(retVal, @"");
        }
    }
    

    
    viewController.previewSetting.finishingSide = kFinishingSideRight;
    for(NSDictionary *option in options)
    {
        NSString *optionString = [option objectForKey:@"content-body"];
        BOOL retVal = [viewController isApplicableOption:optionString];
        if([optionString isEqualToString:@"ids_lbl_staple_upperleft"] ||
            [optionString isEqualToString:@"ids_lbl_staple_upperright"])
        {
            GHAssertFalse(retVal, @"");
        }
        else
        {
            GHAssertTrue(retVal, @"");
        }
    }

    viewController.previewSetting.finishingSide = kFinishingSideLeft;
    viewController.previewSetting.finishingSide = kFinishingSideRight;
    for(NSDictionary *option in options)
    {
        NSString *optionString = [option objectForKey:@"content-body"];
        BOOL retVal = [viewController isApplicableOption:optionString];
        if([optionString isEqualToString:@"ids_lbl_staple_upperleft"] ||
           [optionString isEqualToString:@"ids_lbl_staple_upperright"])
        {
            GHAssertFalse(retVal, @"");
        }
        else
        {
            GHAssertTrue(retVal, @"");
        }
    }
}

-(void)test005_isApplicableOption_ImpositionOrder
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsOptionTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    
    viewController.previewSetting = previewSetting;
    viewController.key = KEY_IMPOSITION_ORDER;
    
    NSDictionary *setting = [self getSettingsForKey:KEY_IMPOSITION_ORDER];
    NSArray *options = [setting objectForKey:@"options"];
    
    viewController.previewSetting.imposition = kImposition2Pages;
    for(NSDictionary *option in options)
    {
        NSString *optionString = [option objectForKey:@"content-body"];
        BOOL retVal = [viewController isApplicableOption:optionString];
        if([optionString isEqualToString:@"ids_lbl_imposition_order_2up_lr"] ||
           [optionString isEqualToString:@"ids_lbl_imposition_order_2up_rl"] ||
           [optionString isEqualToString:@"ids_lbl_off"])
        {
            GHAssertTrue(retVal, @"");
        }
        else
        {
            GHAssertFalse(retVal, @"");
        }
    }

    viewController.previewSetting.imposition = kImposition4pages;
    for(NSDictionary *option in options)
    {
        NSString *optionString = [option objectForKey:@"content-body"];
        BOOL retVal = [viewController isApplicableOption:optionString];
        if([optionString isEqualToString:@"ids_lbl_imposition_order_2up_lr"] ||
           [optionString isEqualToString:@"ids_lbl_imposition_order_2up_rl"])
        {
            GHAssertFalse(retVal, @"");
        }
        else
        {
            GHAssertTrue(retVal, @"");
        }
    }
}

-(void) test006_selectRow
{
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];
    NSDictionary *setting = [self getSettingsForKey:@"finishingSide"];
    
    previewSetting.finishingSide = kFinishingSideLeft;
    
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    PrintSettingsOptionTableViewController *viewController = [storyboard instantiateViewControllerWithIdentifier:storyboardId];
    
    viewController.previewSetting = previewSetting;
    viewController.setting = setting;
    
    [viewController view];
    
    NSInteger expectedSelectedIndex = 2;
    
    [viewController tableView:viewController.tableView didSelectRowAtIndexPath:[NSIndexPath indexPathForItem:expectedSelectedIndex + 1 inSection:0]];

    GHAssertEquals(expectedSelectedIndex, viewController.selectedIndex, @"");
    
    NSUInteger rowCount = [viewController.tableView numberOfRowsInSection:0];
    for(NSUInteger i = 1; i < rowCount; i++)
    {
        PrintSettingsOptionsItemCell *cell = (PrintSettingsOptionsItemCell *)[viewController.tableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:i inSection:0]];
        if(viewController.selectedIndex == (i - 1))
        {
            GHAssertTrue(cell.isSelected, @"");
            GHAssertTrue(cell.radioImageView.isHighlighted, @"");
        }
        else
        {
            GHAssertFalse(cell.isSelected, @"");
            GHAssertFalse(cell.radioImageView.isHighlighted, @"");
        }
    }
}


- (NSDictionary *) getSettingsForKey:(NSString *) key
{
    NSDictionary *printSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    
    NSArray *groups = [printSettingsTree objectForKey:@"group"];
    
    for(NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        for(NSDictionary *setting in settings)
        {
            if([[setting objectForKey:@"name"] isEqualToString:key] == YES)
            {
                return setting;
            }
            
        }
    }
    return nil;
}

@end
