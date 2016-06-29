package citiaps.statsExperiment;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class ReadFile {
	public StatsS4 readFile(String fileName) {

		Map<Integer, Double> statsZKMem = new TreeMap<Integer, Double>();
		Map<Integer, Double> statsZKCpu = new TreeMap<Integer, Double>();

		Map<Integer, Double> statsNodeMem = new TreeMap<Integer, Double>();
		Map<Integer, Double> statsNodeCpu = new TreeMap<Integer, Double>();

		Map<Integer, Double> statsAdapterMem = new TreeMap<Integer, Double>();
		Map<Integer, Double> statsAdapterCpu = new TreeMap<Integer, Double>();

		String line = null;

		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			int time = 0;
			while ((line = bufferedReader.readLine()) != null) {
				String[] parts = line.split(" ");
				// 0,1,2 Tags
				// 3,4,5 Java
				// 6,7,8 ZK
				// 9,10,11 Node
				// 12,13,14 Adapter

				statsZKMem.put(time, Double.parseDouble(parts[7]));
				statsZKCpu.put(time, Double.parseDouble(parts[8]));

				statsNodeMem.put(time, Double.parseDouble(parts[10]));
				statsNodeCpu.put(time, Double.parseDouble(parts[11]));

				statsAdapterMem.put(time, Double.parseDouble(parts[13]));
				statsAdapterCpu.put(time, Double.parseDouble(parts[14]));
				
				time += 2;
			}

			bufferedReader.close();
			fileReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}

		StatsS4 statsS4 = new StatsS4(statsZKMem, statsZKCpu, statsNodeMem, statsNodeCpu, statsAdapterMem,
				statsAdapterCpu);

		return statsS4;
	}
}
