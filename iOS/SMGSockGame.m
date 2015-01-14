//
//  SMGSockGame.m
//  SockMatcher
//
//  Created by Joseph Caplan on 9/21/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGSockGame.h"
#include <stdlib.h>
#include <AVFoundation/AVFoundation.h>

@interface SMGSockGame() {
    int collisionSize, cols, diffSocks, fireballTime, halfHeight, halfWidth, newSockTime, points, pointsX, pointsY, size, sockHeight, sockWidth;
    NSArray *effects, *socks;
    NSMutableArray *neighbors, *newNeighbors, *validSocks;
    NSOperationQueue *backgroundQueue;
    NSTimer *fireballCountdownTimer, *fireballSpinnerTimer, *newSockTimer, *timer;
}

@end

@implementation SMGSockGame

#pragma mark - Init Methods

- (id)init
{
    self = [super init];
    
    self.hasItem = NO;
    self.isSelected = NO;
    self.newHighScore = NO;
    
    cols = 5;
    backgroundQueue = [NSOperationQueue new];
    collisionSize = 100/cols;
    diffSocks = 15;
    newSockTime = 15;
    size = cols * cols;
    sockHeight = 800/cols;
    sockWidth = 1080/cols;
    halfHeight = sockHeight/2;
    halfWidth = sockWidth/2;
    points = 0;
    pointsX = 0;
    pointsY = 0;
    self.score = 0;
    self.selectedSockNum = 0;
    self.time = 60;
    self.itemRot = 0;
    
    self.item = [[SMGDrawn alloc] initWithBase:CGPointMake(1080, 200) pos:CGPointMake(1080, 200) andVal:0];
    
    neighbors = [NSMutableArray array];
    newNeighbors = [NSMutableArray array];
    self.effectsToDraw = [NSMutableArray array];
    self.socksToDraw = [NSMutableArray array];
    
    self.itemString = @"Item";
    self.itemStringHidden = YES;
    
    [self initSockArrays];
    self.gameState = READY;

    return self;
}

- (void)initSockArrays
{
    int n = 0, x = 0, y = 0;
    
    NSMutableArray *tempSocksArray = [NSMutableArray array];
    NSMutableArray *tempEffectsArray = [NSMutableArray array];
    
    for (int i = 0; i < size; i++) {
        x = i%cols;
        y = i/cols;
        int val = arc4random_uniform(diffSocks);
        SMGSock *newSock = [[SMGSock alloc] initWithIndex:i val:val col:x row:y sockWidth:sockWidth andSockHeight:sockHeight];
        [tempSocksArray addObject:newSock];
        SMGDrawn *newEffect = [[SMGDrawn alloc] initWithBase:newSock.pos pos:newSock.pos andVal:0];
        [tempEffectsArray addObject:newEffect];
        
        if (y > 0) {
            n = i - cols;
            [((SMGSock*)tempSocksArray[i]).neighbors addObject:tempSocksArray[n]];
            [((SMGSock*)tempSocksArray[n]).neighbors addObject:tempSocksArray[i]];
            
            if (x > 0) {
                n = i - (cols + 1);
                [((SMGSock*)tempSocksArray[i]).corners addObject:tempSocksArray[n]];
                [((SMGSock*)tempSocksArray[n]).corners addObject:tempSocksArray[i]];
            }
            
            if (x < cols - 1) {
                n = i - (cols - 1);
                [((SMGSock*)tempSocksArray[i]).corners addObject:tempSocksArray[n]];
                [((SMGSock*)tempSocksArray[n]).corners addObject:tempSocksArray[i]];
            }
        }
        
        if (x > 0) {
            n = i - 1;
            [((SMGSock*)tempSocksArray[i]).neighbors addObject:tempSocksArray[n]];
            [((SMGSock*)tempSocksArray[n]).neighbors addObject:tempSocksArray[i]];
        }

    }  //end for loop
    effects = [NSArray arrayWithArray:tempEffectsArray];
    socks = [NSArray arrayWithArray:tempSocksArray];
    validSocks = [NSMutableArray arrayWithArray:socks];
    //validSocks = [[NSMutableArray alloc] initWithArray:socks];
}

- (void)destroy
{
    effects = nil;
    [neighbors removeAllObjects];
    neighbors = nil;
    [newNeighbors removeAllObjects];
    newNeighbors = nil;
    backgroundQueue = nil;
    fireballCountdownTimer = nil;
    fireballSpinnerTimer = nil;
    newSockTimer = nil;
    timer = nil;
    for (SMGSock *sock in socks)
        [sock destroy];
    socks = nil;
    [self.effectsToDraw removeAllObjects];
    self.effectsToDraw = nil;
    self.soundPlayers = nil;
    [self.socksToDraw removeAllObjects];
    self.socksToDraw = nil;
    [validSocks removeAllObjects];
    validSocks = nil;
    self.itemString = nil;
    self.item = nil;
    self.gameEndProtocol.delegate = nil;
    self.gameEndProtocol = nil;
    self.selectedSock = nil;
}

