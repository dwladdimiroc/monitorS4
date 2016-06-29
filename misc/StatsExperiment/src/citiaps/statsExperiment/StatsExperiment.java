package citiaps.statsExperiment;

public class StatsExperiment {

	public static void main(String[] args) {
		ReadFile readFile = new ReadFile();
		StatsS4 statsS4 = readFile.readFile("stats.log");
		statsS4.calculateStats();
		
		WriteFile writeFile = new WriteFile();
		writeFile.writeFile("stats", statsS4);
		
	}

}
