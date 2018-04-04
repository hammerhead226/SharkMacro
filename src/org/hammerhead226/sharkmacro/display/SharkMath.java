package org.hammerhead226.sharkmacro.display;

import java.awt.Point;

public class SharkMath {
	
	double w, p_i, p_o, r, theta, imag, omag, heading;
	Point i, o, center;
	
	
	public SharkMath(double w, double p_i, double p_o, Point i, Point o) {
		this.w = w;
		this.p_i = p_i;
		this.p_o = p_o;
		
		this.i = i;
		this.o = o;
		
		findRadius();
		findAngle();
		findInsideMag();
		findOutsideMag();
		findHeading();
	}
	
	private void findRadius() {
		r = (w * p_i) / (p_o - p_i);
	}
	
	private void findAngle() {
		theta = (p_o - p_i) / w;
	}
	
	private void findInsideMag() {
		imag = Math.sqrt(2 * r * r * (1 + Math.cos(theta)));
	}
	
	private void findOutsideMag() {
		omag = Math.sqrt(2 * (r+w) * (r+w) * (1 + Math.cos(theta)));
	}
	
	private void findHeading() {
		double alpha = Math.atan2(i.y - o.y, i.x - o.x);
		
		heading = alpha + theta / 2 - Math.PI / 2;
	}
	
	public Point getFinalPositionOfInside() {
		double x = imag * Math.cos(heading) + i.x;
		double y = imag * Math.sin(heading) + i.y;
		
		return new Point((int) x, (int) y);
	}
	
	public Point getFinalPositionOfOutside() {
		double x = omag * Math.cos(heading) + o.x;
		double y = omag * Math.sin(heading) + o.y;
		
		return new Point((int) x, (int) y);
	}
}
