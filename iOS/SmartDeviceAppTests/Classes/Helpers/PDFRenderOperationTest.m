//
//  PDFRenderOperationTest.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 5/7/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import <GHUnitIOS/GHUnit.h>
#import "PDFRenderOperation.h"
#import "PrintDocument.h"
#import "PrintPreviewHelper.h"
#import "PDFFileManager.h"
#import "Printer.h"
#import "DatabaseManager.h"

@interface PDFRenderOperation(Test)

@property (nonatomic, strong) NSArray *pageIndices;
@property (nonatomic, weak) PrintDocument *printDocument;
@property (nonatomic) CGSize size;
@property (nonatomic) NSUInteger currentPage;
@property (nonatomic) BOOL isFrontPage;

- (BOOL)shouldInvertImage;
@end

@interface PDFRenderOperationTest : GHTestCase

@property (nonatomic,strong) UIImage *expectedImageUpperLeftStaple;

@end

@implementation PDFRenderOperationTest
{
    CGSize testSize;
    CGFloat drawingMargins;
    NSURL *testURL;
    NSURL *pdfOriginalFileURL;

}
 - (void)setUpClass
{
    testSize = CGSizeMake(612.283, 793.701);
    drawingMargins = 10.0f;
    
    pdfOriginalFileURL= [[NSBundle mainBundle] URLForResource:@"TestPDF_3Pages_NoPass" withExtension:@"pdf"];
    NSArray* documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    NSString *testFilePath = [documentsDir stringByAppendingString: [NSString stringWithFormat:@"/%@",[pdfOriginalFileURL.path lastPathComponent]]];
    
    testURL = [NSURL URLWithString:[testFilePath stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
}

- (void)setUp
{
    NSError *error;
    [[NSFileManager defaultManager] copyItemAtPath:[pdfOriginalFileURL path] toPath: [testURL path] error:&error];
    PDFFileManager *manager = [PDFFileManager sharedManager];
    [manager setFileURL:testURL];
    [manager setupDocument];
}

-(void)testInit
{
    NSArray *indices = [NSArray arrayWithObjects:[NSNumber numberWithInteger: 0], nil];
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:indices size:testSize delegate:nil];

    GHAssertNotNil(renderOperation, @"");
    GHAssertEqualObjects([[PDFFileManager sharedManager] printDocument], renderOperation.printDocument, @"");
    
}

-(void) testShouldInvertImage
{
    PDFFileManager *manager = [PDFFileManager sharedManager];
    PDFRenderOperation *renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:testSize delegate:nil];

    renderOperation.isFrontPage = YES;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    renderOperation.isFrontPage = NO;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.finishingSide = kFinishingSideLeft;
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingShortEdge;
    GHAssertTrue([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.finishingSide = kFinishingSideTop;
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    GHAssertTrue([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingShortEdge;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    CGSize landscapeTestSize = CGSizeMake(testSize.height, testSize.width);
    renderOperation = [[PDFRenderOperation alloc] initWithPageIndexSet:nil size:landscapeTestSize delegate:nil];
    
    renderOperation.isFrontPage = YES;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    renderOperation.isFrontPage = NO;
    manager.printDocument.previewSetting.finishingSide = kFinishingSideLeft;
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    GHAssertTrue([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingShortEdge;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.finishingSide = kFinishingSideTop;
    manager.printDocument.previewSetting.duplex = kDuplexSettingLongEdge;
    GHAssertFalse([renderOperation shouldInvertImage], @"");
    
    manager.printDocument.previewSetting.duplex = kDuplexSettingShortEdge;
    GHAssertTrue([renderOperation shouldInvertImage], @"");
}

@end
