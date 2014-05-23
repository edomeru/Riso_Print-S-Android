//
//  DirectPrintManager.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "DirectPrintManager.h"
#import "PDFFileManager.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "Printer.h"
#import "CXAlertView.h"
#import "UIColor+Theme.h"
#import "AlertHelper.h"
#import "PrintJobHistoryHelper.h"
#include "common.h"

#define PROGRESS_WIDTH 280.0f
#define PROGRESS_HEIGHT 70.0f
#define PROGRESS_FORMAT @"%.2f%%"

static NSLock *lock = nil;
static NSMutableArray *taskList = nil;

void printProgressCallback(directprint_job *job, int status, float progress);

@interface DirectPrintManager()

@property (nonatomic, weak) CXAlertView *alertView;
@property (nonatomic, weak) UILabel *progressLabel;
@property (nonatomic, weak) UIActivityIndicatorView *progressIndicator;
@property (nonatomic, weak) PrintDocument *printDocument;
@property (nonatomic, assign) directprint_job *job;
@property (nonatomic, assign) BOOL isPrinting;

- (void)preparePrintJob;
- (UIView *)createProgressView;
- (void)updateProgress:(float)progress;
- (void)updateSuccess;
- (void)updateError;
- (void)cancelJob;

@end

@implementation DirectPrintManager

+ (void)initialize
{
    lock = [[NSLock alloc] init];
    taskList = [[NSMutableArray alloc] init];
}

+ (BOOL)idle
{
    BOOL result = YES;
    [lock lock];
    result = [taskList count] == 0;
    [lock unlock];
    
    return result;
}

+ (void)cancelAll
{
    while (![DirectPrintManager idle])
    {
        [lock lock];
        DirectPrintManager *manager = [taskList firstObject];
        [lock unlock];
        [manager cancelJob];
    }
}

+ (void)addTask:(DirectPrintManager *)manager
{
    [lock lock];
    [taskList addObject:manager];
    [lock unlock];
}

+ (void)removeTask:(DirectPrintManager *)manager
{
    [lock lock];
    [taskList removeObject:manager];
    [lock unlock];
}

- (id)init
{
    self = [super init];
    if (self)
    {
        _job = nil;
        _isPrinting = NO;
    }
    
    return self;
}

- (void)dealloc
{
    if (self.isPrinting)
    {
        [self cancelJob];
    }
}

- (void)printDocumentViaLPR
{
    self.isPrinting = YES;
    [self preparePrintJob];
    [DirectPrintManager addTask:self];
    directprint_job_lpr_print(self.job);
}

- (void)printDocumentViaRaw
{
    self.isPrinting = YES;
    [self preparePrintJob];
    [DirectPrintManager addTask:self];
    directprint_job_raw_print(self.job);
}

- (void)preparePrintJob
{
    self.printDocument = [[PDFFileManager sharedManager] printDocument];
    
    NSString *fullPath = [self.printDocument.url path];
    NSString *fileName = self.printDocument.name;
    NSString *ipAddress = [self.printDocument.printer ip_address];
    NSString *printSettings = [self.printDocument.previewSetting formattedString];
    
    self.job = directprint_job_new([NSLocalizedString(IDS_APP_NAME, @"") UTF8String], [fileName UTF8String], [fullPath UTF8String], [printSettings UTF8String], [ipAddress UTF8String], printProgressCallback);
    directprint_job_set_caller_data(self.job, (void *)CFBridgingRetain(self));
    UIView *progressView = [self createProgressView];
    CXAlertView *alertView = [[CXAlertView alloc] initWithTitle:NSLocalizedString(IDS_INFO_MSG_PRINTING, @"") contentView:progressView cancelButtonTitle:nil];
    [alertView addButtonWithTitle:NSLocalizedString(IDS_LBL_CANCEL, @"") type:CXAlertViewButtonTypeCancel handler:^(CXAlertView * alertView, CXAlertButtonItem *button){
        [self cancelJob];
    }];
    self.alertView = alertView;
    [alertView show];
}

