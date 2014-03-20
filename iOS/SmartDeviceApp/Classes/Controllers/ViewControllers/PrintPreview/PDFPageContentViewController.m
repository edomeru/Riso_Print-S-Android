//
//  PDFPageViewController.m
//  SmartDeviceApp
//
//  Created by Amor Corazon Rio on 3/11/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "PDFPageContentViewController.h"
#import "PrintPreviewHelper.h"

@interface PDFPageContentViewController ()
@property (strong, nonatomic) IBOutlet PDFPageView *pageView;
@end

@implementation PDFPageContentViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    NSLog(@"load pdf page content view controller");
   	// Do any additional setup after loading the view.
    self.pageView.datasource = self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - PDFPageViewDatasource methods

-(CGPDFPageRef) getPage:(NSUInteger)pageNum
{
    return [self.datasource getPDFPage:self.pageIndex withPageOffset:pageNum];
}

-(NSUInteger) getNumPages
{
    PreviewSetting *previewSetting = [self.datasource getPreviewSetting];
    return getNumberOfPagesPerSheet(previewSetting.pagination);
}

-(BOOL) isGrayScale
{
    PreviewSetting *previewSetting = [self.datasource getPreviewSetting];
    return isGrayScale(previewSetting.colorMode);
}

@end
