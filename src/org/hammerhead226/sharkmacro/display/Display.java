package org.hammerhead226.sharkmacro.display;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Display extends JPanel {
	private static final long serialVersionUID = 1L;
	private static BufferedImage image;
	private static ArrayList<Integer> leftPosition;
	private static ArrayList<Integer> rightPosition;

	public Display(String fileLocation) {

		leftPosition = CSVParser.readLeftPosition(fileLocation);
		rightPosition = CSVParser.readRightPosition(fileLocation);

		try {
			image = ImageIO.read(new File("field.jpg"));
		} catch (IOException e) {

		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
		Graphics2D g2 = (Graphics2D) g;
		g.setFont(new Font("Arial", Font.PLAIN, 14));
	}

	public static void drawFrame(String fileLocation) {

		JFrame frame = new JFrame();
		Display panel = new Display(fileLocation);
		frame.add(panel);
		frame.setSize(image.getWidth(), image.getHeight());
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}
}
