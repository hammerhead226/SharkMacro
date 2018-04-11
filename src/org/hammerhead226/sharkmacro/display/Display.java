package org.hammerhead226.sharkmacro.display;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
	private static int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private static int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private BufferedImage image;
	ArrayList<Double> rightPosition;
	ArrayList<Double> leftPosition;
	ArrayList<Point> rightCoords;
	ArrayList<Point> leftCoords;
	boolean clickFlag = false;
	String fileLocation;
	SharkMath math;

	public Display(String fileLocation) {

		this.fileLocation = fileLocation;
		rightPosition = CSVParser.readRightPosition(fileLocation);
		leftPosition = CSVParser.readLeftPosition(fileLocation);

		for (int i = 0; i < rightPosition.size() && i < leftPosition.size(); i++) {
			rightPosition.set(i, rightPosition.get(i) * 6.15 * Math.PI / 4096 * 5.8);
			leftPosition.set(i, leftPosition.get(i) * 6.15 * Math.PI / 4096 * 5.8);
		}

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!clickFlag) {
					math = new SharkMath(new Point(height - e.getY(), width - (e.getX() - 65)), new Point(height - e.getY(), width - (e.getX() + 65)),
							rightPosition, leftPosition, 130, 0.97);

					rightCoords = math.createCoordList().get(1);
					leftCoords = math.createCoordList().get(0);

					repaint();
				}
			}
		});
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(!clickFlag) {
				clickFlag = true;
				} else {
					clickFlag = false;
					math = new SharkMath(new Point(e.getY(), width - (e.getX() - 65)), new Point(e.getY(), width - (e.getX() + 65)),
							rightPosition, leftPosition, 100, 0.97);

					rightCoords = math.createCoordList().get(1);
					leftCoords = math.createCoordList().get(0);

					repaint();
				}
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
		g.drawImage(image, -100, -1800, this);
		Graphics2D g2 = (Graphics2D) g;
		g.setFont(new Font("Arial", Font.PLAIN, 14));
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
