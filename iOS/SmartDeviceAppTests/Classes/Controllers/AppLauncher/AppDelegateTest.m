//
//  AppDelegateTest.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2015 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <GHUnitIOS/GHUnit.h>
#import "OCMock.h"
#import "AppDelegate.h"
#import "PDFFileManager.h"

@interface AppDelegateTest : GHTestCase

@end

@implementation AppDelegateTest

-(void)testDidFinishLaunchingWithOptions
{
    AppDelegate *appDelegate = [[AppDelegate alloc] init];
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    //expect these methods to be called with arguments
    [[mockPDFFileManager expect] setFileAvailableForLoad:NO];
    [[mockPDFFileManager expect] setFileURL:nil];
    
    /*Test with no URL*/
    NSMutableDictionary *testDictionary = [[NSMutableDictionary alloc] init];
    //call method under test
    [appDelegate application:nil didFinishLaunchingWithOptions:testDictionary];
    //verify if expected methods are called
    [mockPDFFileManager verify];

    BOOL isOpenInHandled = [[appDelegate valueForKey:@"isOpenInHandled"] boolValue];
    GHAssertFalse(isOpenInHandled, @"");
}

-(void)testOpenURL
{
    AppDelegate *appDelegate = [[AppDelegate alloc] init];
    
    //set-up test data
    NSMutableDictionary *testDictionary = [[NSMutableDictionary alloc] init];
    NSURL *documentsURL = [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory
                                            inDomains:NSUserDomainMask] lastObject];
    NSURL *testFileURL  = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    NSURL *testURL = [documentsURL URLByAppendingPathComponent:[testFileURL lastPathComponent]];
    
    [[NSFileManager defaultManager] copyItemAtURL:testFileURL toURL:testURL error:nil];
    
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    [[[mockPDFFileManager stub] andReturn:testURL] fileURL];
    
    [appDelegate setValue:[NSNumber numberWithBool:NO] forKey: @"isOpenInHandled"];
    //expect these methods to be called with arguments
    [[mockPDFFileManager expect] setFileAvailableForLoad:YES];
    [[mockPDFFileManager expect] setFileURL:testURL];
    //call method under test
    [appDelegate application:nil openURL:testURL sourceApplication:@"" annotation:testDictionary];
    //verify if expected methods are called
    [mockPDFFileManager verify];
    
    BOOL isOpenInHandled = [[appDelegate valueForKey:@"isOpenInHandled"] boolValue];
    GHAssertTrue(isOpenInHandled, @"");
    
    //call again, expect no processing since isOpenInHandled is already YES
    [[mockPDFFileManager reject] setFileAvailableForLoad:OCMOCK_ANY];
    [[mockPDFFileManager reject] setFileURL:testURL];
    
    [appDelegate application:nil openURL:testURL sourceApplication:@"" annotation:testDictionary];
    
    [mockPDFFileManager verify];
    
    //value of isOpenInHandled is not changed
    isOpenInHandled = [[appDelegate valueForKey:@"isOpenInHandled"] boolValue];
    GHAssertTrue(isOpenInHandled, @"");

    //unset isOpenInHandled flag
    [appDelegate setValue:[NSNumber numberWithBool:NO] forKey: @"isOpenInHandled"];
    //remove the file from the testURL
    [[NSFileManager defaultManager] removeItemAtURL:testURL error:nil];
    
    //call again but expect no processing since file in URL does not exist and is the same url as the current url
    //this simulates openURL being called twice for the same open-In transaction
    [[mockPDFFileManager reject] setFileAvailableForLoad:OCMOCK_ANY];
    [[mockPDFFileManager reject] setFileURL:testURL];

    [appDelegate application:nil openURL:testURL sourceApplication:@"" annotation:testDictionary];
    
    isOpenInHandled = [[appDelegate valueForKey:@"isOpenInHandled"] boolValue];
    GHAssertFalse(isOpenInHandled, @"");
    
    [mockPDFFileManager verify];
    
    [mockPDFFileManager stopMocking];
    
}

-(void)testOpenURL_FileDoesNotExist_ButURLNotSame
{
    AppDelegate *appDelegate = [[AppDelegate alloc] init];
    //set-up test data
    NSMutableDictionary *testDictionary = [[NSMutableDictionary alloc] init];
    NSURL *documentsURL = [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory
                                                                  inDomains:NSUserDomainMask] lastObject];
    NSURL *testFileURL  = [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    NSURL *testURL = [documentsURL URLByAppendingPathComponent:[testFileURL lastPathComponent]];
    
    // Mock PDFFileManager
    PDFFileManager *pdfFileManager = [PDFFileManager sharedManager];
    id mockPDFFileManager = [OCMockObject partialMockForObject:pdfFileManager];
    [[[mockPDFFileManager stub] andReturn:testFileURL] fileURL]; //returned current fileURL is different from the input URL
    
    //call and expect processing, even if file does not exist, it is not the same url as the current url so it may be a new session
    // PDFFileManager will handle the error this time.
    [[mockPDFFileManager expect] setFileAvailableForLoad:YES];
    [[mockPDFFileManager expect] setFileURL:testURL];
    
    [appDelegate setValue:[NSNumber numberWithBool:NO] forKey: @"isOpenInHandled"];
    
    [appDelegate application:nil openURL:testURL sourceApplication:@"" annotation:testDictionary];
    
    [mockPDFFileManager verify];
    
    BOOL isOpenInHandled = [[appDelegate valueForKey:@"isOpenInHandled"] boolValue];
    GHAssertTrue(isOpenInHandled, @"");
    
    [mockPDFFileManager stopMocking];
    
}

-(void)testApplicationWillResignActive
{
    AppDelegate *appDelegate = [[AppDelegate alloc] init];
    [appDelegate setValue:[NSNumber numberWithBool:YES] forKey: @"isOpenInHandled"];
    
    //will resign active will always set the isOpenInHandled flag to NO to anticipate open-in operation
    BOOL isOpenInHandled = [[appDelegate valueForKey:@"isOpenInHandled"] boolValue];
    GHAssertTrue(isOpenInHandled, @"");
    
    [appDelegate applicationWillResignActive:nil];
    
    isOpenInHandled = [[appDelegate valueForKey:@"isOpenInHandled"] boolValue];
    GHAssertFalse(isOpenInHandled, @"");
}


@end
