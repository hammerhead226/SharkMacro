package org.hammerhead226.sharkmacro.display;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;

public class SharkMath {

	ArrayList<Double> rightPosition = new ArrayList<Double>();
	ArrayList<Double> leftPosition = new ArrayList<Double>();
	Point rightStart = new Point(0, 0);
	Point leftStart = new Point(0, 0);
	double adjustment;
	double w;

	public SharkMath(Point rightStart, Point leftStart, ArrayList<Double> right, ArrayList<Double> left, double w, double adjustment) {
		for (int i = 0; i < right.size() - 1 && i < left.size() - 1; i++) {
			this.rightPosition.add(right.get(i + 1) - right.get(i));
			this.leftPosition.add(left.get(i + 1) - left.get(i));
		}
		this.w = w * 100;
		this.rightStart.x = rightStart.x * 100;
		this.rightStart.y = rightStart.y * 100;
		this.leftStart.x = leftStart.x * 100;
		this.leftStart.y = leftStart.y * 100;
		this.adjustment = adjustment;
	}

	private double calcHeading(Point left, Point right, double left_p, double right_p) {
		double theta_1 = Math.atan2(right.y - left.y, right.x - left.x);
		double theta_2 = Math.acos((left_p - right_p) / 2 / w);

		return theta_1 + theta_2;
	}

	private double adjustedMag(double p) {
		return p * adjustment;
	}

	private ArrayList<Point> calcCoords(Point left, Point right, double left_p, double right_p) {
		double heading = calcHeading(left, right, left_p, right_p);

		Point leftCoords = new Point((int) (100 * (adjustedMag(left_p) * Math.cos(heading)) + left.x),
				(int) (100 * (adjustedMag(left_p) * Math.sin(heading)) + left.y));
		Point rightCoords = new Point((int) (100 * (adjustedMag(right_p) * Math.cos(heading)) + right.x),
				(int) (100 * (adjustedMag(right_p) * Math.sin(heading)) + right.y));
		
		double midpointX = 0.5 * (leftCoords.x + rightCoords.x);
		double midpointY = 0.5 * (leftCoords.y + rightCoords.y);
		
		double leftMidX = leftCoords.x - midpointX;
		double leftMidY = leftCoords.y - midpointY;
		double rightMidX = rightCoords.x - midpointX;
		double rightMidY = rightCoords.y - midpointY;
		
		double deviatedWidth = Math.sqrt(Math.pow(leftCoords.x - rightCoords.x, 2) + Math.pow(leftCoords.y - rightCoords.y, 2));
		
		double fixFactor = w / deviatedWidth;
		
		leftCoords.x = (int) (leftMidX * fixFactor + midpointX);
		leftCoords.y = (int) (leftMidY * fixFactor + midpointY);
		rightCoords.x = (int) (rightMidX * fixFactor + midpointX);
		rightCoords.y = (int) (rightMidY * fixFactor + midpointY);
		
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