#pragma mark - Touch Handle Methods

- (void)handleTouchDownWithX:(float)x andY:(float)y
{
    if (!self.isSelected) {
        if (x < 1080) {
            SMGSock *highlightedSock = [self getSockWithX:x andY:y];
            
            if ([validSocks containsObject:highlightedSock]) {
                [self selectSock:highlightedSock withX:x andY:y];
                if (self.sound)
                    [((AVAudioPlayer*)self.soundPlayers[4]) play];
            }
        }
        
        else if (self.hasItem && [self isPointWithX:x andY:y inBoundsLeft:1080 right:1280 top:200 bottom:400]) {
            [self selectItemAtX:x andY:y];
            
            if (self.item.val == 2) {
                [self countdownFireball2];
                fireballCountdownTimer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(countdownFireball2) userInfo:nil repeats:YES];
            }
            
        }
    }
}  //end method handleTouchDownWithXandY

- (void)handleTouchDraggedWithX:(float)x andY:(float)y
{
    if (self.isSelected) {
        switch (self.selected) {
            case SOCK:
                self.selectedSock.pos = CGPointMake(x-halfWidth, y-halfHeight);
                break;
            case ITEM:
                self.item.pos = CGPointMake(x-100, y-100);
                
                if (self.item.val == 2 && [self isPointWithX:x andY:y inBoundsLeft:0 right:1080 top:0 bottom:800]) {
                    SMGSock *sock = [self getSockWithX:x andY:y];
                    BOOL sockValid = NO;
                    
                    @synchronized(validSocks) {
                        sockValid = [validSocks containsObject:sock];
                    }
                    if (sockValid) {
                        @synchronized(validSocks){
                            [validSocks removeObject:sock];
                        }
                        
                        [backgroundQueue addOperationWithBlock:^{[self burnSock:sock];}];
                    }
                }
                break;
            default:
                break;
        }
    }
}

- (void)handleTouchUpWithX:(float)x andY:(float)y
{
    BOOL matchFound = NO;
    CGRect sockRect;
    float sockX = 0.0f, sockY = 0.0f;
    SMGSock *otherSock;
    
    if (self.isSelected) {
        switch (self.selected) {
            case SOCK:
                sockRect = CGRectMake(self.selectedSock.pos.x+collisionSize, self.selectedSock.pos.y+collisionSize, sockWidth-2*collisionSize, sockHeight-2*collisionSize);
                
                for (int n = 0; n < 4 && !matchFound; n++) {
                    switch (n) {
                        case 0:
                            sockX = sockRect.origin.x;
                            sockY = sockRect.origin.y;
                            break;
                        case 1:
                            sockX = sockRect.origin.x + sockRect.size.width;
                            break;
                        case 2:
                            sockY = sockRect.origin.y + sockRect.size.height;
                            break;
                        case 3:
                            sockX = sockRect.origin.x;
                            break;
                        default:
                            sockX = -1;
                            sockY = -1;
                            break;
                    }
                    
                    if ([self isPointWithX:sockX andY:sockY inBoundsLeft:0 right:1080 top:0 bottom:800]) {
                        otherSock = [self getSockWithX:sockX andY:sockY];
                        if ([validSocks containsObject:otherSock] && [self.selectedSock matchesSock:otherSock]) {
                            matchFound = YES;
                            //[self matchSelectedSock:self.selectedSock withOtherSock:otherSock];
                            //[self matchUsingSockFillerThreadSelectedSock:self.selectedSock withOtherSock:otherSock];
                            [self matchUsingSockArraySelectedSock:self.selectedSock withOtherSock:otherSock];
                        }  //end if otherSock is valid and matches selected sock
                    }  //end if sock corner in sock area
                }  //end for loop iterating through sock corners
                
                if (!matchFound) {
                    [self.socksToDraw addObject:self.selectedSock];
                    [validSocks addObject:self.selectedSock];
                }
                
                [self.selectedSock returnToBase];
                break;
                
            case ITEM:
                switch (self.item.val) {
                    case 0:  //Bleach
                        if (x < 1080) {
                            SMGSock *sock = [self getSockWithX:x andY:y];
                            if ([validSocks containsObject:sock]) {
                                [backgroundQueue addOperationWithBlock:^{[self bleachSocksAroundSock:sock];}];
                                //[self performSelectorInBackground:@selector(bleachSocksAroundSock:) withObject:sock];
                                self.hasItem = NO;
                            }
                        }
                        break;
                    case 1:  //Sock Remover
                        if (x < 1080) {
                            SMGSock *sock = [self getSockWithX:x andY:y];
                            if ([validSocks containsObject:sock]) {
                                [backgroundQueue addOperationWithBlock:^{[self useSockRemoverWithSockArrayOnSock:sock];}];
                                //[backgroundQueue addOperationWithBlock:^{[self useSockRemoverWithSockFillerThreadOnSock:sock];}];
                                //[self performSelectorInBackground:@selector(useSockRemoverOnSock:) withObject:sock];
                                self.hasItem = NO;
                            }
                        }
                        break;
                    case 2:  //Fireball
                        [fireballCountdownTimer invalidate];
                        fireballCountdownTimer = nil;
                        break;
                    default:
                        break;
                }  //end switch item.val statement
                [self.item returnToBase];
                break;
                
            default:
                break;
        }
        
        self.isSelected = NO;
    }  //end if isSelected = YES
}
 
