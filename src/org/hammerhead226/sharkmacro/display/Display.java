package org.hammerhead226.sharkmacro.display;

import java.awt.Color;
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
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Display extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1L;

	private static int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	private static int width = Toolkit.getDefaultToolkit().getScreenSize().width;
	private int pathCounter = 0;

	private BufferedImage background;
	private BufferedImage robot;

	private static JButton drawPath;
	private static JButton select;
	private static JButton addMacro;
	private static JButton deselect;

	private static JButton startPath;
	private static JButton stopPath;
	private static JButton toggleMode;

	private static JTextField path1;
	private static JTextField path2;
	private static JTextField path3;

	private static JComboBox pathChooser;

	private ArrayList<Double> rightPosition;
	private ArrayList<Double> leftPosition;

	private ArrayList<Double> rightPosition1;
	private ArrayList<Double> leftPosition1;

	private ArrayList<Double> rightPosition2;
	private ArrayList<Double> leftPosition2;

	private ArrayList<Point> rightCoords;
	private ArrayList<Point> leftCoords;

	private ArrayList<Point> pathCoords;

	private Point currentPosition;

	private boolean clickFlag = false;
	private boolean pathFlag = false;
	private boolean mode = false;
	private boolean pathStarted = false;

	private SharkMath math;

	private List<String> macroLocation;

	private String imageLocation;
	private String robotLocation;
	private String message;

	private JFrame frame;

	public Display panel;

	private Graphics2D g2;

	private double scale = 6.15 * Math.PI / 4096 * 5.8;

	public Display(List<String> macroLocation, String backgroundLocation, String robotLocation) {
		pathCoords = new ArrayList<Point>();

		path1 = new JTextField(20);
		path2 = new JTextField(20);
		path3 = new JTextField(20);

		pathChooser = new JComboBox((String[]) macroLocation.toArray());
		pathChooser.setEditable(true);

		path1.setText(macroLocation.get(0));
		path2.setText(macroLocation.get(1));
		path3.setText(macroLocation.get(2));

		toggleMode = new JButton("Toggle Mode");
		toggleMode.setActionCommand("toggle");
		toggleMode.addActionListener(this);

		addMacro = new JButton("Add Macro");
		addMacro.setActionCommand("add");
		addMacro.addActionListener(this);
		addMacro.setEnabled(true);

		drawPath = new JButton("Show Path");
		drawPath.setActionCommand("show path");
		drawPath.addActionListener(this);
		drawPath.setEnabled(false);

		startPath = new JButton("Start Path");
		startPath.setActionCommand("start");
		startPath.addActionListener(this);
		startPath.setVisible(false);

		stopPath = new JButton("Stop Path");
		stopPath.setActionCommand("stop");
		stopPath.addActionListener(this);
		stopPath.setVisible(false);
		
		deselect = new JButton("Deselect");
		deselect.setActionCommand("deselect");
		deselect.addActionListener(this);

		select = new JButton("Select");
		select.setActionCommand("select");
		select.addActionListener(this);

		this.macroLocation = macroLocation;
		this.imageLocation = backgroundLocation;
		this.robotLocation = robotLocation;

		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {

				if (!mode) {
					if (!clickFlag) {
						if (rightPosition != null & leftPosition != null) {
							math = new SharkMath(new Point(e.getX() * 3, height - (e.getY() * 3 - 1095)),
									new Point(e.getX() * 3, height - (e.getY() * 3 - 965)), rightPosition, leftPosition,
									130, 0.97);

							rightCoords = math.createCoordList().get(1);
							leftCoords = math.createCoordList().get(0);
						}
					}

				} else {

				}

				repaint();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {

				if (!mode) {
					if (!clickFlag) {
						if (math != null) {
							clickFlag = true;
							drawPath.setEnabled(true);
						}

					} else {
						drawPath.setEnabled(false);
						clickFlag = false;
						if (rightPosition != null && leftPosition != null) {
							math = new SharkMath(new Point(e.getX() * 3, height - (e.getY() * 3 - 1095)),
									new Point(e.getX() * 3, height - (e.getY() * 3 - 965)), rightPosition, leftPosition,
									100, 0.97);

							rightCoords = math.createCoordList().get(1);
							leftCoords = math.createCoordList().get(0);
						}

						clickFlag = false;
					}
				} else {
					pathCoords.add(e.getPoint());
				}

				repaint();
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
		g.drawImage(background, 0, 0, this);
		g2 = (Graphics2D) g;
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		if (!mode) {
			if (math != null) {
				Draw.drawPath(g2, rightCoords, leftCoords);
				if (pathFlag) {
					if (pathCounter < rightCoords.size()) {
						Draw.drawRobot(g2, rightCoords, leftCoords, robot, pathCounter);
					}
				}
			}
		}
		
		if(message != null) {
			g2.setColor(Color.BLACK);
			g2.fillRect(100, 100, 100 + message.length() + 10, 20);
			g2.setColor(Color.WHITE);
			g2.setFont(new Font("Serif", Font.BOLD, 14));
			g2.drawString(message, 100, 110);
		}
		g2.setColor(Color.GRAY);
		g2.fillRect(0, height - 150, width, 150);
	}

	public void drawFrame() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame = new JFrame();
				panel = new Display(macroLocation, imageLocation, robotLocation);
				panel.setLayout(null);

				drawPath.setBounds(20, height - 120, width / 5 - 40, 50);
				panel.add(drawPath);

				startPath.setBounds(20, height - 120, width / 5 - 40, 50);
				panel.add(startPath);

				stopPath.setBounds(20, height - 120, width / 5 - 40, 50);
				panel.add(stopPath);

				addMacro.setBounds(width / 5, height - 120, width / 5 - 80, 50);
				panel.add(addMacro);

				pathChooser.setBounds(2 * width / 5 - 60, height - 120, width / 5 + 20, 50);
				panel.add(pathChooser);

				select.setBounds(3 * width / 5 - 20, height - 120, width / 5 - 80, 50);
				panel.add(select);
				
				deselect.setBounds(4 * width / 5 - 80, height - 120, width / 5 - 80, 50);
				panel.add(deselect);

				toggleMode.setBounds(width - 150, height - 120, 130, 50);
				panel.add(toggleMode);

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

					for (int i = 0; i < rightCoords.size(); i = i + 1) {
						int j = i;
						panel = new Display(macroLocation, imageLocation, robotLocation);
						pathCounter = j;
						try {
							Robot bot = new Robot();
							if (i < 2) {
								bot.mouseMove(MouseInfo.getPointerInfo().getLocation().x + 1000,
										MouseInfo.getPointerInfo().getLocation().y);
							}
							if (i % 2 == 0) {
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

		if ("select".equals(e.getActionCommand())) {

			String[] split = ((String) pathChooser.getSelectedItem()).split("&");

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
					rightPosition1.set(i,
							(rightPosition1.get(i) * scale) + rightPosition.get(rightPosition.size() - 1));
					leftPosition1.set(i, (leftPosition1.get(i) * scale) + rightPosition.get(leftPosition.size() - 1));
				}
				rightPosition.addAll(rightPosition1);
				leftPosition.addAll(leftPosition1);
			}

			if (rightPosition2 != null && leftPosition2 != null) {
				for (int i = 0; i < rightPosition2.size() && i < leftPosition2.size(); i++) {
					rightPosition2.set(i, rightPosition2.get(i) * scale + rightPosition.get(rightPosition.size() - 1));
					leftPosition2.set(i, leftPosition2.get(i) * scale + leftPosition.get(leftPosition.size() - 1));
				}

				rightPosition.addAll(rightPosition2);
				leftPosition.addAll(leftPosition2);
			}

			math = new SharkMath(
					new Point(MouseInfo.getPointerInfo().getLocation().x * 3,
							(MouseInfo.getPointerInfo().getLocation().y * 3 - 1095)),
					new Point(MouseInfo.getPointerInfo().getLocation().x * 3,
							(MouseInfo.getPointerInfo().getLocation().y * 3 - 965)),
					rightPosition, leftPosition, 130, 0.97);

			rightCoords = math.createCoordList().get(1);
			leftCoords = math.createCoordList().get(0);

			clickFlag = false;
			this.repaint();
		}

		if ("add".equals(e.getActionCommand())) {
			pathChooser.addItem((String) pathChooser.getSelectedItem());
		}
		
		if("deselct".equals(e.getActionCommand())) {
			rightCoords = new ArrayList<Point>();
			leftCoords = new ArrayList<Point>();
			
			this.repaint();
		}

		if ("toggle".equals(e.getActionCommand())) {

			if (!pathFlag) {
				if (mode) {
					mode = false;
					drawPath.setVisible(true);
					addMacro.setVisible(true);
					pathChooser.setVisible(true);
					select.setVisible(true);
					deselect.setVisible(true);

					startPath.setVisible(false);
					stopPath.setVisible(false);
				} else {
					mode = true;
					drawPath.setVisible(false);
					addMacro.setVisible(false);
					pathChooser.setVisible(false);
					select.setVisible(false);
					deselect.setVisible(false);
					startPath.setVisible(true);
				}
			} else {
				message = "Path still running";
				this.repaint();
				
			}
			
		}

		if ("start".equals(e.getActionCommand())) {
			pathFlag = true;
			startPath.setVisible(false);
			stopPath.setVisible(true);
			
			message = "Started";
		}

		if ("stop".equals(e.getActionCommand())) {
			pathFlag = false;
			stopPath.setVisible(false);
			startPath.setVisible(true);
			
			message = "Stopped";
		}

	}
}
