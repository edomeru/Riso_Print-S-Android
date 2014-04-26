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

void printProgressCallback(directprint_job *job, int status, float progress);

@interface DirectPrintManager()

@property (nonatomic, weak) CXAlertView *alertView;
@property (nonatomic, weak) UILabel *progressLabel;
@property (nonatomic, weak) UIActivityIndicatorView *progressIndicator;
@property (nonatomic, weak) PrintDocument *printDocument;

- (directprint_job *)preparePrintJob;
- (UIView *)createProgressView;
- (void)updateProgress:(float)progress;
- (void)updateSuccess;
- (void)updateError;

@end

@implementation DirectPrintManager

- (void)printDocumentViaLPR
{
    directprint_job *job = [self preparePrintJob];
    directprint_job_lpr_print(job);
}

- (void)printDocumentViaRaw
{
    directprint_job *job = [self preparePrintJob];
    directprint_job_raw_print(job);
}

- (directprint_job *)preparePrintJob
{
    self.printDocument = [[PDFFileManager sharedManager] printDocument];
    
    NSString *fullPath = [self.printDocument.url path];
    NSString *fileName = self.printDocument.name;
    NSString *ipAddress = [self.printDocument.printer ip_address];
    NSString *printSettings = [self.printDocument.previewSetting formattedString];
    
    directprint_job *job = directprint_job_new([fileName UTF8String], [fullPath UTF8String], [printSettings UTF8String], [ipAddress UTF8String], printProgressCallback);
    directprint_job_set_caller_data(job, (void *)CFBridgingRetain(self));
    UIView *progressView = [self createProgressView];
    CXAlertView *alertView = [[CXAlertView alloc] initWithTitle:NSLocalizedString(IDS_LBL_PRINTING, @"") contentView:progressView cancelButtonTitle:nil];
    self.alertView = alertView;
    [alertView addButtonWithTitle:NSLocalizedString(IDS_LBL_CANCEL, @"")
                             type:CXAlertViewButtonTypeDefault
                          handler:^(CXAlertView *alertView, CXAlertButtonItem *button) {
                              CFBridgingRelease(directprint_job_get_caller_data(job));
                              directprint_job_cancel(job);
                              directprint_job_free(job);
                              [self.alertView dismiss];
                          }];
    [alertView show];
    
    return job;
}

- (UIView *)createProgressView
{
    UIView *progressView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, PROGRESS_WIDTH, PROGRESS_HEIGHT)];
    progressView.backgroundColor = [UIColor clearColor];
    
    // Create progress label
    UILabel *progressLabel = [[UILabel alloc] initWithFrame:CGRectZero];
    progressLabel.translatesAutoresizingMaskIntoConstraints = NO;
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
    [self.alertView dismiss];
    
    [PrintJobHistoryHelper createPrintJobFromDocument:self.printDocument withResult:1];
    
    [self.delegate documentDidFinishPrinting:YES];
}

- (void)updateError
{
    [self.alertView dismiss];
    
    [PrintJobHistoryHelper createPrintJobFromDocument:self.printDocument withResult:0];
    
    [AlertHelper displayResult:kAlertResultErrDefault withTitle:kAlertTitleDefault withDetails:nil];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.delegate documentDidFinishPrinting:NO];
    });
}

@end

void printProgressCallback(directprint_job *job, int status, float progress)
{
    void *callerData = directprint_job_get_caller_data(job);
    DirectPrintManager *manager = (__bridge DirectPrintManager *)callerData;
    [manager updateProgress:progress];
    if (status < 0)
    {
        // Error
        [manager updateError];
        CFBridgingRelease(callerData);
        directprint_job_free(job);
    }
    else if (status == kJobStatusSent)
    {
        // Success
        [manager updateSuccess];
        CFBridgingRelease(callerData);
        directprint_job_free(job);
    }
}