package org.apache.s4.core.monitor;

import java.text.DecimalFormat;

public class MarkovChain {
	private double transitionMatrix[][];
	private double prediction[];

	/**
	 * Inicialización de la matriz de transición y el arreglo para la
	 * distribución estacionaria
	 */
	public MarkovChain() {
		setTransitionMatrix(new double[3][3]);
		setPrediction(new double[3]);
	}

	/**
	 * Cálculo para la distribución estacionaria, para esto se analizará la tasa
	 * de procesamiento respecto a un período t y t+1, de esta manera se
	 * verifica a que estado a variado. Por lo que se consideraron tres posibles
	 * estados en la Cadena de Markov: Ocioso, Estable e Inestable. Ocioso se
	 * refiere a cuando existen una cantidad de servicios que poseen un alto
	 * tiempo de ocio. Estable es cuando se posee con un buen número de
	 * procesamiento. E inestable cuando existe un tasa de procesamiento menor
	 * que la tasa de llegada, por lo que generará colas en el sistema.
	 * 
	 * @param rho Tasa de procesamiento del sistema en un 
	 * @param n
	 * @param iteration
	 * @return
	 */

	public double[] calculatePrediction(double[] rho, int n, int iteration) {
		int cont[] = new int[3];

		for (int i = 0; i < n - 1; i++) {
			if ((rho[i] < 0.5) && (rho[i + 1] < 0.5)) {
				transitionMatrix[0][0]++;
				cont[0]++;
			} else if ((rho[i] < 0.5) && (rho[i + 1] >= 0.5)
					&& (rho[i + 1] <= 1.5)) {
				transitionMatrix[0][1]++;
				cont[0]++;
			} else if ((rho[i] < 0.5) && (rho[i + 1] > 1.5)) {
				transitionMatrix[0][2]++;
				cont[0]++;
			} else if ((rho[i] >= 0.5) && (rho[i] <= 1.5) && (rho[i + 1] < 0.5)) {
				transitionMatrix[1][0]++;
				cont[1]++;
			} else if ((rho[i] >= 0.5) && (rho[i] <= 1.5)
					&& (rho[i + 1] >= 0.5) && (rho[i + 1] <= 1.5)) {
				transitionMatrix[1][1]++;
				cont[1]++;
			} else if ((rho[i] >= 0.5) && (rho[i] <= 1.5) && (rho[i + 1] > 1.5)) {
				transitionMatrix[1][2]++;
				cont[1]++;
			} else if ((rho[i] > 1.5) && (rho[i + 1] < 0.5)) {
				transitionMatrix[2][0]++;
				cont[2]++;
			} else if ((rho[i] > 1.5) && (rho[i + 1] >= 0.5)
					&& (rho[i + 1] <= 1.5)) {
				transitionMatrix[2][1]++;
				cont[2]++;
			} else if ((rho[i] > 1.5) && (rho[i + 1] > 1.5)) {
				transitionMatrix[2][2]++;
				cont[2]++;
			}
		}

		for (int i = 0; i < 3; i++) {
			if (cont[i] != 0) {
				for (int j = 0; j < 3; j++) {
					transitionMatrix[i][j] /= cont[i];
				}
			}
		}

		int acum;
		int i = 0;

		for (int k = 0; k < 3; k++) {

			acum = 0;
			for (int j = 0; j < 3; j++) {
				if (transitionMatrix[k][j] == 0) {
					acum++;
				}
			}

			if (acum != 3) {
				i = k;
				break;
			}

		}

		double u;
		double probAcum;
		cont = new int[3];

		for (int k = 0; k < iteration; k++) {

			u = Math.random();
			probAcum = 0;
			for (int j = 0; j < 3; j++) {
				probAcum += transitionMatrix[i][j];
				if (u <= probAcum) {
					cont[j]++;
					i = j;
					break;
				}
			}

		}

		for (int k = 0; k < 3; k++) {
			prediction[k] = (double) cont[k] / (double) iteration;
		}

		return prediction;
	}

	public double[][] getTransitionMatrix() {
		return transitionMatrix;
	}

	public void setTransitionMatrix(double transitionMatrix[][]) {
		this.transitionMatrix = transitionMatrix;
	}

	public double[] getPrediction() {
		return prediction;
	}

	public void setPrediction(double prediction[]) {
		this.prediction = prediction;
	}

	public String showPrediction() {
		DecimalFormat decimalFormat = new DecimalFormat("0.000");
		return "[ " + decimalFormat.format(prediction[0]) + " "
				+ decimalFormat.format(prediction[1]) + " "
				+ decimalFormat.format(prediction[2]) + " ]";
	}

	@Override
	public String toString() {
		DecimalFormat decimalFormat = new DecimalFormat("0.000");

		String transitionMatrixString = new String("╔═══════════════════╗\n║ "
				+ decimalFormat.format(this.transitionMatrix[0][0]) + " "
				+ decimalFormat.format(this.transitionMatrix[0][1]) + " "
				+ decimalFormat.format(this.transitionMatrix[0][2]) + " ║\n║ "
				+ decimalFormat.format(this.transitionMatrix[1][0]) + " "
				+ decimalFormat.format(this.transitionMatrix[1][1]) + " "
				+ decimalFormat.format(this.transitionMatrix[1][2]) + " ║\n║ "
				+ decimalFormat.format(this.transitionMatrix[2][0]) + " "
				+ decimalFormat.format(this.transitionMatrix[2][1]) + " "
				+ decimalFormat.format(this.transitionMatrix[2][2])
				+ " ║\n╚═══════════════════╝");
		return transitionMatrixString;
	}
}
