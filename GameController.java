package com.game.main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class GameController implements ActionListener {
	private final GameModel model;
	private final GameView view;
	private final Timer timer;

	public GameController(GameModel model, GameView view) {
		this.model = model;
		this.view = view;

		setupKeyBindings();

		this.timer = new Timer(16, this);
	}

	public void startGameLoop() {
		timer.start();
	}

	public void stopGameLoop() {
		timer.stop();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		model.update();
		view.repaint();
	}

	private void setupKeyBindings() {
		InputMap im = view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = view.getActionMap();

		// Left controls (press/release)
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "leftPress");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "leftRelease");

		// Right controls (press/release)
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "rightPress");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "rightRelease");

		// Menu controls
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "startSpace");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "restartR");

		am.put("leftPress", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setLeftPressed(true);
			}
		});
		am.put("leftRelease", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setLeftPressed(false);
			}
		});
		am.put("rightPress", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setRightPressed(true);
			}
		});
		am.put("rightRelease", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setRightPressed(false);
			}
		});
		am.put("startSpace", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!model.isGameStarted()) {
					model.setGameStarted(true);
				}
			}
		});
		am.put("restartR", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (model.isGameOver()) {
					model.initGame();
					model.setGameOver(false);
					model.setGameStarted(true);
				}
			}
		});
	}
}