- (UIView *)createProgressView
{
    UIView *progressView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, PROGRESS_WIDTH, PROGRESS_HEIGHT)];
    progressView.backgroundColor = [UIColor clearColor];
    
    // Create progress label
    UILabel *progressLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    progressLabel.translatesAutoresizingMaskIntoConstraints = NO;
    progressLabel.backgroundColor = [UIColor clearColor];
    progressLabel.textAlignment = NSTextAlignmentCenter;
    [progressView addSubview:progressLabel];
    [progressView addConstraint:[NSLayoutConstraint constraintWithItem:progressLabel attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:progressView attribute:NSLayoutAttributeCenterX multiplier:1.0f constant:0.0f]];
    [progressView addConstraint:[NSLayoutConstraint constraintWithItem:progressLabel attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:progressView attribute:NSLayoutAttributeTop multiplier:1.0f constant:8.0f]];
    progressLabel.text = [NSString stringWithFormat:PROGRESS_FORMAT, 0.0f];
    self.progressLabel = progressLabel;
    
    // Create activity indicator
    UIActivityIndicatorView *progressIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
    progressIndicator.color = [UIColor purple1ThemeColor];
    progressIndicator.translatesAutoresizingMaskIntoConstraints = NO;
    [progressView addSubview:progressIndicator];
    [progressView addConstraint:[NSLayoutConstraint constraintWithItem:progressIndicator attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:progressView attribute:NSLayoutAttributeCenterX multiplier:1.0f constant:0.0]];
    [progressView addConstraint:[NSLayoutConstraint constraintWithItem:progressIndicator attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:progressLabel attribute:NSLayoutAttributeBottom multiplier:1.0f constant:8.0f]];
    [progressIndicator startAnimating];
    self.progressIndicator = progressIndicator;
    
    return progressView;
}

- (void)updateProgress:(float)progress
{
    dispatch_async(dispatch_get_main_queue(), ^
    {
        [UIView animateWithDuration:0.1f animations:^
         {
            self.progressLabel.text = [NSString stringWithFormat:PROGRESS_FORMAT, progress];
         }];
    });
}

- (void)updateSuccess
{
    self.isPrinting = NO;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [AlertHelper displayResult:kAlertResultPrintSuccessful withTitle:kAlertTitleDefault withDetails:nil withDismissHandler:^(CXAlertView *alertView){
            dispatch_async(dispatch_get_main_queue(), ^{
                [PrintJobHistoryHelper createPrintJobFromDocument:self.printDocument withResult:1];
                [self.delegate documentDidFinishPrinting:YES];
            });
        }];
        [self.alertView dismiss];
    });
    
}

- (void)updateError
{
    self.isPrinting = NO;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [PrintJobHistoryHelper createPrintJobFromDocument:self.printDocument withResult:0];
        [AlertHelper displayResult:kAlertResultPrintFailed withTitle:kAlertTitleDefault withDetails:nil];
        [self.alertView dismiss];
        [self.delegate documentDidFinishPrinting:NO];
    });
}

- (void)cancelJob
{
    if (self.isPrinting)
    {
        self.isPrinting = NO;
        [self.alertView dismiss];
        self.alertView = nil;
        CFBridgingRelease(directprint_job_get_caller_data(self.job));
        [DirectPrintManager removeTask:self];
        directprint_job_cancel(self.job);
        directprint_job_free(self.job);
    }
}

@end

void printProgressCallback(directprint_job *job, int status, float progress)
{
    void *callerData = directprint_job_get_caller_data(job);
    DirectPrintManager *manager = (__bridge DirectPrintManager *)callerData;
    NSLog(@"Progress: %f", progress);
    [manager updateProgress:progress];
    if (status < 0)
    {
        // Error
        [manager updateError];
        CFBridgingRelease(callerData);
        [DirectPrintManager removeTask:manager];
        directprint_job_free(job);
    }
    else if (status == kJobStatusSent)
    {
        // Success
        [manager updateSuccess];
        CFBridgingRelease(callerData);
        [DirectPrintManager removeTask:manager];
        directprint_job_free(job);
    }
}