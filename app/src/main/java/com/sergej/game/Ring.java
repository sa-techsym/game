package com.sergej.game;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import android.util.*;

public class Ring {
	private final String _vertexShaderCode =
		// This matrix member variable provides a hook to manipulate
		// the coordinates of the objects that use this vertex shader
		"uniform mat4 uMVPMatrix;" +
		"attribute vec4 vPosition;" +
		"void main() {" +
		// The matrix must be included as a modifier of gl_Position.
		// Note that the uMVPMatrix factor *must be first* in order
		// for the matrix multiplication product to be correct.
		"gl_Position = uMVPMatrix * vPosition;" +
		"}";

    private final String _fragmentShaderCode =
		"precision mediump float;" +
		"uniform vec4 vColor;" +
		"void main() {" +
		"gl_FragColor = vColor;" +
		"}";
		
    private int _shaderProgram;

    private float _center_x, _center_y, _radius;
	
	private int _vertices = 2 * 8, _height = 1;
	
	private static final int COORDS_PER_VERTEX = 3;
	private final float [] _ring_coords = new float[_vertices * COORDS_PER_VERTEX];

    private FloatBuffer _vertexBuffer;
    private ShortBuffer _drawListBuffer;

    private final short [] drawOrder = {
		 0,  1,  3,  0,  3,  2,
		 2,  3,  5,  2,  5,  4,
		 4,  5,  7,  4,  7,  6,
		 6,  7,  9,  6,  9,  8,
		 8,  9, 11,  8, 11, 10,
	 	10, 11, 13, 10, 13, 12,
	 	12, 13, 15, 12, 15, 14,
		14, 15,  1, 14,  1,  0
	};

    public Ring(int height, float inner_radius, float outer_radius) {
		//initialize vertex byte buffer for shape coordinates
		for (int i = 0; i < 8; i++) { 
			int offset = 2 * COORDS_PER_VERTEX * i;
			double theta = 2 * Math.PI * i / 8;
			_ring_coords[offset + 0] = outer_radius * (float) Math.cos(theta); 
			_ring_coords[offset + 1] = outer_radius * (float) Math.sin(theta); 
			_ring_coords[offset + 3] = inner_radius * (float) Math.cos(theta); 
			_ring_coords[offset + 4] = inner_radius * (float) Math.sin(theta); 
			_ring_coords[offset + 5] = _ring_coords[offset + 2] = height;
	 		}

		_vertexBuffer = ByteBuffer.allocateDirect(_vertices * COORDS_PER_VERTEX * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		_vertexBuffer.put(_ring_coords).position(0);

		_drawListBuffer = ByteBuffer.allocateDirect(drawOrder.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer(); 
		_drawListBuffer.put(drawOrder).position(0);

        // prepare shaders and OpenGL program
        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, _vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, _fragmentShaderCode);

        _shaderProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(_shaderProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(_shaderProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(_shaderProgram);                  // create OpenGL program executables
    	}

	private int mColorHandle, _positionHandle, _mvpMatrixHandle;
	private final int _vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	private float color[] = { 1.0f, 1.0f, 0f, 1.0f };
	
	public void draw(float[] mvpMatrix) {
        // Add program to OpenGL environment
        GLES20.glUseProgram(_shaderProgram);

		// get handle to vertex shader's vPosition member
        _positionHandle = GLES20.glGetAttribLocation(_shaderProgram, "vPosition");

		// Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(_positionHandle);

		// Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(_positionHandle, COORDS_PER_VERTEX,GLES20.GL_FLOAT, false, _vertexStride, _vertexBuffer);

		// get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(_shaderProgram, "vColor");

		// Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        _mvpMatrixHandle = GLES20.glGetUniformLocation(_shaderProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(_mvpMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, _drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(_positionHandle);
		}
	}
        
