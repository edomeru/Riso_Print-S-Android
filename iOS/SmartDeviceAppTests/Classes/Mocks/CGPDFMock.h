//
//  CGPDFMock.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#include "fff.h"
DEFINE_FFF_GLOBALS;

DECLARE_FAKE_VALUE_FUNC0(CGPDFDocumentRef, CGPDFDocumentCreateWithURL);
DECLARE_FAKE_VOID_FUNC0(CGPDFDocumentRelease);
DECLARE_FAKE_VALUE_FUNC0(bool, CGPDFDocumentIsUnlocked);
DECLARE_FAKE_VALUE_FUNC0(bool, CGPDFDocumentIsEncrypted);
DECLARE_FAKE_VALUE_FUNC0(bool, CGPDFDocumentAllowsPrinting);
DECLARE_FAKE_VALUE_FUNC0(size_t, CGPDFDocumentGetNumberOfPages)
DECLARE_FAKE_VALUE_FUNC0(CGPDFPageRef, CGPDFDocumentGetPage)
DECLARE_FAKE_VALUE_FUNC0(CGRect, CGPDFPageGetBoxRect)

/*    CGPDFPageRef pageRef = CGPDFDocumentGetPage(docRef, 1);
    CGRect pageRect = CGPDFPageGetBoxRect(pageRef, kCGPDFMediaBox);*/