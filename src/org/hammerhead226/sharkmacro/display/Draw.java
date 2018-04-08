package org.hammerhead226.sharkmacro.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;

public class Draw {

	public static void drawPath(Graphics2D g, ArrayList<Point> right, ArrayList<Point> left) {

		int j = 0;

		for (int i = 0; i < right.size() - 1; i++) {

			g.setColor(Color.RED);
			g.fillOval(right.get(i).x, right.get(i).y, 2, 2);
			g.drawLine(right.get(i).x - 1, (right.get(i).y - 1), right.get(i + 1).x - 1, (right.get(i + 1).y - 1));
			g.setColor(Color.BLUE);
			g.fillOval(left.get(i).x, left.get(i).y, 2, 2);
			g.drawLine(left.get(i).x - 1, (left.get(i).y - 1), left.get(i + 1).x - 1, (left.get(i + 1).y - 1));

		}
	}
}
