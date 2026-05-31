package com.game.main;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.geom.*;

public class GameView extends JPanel {
	private GameModel model;

	public GameView(GameModel model) {
		this.model = model;
		setPreferredSize(new Dimension(GameModel.WIDTH, GameModel.HEIGHT));
		setBackground(new Color(15, 15, 30));
		setFocusable(true);
		setDoubleBuffered(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		AffineTransform baseTransform = g2d.getTransform();

		// Screen shake effect
		if (model.getScreenShakeTime() > 0 && model.isGameStarted()) {
			double sx = (Math.random() - 0.5) * model.getScreenShakeIntensity();
			double sy = (Math.random() - 0.5) * model.getScreenShakeIntensity();
			g2d.translate(sx, sy);
		}

		int centerX = GameModel.WIDTH / 2;
		int centerY = GameModel.HEIGHT / 2;

		// Draw background stars
		for (Star star : model.getStars()) {
			star.draw(g2d);
		}

		// Draw orbital path 
		float[] dash = { 8.0f, 8.0f };
		g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash,
				model.getDashOffset()));
		g2d.setColor(new Color(100, 100, 120));
		int orbitR = (int) GameModel.ORBIT_RADIUS;
		g2d.drawOval(centerX - orbitR, centerY - orbitR, orbitR * 2, orbitR * 2);

		// Draw planet 
		int planetR = (int) GameModel.PLANET_RADIUS;
		g2d.setColor(new Color(50, 100, 220));
		g2d.fillOval(centerX - planetR, centerY - planetR, planetR * 2, planetR * 2);
		g2d.setColor(new Color(80, 140, 255));
		g2d.setStroke(new BasicStroke(2));
		g2d.drawOval(centerX - planetR, centerY - planetR, planetR * 2, planetR * 2);

		// Draw shield
		double shieldAngleDeg = Math.toDegrees(-model.getPlayerAngle()) - Math.toDegrees(GameModel.SHIELD_SWEEP) / 2.0;
		Arc2D.Double shieldArc = new Arc2D.Double(centerX - orbitR, centerY - orbitR, orbitR * 2, orbitR * 2,
				shieldAngleDeg, Math.toDegrees(GameModel.SHIELD_SWEEP), Arc2D.OPEN);
		g2d.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g2d.setColor(new Color(0, 220, 200));
		g2d.draw(shieldArc);

		// Draw asteroids with trajectory trails
		for (Asteroid ast : model.getAsteroids()) {
			for (int i = 0; i < ast.trailCount; i++) {
				
				float fade = 1.0f - (float) i / Asteroid.TRAIL_SIZE;
				int alpha = (int) (fade * 80);
				int dotSize = Math.max(1, (int) (ast.radius * 0.4 * fade));

				if (ast.deflected) {
					g2d.setColor(new Color(80, 160, 200, alpha));
				} else {
					g2d.setColor(new Color(160, 130, 100, alpha));
				}
				g2d.fillOval((int) (ast.trailX[i] - dotSize / 2), (int) (ast.trailY[i] - dotSize / 2), dotSize,
						dotSize);
			}

			// Draw the asteroid
			int r = (int) ast.radius;
			if (ast.deflected) {
				g2d.setColor(new Color(100, 140, 160));
			} else {
				g2d.setColor(new Color(140, 120, 100));
			}
			g2d.fillOval((int) (ast.x - r), (int) (ast.y - r), r * 2, r * 2);

			g2d.setColor(new Color(180, 160, 140));
			g2d.setStroke(new BasicStroke(1));
			g2d.drawOval((int) (ast.x - r), (int) (ast.y - r), r * 2, r * 2);
		}

		// Restore transform for HUD 
		g2d.setTransform(baseTransform);

		// Draw HUD
		drawHUD(g2d);

