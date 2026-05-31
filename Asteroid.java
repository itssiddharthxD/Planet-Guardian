package com.game.main;

import java.awt.geom.Rectangle2D;

public class Asteroid {
	public double x, y; // position
	public double dx, dy; // velocity
	public double radius; // size
	public boolean deflected; // true after shield bounce (ignores gravity)
	public int age; // frames alive (removed after too long to prevent orbiting forever)
	private Rectangle2D.Double bounds;

	// Trail to show the curved path
	public static final int TRAIL_SIZE = 30;
	public double[] trailX = new double[TRAIL_SIZE];
	public double[] trailY = new double[TRAIL_SIZE];
	public int trailCount = 0;

	public Asteroid(double x, double y, double dx, double dy, double radius) {
		this.x = x;
		this.y = y;
		this.dx = dx;
		this.dy = dy;
		this.radius = radius;
		this.deflected = false;
		this.age = 0;
		this.bounds = new Rectangle2D.Double(x - radius, y - radius, radius * 2, radius * 2);
	}

	public void update() {
		// Save current position to trail before moving
		for (int i = TRAIL_SIZE - 1; i > 0; i--) {
			trailX[i] = trailX[i - 1];
			trailY[i] = trailY[i - 1];
		}
		trailX[0] = x;
		trailY[0] = y;
		if (trailCount < TRAIL_SIZE)
			trailCount++;

		// Move
		x += 2 * dx;
		y += 2 * dy;
		age++;
		bounds.setFrame(x - radius, y - radius, radius * 2, radius * 2);
	}

	public Rectangle2D.Double getBounds() {
		return bounds;
	}
}
