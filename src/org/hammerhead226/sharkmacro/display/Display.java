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
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Display extends JPanel implements ActionListener {
	
	private static final long serialVersionUID = 1L;
	
	private int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private static int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	
	private BufferedImage background;
	private BufferedImage robot;
	
	private JButton drawPath;
	private JButton one;
	private JButton two;
	private JButton three;
	
	private int pathCounter = 0;
	private double scale = 6.15 * Math.PI / 4096 * 5.8;
	
	private ArrayList<Double> rightPosition;
	private ArrayList<Double> leftPosition;

	private ArrayList<Double> rightPosition1;
	private ArrayList<Double> leftPosition1;

	private ArrayList<Double> rightPosition2;
	private ArrayList<Double> leftPosition2;

	private ArrayList<Point> rightCoords;
	private ArrayList<Point> leftCoords;
	
	private List<String> macroLocation;
	
	private boolean clickFlag = false;
	private boolean pathFlag = false;
	
	private SharkMath math;
	
	private String imageLocation;
	private String robotLocation;
	
	private Display panel;
	
	JFrame frame;
	
	Graphics2D g2;
	

	public Display(List<String> macroLocation, String backgroundLocation, String robotLocation) {

		drawPath = new JButton("Show Path");
		drawPath.setActionCommand("show path");
		drawPath.addActionListener(this);
		drawPath.setEnabled(false);

		one = new JButton("Path One");
		one.setActionCommand("one");
		one.addActionListener(this);

		three = new JButton("Path Three");
		three.setActionCommand("three");
		three.addActionListener(this);

		two = new JButton("Path Two");
		two.setActionCommand("two");
		two.addActionListener(this);

		this.macroLocation = macroLocation;
		this.imageLocation = backgroundLocation;
		this.robotLocation = robotLocation;

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				
				repaint();
				
				if (!clickFlag) {
					math = new SharkMath(new Point(height - e.getY(), width - (e.getX() - 95)),
							new Point(height - e.getY(), width - (e.getX() + 35)), rightPosition, leftPosition, 130,
							0.97);

					rightCoords = math.createCoordList().get(1);
					leftCoords = math.createCoordList().get(0);
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				
				if (!clickFlag) {
					clickFlag = true;
					drawPath.setEnabled(true);

				} else {
					drawPath.setEnabled(false);
					clickFlag = false;
					math = new SharkMath(new Point(e.getY(), width - (e.getX() - 95)),
							new Point(e.getY(), width - (e.getX() + 35)), rightPosition, leftPosition, 100, 0.97);

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
		g.drawImage(background, -150, -1775, this);
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
				panel.add(one);
				panel.add(two);
				panel.add(three);
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

					for (int i = 0; i < rightCoords.size(); i = i + 3) {
						int j = i;
						panel = new Display(macroLocation, imageLocation, robotLocation);
						pathCounter = j;
						try {
							Robot bot = new Robot();
							if (i < 2) {
								bot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + 1000,
										MouseInfo.getPointerInfo().getLocation().y);
							}
							if (i % 6 == 0) {
								bot.mouseMove(MouseInfo.getPointerInfo().getLocation().x,
										MouseInfo.getPointerInfo().getLocation().y + 1);
							} else {
								bot.mouseMove(MouseInfo.getPointerInfo().getLocation().x,
										MouseInfo.getPointerInfo().getLocation().y - 1);
							}

							if (i > rightCoords.size() - 2) {
								bot.mouseMove(750, 40);
							}
						} catch (Exception k) {
						}
						panel.repaint();

						try {
							Thread.sleep(1);
						} catch (Exception k) {
						}
					}

				}
			}.start();
		}

		if ("one".equals(e.getActionCommand())) {

			String[] split = macroLocation.get(0).split("&");

			if (split.length >= 1) {
				rightPosition = CSVParser.readRightPosition(split[0]);
				leftPosition = CSVParser.readLeftPosition(split[0]);
			}

			if (split.length >= 2) {
				rightPosition1 = CSVParser.readRightPosition(split[1]);
				leftPosition1 = CSVParser.readLeftPosition(split[1]);
			}

			if (split.length >= 3) {
				rightPosition2 = CSVParser.readRightPosition(split[2]);
				leftPosition2 = CSVParser.readLeftPosition(split[2]);
			}
			if (rightPosition != null && leftPosition != null) {
				for (int i = 0; i < rightPosition.size() && i < leftPosition.size(); i++) {
					rightPosition.set(i, rightPosition.get(i) * scale);
					leftPosition.set(i, leftPosition.get(i) * scale);
				}
			}

			if (rightPosition1 != null && leftPosition1 != null) {
				for (int i = 0; i < rightPosition1.size() && i < leftPosition1.size(); i++) {
					rightPosition1.set(i, (rightPosition1.get(i) * scale)
							+ rightPosition.get(rightPosition.size() - 1));
					leftPosition1.set(i, (leftPosition1.get(i) * scale)
							+ rightPosition.get(leftPosition.size() - 1));
				}
				rightPosition.addAll(rightPosition1);
				leftPosition.addAll(leftPosition1);
			}

			if (rightPosition2 != null && leftPosition2 != null) {
				for (int i = 0; i < rightPosition2.size() && i < leftPosition2.size(); i++) {
					rightPosition2.set(i, rightPosition2.get(i) * scale
							+ rightPosition.get(rightPosition.size() - 1));
					leftPosition2.set(i, leftPosition2.get(i) * scale
							+ leftPosition.get(leftPosition.size() - 1));
				}

				rightPosition.addAll(rightPosition2);
				leftPosition.addAll(leftPosition2);
			}

		}
		if ("three".equals(e.getActionCommand())) {

			String[] split = macroLocation.get(2).split("&");

			if (split.length >= 1) {
				rightPosition = CSVParser.readRightPosition(split[0]);
				leftPosition = CSVParser.readLeftPosition(split[0]);
			}

			if (split.length >= 2) {
				rightPosition1 = CSVParser.readRightPosition(split[1]);
				leftPosition1 = CSVParser.readLeftPosition(split[1]);
			}

			if (split.length >= 3) {
				rightPosition2 = CSVParser.readRightPosition(split[2]);
				leftPosition2 = CSVParser.readLeftPosition(split[2]);
			}
			if (rightPosition != null && leftPosition != null) {
				for (int i = 0; i < rightPosition.size() && i < leftPosition.size(); i++) {
					rightPosition.set(i, rightPosition.get(i) * scale);
					leftPosition.set(i, leftPosition.get(i) * scale);
				}
			}

			if (rightPosition1 != null && leftPosition1 != null) {
				for (int i = 0; i < rightPosition1.size() && i < leftPosition1.size(); i++) {
					rightPosition1.set(i, (rightPosition1.get(i) * scale)
							+ rightPosition.get(rightPosition.size() - 1));
					leftPosition1.set(i, (leftPosition1.get(i) * scale)
							+ rightPosition.get(leftPosition.size() - 1));
				}
				rightPosition.addAll(rightPosition1);
				leftPosition.addAll(leftPosition1);
			}

			if (rightPosition2 != null && leftPosition2 != null) {
				for (int i = 0; i < rightPosition2.size() && i < leftPosition2.size(); i++) {
					rightPosition2.set(i, rightPosition2.get(i) * scale
							+ rightPosition.get(rightPosition.size() - 1));
					leftPosition2.set(i, leftPosition2.get(i) * scale
							+ leftPosition.get(leftPosition.size() - 1));
				}

				rightPosition.addAll(rightPosition2);
				leftPosition.addAll(leftPosition2);
			}
		}
		if ("two".equals(e.getActionCommand())) {
			String[] split = macroLocation.get(1).split("&");

			if (split.length >= 1) {
				rightPosition = CSVParser.readRightPosition(split[0]);
				leftPosition = CSVParser.readLeftPosition(split[0]);
			}

			if (split.length >= 2) {
				rightPosition1 = CSVParser.readRightPosition(split[1]);
				leftPosition1 = CSVParser.readLeftPosition(split[1]);
			}

			if (split.length >= 3) {
				rightPosition2 = CSVParser.readRightPosition(split[2]);
				leftPosition2 = CSVParser.readLeftPosition(split[2]);
			}
			if (rightPosition != null && leftPosition != null) {
				for (int i = 0; i < rightPosition.size() && i < leftPosition.size(); i++) {
					rightPosition.set(i, rightPosition.get(i) * scale);
					leftPosition.set(i, leftPosition.get(i) * scale);
				}
			}

			if (rightPosition1 != null && leftPosition1 != null) {
				for (int i = 0; i < rightPosition1.size() && i < leftPosition1.size(); i++) {
					rightPosition1.set(i, (rightPosition1.get(i) * scale)
							+ rightPosition.get(rightPosition.size() - 1));
					leftPosition1.set(i, (leftPosition1.get(i) * scale)
							+ rightPosition.get(leftPosition.size() - 1));
				}
				rightPosition.addAll(rightPosition1);
				leftPosition.addAll(leftPosition1);
			}

			if (rightPosition2 != null && leftPosition2 != null) {
				for (int i = 0; i < rightPosition2.size() && i < leftPosition2.size(); i++) {
					rightPosition2.set(i, rightPosition2.get(i) * scale
							+ rightPosition.get(rightPosition.size() - 1));
					leftPosition2.set(i, leftPosition2.get(i) * scale
							+ leftPosition.get(leftPosition.size() - 1));
				}

				rightPosition.addAll(rightPosition2);
				leftPosition.addAll(leftPosition2);
			}
		}

	}
}
