import java.io.*;
import java.util.*;

public class normConsumeRAM {
	public static void main(String args[]) throws IOException {

		String line = null;

		try {

			FileReader fileReader = new FileReader("consumeRAM.csv");
			FileWriter fileWriter = new FileWriter("consumeRAM-Norm.csv");

			BufferedReader bufferedReader = new BufferedReader(fileReader);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(new String(
					"VIR	RES (kB)	RES (MB)	SHR	time (s)\n"));

			int time = 1;
			while ((line = bufferedReader.readLine()) != null) {
				line = line.replace("kB totales       ", "");
				line = line.replace("   ", " ");
				line = line.replace("  ", " ");
				// System.out.println(line);
				// Separar línea
				String[] word = line.split(" ");
				String lineFile = "";
				for (int i = 0; i < word.length; i++) {

					if (i != (word.length - 1)) {
						lineFile = lineFile.concat(word[i] + "\t");
					} else {
						lineFile = lineFile.concat(word[i] + "\t");
						lineFile = lineFile.concat("\t" + time);
					}

					if (i == 1) {
						long RES = Long.parseLong(word[i]);
						lineFile = lineFile.concat(new String(Double
								.toString((double) RES / 1000)));
						lineFile = lineFile.concat("\t");
					}

				}
				time++;
				bufferedWriter.write(lineFile + "\n");
			}

			bufferedReader.close();
			bufferedWriter.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file'");
		} catch (IOException ex) {
			System.out.println("Error reading file");
			System.out.println("Error writing to file");

		}
	}
}