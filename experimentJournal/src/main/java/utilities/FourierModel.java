package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FourierModel {
	private double a0;
	private double a1;
	private double b1;
	private double a2;
	private double b2;
	private double a3;
	private double b3;
	private double a4;
	private double b4;
	private double a5;
	private double b5;
	private double a6;
	private double b6;
	private double a7;
	private double b7;
	private double a8;
	private double b8;
	private double w;

	public FourierModel(double a0, double a1, double b1, double a2, double b2, double a3, double b3, double a4,
			double b4, double a5, double b5, double a6, double b6, double a7, double b7, double a8, double b8,
			double w) {
		this.a0 = a0;
		this.a1 = a1;
		this.b1 = b1;
		this.a2 = a2;
		this.b2 = b2;
		this.a3 = a3;
		this.b3 = b3;
		this.a4 = a4;
		this.b4 = b4;
		this.a5 = a5;
		this.b5 = b5;
		this.a6 = a6;
		this.b6 = b6;
		this.a7 = a7;
		this.b7 = b7;
		this.a8 = a8;
		this.b8 = b8;
		this.w = w;
	}

	public int value(double x) {
		double y = a0 + a1 * Math.cos(x * w) + b1 * Math.sin(x * w) + a2 * Math.cos(2 * x * w)
				+ b2 * Math.sin(2 * x * w) + a3 * Math.cos(3 * x * w) + b3 * Math.sin(3 * x * w)
				+ a4 * Math.cos(4 * x * w) + b4 * Math.sin(4 * x * w) + a5 * Math.cos(5 * x * w)
				+ b5 * Math.sin(5 * x * w) + a6 * Math.cos(6 * x * w) + b6 * Math.sin(6 * x * w)
				+ a7 * Math.cos(7 * x * w) + b7 * Math.sin(7 * x * w) + a8 * Math.cos(8 * x * w)
				+ b8 * Math.sin(8 * x * w);

		return (int) Math.floor(y);
	}

	public Collection<Integer> normTweet(int size) {

		Integer[] arrayNumber = new Integer[100];
		for (int j = 0; j < arrayNumber.length; j++)
			arrayNumber[j] = 0;

		for (int cont = 0; cont < size; cont++)
			arrayNumber[cont % 100] += 1;

		return Arrays.asList(arrayNumber);
	}

	public List<Integer> cantTweet(int time) {
		List<Integer> cantTweet = new ArrayList<Integer>();

		for (int i = 0; i < time; i++) {
			int cantTweetSeg = value(i);
			cantTweet.addAll(normTweet(cantTweetSeg));
		}

		return cantTweet;
	}

}
