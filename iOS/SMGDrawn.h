//
//  SMGDrawn.h
//  SockMatcher
//
//  Created by Joseph Caplan on 9/21/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SMGDrawn : NSObject

@property CGPoint base;
@property CGPoint pos;
@property int val;

- (id)init;
- (id)initWithVal:(int)val;
- (id)initWithBase:(CGPoint)base pos:(CGPoint)pos andVal:(int)val;
- (void)returnToBase;

@end
