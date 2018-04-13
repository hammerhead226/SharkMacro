package org.hammerhead226.sharkmacro.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Draw {

	static int height = Toolkit.getDefaultToolkit().getScreenSize().height;
	static int width = Toolkit.getDefaultToolkit().getScreenSize().width;

	public static void drawPath(Graphics2D g, ArrayList<Point> right, ArrayList<Point> left) {

		int j = 0;

		for (int i = 0; i < right.size() - 1; i++) {

			g.setColor(Color.RED);
			g.fillOval(width - (right.get(i).y / 120), height - right.get(i).x / 120, 2, 2);
			g.drawLine(width - ((right.get(i).y - 1) / 120), height - ((right.get(i).x - 1) / 120),
					width - ((right.get(i + 1).y - 1) / 120), height - ((right.get(i + 1).x - 1) / 120));
			g.setColor(Color.BLUE);
			g.fillOval(width - (left.get(i).y / 120), height - left.get(i).x / 120, 2, 2);
			g.drawLine(width - ((left.get(i).y - 1) / 120), height - ((left.get(i).x - 1) / 120),
					width - ((left.get(i + 1).y - 1) / 120), height - ((left.get(i + 1).x - 1) / 120));

		}
	}

	public static void drawRobot(Graphics2D g, ArrayList<Point> right, ArrayList<Point> left, BufferedImage robot,
			int point) {

		g.setColor(Color.BLACK);
		g.drawLine(width - (left.get(point).y / 120), height - (left.get(point).x / 120),
				width - (right.get(point).y / 120), height - (right.get(point).x / 120));

	}
}
