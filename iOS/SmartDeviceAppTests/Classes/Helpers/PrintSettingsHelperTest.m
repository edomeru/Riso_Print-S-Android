//
//  PrintSettingsHelperTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 4/28/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//
#import <GHUnitIOS/GHUnit.h>
#import "PrintSettingsHelper.h"
#import "PreviewSetting.h"
#import "PrintSetting.h"
#import "DatabaseManager.h"
#import "PrintPreviewHelper.h"


#define PRINTSETTINGS_HELPER_TEST 1
#if PRINTSETTINGS_HELPER_TEST


@interface PrintSettingsHelperTest:GHTestCase

@property PreviewSetting *previewSetting;

@end

@implementation PrintSettingsHelperTest
{
    NSArray *expectedKeys;
    NSDictionary *numValues;
    NSDictionary *defaultValues;
    NSMutableDictionary *observedKeys;
}

- (void)setUpClass
{
    self.previewSetting = [[PreviewSetting alloc] init];
    expectedKeys = [NSArray arrayWithObjects:
                    @"colorMode",
                    @"orientation",
                    @"copies",
                    @"duplex",
                    @"paperSize",
                    @"scaleToFit",
                    @"paperType",
                    @"inputTray",
                    @"imposition",
                    @"impositionOrder",
                    @"sort",
                    @"booklet",
                    @"bookletFinish",
                    @"bookletLayout",
                    @"finishingSide",
                    @"staple",
                    @"punch",
                    @"outputTray"
                    , nil];
    
    numValues = [NSDictionary dictionaryWithObjects:
                    [NSArray arrayWithObjects:
                            [NSNumber numberWithInt:3],
                            [NSNumber numberWithInt:2],
                            [NSNumber numberWithInt:9999],
                            [NSNumber numberWithInt:3],
                            [NSNumber numberWithInt:13],
                            [NSNumber numberWithInt:3],
                            [NSNumber numberWithInt:7],
                            [NSNumber numberWithInt:5],
                            [NSNumber numberWithInt:3],
                            [NSNumber numberWithInt:6],
                            [NSNumber numberWithInt:2],
                            [NSNumber numberWithInt:2],
                            [NSNumber numberWithInt:3],
                            [NSNumber numberWithInt:3],
                            [NSNumber numberWithInt:3],
                            [NSNumber numberWithInt:5],
                            [NSNumber numberWithInt:3],
                            [NSNumber numberWithInt:4],
                            nil]
                        forKeys:expectedKeys];
    
    defaultValues =[NSDictionary dictionaryWithObjects:
                    [NSArray arrayWithObjects:
                        [NSNumber numberWithInt:1], //colormode
                        [NSNumber numberWithInt:0], //orientation
                        [NSNumber numberWithInt:1], //copies
                        [NSNumber numberWithInt:0], //duplex
                        [NSNumber numberWithInt:2], //paper size
                        [NSNumber numberWithInt:1], //scale to fit
                        [NSNumber numberWithInt:0], //paper type
                        [NSNumber numberWithInt:0], //input tray
                        [NSNumber numberWithInt:0], //imposition
                        [NSNumber numberWithInt:0], //imposition order
                        [NSNumber numberWithInt:0], //sort
                        [NSNumber numberWithInt:0], //booklet
                        [NSNumber numberWithInt:0], //booklet finish
                        [NSNumber numberWithInt:0], //booklet layout
                        [NSNumber numberWithInt:0], //finishing side
                        [NSNumber numberWithInt:0], //staple
                        [NSNumber numberWithInt:0], //punch
                        [NSNumber numberWithInt:0], //output tray
                        nil]
                    forKeys:expectedKeys];
    
    observedKeys = [[NSMutableDictionary alloc] init];
}

- (void)tearDown
{
    [DatabaseManager discardChanges];
    [observedKeys removeAllObjects];
}

