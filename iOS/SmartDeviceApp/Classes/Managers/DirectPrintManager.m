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
#import "AppSettings.h"
#include "common.h"

#define PROGRESS_WIDTH 280.0f
#define PROGRESS_HEIGHT 70.0f
#define PROGRESS_FORMAT @"%.2f%%"

static NSLock *lock = nil;
static NSMutableArray *taskList = nil;

void printProgressCallback(directprint_job *job, int status, float progress);

@interface DirectPrintManager()

/**
 * Popup displaying the progress in sending the print job to the printer.
 */
@property (nonatomic, weak) CXAlertView *alertView;

/**
 * Subview of the {@link alertView} showing the progress as a percentage.
 */
@property (nonatomic, weak) UILabel *progressLabel;

/**
 * Subview of the {@link alertView} showing the progress animation.
 */
@property (nonatomic, weak) UIActivityIndicatorView *progressIndicator;

/**
 * Reference to the document object to be sent to the printer.
 */
@property (nonatomic, weak) PrintDocument *printDocument;

/**
 * Reference to the print job that will be sent to the printer.
 * This is created using the Direct Print common library.\n
 * It contains the name and path to the document, the printer info,
 * and the print settings.
 */
@property (nonatomic, assign) directprint_job *job;

/** 
 * Flag that indicates whether a print operation is ongoing.
 */
@property (nonatomic, assign) BOOL isPrinting;

/**
 * Creates the print job that will be sent to the printer.
 * The print job is created using the Direct Print common library.\n
 * After the print job is created, the printing progress popup is displayed.
 */
- (void)preparePrintJob;

/**
 * Prepares the contents of the {@link alertView}.
 *
 * @return the combined contents of the {@link alertView}
 */
- (UIView *)createProgressView;

/**
 * Updates the percentage value displayed in the printing progress popup.
 *
 * @param progress the new percentage to display
 */
- (void)updateProgress:(float)progress;

/**
 * Replaces the printing progress popup with another popup showing the
 * printing success message.
 * A PrintJob object is also created containing the details of the
 * print operation which can be viewed in the Print Job History screen.
 */
- (void)updateSuccess;

/**
 * Replaces the printing progress popup with another popup showing the
 * printing failed message.
 * A PrintJob object is also created containing the details of the
 * print operation which the user can view in the Print Job History screen.
 */
- (void)updateError;

/**
 * Stops an ongoing print operation started by either {@link printDocumentViaLPR}
 * or {@link printDocumentViaRaw}. This method waits until the print operation
 * in the Direct Print common library has been terminated before returning. \n
 */
- (void)cancelJob;

/**
 * Initializes the list that will contain DirectPrintManager instances
 * performing print operations.
 * The list is used for handling printing when the application goes to
 * either the background or to an inactive state.
 */
+ (void)initialize;

/**
 * Adds an instance to the list of DirectPrintManager instances
 * performing print operations.
 * The list is used for handling printing when the application goes to
 * either the background or to an inactive state.
 *
 * @param manager a DirectPrintManager instance
 */
+ (void)addTask:(DirectPrintManager *)manager;

/**
 * Removes an instance from the list of DirectPrintManager instances
 * performing print operations.
 * The list is used for handling printing when the application goes to
 * either the background or to an inactive state.
 *
 * @param manager a DirectPrintManager instance
 */
+ (void)removeTask:(DirectPrintManager *)manager;

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
    NSString* loginId = [[NSUserDefaults standardUserDefaults] valueForKey:KEY_APPSETTINGS_LOGIN_ID];
    if (loginId == nil)
    {
        loginId = @"";
    }
    
    self.job = directprint_job_new([loginId UTF8String], [fileName UTF8String], [fullPath UTF8String], [printSettings UTF8String], [ipAddress UTF8String], printProgressCallback);
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
    
    [PrintJobHistoryHelper createPrintJobFromDocument:self.printDocument withResult:1];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [AlertHelper displayResult:kAlertResultPrintSuccessful withTitle:kAlertTitleDefault withDetails:nil withDismissHandler:^(CXAlertView *alertView){
            dispatch_async(dispatch_get_main_queue(), ^{
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
    
#if DEBUG_LOG_DIRECTPRINT_MANAGER
    NSLog(@"[INFO][DirectPrintManager] Progress: %f", progress);
#endif
    
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