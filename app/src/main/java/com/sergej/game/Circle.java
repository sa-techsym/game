package com.sergej.game;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import android.util.*;

public class Circle {

    private float[] mColor;
    private int mProgram;

    private float mCenterX;
    private float mCenterY;
    private float mRadius;
	
	private int _vertices = 8 + 1, _indices = _vertices + 1;// _height = 1;
	private final float [] tubeCoords = new float[_vertices * COORDS_PER_VERTEX];
	//private final short [] drawOrder = new short[_indices]; ; //= new float;//(_indices); //;{ 0, 1, 2, 0, 2, 3 }; // order to draw vertices
	

    private static final int COORDS_PER_VERTEX = 3;

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;

    private final short [] drawOrder = { 
		0, 2, 1, 0, 3, 2, 
		0, 4, 3, 0, 5, 4,
		0, 6, 5, 0, 7, 6,
		0, 8, 7, 0, 1, 8 
		};

	private int mMVPMatrixHandle; // order to draw vertices

    public Circle(float[] color, int _height) {
        mColor = color;
        String vertexShaderSource = "" +
			"uniform mat4 uMVPMatrix;" +
			"attribute vec4 vPosition;" +
			"void main() {" +
			// The matrix must be included as a modifier of gl_Position.
			// Note that the uMVPMatrix factor *must be first* in order
			// for the matrix multiplication product to be correct.
			"  gl_Position = uMVPMatrix * vPosition;" +
			"}";
				
		String fragmentShaderSource =
			"precision mediump float;" +
			"uniform vec4 vColor;" +
			"void main() {" +
			"  gl_FragColor = vColor;" +
			"}";

        int vertexShader = compileVertexShader(vertexShaderSource);
        int fragmentShader = compileFragmentShader(fragmentShaderSource);
        mProgram = linkProgram(vertexShader, fragmentShader);
        /*if (BuildConfig.DEBUG) {
            validateProgram(mProgram);
        }*/
		
		tubeCoords[0] = mCenterX;
		tubeCoords[1] = mCenterY;
		tubeCoords[2] = _height;
		
		//initialize vertex byte buffer for shape coordinates
		for (int i = 1; i < _vertices; i ++) { 
			
		int j = i * 3;
		double theta = 2 * Math.PI * (i-1) / ((_vertices-1));
			tubeCoords[j + 0] = (float) Math.cos(theta); 
			tubeCoords[j + 1] = (float) Math.sin(theta); 
			tubeCoords[j + 2] = _height;
	 	}

		mVertexBuffer = ByteBuffer.allocateDirect(_vertices * COORDS_PER_VERTEX * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		mVertexBuffer.put(tubeCoords).position(0);
			
		mDrawListBuffer = ByteBuffer.allocateDirect(2* drawOrder.length).order(ByteOrder.nativeOrder()).asShortBuffer(); 
		mDrawListBuffer.put(drawOrder).position(0);
    }

    private static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        final int programObjectId = GLES20.glCreateProgram();
        if (programObjectId == 0) {
            return 0;
        }

        GLES20.glAttachShader(programObjectId, vertexShaderId);
        GLES20.glAttachShader(programObjectId, fragmentShaderId);

        GLES20.glLinkProgram(programObjectId);

        final int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] == 0) {
            // If it failed, delete the program object. glDeleteProgram(programObjectId);
            GLES20.glDeleteProgram(programObjectId);
            return 0;
        }
        return programObjectId;
    }

    private static boolean validateProgram(int programObjectId) {
        GLES20.glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        GLES20.glGetProgramiv(programObjectId, GLES20.GL_VALIDATE_STATUS, validateStatus, 0);
        return validateStatus[0] != 0;
    }

    private static int compileVertexShader(String shaderCode) {
        return compileShader(GLES20.GL_VERTEX_SHADER, shaderCode);
    }

    private static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        final int shaderObjectId = GLES20.glCreateShader(type);
        if (shaderObjectId == 0) {
            return 0;
        }
        GLES20.glShaderSource(shaderObjectId, shaderCode);
        GLES20.glCompileShader(shaderObjectId);

        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shaderObjectId);
            return 0;
        }
        return shaderObjectId;
    }
	
	private int mColorHandle;

    public void draw(float [] mvpMatrix) {
		
		//Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
		
		int vertexStride = COORDS_PER_VERTEX * 4;

        // Prepare the triangle coordinate data
       GLES20.glVertexAttribPointer(
			mPositionHandle, COORDS_PER_VERTEX,
			GLES20.GL_FLOAT, false,
			vertexStride, mVertexBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MyGLRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        MyGLRenderer.checkGlError("glUniformMatrix4fv");

        // Draw the square
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
	}

    public float getCenterX() {
        return mCenterX;
    }

    public void setCenterX(float centerX) {
        mCenterX = centerX;
    }

    public float getCenterY() {
        return mCenterY;
    }

    public void setCenterY(float centerY) {
        mCenterY = centerY;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
    }
}
