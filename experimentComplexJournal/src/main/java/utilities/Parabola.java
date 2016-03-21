package utilities;

public class Parabola {
	private double A;
	private double B;
	private double C;

	public Parabola() {
		this.setA(0);
		this.setB(0);
		this.setC(0);
	}

	public Parabola(double A, double B, double C) {
		this.setA(A);
		this.setB(B);
		this.setC(C);
	}

	public double valor(int x) {
		double y = getA() * Math.pow(x, 2) + getB() * x + getC();
		return y;
	}

	public void calcularParabola(int x1, int y1, int x2, int y2, int x3, int y3) {
		this.setA((y2 * (x3 - x1) - y1 * (x3 - x2) - y3 * (x2 - x1))
				/ (Math.pow(x1, 2) * (x2 - x3) - Math.pow(x3, 2) * (x2 - x1) - Math.pow(x2, 2) * (x1 - x3)));
		this.setB((y2 - y1 + this.getA() * (Math.pow(x1, 2) - Math.pow(x2, 2))) / (x2 - x1));
		this.setC(-1 * getA() * Math.pow(x1, 2) - getB() * x1 + y1);
	}

	public double getA() {
		return A;
	}

	public void setA(double a) {
		A = a;
	}

	public double getB() {
		return B;
	}

	public void setB(double b) {
		B = b;
	}

	public double getC() {
		return C;
	}

	public void setC(double c) {
		C = c;
	}
}