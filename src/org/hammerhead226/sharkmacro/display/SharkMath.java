package org.hammerhead226.sharkmacro.display;

import java.awt.Point;

public class SharkMath {
	
	public static double findRadius(double w, double p_i, double p_o) {
		return (w * p_i) / (p_o - p_i);
	}
	
	public static double findAngle(double w, double p_i, double p_o) {
		return (p_o - p_i) / w;
	}
	
	public static Point findCenter(Point i, Point o, double p_i, double p_o) {
		double w = Math.sqrt(Math.pow(i.x - o.x, 2) + Math.pow(i.y - o.x, 2));
		
		double r = findRadius(w, p_i, p_o);
		
		double y = ((r + w) * (i.y - o.y) + w *o.y) / w;
		double x = ((r + w) * (i.x - o.x) + w *o.x) / w;
		
		return new Point((int) x, (int) y);
	}
}
