//
//  SMGTexturedSquare.m
//  SockMatcher
//
//  Created by Joseph Caplan on 9/17/14.
//  Copyright (c) 2014 Joseph Caplan. All rights reserved.
//

#import "SMGTexturedSquare.h"

@interface SMGTexturedSquare()

@property (strong) GLKBaseEffect *effect;
@property (assign) TexturedQuad quad;
@property (strong) GLKTextureInfo *textureInfo;

@end

@implementation SMGTexturedSquare

@synthesize effect = _effect;
@synthesize quad = _quad;
@synthesize textureInfo = _textureInfo;

//- (id)initWithEffect:(GLKBaseEffect *)effect andTextureInfo:(GLKTextureInfo *)textureInfo
- (id)initWithEffect:(GLKBaseEffect *)effect andTextureInfo:(GLKTextureInfo *)textureInfo
{
    self = [super init];
    
    if (self) {
        self.effect = effect;
        self.textureInfo = textureInfo;
        
        TexturedQuad newQuad;
        newQuad.bl.geometryVertex = GLKVector2Make(0, 0);
        newQuad.br.geometryVertex = GLKVector2Make(textureInfo.width, 0);
        newQuad.tl.geometryVertex = GLKVector2Make(0, textureInfo.height);
        newQuad.tr.geometryVertex = GLKVector2Make(textureInfo.width, textureInfo.height);
        
        newQuad.bl.textureVertex = GLKVector2Make(0, 0);
        newQuad.br.textureVertex = GLKVector2Make(1, 0);
        newQuad.tl.textureVertex = GLKVector2Make(0, 1);
        newQuad.tr.textureVertex = GLKVector2Make(1, 1);
        self.quad = newQuad;
    }
    
    return self;
}

- (id)initWithEffect:(GLKBaseEffect *)effect textureInfo:(GLKTextureInfo *)textureInfo texturedQuad:(TexturedQuad)texturedQuad
{
    self = [super init];
    
    if (self) {
        self.effect = effect;
        self.textureInfo = textureInfo;
        self.quad = texturedQuad;
    }
    
    return self;
}

- (void)render
{
    self.effect.texture2d0.name = self.textureInfo.name;
    self.effect.texture2d0.enabled = YES;
    
    [self.effect prepareToDraw];
    
    glEnableVertexAttribArray(GLKVertexAttribPosition);
    glEnableVertexAttribArray(GLKVertexAttribTexCoord0);
    
    long offset = (long)&_quad;
    glVertexAttribPointer(GLKVertexAttribPosition, 2, GL_FLOAT, GL_FALSE, sizeof(TexturedVertex), (void *)(offset + offsetof(TexturedVertex, geometryVertex)));
    glVertexAttribPointer(GLKVertexAttribTexCoord0, 2, GL_FLOAT, GL_FALSE, sizeof(TexturedVertex), (void *)(offset + offsetof(TexturedVertex, textureVertex)));
    
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}

- (void)destroy
{
    self.effect = nil;
    self.textureInfo = nil;
}

@end
