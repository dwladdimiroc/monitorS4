import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class ReadFile {

	public static void main(String[] args) throws IOException {
		File folder = new File("../statistics/");

		List<List<Integer>> replicasTotal = new ArrayList<List<Integer>>();
		List<Integer> timeArray = new ArrayList<Integer>();
		boolean time = false;

		for (File fileEntry : folder.listFiles()) {
			if ((fileEntry.getName().contains("lambda")) || (fileEntry.getName().contains("mu"))) {
				normValue("../statistics/".concat(fileEntry.getName()));
			} else if (fileEntry.getName().contains("replication")) {
				replicasTotal.add(replicasPE("../statistics/".concat(fileEntry.getName())));

				if (!time)
					timeArray = time("../statistics/".concat(fileEntry.getName()));
				else
					time = true;
			}
		}
		writeReplicasTotal(replicasTotal, timeArray);
	}

	public static void normValue(String file) throws IOException {
		CSVReader reader = new CSVReader(new FileReader(file));
		List<String[]> myEntries = reader.readAll();
		reader.close();

		List<String[]> newMyEntries = new ArrayList<String[]>();
		boolean firstLine = true;
		for (String[] entry : myEntries) {
			if (!firstLine) {
				double num = Double.parseDouble(entry[1]) / 5;
				String[] newEntry = new String[] { entry[0], Double.toString(num) };
				newMyEntries.add(newEntry);
			} else {
				String[] newEntry = new String[] { entry[0], entry[1] };
				newMyEntries.add(newEntry);
				firstLine = false;
			}
		}

		CSVWriter writer = new CSVWriter(new FileWriter(file), ',');
		for (String[] lines : newMyEntries) {
			writer.writeNext(lines);
		}

		writer.close();
	}

	public static List<Integer> replicasPE(String file) throws IOException {
		List<Integer> numReplicas = new ArrayList<Integer>();
		CSVReader reader = new CSVReader(new FileReader(file));
		List<String[]> myEntries = reader.readAll();
		reader.close();

		boolean firstLine = true;
		for (String[] entry : myEntries) {
			if (!firstLine) {
				numReplicas.add(Integer.parseInt(entry[1]));

			} else {
				firstLine = false;
			}
		}

		return numReplicas;
	}

	public static List<Integer> time(String file) throws IOException {
		List<Integer> numReplicas = new ArrayList<Integer>();
		CSVReader reader = new CSVReader(new FileReader(file));
		List<String[]> myEntries = reader.readAll();
		reader.close();

		boolean firstLine = true;
		for (String[] entry : myEntries) {
			if (!firstLine) {
				numReplicas.add(Integer.parseInt(entry[0]));

			} else {
				firstLine = false;
			}
		}

		return numReplicas;
	}

	public static void writeReplicasTotal(List<List<Integer>> replicasTotal, List<Integer> timeArray)
			throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter("../statistics/replicationTotal.csv"), ',');
		writer.writeNext(new String[] { "time", "total" });

		int size = replicasTotal.get(0).size();

		for (int i = 0; i < size; i++) {
			int acum = 0;
			for (List<Integer> replicaPE : replicasTotal) {
				acum += replicaPE.get(i);
			}
			writer.writeNext(new String[] { Integer.toString(timeArray.get(i)), Integer.toString(acum) });
		}

		writer.close();
	}

}
