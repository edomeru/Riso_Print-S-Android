//
//  XMLParser.h
//  SmartDeviceApp
//
//  Created by a-LINK Group.
//  Copyright (c) 2014 RISO KAGAKU CORPORATION. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * XMLParser class is used to parse an XML file and convert it to an instance of NSDictionary for reading.
 */
@interface XMLParser : NSObject<NSXMLParserDelegate>

/**
 * Creates a dictionary by parsing an XML file
 * @param path
 *        Path of the XML file
 * @return Dictionary object of the parsed XML file
 */
+ (NSDictionary *)dictionaryFromXMLFile:(NSString *)path;

@end
