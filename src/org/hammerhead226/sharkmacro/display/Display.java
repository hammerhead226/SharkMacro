package org.hammerhead226.sharkmacro.display;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
	ArrayList<Double> rightPosition;
	ArrayList<Double> leftPosition;
	ArrayList<Point> rightCoords;
	ArrayList<Point> leftCoords;
	String fileLocation;
	SharkMath math;

	public Display(String fileLocation) {

		this.fileLocation = fileLocation;
		rightPosition = CSVParser.readRightPosition(fileLocation);
		leftPosition = CSVParser.readLeftPosition(fileLocation);

		for (int i = 0; i < rightPosition.size() && i < leftPosition.size(); i++) {
			rightPosition.set(i, rightPosition.get(i) * 6.15 * Math.PI / 4096 * 6);
			leftPosition.set(i, leftPosition.get(i) * 6.15 * Math.PI / 4096 * 6);
		}

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				math = new SharkMath(
						new Point(e.getX(), e.getY() + 65),
						new Point(e.getX(), e.getY() - 65),
						rightPosition, leftPosition, 100);

				rightCoords = math.createCoordList().get(1);
				leftCoords = math.createCoordList().get(0);

				System.out.println(rightCoords);
				System.out.println(leftCoords);
				repaint();
			}
		});

		try {
			image = ImageIO.read(new File("field.jpg"));
		} catch (IOException e) {

		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		// g.drawImage(image, -2000, -300, this);
		Graphics2D g2 = (Graphics2D) g;
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		System.out.println("repainted");
		if (math != null) {
			Draw.drawPath(g2, rightCoords, leftCoords);
		}
	}

	public void drawFrame() {

		JFrame frame = new JFrame();
		Display panel = new Display(fileLocation);
		frame.add(panel);
		frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width,
				Toolkit.getDefaultToolkit().getScreenSize().height);
		frame.setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

	}

}
