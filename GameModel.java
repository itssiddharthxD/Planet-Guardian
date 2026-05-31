package com.game.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameModel {
	public static final int WIDTH = 1200;
	public static final int HEIGHT = 750;
	public static final double ORBIT_RADIUS = 150.0;
	public static final double PLANET_RADIUS = 40.0;
	public static final double SHIELD_SWEEP = Math.toRadians(45.0);
	public static final double GRAVITY_STRENGTH = 15000.0;
	public static final int MAX_ASTEROID_AGE = 600;

	private double playerAngle;
	private boolean leftPressed = false;
	private boolean rightPressed = false;

	private ArrayList<Asteroid> asteroids;
	private ArrayList<Star> stars;

	private int score;
	private int highScore;
	private int planetHealth;

	private boolean gameStarted = false;
	private boolean gameOver = false;

	private int spawnTimer = 0;
	private int baseSpawnInterval = 120; // 2 seconds at 60 FPS

	private int screenShakeTime = 0;
	private double screenShakeIntensity = 0.0;

	private float dashOffset = 0.0f;

	// FPS tracking
	private int frameCount = 0;
	private long lastFpsTime = System.nanoTime();
	private int currentFps = 0;

	private Random random;

	public GameModel() {
		random = new Random();
		highScore = 0;
		initGame();
	}

	public void initGame() {
		playerAngle = -Math.PI / 2; // start at top
		asteroids = new ArrayList<>();
		score = 0;
		planetHealth = 100;
		spawnTimer = 0;
		screenShakeTime = 0;
		dashOffset = 0.0f;

		// Create static background stars
		stars = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			stars.add(new Star(random.nextDouble() * WIDTH, random.nextDouble() * HEIGHT, 1.0 + random.nextDouble() * 2.0,
					30 + random.nextInt(180)));
		}
	}

	public void update() {
		// FPS tracking
		frameCount++;
		long now = System.nanoTime();
		if (now - lastFpsTime >= 1_000_000_000L) {
			currentFps = frameCount;
			frameCount = 0;
			lastFpsTime = now;
		}

		if (!gameStarted || gameOver)
			return;

		// Update player angle
		double rotationSpeed = 0.06;
		if (leftPressed)
			playerAngle -= rotationSpeed;
		if (rightPressed)
			playerAngle += rotationSpeed;

		// Normalize angle to [-PI, PI]
		playerAngle = (playerAngle + Math.PI) % (2 * Math.PI);
		if (playerAngle < 0)
			playerAngle += 2 * Math.PI;
		playerAngle -= Math.PI;

		// Spawn asteroids on a timer
		spawnTimer++;
		int spawnInterval = Math.max(50, baseSpawnInterval - (score / 10));
		if (spawnTimer >= spawnInterval) {
			spawnAsteroid();
			spawnTimer = 0;
		}

		double centerX = WIDTH / 2.0;
		double centerY = HEIGHT / 2.0;

		// Update each asteroid
		Iterator<Asteroid> it = asteroids.iterator();
		while (it.hasNext()) {
			Asteroid ast = it.next();

			if (!ast.deflected) {
				double gx = centerX - ast.x;
				double gy = centerY - ast.y;
				double dist = Math.sqrt(gx * gx + gy * gy);
				if (dist > 10) {
					// Softened inverse-square gravity: creates visible curved trajectories
					double gravity = GRAVITY_STRENGTH / (dist * dist + 200);
					ast.dx += (gx / dist) * gravity;
					ast.dy += (gy / dist) * gravity;
				}
			}

			ast.update();

			// Distance from asteroid to planet center
			double dx = ast.x - centerX;
			double dy = ast.y - centerY;
			double distToCenter = Math.sqrt(dx * dx + dy * dy);

			// --- Planet collision ---
			if (distToCenter <= PLANET_RADIUS + ast.radius) {
				planetHealth -= 20;
				if (planetHealth <= 0) {
					planetHealth = 0;
					gameOver = true;
					if (score > highScore)
						highScore = score;
				}
				screenShakeTime = 10;
				screenShakeIntensity = 8.0;
				it.remove();
				continue;
			}

			// --- Shield collision ---
			if (distToCenter >= ORBIT_RADIUS - 20 && distToCenter <= ORBIT_RADIUS + 20) {
				double astAngle = Math.atan2(ast.y - centerY, ast.x - centerX);
				double angleDiff = normalizeAngle(astAngle - playerAngle);

				if (Math.abs(angleDiff) <= SHIELD_SWEEP / 2.0) {
					// Normal vector pointing outward from center at shield position
					double nx = Math.cos(playerAngle);
					double ny = Math.sin(playerAngle);
					double dot = ast.dx * nx + ast.dy * ny;

					if (dot < 0) { 
						ast.dx = ast.dx - 2 * dot * nx;
						ast.dy = ast.dy - 2 * dot * ny;

						// Boost speed so it escapes the gravity well
						ast.dx *= 1.8;
						ast.dy *= 1.8;

						// Mark as deflected so gravity doesn't pull it back
						ast.deflected = true;

						score += 10;
					}
				}
			}
			
			if (distToCenter > WIDTH || distToCenter > HEIGHT || ast.age > MAX_ASTEROID_AGE) {
				it.remove();
			}
		}

		// Animate the dashed orbit line
		dashOffset += 0.5f;
		if (dashOffset >= 16.0f)
			dashOffset -= 16.0f;

		// Decrement screen shake
		if (screenShakeTime > 0)
			screenShakeTime--;
	}

	/** Normalize angle to [-PI, PI] range. */
	private double normalizeAngle(double angle) {
		while (angle > Math.PI)
			angle -= 2 * Math.PI;
		while (angle < -Math.PI)
			angle += 2 * Math.PI;
		return angle;
	}

	/**
	 * Spawn a new asteroid from a random screen edge, aimed roughly at the center.
	 */
	private void spawnAsteroid() {
		double centerX = WIDTH / 2.0;
		double centerY = HEIGHT / 2.0;

		double spawnX, spawnY;
		int edge = random.nextInt(4);
		double margin = 50.0;

		switch (edge) {
		case 0: // top
			spawnX = random.nextDouble() * WIDTH;
			spawnY = -margin;
			break;
		case 1: // bottom
			spawnX = random.nextDouble() * WIDTH;
			spawnY = HEIGHT + margin;
			break;
		case 2: // left
			spawnX = -margin;
			spawnY = random.nextDouble() * HEIGHT;
			break;
		default: // right
			spawnX = WIDTH + margin;
			spawnY = random.nextDouble() * HEIGHT;
			break;
		}

		// Direction toward center
		double dirX = centerX - spawnX;
		double dirY = centerY - spawnY;
		double dist = Math.sqrt(dirX * dirX + dirY * dirY);
		double ndx = dirX / dist;
		double ndy = dirY / dist;

		// Slow initial speed 
		double speed = 0.6 + random.nextDouble() * 0.6;
		
		double offsetAngle = (random.nextDouble() - 0.5) * 1.2;
		double cos = Math.cos(offsetAngle);
		double sin = Math.sin(offsetAngle);
		double fdx = (ndx * cos - ndy * sin) * speed;
		double fdy = (ndx * sin + ndy * cos) * speed;

		double radius = 8.0 + random.nextDouble() * 10.0;
		asteroids.add(new Asteroid(spawnX, spawnY, fdx, fdy, radius));
	}

	// --- Getters and Setters ---

	public double getPlayerAngle() {
		return playerAngle;
	}

	public void setLeftPressed(boolean val) {
		leftPressed = val;
	}

	public void setRightPressed(boolean val) {
		rightPressed = val;
	}

	public boolean isLeftPressed() {
		return leftPressed;
	}

	public boolean isRightPressed() {
		return rightPressed;
	}

	public ArrayList<Asteroid> getAsteroids() {
		return asteroids;
	}

	public ArrayList<Star> getStars() {
		return stars;
	}

	public int getScore() {
		return score;
	}

	public int getHighScore() {
		return highScore;
	}

	public int getPlanetHealth() {
		return planetHealth;
	}

	public boolean isGameStarted() {
		return gameStarted;
	}

	public void setGameStarted(boolean val) {
		gameStarted = val;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean val) {
		gameOver = val;
	}

	public int getScreenShakeTime() {
		return screenShakeTime;
	}

	public double getScreenShakeIntensity() {
		return screenShakeIntensity;
	}

	public float getDashOffset() {
		return dashOffset;
	}

	public int getCurrentFps() {
		return currentFps;
	}
}
