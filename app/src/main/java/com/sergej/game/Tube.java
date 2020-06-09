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
import java.util.Arrays;

import android.opengl.GLES20;

/**
 * A three-dimensional tube as part of cylinder for use as a drawn object in OpenGL ES 2.0.
 */
public class Tube extends CylinderElement {
	public Tube(float start_angle, float end_angle, float radius, float height) {
		int slices = (int) ((end_angle - start_angle) / DEGREES_PER_SLICE);

		float[] coords = new float[2 * slices * COORDS_PER_VERTEX];

		// initialize _coords array from base class as vertex byte buffer for shape coordinates
		for (int i = 0; i < slices; i++) {
			int offset = 2 * COORDS_PER_VERTEX * i;
			coords[offset + 0] = coords[offset + 3] = (float) Math.cos(start_angle + i * DEGREES_PER_SLICE);
			coords[offset + 1] = coords[offset + 4] = (float) Math.sin(start_angle + i * DEGREES_PER_SLICE);
			coords[offset + 2] = height;
			coords[offset + 5] = -height;
			}

		// initialize buffers for vertices and indices, which in shaders and
		// in draw method are used
		_vertexBuffer = ByteBuffer.allocateDirect(2 * slices * COORDS_PER_VERTEX * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		_vertexBuffer.put(coords).position(0);

		calculateDrawOrder(slices);
		}
	}