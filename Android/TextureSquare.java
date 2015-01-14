/*
 * TextureSquare.java
 * Class for a drawable texture square
 * Copyright Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

public class TextureSquare {
	private int mColorHandle, mPositionHandle, textureId;
	private FloatBuffer uvBuffer, vertexBuffer;
	private ShortBuffer drawListBuffer;
	
	private short drawOrder[] = {0, 1, 2, 0, 2, 3};

    /**
     * Constructor
     * @param uvs float[] coordinates on texture
     * @param squareCoords float[] coordinates on screen
     * @param textureId int id of texture
     */
	public TextureSquare(float[] uvs, float[] squareCoords, int textureId) {		
		ByteBuffer bb = ByteBuffer.allocateDirect(uvs.length * 4);
		bb.order(ByteOrder.nativeOrder());
		uvBuffer = bb.asFloatBuffer();
		uvBuffer.put(uvs);
		uvBuffer.position(0);
		
		ByteBuffer bb2 = ByteBuffer.allocateDirect(squareCoords.length * 4);
		bb2.order(ByteOrder.nativeOrder());
		vertexBuffer = bb2.asFloatBuffer();
		vertexBuffer.put(squareCoords);
		vertexBuffer.position(0);
		
		ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		drawListBuffer = dlb.asShortBuffer();
		drawListBuffer.put(drawOrder);
		drawListBuffer.position(0);
		
		this.textureId = textureId;
	}

    /**
     * Draws the texture square using OpenGLES 2 calls
     * @param mvpMatrix float[] matrix to apply to texture
     */
	public void draw(float[] mvpMatrix) {
	    // Add program to OpenGL ES environment
	    GLES20.glUseProgram(riGraphicTools.textureProgram);

	    // get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(riGraphicTools.textureProgram, "vPosition");

	    // Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);

	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, 3,
	                                 GLES20.GL_FLOAT, false,
	                                 0, vertexBuffer);
	    
	    int mTexCoordLoc = GLES20.glGetAttribLocation(riGraphicTools.textureProgram, "a_texCoord");
	    
	    GLES20.glEnableVertexAttribArray ( mTexCoordLoc );
	    
	    GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, uvBuffer);

	    // get handle to fragment shader's vColor member
	    mColorHandle = GLES20.glGetUniformLocation(riGraphicTools.textureProgram, "uMVPMatrix");

	    GLES20.glUniformMatrix4fv(mColorHandle, 1, false, mvpMatrix, 0);

	    int sampleLoc = GLES20.glGetUniformLocation(riGraphicTools.textureProgram, "s_texture");
	    
	    GLES20.glUniform1i(sampleLoc, textureId);
	    
	    GLES20.glEnable(GLES20.GL_BLEND);
	    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	    
	    // Draw the triangle
	    GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
	    		GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
	    GLES20.glDisableVertexAttribArray(mTexCoordLoc);
	}
}
