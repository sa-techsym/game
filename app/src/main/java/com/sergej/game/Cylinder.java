package com.sergej.game;

public class Cylinder {
	private Tube _tube = new Tube(0f, 2 * (float) Math.PI, 0.8f, 0f);
	float [] color1 = {1.0f, 1.0f, 1.0f, 1.0f};
	float [] color2 = {0.0f, 1.0f, 0.0f, 0.0f};//ne60fw float
	//private Circle _topcircle = new Circle(color1, 1); 
	//private Circle _bottomcircle = new Circle(color2, -1);
	private Ring _ring1 = new Ring(1, 0.2f, 1f);
	private Ring _ring2 = new Ring(-1, 0.2f, 1f);
	
	public Cylinder() {
	}

public void draw(float[] mvpMatrix) {
	//_topcircle.draw(mvpMatrix);
	//_bottomcircle.draw(mvpMatrix);
	_tube.draw(mvpMatrix);
	//_ring1.draw(mvpMatrix);
	//_ring2.draw(mvpMatrix);
	}
}
