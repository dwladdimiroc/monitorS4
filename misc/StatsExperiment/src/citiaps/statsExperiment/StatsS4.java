package citiaps.statsExperiment;

import java.util.Map;
import java.util.TreeMap;

public class StatsS4 {
	private Map<Integer, Double> statsZKMem;
	private Map<Integer, Double> statsZKCpu;

	private Map<Integer, Double> statsNodeMem;
	private Map<Integer, Double> statsNodeCpu;

	private Map<Integer, Double> statsAdapterMem;
	private Map<Integer, Double> statsAdapterCpu;

	private Map<Integer, Double> statsMem;
	private Map<Integer, Double> statsCpu;

	public StatsS4(Map<Integer, Double> statsZKMem, Map<Integer, Double> statsZKCpu, Map<Integer, Double> statsNodeMem,
			Map<Integer, Double> statsNodeCpu, Map<Integer, Double> statsAdapterMem,
			Map<Integer, Double> statsAdapterCpu) {
		this.setStatsZKMem(statsZKMem);
		this.setStatsZKCpu(statsZKCpu);
		this.setStatsNodeMem(statsNodeMem);
		this.setStatsNodeCpu(statsNodeCpu);
		this.setStatsAdapterMem(statsAdapterMem);
		this.setStatsAdapterCpu(statsAdapterCpu);
		this.setStatsMem(new TreeMap<Integer, Double>());
		this.setStatsCpu(new TreeMap<Integer, Double>());
	}

	public void calculateStats() {
		for (int time : statsZKMem.keySet()) {
			double memTime = 0;
			memTime += statsZKMem.get(time);
			memTime += statsNodeMem.get(time);
			memTime += statsAdapterMem.get(time);
			statsMem.put(time, memTime);
		}

		for (int time : statsZKCpu.keySet()) {
			double cpuTime = 0;
			cpuTime += statsZKCpu.get(time);
			cpuTime += statsNodeCpu.get(time);
			cpuTime += statsAdapterCpu.get(time);
			statsCpu.put(time, cpuTime);
		}
	}

	public Map<Integer, Double> getStatsZKMem() {
		return statsZKMem;
	}

	public void setStatsZKMem(Map<Integer, Double> statsZKMem) {
		this.statsZKMem = statsZKMem;
	}

	public Map<Integer, Double> getStatsZKCpu() {
		return statsZKCpu;
	}

	public void setStatsZKCpu(Map<Integer, Double> statsZKCpu) {
		this.statsZKCpu = statsZKCpu;
	}

	public Map<Integer, Double> getStatsNodeMem() {
		return statsNodeMem;
	}

	public void setStatsNodeMem(Map<Integer, Double> statsNodeMem) {
		this.statsNodeMem = statsNodeMem;
	}

	public Map<Integer, Double> getStatsNodeCpu() {
		return statsNodeCpu;
	}

	public void setStatsNodeCpu(Map<Integer, Double> statsNodeCpu) {
		this.statsNodeCpu = statsNodeCpu;
	}

	public Map<Integer, Double> getStatsAdapterMem() {
		return statsAdapterMem;
	}

	public void setStatsAdapterMem(Map<Integer, Double> statsAdapterMem) {
		this.statsAdapterMem = statsAdapterMem;
	}

	public Map<Integer, Double> getStatsAdapterCpu() {
		return statsAdapterCpu;
	}

	public void setStatsAdapterCpu(Map<Integer, Double> statsAdapterCpu) {
		this.statsAdapterCpu = statsAdapterCpu;
	}

	public Map<Integer, Double> getStatsMem() {
		return statsMem;
	}

	public void setStatsMem(Map<Integer, Double> statsMem) {
		this.statsMem = statsMem;
	}

	public Map<Integer, Double> getStatsCpu() {
		return statsCpu;
	}

	public void setStatsCpu(Map<Integer, Double> statsCpu) {
		this.statsCpu = statsCpu;
	}

}
