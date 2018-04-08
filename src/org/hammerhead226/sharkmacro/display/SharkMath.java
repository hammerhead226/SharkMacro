package org.hammerhead226.sharkmacro.display;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

public class SharkMath {

	ArrayList<Double> rightPosition = new ArrayList<Double>();
	ArrayList<Double> leftPosition = new ArrayList<Double>();
	Point rightStart;
	Point leftStart;
	double w;

	public SharkMath(Point rightStart, Point leftStart, ArrayList<Double> right, ArrayList<Double> left, double w) {
		for (int i = 0; i < right.size() - 1 && i < left.size() - 1; i++) {
			this.rightPosition.add(right.get(i + 1) - right.get(i));
			this.leftPosition.add(left.get(i + 1) - left.get(i));
		}
		this.w = w;
		this.rightStart = rightStart;
		this.leftStart = leftStart;
	}

	private double calcHeading(Point left, Point right, double left_p, double right_p) {
		double theta_1 = Math.atan2(right.y - left.y, right.x - left.x);
		double theta_2 = Math.acos((left_p - right_p) / 2 / w);

		return theta_1 + theta_2;
	}

	private double adjustedMag(double p) {
		return p * 0.87;
	}

	private ArrayList<Point> calcCoords(Point left, Point right, double left_p, double right_p) {
		double heading = calcHeading(left, right, left_p, right_p);

		Point leftCoords = new Point((int) (1.001 * (adjustedMag(left_p) * Math.cos(heading) + left.x) ),
				(int) ((adjustedMag(left_p) * Math.sin(heading) + left.y)));
		Point rightCoords = new Point((int) (1.001 * (adjustedMag(right_p) * Math.cos(heading) + right.x)),
				(int) ((adjustedMag(right_p) * Math.sin(heading) + right.y)));

		ArrayList<Point> temp = new ArrayList<Point>(2);
		temp.add(leftCoords);
		temp.add(rightCoords);

		return temp;
	}

	public ArrayList<ArrayList<Point>> createCoordList() {
		ArrayList<Point> tempRight = new ArrayList<Point>(rightPosition.size());
		ArrayList<Point> tempLeft = new ArrayList<Point>(leftPosition.size());
		tempRight.add(rightStart);
		tempLeft.add(leftStart);

		for (int i = 0; i < rightPosition.size() && i < leftPosition.size(); i++) {
			tempLeft.add(
					(calcCoords(tempLeft.get(i), tempRight.get(i), leftPosition.get(i), rightPosition.get(i)).get(0)));
			tempRight.add(
					(calcCoords(tempLeft.get(i), tempRight.get(i), leftPosition.get(i), rightPosition.get(i)).get(1)));
		}

		ArrayList<ArrayList<Point>> temp = new ArrayList<ArrayList<Point>>(Arrays.asList(tempLeft, tempRight));
		return temp;
	}

}
