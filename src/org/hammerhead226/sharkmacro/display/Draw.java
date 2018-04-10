package org.hammerhead226.sharkmacro.display;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;

public class Draw {

	public static void drawPath(Graphics2D g, ArrayList<Point> right, ArrayList<Point> left) {

		int j = 0;
		
		int height = Toolkit.getDefaultToolkit().getScreenSize().height;

		for (int i = 0; i < right.size() - 1; i++) {

			g.setColor(Color.RED);
			g.fillOval(right.get(i).y / 120, height - right.get(i).x / 120, 2, 2);
			g.drawLine((right.get(i).y - 1) / 120, height - ((right.get(i).x - 1) / 120), (right.get(i + 1).y - 1) / 120, height - ((right.get(i + 1).x - 1) / 120));
			g.setColor(Color.BLUE);
			g.fillOval(left.get(i).y / 120, height - left.get(i).x / 120, 2, 2);
			g.drawLine((left.get(i).y - 1) / 120, height - ((left.get(i).x - 1) / 120), (left.get(i + 1).y - 1) / 120, height - ((left.get(i + 1).x - 1) / 120));

		}
	}
}
