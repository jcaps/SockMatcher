//
//  SMGDrawn.m
//  SockMatcher
//
//  Created by Joseph Caplan on 9/21/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGDrawn.h"

@implementation SMGDrawn

- (id)init
{
    self = [super init];
    
    self.base = CGPointMake(0, 0);
    self.pos = CGPointMake(0, 0);
    self.val = 0;
    
    return self;
}

- (id)initWithVal:(int)val
{
    self = [super init];
    
    self.base = CGPointMake(0, 0);
    self.pos = CGPointMake(0, 0);
    self.val = val;
    
    return self;
}

- (id)initWithBase:(CGPoint)base pos:(CGPoint)pos andVal:(int)val
{
    self = [super init];
    
    self.base = base;
    self.pos = pos;
    self.val = val;
    
    return self;
}

- (void)returnToBase
{
    self.pos = self.base;
}

@end
