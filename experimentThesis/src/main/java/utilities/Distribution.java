package utilities;

import java.util.Arrays;
import java.util.Stack;

public class Distribution {
	Stack<Integer> cantTweets;

	public Distribution() {
		this.cantTweets = new Stack<Integer>();
	}

	public Stack<Integer> parabolaTweets() {
		Parabola parabola = new Parabola();

		parabola.calcularParabola(0, 100, 600, 200, 1200, 500);
		for (int i = 1; i <= 1200; i++) {
			int auxCont = (int) Math.ceil(parabola.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}

		parabola.calcularParabola(0, 100, 600, 400, 1200, 500);
		for (int i = 1; i <= 1200; i++) {
			int auxCont = (int) Math.ceil(parabola.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}

		parabola.calcularParabola(0, 100, 300, 800, 1200, 500);
		for (int i = 1; i <= 1200; i++) {
			int auxCont = (int) Math.ceil(parabola.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}

		return cantTweets;
	}

	public Stack<Integer> dynamicTweets() {
		Words read = new Words();

		// Cambiar Path
		return read.readCantTweets("/home/daniel/Proyectos/monitorS4/experimentThesis/config/streamTwitter.csv");
	}

	public int totalTweet() {
		int total = 0;

		for (int cant : cantTweets) {
			total += cant;
		}

		return total;
	}
}