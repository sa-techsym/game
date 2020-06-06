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

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * A three-dimensional basis for cylinder elements for use as a drawn cylinder elements
 * 		as objects in OpenGL ES 2.0.
 */
public class CylinderElement {
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

	// number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private final int _shaderProgram;

	private final int _vertices = 8 * 2;

	private final float [] tubeCoords = new float[_vertices * COORDS_PER_VERTEX];
	private final short [] drawOrder = {
		 0,  1,  3,  0,  3,  2,  2,  3,  5,  2,  5,  4,
		 4,  5,  7,  4,  7,  6,  6,  7,  9,  6,  9,  8, // !!! + 4 for each element
	 	 8,  9, 11,  8, 11, 10, 10, 11, 13, 10, 13, 12, //		from previous row
	 	12, 13, 15, 12, 15, 14,	14, 15,  1, 14,  1,  0
	 	}; //new short[_indices]; ; //;{ 0, 1, 2, 0, 2, 3 }; // order to draw vertices

	private final FloatBuffer _vertexBuffer;
	private final ShortBuffer _drawListBuffer;

	public CylinderElement() {
		// create empty OpenGL Program
		_shaderProgram = GLES20.glCreateProgram();

		// prepare shaders and OpenGL program
		int vertex_shader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
		GLES20.glAttachShader(_shaderProgram, vertex_shader); // add the vertex shader to program

		int fragment_shader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
		GLES20.glAttachShader(_shaderProgram, fragment_shader); // add the fragment shader to program

		// create OpenGL shader program executables
		GLES20.glLinkProgram(_shaderProgram);

		// initialize buffers for vertices and indices, which in shaders and
		// in draw method are used
		_vertexBuffer = ByteBuffer.allocateDirect(_vertices * COORDS_PER_VERTEX * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		_drawListBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer();
		}

	private float color [] = { 1.0f, 0f, 0f, 1.0f };

	/**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float [] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(_shaderProgram);
 		
		// get handle to vertex shader's vPosition member
		int position_handle = GLES20.glGetAttribLocation(_shaderProgram, "vPosition");
       
		// Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(position_handle);
        
		// Prepare the triangle coordinate data
		// 4 bytes per vertex
		int _vertexStride = COORDS_PER_VERTEX * 4;
		GLES20.glVertexAttribPointer(position_handle, COORDS_PER_VERTEX,GLES20.GL_FLOAT, false, _vertexStride, _vertexBuffer);
        
		// get handle to fragment shader's vColor member
		int _colorHandle = GLES20.glGetUniformLocation(_shaderProgram, "vColor");
        
		// Set color for drawing the triangle
        GLES20.glUniform4fv(_colorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
		int mvp_matrix_handle = GLES20.glGetUniformLocation(_shaderProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mvp_matrix_handle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, _drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(position_handle);
    	}
	}
