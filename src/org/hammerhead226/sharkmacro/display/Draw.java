package org.hammerhead226.sharkmacro.display;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;

public class Draw {
	
	public static void drawPath(Graphics2D g, ArrayList<Point> right, ArrayList<Point> left) {
		for(int i = 0; i < right.size() - 1; i++) {
			
			if(i != 0) {
				//g.drawLine(right.get(i).x, right.get(i).y, right.get(i+1).x, right.get(i+1).y);
			}
			g.drawOval(right.get(i).x, Toolkit.getDefaultToolkit().getScreenSize().height - right.get(i).y, 2, 2);
			g.drawOval(left.get(i).x, Toolkit.getDefaultToolkit().getScreenSize().height - left.get(i).y, 2, 2);
		}
	}
}
