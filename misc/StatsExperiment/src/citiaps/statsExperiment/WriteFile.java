package citiaps.statsExperiment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFile {

	public void writeFile(String fileName, StatsS4 statsS4) {
		try {
			FileWriter fileWriterMem = new FileWriter(fileName.concat("Mem.csv"));
			BufferedWriter bufferedWriterMem = new BufferedWriter(fileWriterMem);

			FileWriter fileWriterCpu = new FileWriter(fileName.concat("Cpu.csv"));
			BufferedWriter bufferedWriterCpu = new BufferedWriter(fileWriterCpu);

			bufferedWriterMem.write("\"Time\";\"Memory (GB)\"\n");
			bufferedWriterCpu.write("\"Time\";\"CPU\"\n");
			for (int time : statsS4.getStatsMem().keySet()) {
				bufferedWriterMem.write("\"" + time + "\";\""
						+ ((double) 128 * ((double) statsS4.getStatsMem().get(time) / (double) 100)) + "\"\n");
				bufferedWriterCpu.write(
						"\"" + time + "\";\"" + ((double) statsS4.getStatsCpu().get(time) / (double) 8) + "\"\n");
			}

			bufferedWriterMem.close();
			fileWriterMem.close();

			bufferedWriterCpu.close();
			fileWriterCpu.close();
		} catch (IOException ex) {
			System.out.println("Error writing to file '" + fileName + "'");
		}

	}
}
