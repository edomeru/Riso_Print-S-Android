//
//  GTestEventListener.h
//  SmartDeviceApp
//
//  Created by Seph on 12/17/13.
//  Copyright (c) 2013 aLink. All rights reserved.
//

#ifndef __SmartDeviceApp__GTestEventListener__
#define __SmartDeviceApp__GTestEventListener__

#include <gtest/gtest.h>
#include <GHUnitIOS/GHUnit.h>

class GTestEventListener : public ::testing::EmptyTestEventListener
{
    virtual void OnTestStart(const ::testing::TestInfo& testInfo)
    {
    }
    
    virtual void OnTestPartResult(const ::testing::TestPartResult& testPartResult)
    {
        if (testPartResult.failed() && failString == nil)
        {
            this->failString = [NSString stringWithFormat:@"Failure in %s:%d\n%s", testPartResult.file_name(), testPartResult.line_number(), testPartResult.summary()];
        }
    }
    
    virtual void OnTestEnd(const ::testing::TestInfo& testInfo)
    {
    }
    
public:
    NSString *failString;
};

#endif /* defined(__SmartDeviceApp__GTestEventListener__) */
