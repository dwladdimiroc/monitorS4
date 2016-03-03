package utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Distribution {
	List<Integer> cantTweets;

	public Distribution() {
		this.cantTweets = new ArrayList<Integer>();
	}

	public List<Integer> parabolaTweets(int A[], int B[], int C[]) {
		Parabola parabolaA = new Parabola();
		parabolaA.calcularParabola(A[0], A[1], A[2], A[3], A[4], A[5]);
		Parabola parabolaB = new Parabola();
		parabolaB.calcularParabola(B[0], B[1], B[2], B[3], B[4], B[5]);
		Parabola parabolaC = new Parabola();
		parabolaC.calcularParabola(C[0], C[1], C[2], C[3], C[4], C[5]);
		Parabola parabolaAux = new Parabola();

		for (int i = 0; i < 150; i++) {
			int auxCont = (int) Math.ceil(parabolaA.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}
		
		parabolaAux.calcularParabola(0, (int) Math.ceil(parabolaA.valor(50)), 50, 100, 100,
				(int) Math.ceil(parabolaB.valor(50)));
		for (int i = 0; i < 100; i++) {
			int auxCont = (int) Math.ceil(parabolaAux.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}

		for (int i = 50; i < 150; i++) {
			int auxCont = (int) Math.ceil(parabolaB.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}
		
		parabolaAux.calcularParabola(0, (int) Math.ceil(parabolaB.valor(50)), 50, 100, 100,
				(int) Math.ceil(parabolaC.valor(50)));
		for (int i = 0; i < 100; i++) {
			int auxCont = (int) Math.ceil(parabolaAux.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}

		for (int i = 50; i < 200; i++) {
			int auxCont = (int) Math.ceil(parabolaC.valor(i));

			Integer[] arrayNumber = new Integer[100];
			for (int j = 0; j < arrayNumber.length; j++)
				arrayNumber[j] = 0;

			for (int cont = 0; cont < auxCont; cont++)
				arrayNumber[cont % 100] += 1;

			cantTweets.addAll(Arrays.asList(arrayNumber));
		}
		
		for (int i = 0; i < 200; i++) {
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