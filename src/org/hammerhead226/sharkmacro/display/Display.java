package org.hammerhead226.sharkmacro.display;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
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
	private final long serialVersionUID = 1L;
	private BufferedImage image;
	ArrayList<Point> rightCoords;
	ArrayList<Point> leftCoords;
	private String fileLocation;

	public Display(ArrayList<Point> rightCoords, ArrayList<Point> leftCoords) {

		this.rightCoords = rightCoords;
		this.leftCoords = leftCoords;

		try {
			image = ImageIO.read(new File("field.jpg"));
		} catch (IOException e) {

		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		//g.drawImage(image, 0, 0, this);
		Graphics2D g2 = (Graphics2D) g;
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		Draw.drawPath(g2, rightCoords, leftCoords);
	}

	public void drawFrame() {

		JFrame frame = new JFrame();
		Display panel = new Display(rightCoords, leftCoords);
		frame.add(panel);
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}
}
