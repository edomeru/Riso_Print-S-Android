//
//  DirectPrintManager.m
//  SmartDeviceApp
//
//  Created by Seph on 4/17/14.
//  Copyright (c) 2014 aLink. All rights reserved.
//

#import "DirectPrintManager.h"
#import "PDFFileManager.h"
#import "PrintDocument.h"
#import "PreviewSetting.h"
#import "Printer.h"
#import "CXAlertView.h"
#import "UIColor+Theme.h"
#import "AlertHelper.h"
#include "common.h"

#define PROGRESS_WIDTH 280.0f
#define PROGRESS_HEIGHT 2.0f
#define PROGRESS_MARGIN 40.0f

void printProgressCallback(directprint_job *job, int status, float progress);

@interface DirectPrintManager()

@property (nonatomic, weak) CXAlertView *alertView;
@property (nonatomic, weak) NSLayoutConstraint *progressConstraint;

- (UIView *)createProgressView;
- (void)updateProgress:(float)progress;
- (void)updateSuccess;
- (void)updateError;

@end

@implementation DirectPrintManager

- (void)printDocumentViaLPR
{
    PrintDocument *printDocument = [[PDFFileManager sharedManager] printDocument];
    
    NSString *fullPath = [printDocument.url path];
    NSString *fileName = printDocument.name;
    NSString *ipAddress = [printDocument.printer ip_address];
    NSString *printSettings = [printDocument.previewSetting formattedString];
    
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
    directprint_job_lpr_print(job);
}

- (UIView *)createProgressView
{
    // Create container
    UIView *progressView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, PROGRESS_WIDTH, PROGRESS_HEIGHT)];
    progressView.backgroundColor = [UIColor clearColor];
    
    // Create progress indicator
    UIView *indicator = [[UIView alloc] initWithFrame:CGRectZero];
    indicator.translatesAutoresizingMaskIntoConstraints = NO;
    indicator.backgroundColor = [UIColor purple1ThemeColor];
    [progressView addSubview:indicator];
    NSDictionary *metrics = @{@"margin": @PROGRESS_MARGIN, @"height": @PROGRESS_HEIGHT};
    [progressView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-(margin)-[indicator]" options:0 metrics:metrics views:NSDictionaryOfVariableBindings(indicator)]];
    [progressView addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:[indicator(height)]-(0)-|" options:0 metrics:metrics views:NSDictionaryOfVariableBindings(indicator)]];
    NSLayoutConstraint *constraint = [NSLayoutConstraint constraintWithItem:indicator attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0f constant:2.0f];
    [indicator addConstraint:constraint];
    self.progressConstraint = constraint;
    
    return progressView;
}

- (void)updateProgress:(float)progress
{
    self.progressConstraint.constant = progress * 2.0f;
    dispatch_async(dispatch_get_main_queue(), ^
    {
        [UIView animateWithDuration:0.1f animations:^
         {
             [self.alertView.contentView layoutSubviews];
         }];
    });
}

- (void)updateSuccess
{
    [self.alertView dismiss];
}

- (void)updateError
{
    [self.alertView dismiss];
    [AlertHelper displayResult:kAlertResultErrDefault withTitle:kAlertTitleDefault withDetails:nil];
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