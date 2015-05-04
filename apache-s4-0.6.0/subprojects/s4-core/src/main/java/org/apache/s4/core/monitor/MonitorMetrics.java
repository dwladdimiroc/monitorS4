package org.apache.s4.core.monitor;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Maps;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.reporting.CsvReporter;

public class MonitorMetrics {
	static final Logger logger = LoggerFactory
			.getLogger(MonitorMetrics.class);

	/*
	 * Estadísticas del monitor replicationPE: Cantidad de réplicas por PE
	 * avgEventSystem: Número promedio de eventos en el sistema avgEventQueue:
	 * Número promedio de eventos en cola avgTimeResident: Tiempo promedio de
	 * residencia avgTimeQueue: Tiempo promedio de espera avgTimeProcess: Tiempo
	 * promedio de procesamiento timeSendHistoryMonitor: Tiempo que se demora en
	 * enviar el historial de los PEs timeSendMonitor: Tiempo que se demora en
	 * enviar los PEs timeAskMonitor: Tiempo que se demora en consultar el
	 * estado de los PEs
	 */

	private final Map<String, Counter> replicationPE = Maps.newHashMap();

	private final Map<String, Double> avgEventSystem = Maps.newHashMap();
	private final Map<String, Double> avgEventQueue = Maps.newHashMap();

	private final Map<String, Double> avgTimeResident = Maps.newHashMap();
	private final Map<String, Double> avgTimeQueue = Maps.newHashMap();
	private final Map<String, Double> avgTimeProcess = Maps.newHashMap();

	private long timeSendHistoryMonitor;
	private long timeSendMonitor;
	private long timeAskMonitor;

	public MonitorMetrics() {
		logger.info("Init S4Monitor Metrics");

		/* Init value */
		timeSendHistoryMonitor = 0;
		timeSendMonitor = 0;
		timeAskMonitor = 0;

		/* Init registry */
		String outputDir = "/home/daniel/Proyectos/monitorS4/statistics/";
		long period = 1;
		TimeUnit timeUnit = TimeUnit.SECONDS;
		logger.info(
				"Reporting metrics of the monitor through csv files in directory [{}] with frequency of [{}] [{}]",
				new String[] { outputDir, String.valueOf(period),
						timeUnit.name() });
		CsvReporter.enable(new File(outputDir), period, timeUnit);

		/* Init gauge of the system */
		Metrics.newGauge(S4Monitor.class, "timeSendHistoryMonitor", "ms",
				new Gauge<Long>() {
					@Override
					public Long value() {
						return timeSendHistoryMonitor;
					}
				});

		Metrics.newGauge(S4Monitor.class, "timeSendMonitor", "ms",
				new Gauge<Long>() {
					@Override
					public Long value() {
						return timeSendMonitor;
					}
				});

		Metrics.newGauge(S4Monitor.class, "timeAskMonitor", "ms",
				new Gauge<Long>() {
					@Override
					public Long value() {
						return timeAskMonitor;
					}
				});
	}

	public void createCounterReplicationPE(String name) {
		replicationPE.put(name,
				Metrics.newCounter(S4Monitor.class, "replication@" + name));
		replicationPE.get(name).inc(); 
	}

	public void counterReplicationPE(String name, boolean status) {
		if (status)
			replicationPE.get(name).inc();
		else
			replicationPE.get(name).dec();
	}

	public void createGaugeAvgEventSystem(final String name) {
		avgEventSystem.put(name, Double.valueOf(0));

		Metrics.newGauge(S4Monitor.class, "avgEventSystem@" + name, "ms",
				new Gauge<Double>() {
					@Override
					public Double value() {
						return avgEventSystem.get(name);
					}
				});
	}

	public void gaugeAvgEventSystem(String name, double avg) {
		avgEventSystem.put(name, avg);
	}

	public void createGaugeAvgEventQueue(final String name) {
		avgEventQueue.put(name, Double.valueOf(0));

		Metrics.newGauge(S4Monitor.class, "avgEventQueue@" + name, "ms",
				new Gauge<Double>() {
					@Override
					public Double value() {
						return avgEventQueue.get(name);
					}
				});
	}

	public void gaugeAvgEventQueue(String name, double avg) {
		avgEventQueue.put(name, avg);
	}

	public void createGaugeAvgTimeResident(final String name) {
		avgTimeResident.put(name, Double.valueOf(0));

		Metrics.newGauge(S4Monitor.class, "avgTimeResident@" + name, "ms",
				new Gauge<Double>() {
					@Override
					public Double value() {
						return avgTimeResident.get(name);
					}
				});
	}

	public void gaugeAvgTimeResident(String name, double avg) {
		avgTimeResident.put(name, avg);
	}

	public void createGaugeAvgTimeQueue(final String name) {
		avgTimeQueue.put(name, Double.valueOf(0));

		Metrics.newGauge(S4Monitor.class, "avgTimeQueue@" + name, "ms",
				new Gauge<Double>() {
					@Override
					public Double value() {
						return avgTimeQueue.get(name);
					}
				});
	}

	public void gaugeAvgTimeQueue(String name, double avg) {
		avgTimeQueue.put(name, avg);
	}

	public void createGaugeAvgTimeProcess(final String name) {
		avgTimeProcess.put(name, Double.valueOf(0));

		Metrics.newGauge(S4Monitor.class, "avgTimeProcess@" + name, "ms",
				new Gauge<Double>() {
					@Override
					public Double value() {
						return avgTimeProcess.get(name);
					}
				});
	}

	public void gaugeAvgTimeProcess(String name, double avg) {
		avgTimeProcess.put(name, avg);
	}

	public void setTimeSendHistoryMonitor(long timeSendHistoryMonitor) {
		this.timeSendHistoryMonitor = timeSendHistoryMonitor;
	}

	public void setTimeSendMonitor(long timeSendMonitor) {
		this.timeSendMonitor = timeSendMonitor;
	}

	public void setTimeAskMonitor(long timeAskMonitor) {
		this.timeAskMonitor = timeAskMonitor;
	}

}
