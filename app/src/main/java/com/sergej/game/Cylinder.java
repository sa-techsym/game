package com.sergej.game;

import android.text.BoringLayout;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OpenGLPipe {
	// 	By this class we describe a pipe, so the variables that are declared in it should apply
	// 	only to the pipe. Information about how this pipe is cut is beyond the scope
	// 	of this set of variables.
	static private float _ANGLE_FROM = 0f, _ANGLE_TO  = 3 * (float) Math.PI / 2;

	static private int _MAX_SLICES = 8;

	private float _inner_radius, _outer_radius, _height;

	private interface VertexInitializationRule {
		abstract public float [] apply();
		}

	static private int _SLICES = (_ANGLE_TO - _ANGLE_FROM) / (/*degrees per one slice*/ 2 * Math.PI / _MAX_SLICES), _COORDS_PER_VERTEX = 3;

	class TubeVerticesRule implements VertexInitializationRule {
		private float _radius; TubeVerticesRule(float radius) { _radius = radius; }

		@Override public float [] apply() {
			// calculate coordinates array for shape coordinates
			float [] coords = new float[2 * _SLICES * _COORDS_PER_VERTEX];

			// calculating theta step for every slice
			float theta = (_ANGLE_TO - _ANGLE_FROM) / _MAX_SLICES;

			for (int i = 0; i < _SLICES; i++) {
				int offset = 2 * _COORDS_PER_VERTEX * i;
				coords[offset + 0] = coords[offset + 3] = _radius * (float) Math.cos(_ANGLE_FROM + i * theta);
				coords[offset + 1] = coords[offset + 4] = _radius * (float) Math.sin(_ANGLE_FROM + i * theta);
				coords[offset + 2] = _height;
				coords[offset + 5] =-_height;
				}

			return coords;
			}
		}

	private OpenGLPipeElement[] _elements = new OpenGLPipeElement[2];

	public Pipe(float inner_radius, float outer_radius, float height) {

		VertexInitializationRule [] rules = {
				TubeVerticesRule.withParamater   .of("a", _height, "b", iiner_radius), Map.of("a", _height, "b", iiner_radius) };

		int i = 0;
		for (VertexInitializationRule rule : rules)
		 	_elements[i++] = new OpenGLPipeElement().initializeVertexBuffer(rule.apply());
		}

	public void draw(float [] mvpMatrix) {
		for (OpenGLPipeElement item : _elements)
			item.draw(mvpMatrix);
		}
	}