#pragma mark - Utility Methods

- (BOOL)isPointWithX:(float)x andY:(float)y inBoundsLeft:(int)left right:(int)right top:(int)top bottom:(int)bottom
{
    return x > left && x < right && y > top && y < bottom;
}

- (int)getSockNumWithX:(float)x andY:(float)y
{
    int row = (600-y)/sockHeight;
    int col = x/sockWidth;
    return self->cols*row+col;
}

- (SMGSock*)getSockWithX:(float)x andY:(float)y
{
    int row = y/sockHeight;
    int col = x/sockWidth;
    return ((SMGSock*)socks[cols*row+col]);
}

- (void)selectSock:(SMGSock*)sock withX:(float)x andY:(float)y
{
    @synchronized(validSocks) {
        [validSocks removeObject:sock];
    }
    @synchronized(self.socksToDraw) {
        [self.socksToDraw removeObject:sock];
    }
    sock.pos = CGPointMake(x-halfWidth, y-halfHeight);
    self.isSelected = YES;
    self.selectedSock = sock;
    self.selected = SOCK;
}
             
- (void)selectItemAtX:(float)x andY:(float)y
{
    self.item.pos = CGPointMake(x-100, y-100);
    self.isSelected = YES;
    self.selected = ITEM;
}

- (void)matchUsingSockArraySelectedSock:(SMGSock*)selectedSock withOtherSock:(SMGSock*)otherSock
{
    NSMutableArray *socksToFillArray = [NSMutableArray array];
    int sockVal = selectedSock.val;
    points = 2;
    
    @synchronized(validSocks) {
        [validSocks removeObject:otherSock];
    }
    @synchronized(self.socksToDraw) {
        [self.socksToDraw removeObject:otherSock];
    }
    
    selectedSock.visited = YES;
    otherSock.visited = YES;
    
    [socksToFillArray addObject:selectedSock];
    [socksToFillArray addObject:otherSock];
    
    [neighbors addObjectsFromArray:otherSock.neighbors];
    
    while ([neighbors count] > 0) {
        for (SMGSock *sockNode in neighbors) {
            if (!sockNode.visited) {
                sockNode.visited = YES;
                if ([validSocks containsObject:sockNode] && [sockNode matchesSock:otherSock]) {
                    
                    @synchronized(validSocks) {
                        [validSocks removeObject:sockNode];
                    }
                    @synchronized(self.socksToDraw) {
                        [self.socksToDraw removeObject:sockNode];
                    }
                    
                    [socksToFillArray addObject:sockNode];
                    
                    [newNeighbors addObjectsFromArray:sockNode.neighbors];
                    
                    points++;
                }  //end if node matches
            }  //end if node unvisited
        }  //end for nodes in neighbors
        
        [neighbors removeAllObjects];
        
        if ([newNeighbors count] > 0) {
            [neighbors addObjectsFromArray:newNeighbors];
            [newNeighbors removeAllObjects];
        }
    }  //end while neighbors not empty
    
    for (SMGSock *sock in socks)
        sock.visited = NO;
    
    [backgroundQueue addOperationWithBlock:^{[self replenishSocksInArray:socksToFillArray];}];
    
    switch (sockVal) {
        case 11:
            [fireballSpinnerTimer invalidate];
            fireballSpinnerTimer = nil;
            self.itemRot = 0;
            self.item.val = 0;
            self.hasItem = YES;
            self.itemString = @"Bleach";
            self.itemStringHidden = NO;
            [self performSelector:@selector(removeItemString) withObject:nil afterDelay:1.0f];
            
            if (self.sound)
                [((AVAudioPlayer*)self.soundPlayers[0]) play];
            break;
        case 12:
            [fireballSpinnerTimer invalidate];
            fireballSpinnerTimer = nil;
            self.itemRot = 0;
            self.item.val = 1;
            self.hasItem = YES;
            self.itemString = @"Sock Remover";
            self.itemStringHidden = NO;
            [self performSelector:@selector(removeItemString) withObject:nil afterDelay:1.0f];
            
            if (self.sound)
                [((AVAudioPlayer*)self.soundPlayers[5]) play];
            break;
        case 13:
            [fireballSpinnerTimer invalidate];
            fireballSpinnerTimer = nil;
            self.item.val = 2;
            fireballTime = 5;
            self.hasItem = YES;
            fireballSpinnerTimer = [NSTimer scheduledTimerWithTimeInterval:0.075f target:self selector:@selector(spinFireball2) userInfo:nil repeats:YES];
            self.itemString = @"Fireball";
            self.itemStringHidden = NO;
            [self performSelector:@selector(removeItemString) withObject:nil afterDelay:1.0f];
            if (self.sound)
                [((AVAudioPlayer*)self.soundPlayers[3]) play];
            break;
        case 14:
            self.time += points*5;
            break;
        default:
            break;
    }
    
    self.score += points;
}

