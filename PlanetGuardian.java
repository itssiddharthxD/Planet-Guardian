package com.game.main;

import javax.swing.*;

public class PlanetGuardian extends JFrame {

	public PlanetGuardian() {
		setTitle("Orbital Guardian");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		GameModel model = new GameModel();
		GameView view = new GameView(model);
		GameController controller = new GameController(model, view);

		add(view);
		pack();

		setLocationRelativeTo(null);

		controller.startGameLoop();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			PlanetGuardian game = new PlanetGuardian();
			game.setVisible(true);
		});
	}
}
