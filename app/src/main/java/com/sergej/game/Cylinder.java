package com.sergej.game;

import android.text.BoringLayout;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@FunctionalInterface
interface CylinderElementsBuildRule {
	static int _COORDS_PER_VERTEX = 3, _MAX_SLICES = 8;
	abstract public float [] apply();
	}

class TubeBuildRule implements CylinderElementsBuildRule {
	private float _radius, _angle_from;
	private int _direction;

	public void parameters(float radius, float angle_from, int direction) {
		_radius = radius;
		_angle_from = angle_from;
		_direction = direction;
	}

	@Override public float [] apply() {
		// calculate coordinates array for shape coordinates
		float [] coords = new float[2 * Cylinder._slices * _COORDS_PER_VERTEX];

		for (int i = 0; i < Cylinder._slices; i++) {
			int offset = 2 * _COORDS_PER_VERTEX * i;
			coords[offset + 0] = coords[offset + 3] = _radius * (float) Math.cos(_angle_from + _direction * i * 2 * (float)Math.PI / _MAX_SLICES);
			coords[offset + 1] = coords[offset + 4] = _radius * (float) Math.sin(_angle_from + _direction * i * 2 * (float)Math.PI / _MAX_SLICES);
			coords[offset + 2] = Cylinder._height;
			coords[offset + 5] =-Cylinder._height;
			}

		return coords;
		}
	}

public class Cylinder {
	static private final int _MAX_SLICES = 8;
	static private final float _DEGREES_PER_SLICE = 2 * (float) Math.PI / _MAX_SLICES;
	private static final int _COORDS_PER_VERTEX = 3;

	private CylinderElement[] _elements;

	private float _innerRadius = 0.7f, _outerRadius = 0.9f, _angleFrom = 0;

	static public float _height = 1.0f;

	static public int _slices = 5;

	public Cylinder(float inner_radius, float outer_radius, float height) {
		//int slices = (int) (end_angle - start_angle) / _MAX_SLICES;

		TubeBuildRule tube_rule = new TubeBuildRule();

		tube_rule.parameters(outer_radius,_angleFrom, 1);
		_elements[0] = new CylinderElement(_slices);
		_elements[0].initializeVertexBuffer(tube_rule);

		tube_rule.parameters(inner_radius, _angleFrom  +_slices * _DEGREES_PER_SLICE, -1);
		(_elements[0] = new CylinderElement(_slices)).initializeVertexBuffer(tube_rule);
	}

	public void draw(float [] mvpMatrix) {
		for (CylinderElement item : _elements)
			item.draw(mvpMatrix);
		}
	}