#pragma mark - State Change Methods

- (void)pause
{
    [timer invalidate];
    [newSockTimer invalidate];
    [fireballCountdownTimer invalidate];
    [fireballSpinnerTimer invalidate];
    timer = nil;
    newSockTimer = nil;
    fireballCountdownTimer = nil;
    fireballSpinnerTimer = nil;
    
    self.gameState = PAUSED;
    
    if (self.isSelected) {
        switch (self.selected) {
            case SOCK:
                [self.selectedSock returnToBase];
                [validSocks addObject:self.selectedSock];
                [self.socksToDraw addObject:self.selectedSock];
                break;
            case ITEM:
                [self.item returnToBase];
                break;
            default:
                break;
        }
        self.isSelected = NO;
    }
    
    [self.socksToDraw removeAllObjects];
}

- (void)reset
{
    diffSocks = 15;
    for (SMGSock *sock in socks) {
        sock.val = arc4random_uniform(diffSocks);
        [sock returnToBase];
    }
    [validSocks removeAllObjects];
    [validSocks addObjectsFromArray:socks];
    [self.socksToDraw removeAllObjects];
    [self.item returnToBase];
    [self.effectsToDraw removeAllObjects];
    self.itemStringHidden = YES;
    self.isSelected = NO;
    self.hasItem = NO;
    self.time = 60;
    self.score = 0;
    self.gameState = READY;
    newSockTime = 15;
}

- (void)resume
{
    timer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(decrementTime) userInfo:nil repeats:YES];
    [self.socksToDraw addObjectsFromArray:validSocks];
    
    if (diffSocks < 24) {
        newSockTimer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(countdownNewSock) userInfo:nil repeats:YES];
    }
    
    //[sockFillerThread start];
    
    self.gameState = RUNNING;
    if (self.hasItem && self.item.val == 2)
        fireballSpinnerTimer = [NSTimer scheduledTimerWithTimeInterval:0.075f target:self selector:@selector(spinFireball2) userInfo:nil repeats:YES];
}

- (void)endGame
{
    [timer invalidate];
    [newSockTimer invalidate];
    [fireballCountdownTimer invalidate];
    [fireballSpinnerTimer invalidate];
    timer = nil;
    newSockTimer = nil;
    fireballCountdownTimer = nil;
    fireballSpinnerTimer = nil;

    self.newHighScore = self.score > self.highScore;
    if (self.newHighScore) {
        [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithInt:self.score] forKey:@"highScoreKey"];
        [[NSUserDefaults standardUserDefaults] synchronize];
        self.highScore = self.score;
    }
    
    [self.socksToDraw removeAllObjects];
    [self.gameEndProtocol endGame];
    self.gameState = GAMEOVER;
}

#pragma mark - Thread Selector Methods

