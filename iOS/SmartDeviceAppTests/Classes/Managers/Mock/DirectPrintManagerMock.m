//
//  DirectPrintManagerMock.m
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "DirectPrintManagerMock.h"
#import "DirectPrintManager.h"
#include "common.h"

@interface DirectPrintManager()

- (directprint_job *)preparePrintJob;
- (void)updateSuccess;
- (void)updateError;

@end

@implementation DirectPrintManagerMock

- (void)printDocumentViaLPR
{
    id instance = self;
    [instance preparePrintJob];
    [instance updateSuccess];
}

- (void)printDocumentViaRaw
{
    id instance = self;
    [instance preparePrintJob];
    [instance updateSuccess];
}

- (void)printDocumentError
{
    id instance = self;
    [instance preparePrintJob];
    [instance updateError];
}

@end
