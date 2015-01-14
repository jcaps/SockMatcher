//
//  SMGEndGameProtocol.m
//  SockMatcher
//
//  Created by Joseph Caplan on 11/16/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGEndGameProtocol.h"

@implementation SMGEndGameProtocol

- (void)endGame
{
    [self.delegate endingGame];
}

@end