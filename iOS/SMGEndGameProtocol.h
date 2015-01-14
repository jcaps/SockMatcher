//
//  SMGEndGameProtocol.h
//  SockMatcher
//
//  Created by Joseph Caplan on 11/16/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGEndGameProtocol.h"
#import <Foundation/Foundation.h>

@protocol SMGEndGameProtocolDelegate <NSObject>
@required
- (void)endingGame;
@end

@interface SMGEndGameProtocol : NSObject

{
    id <SMGEndGameProtocolDelegate> _delegate;
}

@property (nonatomic, strong) id delegate;

- (void)endGame;

@end