- (void)test001_sharedPrintSettingsTree
{
    NSDictionary *sharedPrintSettingsTree = [PrintSettingsHelper sharedPrintSettingsTree];
    
    GHAssertNotNil(sharedPrintSettingsTree, @"");
    
    NSArray *groups = [sharedPrintSettingsTree objectForKey:@"group"];
    GHAssertNotNil(groups, @"");
    
    for (NSDictionary *group in groups)
    {
        NSArray *settings = [group objectForKey:@"setting"];
        GHAssertNotNil(settings, @"");
        
        for (NSDictionary *setting in settings)
        {
            NSString *key = [setting objectForKey:@"name"];
            
            GHAssertNotNil(key, @"");
            NSString *type = [setting objectForKey:@"type"];
            GHAssertNotNil(type, @"");
            NSString *defaultValue = [setting objectForKey:@"default"];
            GHAssertNotNil(defaultValue, @"");
        }
    }
}

-(void)test002_defaultPreviewSetting
{
    PreviewSetting *setting = [PrintSettingsHelper defaultPreviewSetting];
    GHAssertNotNil(setting, @"");
}

-(void)test003_copyDefaultPrintSettings
{
    PrintSetting *printSetting = (PrintSetting*)[DatabaseManager addObject:E_PRINTSETTING];
    [PrintSettingsHelper copyDefaultPrintSettings:&printSetting];
    
    for(NSString *key in expectedKeys)
    {
        GHAssertEquals([[printSetting valueForKey:key] intValue], [[defaultValues objectForKey:key] intValue], [NSString stringWithFormat:@"default value for print setting: key = %@", key]);
    }
}

-(void)test004_copyPrintSettingToPreviewSetting
{
    PrintSetting *printSetting = [self createTestPrintSetting];
    PreviewSetting *previewSetting = [[PreviewSetting alloc] init];    [PrintSettingsHelper copyPrintSettings:printSetting toPreviewSetting:&previewSetting];
    
    for(NSString *key in expectedKeys)
    {
        GHAssertEquals([[printSetting valueForKey:key] intValue], [[previewSetting valueForKey:key] intValue], [NSString stringWithFormat:@"copy from print setting to preview setting: key = %@", key]);
    }
}

-(void)test005_addObserver
{
    PreviewSetting *previewSetting = self.previewSetting;
    [PrintSettingsHelper addObserver:self toPreviewSetting:&previewSetting];
    
    for(NSString *key in expectedKeys)
    {
        [self.previewSetting setValue:[NSNumber numberWithInt:abs(arc4random() %[[numValues objectForKey:key] intValue])]  forKey:key];
    }
    
    for(NSString *key in expectedKeys)
    {
        GHAssertNotNil([observedKeys objectForKey:key], [NSString stringWithFormat:@"observed preview setting key = %@", key]);
        GHAssertEquals([[self.previewSetting valueForKey:key] intValue], [[observedKeys objectForKey:key] intValue], [NSString stringWithFormat:@"observed preview setting value: key = %@", key]);
    }
}

-(void)test006_removeObserver
{
    PreviewSetting *previewSetting = self.previewSetting;
    [PrintSettingsHelper removeObserver:self fromPreviewSetting:&previewSetting];
    
    for(NSString *key in expectedKeys)
    {
        [self.previewSetting setValue:[NSNumber numberWithInt:abs(arc4random() %[[numValues objectForKey:key] intValue])]  forKey:key];
    }
    
    GHAssertEquals((int)[observedKeys count], 0, @"no observed keys");
}

- (PrintSetting *)createTestPrintSetting
{
    PrintSetting *printSetting = (PrintSetting*)[DatabaseManager addObject:E_PRINTSETTING];
    for(NSString *key in expectedKeys)
    {
        [printSetting setValue:[NSNumber numberWithInt:abs(arc4random() %[[numValues objectForKey:key] intValue])] forKey:key];
    }
    
     return printSetting;
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (context == PREVIEWSETTING_CONTEXT) {
        [observedKeys setObject:[change objectForKey:NSKeyValueChangeNewKey] forKey:keyPath];
    } else {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

@end
#endif
