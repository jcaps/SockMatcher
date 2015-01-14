/*
 * riGraphicTools.java
 * Contains information for texture shaders and function to load shaders
 * Copyright Joseph M. Caplan Dec. 28, 2014
 */
package com.penguin.sockmatcher;

import android.opengl.GLES20;

public class riGraphicTools {
	public static int textureProgram;

	public static final String vs_Image =
			"uniform mat4 uMVPMatrix;" +
					"attribute vec4 vPosition;" +
					"attribute vec2 a_texCoord;" +
					"varying vec2 v_texCoord;" +
					"void main() {" +
					"  gl_Position = uMVPMatrix * vPosition;" +
					"  v_texCoord = a_texCoord;" +
					"}";

	public static final String fs_Image =
			"precision mediump float;" +
					"varying vec2 v_texCoord;" +
					"uniform sampler2D s_texture;" +
					"void main() {" +
					"  gl_FragColor = texture2D( s_texture, v_texCoord );" +
					"}";

    /**
     * Loads and compiles shader
     * @param type int type of shader
     * @param shaderCode int shader code
     * @return int compiled shader
     */
	public static int loadShader(int type, String shaderCode) {
		int shader = GLES20.glCreateShader(type);
		
		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);
		
		return shader;
	}
}
