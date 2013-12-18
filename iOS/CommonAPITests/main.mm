//
//  main.m
//  CommonAPITests
//
//  Created by Seph on 12/17/13.
//  Copyright (c) 2013 aLink. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <objc/runtime.h>
#import <GHUnitIOS/GHUnit.h>

#include <gtest/gtest.h>
#include "GTestEventListener.h"

void ghTestMethod(id self, SEL _cmd);

int main(int argc, char * argv[])
{
    @autoreleasepool {
        // Collect the tests
        int caseCount = ::testing::UnitTest::GetInstance()->total_test_case_count();
        for (int i = 0; i < caseCount; i++)
        {
            const ::testing::TestCase *const testCase = ::testing::UnitTest::GetInstance()->GetTestCase(i);
            
            // Create class for the test case
            NSString *className = [NSString stringWithFormat:@"%s", testCase->name()];
            Class caseClass = objc_allocateClassPair([GHTestCase class], [className UTF8String], 0);
            
            int testCount = testCase->total_test_count();
            for (int j = 0; j < testCount; j++)
            {
                const ::testing::TestInfo *const testInfo = testCase->GetTestInfo(j);
                
                // Add method to case class based on the test
                class_addMethod(caseClass, NSSelectorFromString([NSString stringWithCString:testInfo->name() encoding:NSUTF8StringEncoding]), (IMP)ghTestMethod, "v@:");
            }
            
            // Register class to Obj-C runtime
            objc_registerClassPair(caseClass);
        }
        
        // Remove default listeners
        ::testing::TestEventListeners& listeners = ::testing::UnitTest::GetInstance()->listeners();
        delete listeners.Release(listeners.default_result_printer());
        
        return UIApplicationMain(argc, argv, nil, @"GHUnitIOSAppDelegate");
    }
}

void ghTestMethod(id self, SEL _cmd)
{
    // Create filter from class name and test method
    NSString *className = NSStringFromClass([self class]);
    NSString *selectorName = NSStringFromSelector(_cmd);
    NSString *filter = [NSString stringWithFormat:@"%@.%@", className, selectorName];
    ::testing::GTEST_FLAG(filter) = [filter UTF8String];
    
    // Create event listener
    ::testing::TestEventListeners& listeners = ::testing::UnitTest::GetInstance()->listeners();
    delete listeners.Release(listeners.default_result_printer());
    GTestEventListener *eventListener = new GTestEventListener;
    listeners.Append(eventListener);
    
    // Run test
    char *argv[] = { "SmartDeviceApp" };
    int argc = 1;
    ::testing::InitGoogleTest(&argc, argv);
    int result = RUN_ALL_TESTS();
    
    // Check result
    NSString *failString = eventListener->failString;
    delete listeners.Release(listeners.default_result_printer());
    if (result != 0)
    {
        GHFail(failString);
    }
}