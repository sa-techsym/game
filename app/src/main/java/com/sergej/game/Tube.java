/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sergej.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;

/**
 * A three-dimensional tube as part of cylinder for use as a drawn object in OpenGL ES 2.0.
 */
public class Tube {
	private final String vertexShaderCode =
			// This matrix member variable provides a hook to manipulate
			// the coordinates of the objects that use this vertex shader
			"uniform mat4 uMVPMatrix;" +
					"attribute vec4 vPosition;" +
					"void main() {" +
					// The matrix must be included as a modifier of gl_Position.
					// Note that the uMVPMatrix factor *must be first* in order
					// for the matrix multiplication product to be correct.
					"  gl_Position = uMVPMatrix * vPosition;" +
					"}";

	private final String fragmentShaderCode =
			"precision mediump float;" +
					"uniform vec4 vColor;" +
					"void main() {" +
					"  gl_FragColor = vColor;" +
					"}";

	private final FloatBuffer vertexBuffer;
	private final ShortBuffer drawListBuffer;

	// number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private final int mProgram, vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	
	private final int _vertices = 8 * 2, _indices = _vertices + 2, _height = 1;

	private final float [] tubeCoords = new float[_vertices * COORDS_PER_VERTEX];
	private final short [] drawOrder = {
		 0,  1,  3,  0,  3,  2,  2,  3,  5,  2,  5,  4,
		 4,  5,  7,  4,  7,  6,  6,  7,  9,  6,  9,  8,
	 	 8,  9, 11,  8, 11, 10, 10, 11, 13, 10, 13, 12,
	 	12, 13, 15, 12, 15, 14,	14, 15,  1, 14,  1,  0
	 	}; //new short[_indices]; ; //;{ 0, 1, 2, 0, 2, 3 }; // order to draw vertices

	public Tube() {
		// initialize vertex byte buffer for shape coordinates
		for (int i = 0; i < 8; i++) {
			int j = 6 * i;
			double theta = 2 * Math.PI * i / (8);
			tubeCoords[j + 0] = (float) Math.cos(theta);
			tubeCoords[j + 1] = (float) Math.sin(theta);
			tubeCoords[j + 2] = _height;
			tubeCoords[j + 3] = (float) Math.cos(theta);
			tubeCoords[j + 4] = (float) Math.sin(theta);
			tubeCoords[j + 5] = -_height;
			}

		vertexBuffer = ByteBuffer.allocateDirect(_vertices * COORDS_PER_VERTEX * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		vertexBuffer.put(tubeCoords).position(0);
        
		/*for (int i = 0; i < _indices; i ++)
			drawOrder[i] = (short) (i % _vertices);*/
		drawListBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
		drawListBuffer.put(drawOrder).position(0);

		// create empty OpenGL Program
		mProgram = GLES20.glCreateProgram();

		// prepare shaders and OpenGL program
		int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader to program

		int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program

		// create OpenGL program executables
		GLES20.glLinkProgram(mProgram);
		}

	private float color[] = { 1.0f, 0f, 0f, 1.0f };
	private int mPositionHandle, mColorHandle, mMVPMatrixHandle;

	/**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
 		
		// get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
       
		// Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        
		// Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        
		// get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        
		// Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

}
