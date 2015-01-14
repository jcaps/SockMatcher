//
//  SMGTexturedSquare.h
//  SockMatcher
//
//  Created by Joseph Caplan on 9/17/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <GLKit/GLKit.h>

typedef struct {
    GLKVector2 geometryVertex;
    GLKVector2 textureVertex;
} TexturedVertex;

typedef struct {
    TexturedVertex bl;
    TexturedVertex br;
    TexturedVertex tl;
    TexturedVertex tr;
} TexturedQuad;

@interface SMGTexturedSquare : NSObject

- (id)initWithEffect:(GLKBaseEffect *)effect andTextureInfo:(GLKTextureInfo *)textureInfo;
- (id)initWithEffect:(GLKBaseEffect *)effect textureInfo:(GLKTextureInfo *)textureInfo texturedQuad:(TexturedQuad)texturedQuad;
- (void)render;
- (void)destroy;

@end
