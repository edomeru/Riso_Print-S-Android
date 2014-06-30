//
//  CGPDFMock.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import "CGPDFMock.h"

DEFINE_FAKE_VALUE_FUNC0(CGPDFDocumentRef, CGPDFDocumentCreateWithURL);
DEFINE_FAKE_VOID_FUNC0(CGPDFDocumentRelease);
DEFINE_FAKE_VALUE_FUNC0(bool, CGPDFDocumentIsUnlocked);
DEFINE_FAKE_VALUE_FUNC0(bool, CGPDFDocumentIsEncrypted);
DEFINE_FAKE_VALUE_FUNC0(bool, CGPDFDocumentAllowsPrinting);
DEFINE_FAKE_VALUE_FUNC0(size_t, CGPDFDocumentGetNumberOfPages)
DEFINE_FAKE_VALUE_FUNC0(CGPDFPageRef, CGPDFDocumentGetPage)
DEFINE_FAKE_VALUE_FUNC0(CGRect, CGPDFPageGetBoxRect)