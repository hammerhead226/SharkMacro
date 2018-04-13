package org.hammerhead226.sharkmacro.display;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Display extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;
	private static int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private static int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private BufferedImage background;
	private BufferedImage robot;
	private static JButton drawPath;
	ArrayList<Double> rightPosition;
	ArrayList<Double> leftPosition;
	ArrayList<Point> rightCoords;
	ArrayList<Point> leftCoords;
	boolean clickFlag = false;
	String macroLocation;
	String imageLocation;
	SharkMath math;
	boolean pathFlag = false;
	int pathCounter = 0;
	JFrame frame;
	public Display panel;
	Graphics2D g2;
	String robotLocation;

	public Display(String macroLocation, String backgroundLocation, String robotLocation) {

		drawPath = new JButton("Show Path");
		drawPath.setActionCommand("show path");
		drawPath.addActionListener(this);
		drawPath.setVisible(false);

		this.macroLocation = macroLocation;
		this.imageLocation = backgroundLocation;
		this.robotLocation = robotLocation;
		rightPosition = CSVParser.readRightPosition(macroLocation);
		leftPosition = CSVParser.readLeftPosition(macroLocation);

		for (int i = 0; i < rightPosition.size() && i < leftPosition.size(); i++) {
			rightPosition.set(i, rightPosition.get(i) * 6.15 * Math.PI / 4096 * 5.8);
			leftPosition.set(i, leftPosition.get(i) * 6.15 * Math.PI / 4096 * 5.8);
		}

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				repaint();
				if (!clickFlag) {
					math = new SharkMath(new Point(height - e.getY(), width - (e.getX() - 65)),
							new Point(height - e.getY(), width - (e.getX() + 65)), rightPosition, leftPosition, 130,
							0.97);

					rightCoords = math.createCoordList().get(1);
					leftCoords = math.createCoordList().get(0);

					repaint();
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (!clickFlag) {
					clickFlag = true;
					drawPath.setVisible(true);

				} else {
					drawPath.setVisible(false);
					clickFlag = false;
					math = new SharkMath(new Point(e.getY(), width - (e.getX() - 65)),
							new Point(e.getY(), width - (e.getX() + 65)), rightPosition, leftPosition, 100, 0.97);

					rightCoords = math.createCoordList().get(1);
					leftCoords = math.createCoordList().get(0);

					clickFlag = false;
				}
			}
		});

		try {
			background = ImageIO.read(new File(backgroundLocation));
			
			robot = ImageIO.read(new File(robotLocation));
		} catch (IOException e) {

		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(background, -100, -1800, this);
		g2 = (Graphics2D) g;
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		if (math != null) {
			Draw.drawPath(g2, rightCoords, leftCoords);
			if (pathFlag) {
				if (pathCounter < rightCoords.size()) {
					Draw.drawRobot(g2, rightCoords, leftCoords, robot, pathCounter);
				}
			}
		}
	}

	public void drawFrame() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame = new JFrame();
				panel = new Display(macroLocation, imageLocation, robotLocation);
				panel.add(drawPath);
				frame.add(panel);
				frame.setSize(Toolkit.getDefaultToolkit().getScreenSize().width + 100,
						Toolkit.getDefaultToolkit().getScreenSize().height + 100);
				frame.setVisible(true);
				frame.addWindowListener(new WindowAdapter() {
					public void windowClosing(WindowEvent e) {
						System.exit(0);
					}
				});

			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if ("show path".equals(e.getActionCommand())) {

			

			new Thread() {

				public void run() {
					pathFlag = true;

					for (int i = 0; i < rightCoords.size(); i = i + 2) {
						int j = i;
						panel = new Display(macroLocation, imageLocation, robotLocation);
						pathCounter = j;
						try {
							Robot bot = new Robot();
							if(i < 2) {
								bot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + 100, MouseInfo.getPointerInfo().getLocation().y);
							}
							if(i % 4 == 0 ) {
								bot.mouseMove(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y + 1);
							} else {
								bot.mouseMove(MouseInfo.getPointerInfo().getLocation().x, MouseInfo.getPointerInfo().getLocation().y - 1);
							}
						} catch (Exception k) {
						}
						panel.repaint();

						try {
							Thread.sleep(1 	);
						} catch (Exception k) {
						}
					}

				}
			}.start();
		}

	}
}
