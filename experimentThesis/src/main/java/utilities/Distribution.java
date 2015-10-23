package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Distribution {
	List<Integer> cantTweets;

	public Distribution() {
		this.cantTweets = new ArrayList<Integer>();
	}

	public List<Integer> parabolaTweets() {
		Parabola parabolaA = new Parabola();
		parabolaA.calcularParabola(0, 100, 600, 200, 1200, 100);
		Parabola parabolaB = new Parabola();
		parabolaB.calcularParabola(0, 100, 600, 400, 1200, 100);
		Parabola parabolaC = new Parabola();
		parabolaC.calcularParabola(0, 100, 600, 800, 1200, 100);
		Parabola parabolaAux = new Parabola();

		for (int i = 0; i < 900; i++) {
			int auxCont = (int) Math.ceil(parabolaA.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}
		
		parabolaAux.calcularParabola(0, (int) Math.ceil(parabolaA.valor(300)), 300, 100, 600,
				(int) Math.ceil(parabolaB.valor(300)));
		for (int i = 0; i < 600; i++) {
			int auxCont = (int) Math.ceil(parabolaAux.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}

		for (int i = 300; i < 900; i++) {
			int auxCont = (int) Math.ceil(parabolaB.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}
		
		parabolaAux.calcularParabola(0, (int) Math.ceil(parabolaB.valor(300)), 300, 100, 600,
				(int) Math.ceil(parabolaC.valor(300)));
		for (int i = 0; i < 600; i++) {
			int auxCont = (int) Math.ceil(parabolaAux.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}

		for (int i = 300; i < 1200; i++) {
			int auxCont = (int) Math.ceil(parabolaC.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}
		
		for (int i = 0; i < 1200; i++) {
			int auxCont = (int) Math.ceil(parabolaA.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}

		return cantTweets;
	}

	public List<Integer> dynamicTweets() {
		Words read = new Words();

		// Cambiar Path
		return read.readCantTweets("/alumnos/dwladdimiro/S4/experimentThesis/config/streamTwitter.csv");
	}

	public int totalTweet() {
		int total = 0;

		for (int cant : cantTweets) {
			total += cant;
		}

		return total;
	}
}