		// Overlay screens
		if (!model.isGameStarted()) {
			drawStartScreen(g2d);
		} else if (model.isGameOver()) {
			drawGameOverScreen(g2d);
		}
	}

	private void drawHUD(Graphics2D g2d) {
		g2d.setFont(new Font("Consolas", Font.BOLD, 16));
		g2d.setColor(Color.WHITE);
		g2d.drawString("Score: " + model.getScore(), 20, 30);
		g2d.drawString("High Score: " + model.getHighScore(), 20, 55);

		// Health bar
		g2d.drawString("Planet Health:", 20, 85);
		int barX = 20, barY = 95, barW = 150, barH = 14;

		// Background track
		g2d.setColor(new Color(60, 60, 60));
		g2d.fillRect(barX, barY, barW, barH);

		// Fill based on health
		int fillW = (int) ((model.getPlanetHealth() / 100.0) * barW);
		if (model.getPlanetHealth() > 60) {
			g2d.setColor(new Color(50, 200, 80));
		} else if (model.getPlanetHealth() > 30) {
			g2d.setColor(new Color(220, 180, 50));
		} else {
			g2d.setColor(new Color(220, 50, 50));
		}
		g2d.fillRect(barX, barY, fillW, barH);

		// Border
		g2d.setColor(Color.WHITE);
		g2d.setStroke(new BasicStroke(1));
		g2d.drawRect(barX, barY, barW, barH);

		// FPS counter
		g2d.setFont(new Font("Consolas", Font.PLAIN, 12));
		g2d.setColor(new Color(150, 150, 150));
		String fps = "FPS: " + model.getCurrentFps();
		g2d.drawString(fps, GameModel.WIDTH - 70, 20);
	}

	private void drawStartScreen(Graphics2D g2d) {
		// Dark overlay
		g2d.setColor(new Color(0, 0, 0, 180));
		g2d.fillRect(0, 0, GameModel.WIDTH, GameModel.HEIGHT);

		// Title
		g2d.setFont(new Font("Consolas", Font.BOLD, 36));
		g2d.setColor(new Color(0, 220, 200));
		String title = "PLANET GUARDIAN";
		FontMetrics fm = g2d.getFontMetrics();
		g2d.drawString(title, (GameModel.WIDTH - fm.stringWidth(title)) / 2, 280);

		// Instructions
		g2d.setFont(new Font("Consolas", Font.PLAIN, 16));
		g2d.setColor(Color.LIGHT_GRAY);
		String desc = "Defend the planet from incoming asteroids!";
		String ctrl = "LEFT / RIGHT arrows to move shield";
		fm = g2d.getFontMetrics();
		g2d.drawString(desc, (GameModel.WIDTH - fm.stringWidth(desc)) / 2, 340);
		g2d.drawString(ctrl, (GameModel.WIDTH - fm.stringWidth(ctrl)) / 2, 370);

		// Blinking prompt
		if ((System.currentTimeMillis() / 500) % 2 == 0) {
			g2d.setColor(Color.WHITE);
		} else {
			g2d.setColor(new Color(0, 220, 200));
		}
		g2d.setFont(new Font("Consolas", Font.BOLD, 18));
		String prompt = "Press SPACE to start";
		fm = g2d.getFontMetrics();
		g2d.drawString(prompt, (GameModel.WIDTH - fm.stringWidth(prompt)) / 2, 450);
	}

	private void drawGameOverScreen(Graphics2D g2d) {
		// Dark overlay
		g2d.setColor(new Color(0, 0, 0, 180));
		g2d.fillRect(0, 0, GameModel.WIDTH, GameModel.HEIGHT);

		// Title
		g2d.setFont(new Font("Consolas", Font.BOLD, 36));
		g2d.setColor(new Color(220, 50, 50));
		String title = "GAME OVER";
		FontMetrics fm = g2d.getFontMetrics();
		g2d.drawString(title, (GameModel.WIDTH - fm.stringWidth(title)) / 2, 280);

		// Scores
		g2d.setFont(new Font("Consolas", Font.PLAIN, 18));
		g2d.setColor(Color.LIGHT_GRAY);
		String sc = "Score: " + model.getScore();
		String hi = "High Score: " + model.getHighScore();
		fm = g2d.getFontMetrics();
		g2d.drawString(sc, (GameModel.WIDTH - fm.stringWidth(sc)) / 2, 340);
		g2d.drawString(hi, (GameModel.WIDTH - fm.stringWidth(hi)) / 2, 370);

		// Blinking restart prompt
		if ((System.currentTimeMillis() / 500) % 2 == 0) {
			g2d.setColor(Color.WHITE);
		} else {
			g2d.setColor(new Color(0, 220, 200));
		}
		g2d.setFont(new Font("Consolas", Font.BOLD, 18));
		String prompt = "Press R to restart";
		fm = g2d.getFontMetrics();
		g2d.drawString(prompt, (GameModel.WIDTH - fm.stringWidth(prompt)) / 2, 430);
	}
}