- (void)bleachSocksAroundSock:(SMGSock*)sock
{
    sock.val = 0;
    SMGDrawn *effect = [effects objectAtIndex:sock.index];
    effect.val = 0;
    [self.effectsToDraw addObject:effect];
    
    for (SMGSock *neighborSock in sock.neighbors) {
        if ([validSocks containsObject:neighborSock]) {
            neighborSock.val = 0;
            effect = [effects objectAtIndex:neighborSock.index];
            effect.val = 0;
            [self.effectsToDraw addObject:effect];
        }
    }
    
    for (SMGSock *cornerSock in sock.corners) {
        if ([validSocks containsObject:cornerSock]) {
            cornerSock.val = 0;
            effect = [effects objectAtIndex:cornerSock.index];
            effect.val = 0;
            [self.effectsToDraw addObject:effect];
        }
    }
    
    if (self.sound)
        [((AVAudioPlayer*)self.soundPlayers[2]) play];
    
    [NSThread sleepForTimeInterval:0.25f];
    [self.effectsToDraw removeAllObjects];
}

- (void)burnSock:(SMGSock*)sock
{
    SMGDrawn *effect = [effects objectAtIndex:sock.index];
    effect.val = 2;
    @synchronized(self.effectsToDraw) {
        [self.effectsToDraw addObject:effect];
    }
    if (self.sound)
        [((AVAudioPlayer*)self.soundPlayers[1]) play];
    
        [NSThread sleepForTimeInterval:0.25f];
        @synchronized(self.socksToDraw) {
            [self.socksToDraw removeObject:sock];
        }

        @synchronized(self.effectsToDraw) {
            [self.effectsToDraw removeObject:effect];
        }
        self.score++;
        [self replenishSock:sock];
}

- (void)decrementTime
{
    self.time--;
    
    if (self.time < 1)
        [self endGame];
}


- (void)useSockRemoverWithSockArrayOnSock:(SMGSock*)sock
{
    points = 1;
    
    NSMutableArray *sockToFillArray = [NSMutableArray array];
    SMGDrawn *effect = [effects objectAtIndex:sock.index];
    effect.val = 1;
    [self.effectsToDraw addObject:effect];
    
    [validSocks removeObject:sock];
    [sockToFillArray addObject:sock];
    
    for (SMGSock *otherSock in socks) {
        if ([validSocks containsObject:otherSock] && otherSock.val == sock.val) {
            points++;
            effect = [effects objectAtIndex:otherSock.index];
            effect.val = 1;
            [self.effectsToDraw addObject:effect];
            [validSocks removeObject:otherSock];
            [sockToFillArray addObject:otherSock];
        }
    }
    
    if (self.sound)
        [((AVAudioPlayer*)self.soundPlayers[6]) play];
    
    [NSThread sleepForTimeInterval:0.25f];
    [self.socksToDraw removeObjectsInArray:sockToFillArray];
    [self.effectsToDraw removeAllObjects];
    
    [self replenishSocksInArray:sockToFillArray];
    
    self.score+=points;
}

- (void)countdownNewSock
{
    if (--newSockTime < 1) {
        [newSockTimer invalidate], newSockTimer = nil;
        
        if (++diffSocks < 24) {
            newSockTime = 15;
            newSockTimer = [NSTimer scheduledTimerWithTimeInterval:1.0f target:self selector:@selector(countdownNewSock) userInfo:nil repeats:YES];
        }
        
        else {
            newSockTimer = nil;
        }
    }
}

- (void)replenishSock:(SMGSock*)sock
{
    [NSThread sleepForTimeInterval:0.25f];
    sock.val = arc4random_uniform(diffSocks);
    @synchronized(validSocks) {
        [validSocks addObject:sock];
    }
    @synchronized(self.socksToDraw) {
        [self.socksToDraw addObject:sock];
    }
}

- (void)replenishSocksInArray:(NSArray*)sockArray
{
    [NSThread sleepForTimeInterval:0.25f];
    
    for (SMGSock *sock in sockArray) {
        sock.val = arc4random_uniform(diffSocks);
        @synchronized(validSocks) {
            [validSocks addObject:sock];
        }
        @synchronized(self.socksToDraw) {
            [self.socksToDraw addObject:sock];
        }
    }
}

- (void)countdownFireball2
{
    if (--fireballTime < 1) {
        [fireballCountdownTimer invalidate], fireballCountdownTimer = nil;
        [fireballSpinnerTimer invalidate], fireballSpinnerTimer = nil;
        if (self.hasItem && self.item.val == 2) {
            self.hasItem = NO;
            self.isSelected = NO;
            [self.item returnToBase];
        }
    }
}

- (void)spinFireball2
{
    self.itemRot--;
    
    if (self.itemRot < -360)
        self.itemRot = self.itemRot + 360;
}

- (void)removeItemString
{
    self.itemStringHidden = YES;
}

@end
