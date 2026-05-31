package com.game.main;

import java.awt.Color;
import java.awt.Graphics2D;

public class Star {
	public double x, y;
	public double size;
	public int brightness;

	public Star(double x, double y, double size, int brightness) {
		this.x = x;
		this.y = y;
		this.size = size;
		this.brightness = brightness;
	}

	public void draw(Graphics2D g2d) {
		g2d.setColor(new Color(255, 255, 255, brightness));
		g2d.fillOval((int) (x - size / 2), (int) (y - size / 2), (int) size, (int) size);
	}
}
