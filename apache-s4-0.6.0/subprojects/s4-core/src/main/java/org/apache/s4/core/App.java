/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.s4.core;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.s4.base.Event;
import org.apache.s4.base.Hasher;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.base.Sender;
import org.apache.s4.base.SerializerDeserializer;
import org.apache.s4.comm.serialize.SerializerDeserializerFactory;
import org.apache.s4.comm.topology.Cluster;
import org.apache.s4.comm.topology.RemoteStreams;
import org.apache.s4.core.adapter.AdapterApp;
import org.apache.s4.core.ft.CheckpointingFramework;
import org.apache.s4.core.monitor.S4Monitor;
import org.apache.s4.core.monitor.StatusPE;
import org.apache.s4.core.monitor.TopologyApp;
import org.apache.s4.core.staging.StreamExecutorServiceFactory;
import org.apache.s4.core.util.S4Metrics;
import org.apache.s4.core.window.AbstractSlidingWindowPE;
import org.apache.s4.core.window.SlotFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Container base class to hold all processing elements.
 * 
 * It is also where one defines the application graph: PE prototypes, internal
 * streams, input and output streams.
 */
public abstract class App {

	private static final Logger logger = LoggerFactory.getLogger(App.class);

	/* All the PE prototypes and history in this app. */
	final private List<ProcessingElement> pePrototypes = new ArrayList<ProcessingElement>();

	// /* Stream to PE prototype relations. */
	// final private Multimap<Streamable<? extends Event>, ProcessingElement>
	// stream2pe = LinkedListMultimap.create();
	/* All the internal streams in this app. */
	final private List<Streamable<Event>> streams = new ArrayList<Streamable<Event>>();

	/* Pes indexed by name. */
	final Map<String, ProcessingElement> peByName = Maps.newHashMap();

	/* Adapter indexed by port */
	final Map<Class<? extends AdapterApp>, Integer> adapterbyPort = Maps
			.newHashMap();

	private ClockType clockType = ClockType.WALL_CLOCK;

	private boolean conditionAdapter = false;

	/*
	 * Objecto para poder sincronizar los datos enviados con los datos obtenidos
	 * por parte del monitor. De estar manera, askStatus esperará hasta que
	 * sendStatus haya terminado su función.
	 */
	private Object block = new Object();
	private Object blockAdapter = new Object();

	@Inject
	private Sender sender;
	@Inject
	private ReceiverImpl receiver;

	@Inject
	private Cluster cluster;

	@Inject
	private RemoteSenders remoteSenders;

	@Inject
	private Hasher hasher;

	@Inject
	private RemoteStreams remoteStreams;

	@Inject
	@Named("s4.cluster.name")
	private String clusterName;

	@Inject(optional = true)
	@Named("s4.metrics.peProcessingTime")
	boolean measurePEProcessingTime = true;

	// default is NoOpCheckpointingFramework
	@Inject
	CheckpointingFramework checkpointingFramework;

	@Inject
	S4Metrics metrics;

	@Inject
	private S4Monitor monitor;

	private boolean runMonitor;

	// serialization uses the application class loader
	@Inject
	private SerializerDeserializerFactory serDeserFactory;
	private SerializerDeserializer serDeser;

	@Inject
	StreamExecutorServiceFactory streamExecutorFactory;

	@Inject
	private void initSerDeser() {
		this.serDeser = serDeserFactory.createSerializerDeserializer(getClass()
				.getClassLoader());
	}

	/**
	 * The internal clock can be configured as "wall clock" or "event clock".
	 * The wall clock computes time from the system clock while the
	 * "event clock" uses the most recently seen event time stamp. TODO:
	 * implement event clock functionality.
	 */
	public enum ClockType {
		WALL_CLOCK, EVENT_CLOCK
	};

	/* Should only be used within the core package. */
	void addPEPrototype(ProcessingElement pePrototype) {
		pePrototypes.add(pePrototype);
	}

	public ProcessingElement getPE(String name) {

		return peByName.get(name);
	}

	/* Should only be used within the core package. */
	public void addStream(Streamable<Event> stream) {
		streams.add(stream);
	}

	/*
	 * Returns list of PE prototypes. Should only be used within the core
	 * package.
	 */
	List<ProcessingElement> getPePrototypes() {
		return pePrototypes;
	}

	/*
	 * Returns list of internal streams. Should only be used within the core
	 * package.
	 */
	// TODO visibility
	public List<Streamable<Event>> getStreams() {
		return streams;
	}

	protected abstract void onStart();

	/**
	 * Este método inicializará el monitor, de tal manera de generar dos hebras
	 * independientes, para realizar una acción según un determinado tiempo.
	 */
	private void startMonitor() {
		getLogger().info("Start S4Monitor");
		monitor.startMetrics();

		synchronized (getBlockAdapter()) {
			try {
				getBlockAdapter().wait();
			} catch (InterruptedException e) {
				getLogger().error(e.getMessage());
			}

			if (runMonitor) {

				/*
				 * La primera hebra se utilizará para enviar los datos de los
				 * distintos PEs cada un segundo, para poseer el historial de
				 * cada PE
				 */
				ScheduledExecutorService getEventCount = Executors
						.newSingleThreadScheduledExecutor();
				getEventCount.scheduleAtFixedRate(new OnTimeGetEventCount(),
						1000, 1000, TimeUnit.MILLISECONDS);

				// getLogger().info("TimerMonitor get eventCount");

				/*
				 * La segunda hebra se utilizará para enviar los datos de los
				 * distintos PEs, es decir, los eventos que ha procesado y ha
				 * enviado.
				 */
				ScheduledExecutorService sendStatus = Executors
						.newSingleThreadScheduledExecutor();
				sendStatus.scheduleAtFixedRate(new OnTimeSendStatus(), 10100,
						15000, TimeUnit.MILLISECONDS);

				// getLogger().info("TimerMonitor send status");

				/*
				 * Finalmente, la tercera se dedicará a ser el callback del
				 * monitor. De esta manera cada cierto tiempo, preguntará al
				 * monitor cual es el estado del sistema, enviado las réplicas
				 * necesarias que se requieran del sistema.
				 */
				ScheduledExecutorService pullStatus = Executors
						.newSingleThreadScheduledExecutor();
				pullStatus.scheduleAtFixedRate(new OnTimeAskStatus(), 10000,
						15000, TimeUnit.MILLISECONDS);

				getLogger().info("TimerMonitor pull status");
			}

		}

	}

	/**
	 * Esta clase estará encargada de la correr la primera hebra, la cual
	 * enviará los eventCount de cada uno de los PEs cada un segundo. Para
	 * obtenerlos, se recurrirá a los distintos 'Streams' creandos en la
	 * aplicación de S4. Al obtener cada 'Stream', se extraerá los distintos PEs
	 * Prototipos de cada uno de los 'Streams', enviado después los eventos
	 * procesados y enviados (a la vez) de cada instancia de los PEs Prototipos.
	 */
	private class OnTimeGetEventCount extends TimerTask {
		long timeInit;
		long timeFinal;

		@Override
		public void run() {
			timeInit = System.currentTimeMillis();

			// logger.debug("OnTimeGetEventCount");

			Map<Class<? extends ProcessingElement>, Double> mapEventSeg = new HashMap<Class<? extends ProcessingElement>, Double>();

			/* Obtención de cada 'Stream' */
			for (Streamable<Event> stream : getStreams()) {
				/* Obtención de cada 'PE Prototype' */
				for (ProcessingElement PEPrototype : stream.getTargetPEs()) {
					/* Obtención de la tasa de procesamiento */
					long μ = 0;
					for (ProcessingElement PE : PEPrototype.getInstances()) {
						μ += PE.getEventSeg();
						// Reinicio del contador de eventos para períodos de un
						// segundo
						PE.setEventSeg(0);
					}

					/* Obtención de la tasa de llegada */
					long λ = PEPrototype.getEventSegQueue();
					PEPrototype.setEventSegQueue(0);

					/* Cálculo de la tasa de rendimiento */
					double ρ = 0;
					if (μ != 0) {
						ρ = (double) λ / (double) μ;
					} else if ((μ == 0) && (λ == 0)) {
						ρ = 1;
					} else {
						ρ = Double.POSITIVE_INFINITY;
					}

					/* Para luego guardarlos en el historial del período */
					mapEventSeg.put(PEPrototype.getClass(), ρ);
				}
			}

			/* Donde finalmente, se irán al historial colectivo */
			monitor.sendHistory(mapEventSeg);

			timeFinal = System.currentTimeMillis();
			monitor.getMetrics()
					.setTimeSendHistoryMonitor(timeFinal - timeInit);
		}
	}

	/**
	 * Esta clase estará encargada de la correr la segunda hebra, la cual
	 * enviará los datos de cada uno de los PEs. Para obtenerlos, se recurrirá a
	 * los distintos 'Streams' creandos en la aplicación de S4. Al obtener cada
	 * 'Stream', se extraerá los distintos PEs Prototipos de cada uno de los
	 * 'Streams', enviado después los eventos procesados y enviados (a la vez)
	 * de cada instancia de los PEs Prototipos.
	 */
	private class OnTimeSendStatus extends TimerTask {
		long timeInit;
		long timeFinal;

		@Override
		public void run() {

			// getLogger().debug("Init Task Send Status");

			// synchronized (getBlockSend()) {
			// try {
			// // logger.debug("Wait for SendStatistics");
			// getBlockSend().wait(1000);
			// // logger.debug("Continue for Send Status");
			// } catch (InterruptedException e) {
			// getLogger().error("InterruptedException: " + e.getMessage());
			// }

			synchronized (block) {
				timeInit = System.currentTimeMillis();

				/* Obtención de cada 'Stream' */
				for (Streamable<Event> stream : getStreams()) {
					/* Obtención de cada 'PE Prototype' */
					for (ProcessingElement PEPrototype : stream.getTargetPEs()) {
						/* Obtención del flujo de cada 'PE Instances' */
						long μ = 0;
						for (ProcessingElement PE : PEPrototype.getInstances()) {
							μ += PE.getEventPeriod();
							// Reinicio del contador de eventos para
							// períodos del análisis del monitor
							PE.setEventPeriod(0);
						}

						long λ = PEPrototype.getEventPeriodQueue();
						PEPrototype.setEventPeriodQueue(0);

						/* Envío de los datos al monitor */
						if (!getMonitor().sendStatus(PEPrototype.getClass(), λ,
								μ))
							logger.error("Error en el sendStatus");
					}
				}

				// logger.debug("Finish Send History");

				// logger.debug(monitor.getStatusSystem().toString());

				/*
				 * Avisará al Thread de AskStatus que está disponible para
				 * obtener los datos
				 */
				// logger.info("Notify to AskMonitor");
				block.notify();

				timeFinal = System.currentTimeMillis();
				monitor.getMetrics().setTimeSendMonitor(timeFinal - timeInit);
			}
			// }

			// logger.debug("Finish Task Send Status");

		}

	}

	/**
	 * Esta clase estará encargada de correr la tercera hebra, la cual
	 * preguntará al monitor el estado de los distintos PEs, y en caso de ser
	 * necesario realizar un cambio. Para esto se elaboró tres pasos: preguntar,
	 * obtener PEs emisores y cambio del sistema. Siendo este último dividido en
	 * dos etapas: aumento y disminución de las instancias de PEs.
	 */
	private class OnTimeAskStatus extends TimerTask {
		Map<Class<? extends ProcessingElement>, StatusPE> statusSystem;
		long timeInit;
		long timeFinal;

		@Override
		public void run() {

			synchronized (block) {

				timeInit = System.currentTimeMillis();

				// logger.info("Init AskStatus");
				try {
					// logger.info("Wait to AskMonitor");
					block.wait();
					// logger.info("Go AskMonitor");
				} catch (InterruptedException e) {
					getLogger()
							.error("InterruptedException: " + e.getMessage());
				}

				// logger.info("Post wait AskStatus");

				/* Consulta del estado al monitor */
				statusSystem = getMonitor().askStatus();

				// logger.info("Post function AskStatus");

				// logger.info("Finish AskStatus");

				/* Análisis de cada PE según lo entregado por el monitor */
				// for (StatusPE statusPE : statusSystem) {
				for (Class<? extends ProcessingElement> peCurrent : statusSystem
						.keySet()) {
					StatusPE statusPE = statusSystem.get(peCurrent);

					/* Lista de los Adapters que proveen eventos al PE analizado */
					List<Class<? extends AdapterApp>> listAdapter = getAdapter(statusPE
							.getPE());

					/* Lista de PEs que proveen eventos al PE analizado */
					List<Class<? extends ProcessingElement>> listPE = getPESend(statusPE
							.getPE());

					/* Cambio del sistema en el adapter */
					if (!listAdapter.isEmpty())
						changeReplicationAdapter(listAdapter, statusPE);

					/* Cambio del sistema en los PEs */
					if (!listPE.isEmpty())
						changeReplication(listPE, statusPE);

					// logger.debug("[statusPE] Finish ChangeReplication"
					// + statusPE.getPE().getCanonicalName());
				}
				// logger.info("Finish AskStatus");

				timeFinal = System.currentTimeMillis();
				monitor.getMetrics().setTimeAskMonitor(timeFinal - timeInit);
			}

		}

		/**
		 * Este método obtendrá todos los PEs que proveen de eventos al PE
		 * analizado. De tal manera que se pueda cambiar su llave de
		 * replicación, en caso que el PE analizado haya cambiado su estado.
		 * 
		 * @param peReplication
		 *            PE analizado que se utilizar para buscar todos los PEs que
		 *            le proveen eventos.
		 * @return Todos los PEs que provee eventos al PE que se utilizó como
		 *         parámetro de la función.
		 */
		private List<Class<? extends ProcessingElement>> getPESend(
				Class<? extends ProcessingElement> peReplication) {

			List<Class<? extends ProcessingElement>> listPE = new ArrayList<Class<? extends ProcessingElement>>();

			for (TopologyApp topology : getMonitor().getTopologySystem()) {
				if (peReplication.equals(topology.getPeRecibe())) {
					if (topology.getAdapter() == null)
						listPE.add(topology.getPeSend());
				}
			}

			return listPE;
		}

		/**
		 * Este método obtendrá todos los Adapters que proveen de eventos al PE
		 * analizado. De tal manera que se pueda cambiar su llave de
		 * replicación, en caso que el PE analizado haya cambiado su estado.
		 * 
		 * @param peReplication
		 *            PE analizado que se utilizar para buscar todos los PEs que
		 *            le proveen eventos.
		 * @return Todos los Adapters que provee eventos al PE que se utilizó
		 *         como parámetro de la función.
		 */
		private List<Class<? extends AdapterApp>> getAdapter(
				Class<? extends ProcessingElement> peReplication) {

			List<Class<? extends AdapterApp>> listAdapter = new ArrayList<Class<? extends AdapterApp>>();

			for (TopologyApp topology : getMonitor().getTopologySystem()) {
				if (peReplication.equals(topology.getPeRecibe())) {
					if (topology.getAdapter() != null)
						listAdapter.add(topology.getAdapter());
				}
			}

			return listAdapter;
		}

		/**
		 * Este método analizará si el estado del PE debe aumentar o disminuir
		 * de tal manera que si aumenta o disminuye, todos los Adapters emisores
		 * a ese PE deberán aumentar o disminuir su llave de replicación. Por
		 * otra parte, en caso que disminuya, se deberá eliminar el PE con la
		 * llave eliminada (es decir, el número mayor de los PEs instanciados).
		 * 
		 * @param listAdapter
		 * 
		 * @param listPESend
		 * @param statusPE
		 */
		private void changeReplicationAdapter(
				List<Class<? extends AdapterApp>> listAdapter, StatusPE statusPE) {

			/* Se analiza cada uno de los distintos Adapter que envían datos */
			for (Class<? extends AdapterApp> adapter : listAdapter) {

				Socket clientSocket = null;
				ObjectOutputStream outputStream;
				try {
					clientSocket = new Socket("localhost",
							adapterbyPort.get(adapter));

					outputStream = new ObjectOutputStream(
							clientSocket.getOutputStream());
					outputStream.writeObject(statusPE);

					clientSocket.close();
				} catch (IOException eClientSocket) {
					logger.error(eClientSocket.toString());
				}

			}

		}

		/**
		 * Este método analizará si el estado del PE debe aumentar o disminuir
		 * de tal manera que si aumenta o disminuye, todos los PEs emisores a
		 * ese PE deberán aumentar o disminuir su llave de replicación. Por otra
		 * parte, en caso que disminuya, se deberá eliminar el PE con la llave
		 * eliminada (es decir, el número mayor de los PEs instanciados).
		 * 
		 * @param listPESend
		 * @param statusPE
		 *            estado del PE analizado
		 */
		private void changeReplication(
				List<Class<? extends ProcessingElement>> listPESend,
				StatusPE statusPE) {

			/* Se analiza cada uno de los distintos PE que envían datos */
			for (Class<? extends ProcessingElement> peSend : listPESend) {

				// nextPESend:
				/* Obtención de cada 'Stream' */
				for (Streamable<Event> stream : getStreams()) {
					/* Obtención de cada 'PE Prototype' */
					for (ProcessingElement PEPrototype : stream.getTargetPEs()) {

						/*
						 * En caso que el PE Prototipo sea igual al PE que
						 * estamos analizado de los emisores del PE, se deberá
						 * realizar la comparación para ver si se cambia la
						 * llave de replicación
						 */
						if (peSend.equals(PEPrototype.getClass())) {

							/*
							 * De ser mayor la cantidad de replicas del estado
							 * del monitor, se deberá aumentar la cantidad de
							 * réplicas
							 */

							int replicationAnalyzed = statusPE.getReplication();
							int replicationCurrent = PEPrototype
									.getReplicationPE(statusPE.getPE());

							logger.debug("[replicationAnalyzed] "
									+ replicationAnalyzed
									+ " | [replicationCurrent] "
									+ replicationCurrent);

							if (replicationAnalyzed > replicationCurrent) {

								getLogger().debug(
										"Increment PE  " + statusPE.getPE()
												+ " in PE "
												+ PEPrototype.getClass());

								PEPrototype.setReplicationPE(statusPE.getPE(),
										statusPE.getReplication());

								/* Crea los PEs que sean necesarios */
								addReplication(statusPE);

								/*
								 * Realizando esa modificación en cada instancia
								 * del PE
								 */
								for (ProcessingElement PE : PEPrototype
										.getInstances()) {

									getLogger()
											.debug("["
													+ PE.getClass()
															.getCanonicalName()
													+ "] Replication: "
													+ PEPrototype
															.getReplicationPE(statusPE
																	.getPE()));

									PE.setReplicationPE(statusPE.getPE(),
											statusPE.getReplication());

								}

							}
							/* De ser menor, se deberá disminuir */
							else if (replicationAnalyzed < replicationCurrent) {

								/*
								 * Para esto, es necesario eliminar una de las
								 * instancias del PE analizado
								 */
								removeReplication(statusPE);

								getLogger().debug(
										"Decrement PE  " + statusPE.getPE()
												+ " in PE "
												+ PEPrototype.getClass());

								PEPrototype.setReplicationPE(statusPE.getPE(),
										statusPE.getReplication());

								/*
								 * Para luego disminuir sus llaves de
								 * replicación
								 */

								for (ProcessingElement PE : PEPrototype
										.getInstances()) {

									PE.setReplicationPE(statusPE.getPE(),
											statusPE.getReplication());
								}

							}

							/*
							 * Dejará de analizar, debido que ya encontró el PE
							 * solicitado.
							 * 
							 * Obs: Sólo esto sucede cuando un PE está en un
							 * sólo Stream, pero puede darse el caso que esté en
							 * más de un Stream.
							 */
							// break nextPESend;

						}

					}
				}
			}

		}

	}

	/**
	 * This method is called by the container after initialization. Once this
	 * method is called, threads get started and events start flowing.
	 */
	public final void start() {

		// logger.info("Prepare to start App [{}].", getClass().getName());

		/* Start all streams. */
		for (Streamable<? extends Event> stream : getStreams()) {
			stream.start();
		}

		/* Allow abstract PE to initialize. */
		for (ProcessingElement pe : getPePrototypes()) {
			getLogger().info("Init prototype [{}].", pe.getClass().getName());
			pe.initPEPrototypeInternal();
		}

		/*
		 * Se inicializa el monitor en caso que no sea una aplicación del
		 * adapter.
		 */
		if (!getConditionAdapter())
			startMonitor();

		onStart();

	}

	/**
	 * This method is called by the container to initialize applications.
	 */
	protected abstract void onInit();

	public final void init() {
		onInit();
	}

	/**
	 * This method is called by the container before unloading the application.
	 */
	protected abstract void onClose();

	public final void close() {

		onClose();
		removeAll();
	}

	/**
	 * Función que estará encargada de agregar réplicas entre el valor del
	 * número de instancias + 1 y el número de réplicas deseadas. Es decir, en
	 * caso que el PE posee número de réplica n, significa que habrá 0 a n-1
	 * llaves, de esta manera, se agregará m réplicas, de n a m-1, como n+m
	 * réplicas diga que existen ahora en el sistema.
	 * 
	 * @param statusPE
	 *            Estado del PE analizado
	 */
	public void addReplication(StatusPE statusPE) {
		/* Obtención de cada 'Stream' */
		for (Streamable<Event> stream : getStreams()) {
			/* Obtención de cada 'PE Prototype' */
			for (ProcessingElement PEPrototype : stream.getTargetPEs()) {
				if (PEPrototype.getClass().equals(statusPE.getPE())) {
					for (long i = PEPrototype.getNumPEInstances(); i < statusPE
							.getReplication(); i++) {
						PEPrototype.getInstanceForKey(Long.toString(i));
					}
				}

			}
		}
	}

	/**
	 * Función que estará encargada de eliminar la réplica con el valor de la
	 * llave más alto. Es decir, en caso que el PE posee número de réplica n,
	 * significa que habrá 0 a n-1 llaves, de esta manera, se eliminarán m
	 * réplicas, como n-m réplicas diga que existen ahora en el sistema.
	 * 
	 * @param statusPE
	 *            Estado del PE analizado
	 */
	public void removeReplication(StatusPE statusPE) {

		/* Obtención de cada 'Stream' */
		for (Streamable<Event> stream : getStreams()) {
			/* Obtención de cada 'PE Prototype' */
			for (ProcessingElement PEPrototype : stream.getTargetPEs()) {
				/* Analizamos si el PE es el que deseamos eliminar */
				for (ProcessingElement PE : PEPrototype.getInstances()) {
					if (statusPE.getPE().equals(PEPrototype.getClass())) {
						// logger.debug("[{}] ID: {}", new
						// String[]{PE.getClass().getCanonicalName(),
						// PE.getId()});
						int replicationCurrent = Integer.parseInt(PE.getId());
						/*
						 * En caso que el 'id' del PE sea mayor o igual a la
						 * réplica que se desea, se deberá eliminar el PE. Esto
						 * se debe a que se posee 'id' de 0 a n-1 réplicas, por
						 * lo tanto toda 'id' que sea n o superior serán
						 * réplicas que sobran en el sistema.
						 */
						if (statusPE.getReplication() <= replicationCurrent) {
							PE.close();
						}
					}
					return;
				}

			}
		}

	}

	private void removeAll() {

		/* Get the set of streams and close them. */
		for (Streamable<?> stream : getStreams()) {
			getLogger().trace("Closing stream [{}].", stream.getName());
			stream.close();
		}

		for (ProcessingElement pe : getPePrototypes()) {

			getLogger().trace("Removing PE proto [{}].",
					pe.getClass().getName());

			/* Remove all instances. */
			pe.removeAll();

		}

		/* Finally remove the entire app graph. */
		getLogger().trace("Clear app graph.");

		pePrototypes.clear();
		streams.clear();
	}

	void addPEPrototype(ProcessingElement pePrototype,
			Stream<? extends Event> stream) {

		// logger.info("Add PE prototype [{}] with stream [{}].",
		// toString(pePrototype), toString(stream));
		pePrototypes.add(pePrototype);
		metrics.createCacheGauges(pePrototype, pePrototype.peInstances);

	}

	/**
	 * The internal clock is configured as "wall clock" or "event clock" when
	 * this object is created.
	 * 
	 * @return the App time in milliseconds.
	 */
	public long getTime() {
		return System.currentTimeMillis();
	}

	/**
	 * The internal clock is configured as "wall clock" or "event clock" when
	 * this object is created.
	 * 
	 * @param timeUnit
	 * @return the App time in timeUnit
	 */
	public long getTime(TimeUnit timeUnit) {
		return timeUnit.convert(getTime(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Set the {@link ClockType}.
	 * 
	 * @param clockType
	 *            the clockTyoe for this app must be
	 *            {@link ClockType#WALL_CLOCK} (default) or
	 *            {@link ClockType#EVENT_CLOCK}
	 */
	public void setClockType(ClockType clockType) {
		this.clockType = clockType;

		if (clockType == ClockType.EVENT_CLOCK) {
			getLogger().error("Event clock not implemented yet.");
			System.exit(1);
		}
	}

	/**
	 * @return the clock type.
	 */
	public ClockType getClockType() {
		return clockType;
	}

	/**
	 * @return the monitor
	 */
	public S4Monitor getMonitor() {
		return monitor;
	}

	public boolean getRunMonitor() {
		return runMonitor;
	}

	public void setRunMonitor(boolean runMonitor) {
		this.runMonitor = runMonitor;
	}

	/**
	 * @return the condition adapter.
	 */
	public boolean getConditionAdapter() {
		return conditionAdapter;
	}

	/**
	 * Set the {@link conditionAdapter}
	 * 
	 * @param conditionAdapter
	 *            Para saber si podemos saber si es el Adapter o no
	 */

	public void setConditionAdapter(boolean conditionAdapter) {
		if (!conditionAdapter)
			return;

		this.conditionAdapter = conditionAdapter;
	}

	/**
	 * 
	 * Returns the id of the partition assigned to the current node.
	 * 
	 * NOTE: This method will block until the current node gets assigned a
	 * partition
	 * 
	 */
	public int getPartitionId() {
		return getReceiver().getPartitionId();
	}

	/**
	 * 
	 * Returns the total number of partitions of the cluster this nodes belongs
	 * to.
	 */
	public int getPartitionCount() {
		return getCluster().getPhysicalCluster().getPartitionCount();
	}

	/**
	 * @return the sender object
	 */
	public Sender getSender() {
		return sender;
	}

	/**
	 * @return the receiver object
	 */
	public ReceiverImpl getReceiver() {
		return receiver;
	}

	public SerializerDeserializer getSerDeser() {
		return serDeser;
	}

	public CheckpointingFramework getCheckpointingFramework() {
		return checkpointingFramework;
	}

	public StreamExecutorServiceFactory getStreamExecutorFactory() {
		return streamExecutorFactory;
	}

	/**
	 * Creates a stream with a specific key finder. The event is delivered to
	 * the PE instances in the target PE prototypes by key.
	 * 
	 * <p>
	 * If the value of the key is "joe" and the target PE prototypes are
	 * AddressPE and WorkPE, the event will be delivered to the instances with
	 * key="joe" in the PE prototypes AddressPE and WorkPE.
	 * 
	 * @param name
	 *            the name of the stream
	 * @param finder
	 *            the key finder object
	 * @param eventType
	 *            expected event type
	 * @param processingElements
	 *            the target processing elements
	 * @return the stream
	 */
	protected <T extends Event> Stream<T> createStream(String name,
			KeyFinder<T> finder, Class<T> eventType,
			ProcessingElement... processingElements) {

		return new Stream<T>(this).setName(name).setKey(finder)
				.setPEs(processingElements).setEventType(eventType)
				.setSerializerDeserializerFactory(serDeserFactory).register();
	}

	/**
	 * @see App#createStream(String, KeyFinder, Class, ProcessingElement...)
	 */
	protected <T extends Event> Stream<T> createStream(String name,
			KeyFinder<T> finder, ProcessingElement... processingElements) {
		return createStream(name, finder, null, processingElements);
	}

	/**
	 * @see App#createStream(String, KeyFinder, Class, ProcessingElement...)
	 */
	protected <T extends Event> Stream<T> createStream(String name,
			ProcessingElement... processingElements) {
		return createStream(name, null, processingElements);
	}

	/**
	 * @see App#createStream(String, KeyFinder, Class, ProcessingElement...)
	 */
	public <T extends Event> Stream<T> createStream(Class<T> type) {
		return createStream(null, null, type);
	}

	/**
	 * Creates a "remote" stream, i.e. a stream that forwards events to remote
	 * clusters
	 * 
	 * @param name
	 *            stream name, shared across communicating clusters
	 * @param finder
	 *            key finder
	 * @return a reference to the created remote stream
	 */
	protected <T extends Event> RemoteStream createOutputStream(String name,
			KeyFinder<Event> finder) {
		return new RemoteStream(this, name, 0, finder, remoteSenders, hasher,
				remoteStreams, clusterName);
	}

	/**
	 * @see App#createOutputStream(String, KeyFinder)
	 */
	protected <T extends Event> RemoteStream createOutputStream(String name) {
		return createOutputStream(name, null);
	}

	/**
	 * Creaters an "input" stream, i.e. a stream that listens to events from
	 * remote clusters, and that registers its interest in the stream with the
	 * specified name.
	 * 
	 * @param streamName
	 *            name of the remote stream
	 * @param finder
	 *            key finder
	 * @param processingElements
	 *            target processing elements
	 * @return a reference to the created input stream
	 */
	protected <T extends Event> Stream<T> createInputStream(String streamName,
			KeyFinder<T> finder, ProcessingElement... processingElements) {
		remoteStreams.addInputStream(clusterName, streamName);
		return createStream(streamName, finder, processingElements);

	}

	/**
	 * @see App#createInputStream(String, KeyFinder, ProcessingElement...)
	 */
	protected <T extends Event> Stream<T> createInputStream(String streamName,
			ProcessingElement... processingElements) {
		return createInputStream(streamName, null, processingElements);

	}

	/**
	 * Creates a {@link ProcessingElement} prototype.
	 * 
	 * @param type
	 *            the processing element type.
	 * @param name
	 *            a name for this PE prototype.
	 * @return the processing element prototype.
	 */
	public <T extends ProcessingElement> T createPE(Class<T> type, String name) {

		try {
			// TODO: make sure this doesn't crash if PE has a constructor other
			// than with App as argument.
			Class<?>[] types = new Class<?>[] { App.class };
			try {
				T pe = type.getDeclaredConstructor(types).newInstance(this);
				pe.setName(name);
				return pe;
			} catch (NoSuchMethodException e) {
				// no such constructor. Use the setter
				T pe = type.getDeclaredConstructor(new Class[] {})
						.newInstance();
				pe.setApp(this);
				pe.setName(name);
				return pe;
			}
		} catch (Exception e) {
			getLogger().error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Creates a {@link ProcessingElement} prototype.
	 * 
	 * @param type
	 *            the processing element type.
	 * @return the processing element prototype.
	 */
	public <T extends ProcessingElement> T createPE(Class<T> type) {

		return createPE(type, null);

	}

	public <T extends AbstractSlidingWindowPE> T createSlidingWindowPE(
			Class<T> type, long slotDuration, TimeUnit timeUnit, int numSlots,
			SlotFactory slotFactory) {
		try {
			Class<?>[] types = new Class<?>[] { App.class, long.class,
					TimeUnit.class, int.class, SlotFactory.class };
			T pe = type.getDeclaredConstructor(types).newInstance(
					new Object[] { this, slotDuration, timeUnit, numSlots,
							slotFactory });
			return pe;
		} catch (Exception e) {
			getLogger().error("Cannot instantiate pe for class [{}]",
					type.getName(), e);
			return null;
		}
	}

	public static Logger getLogger() {
		return logger;
	}

	public Object getBlockAdapter() {
		return blockAdapter;
	}

	public Cluster getCluster() {
		return cluster;
	}

	static private String toString(ProcessingElement pe) {
		return pe != null ? pe.getClass().getName() + " " : "null ";
	}

	static private String toString(Streamable<? extends Event> stream) {
		return stream != null ? stream.getName() + " " : "null ";
	}

}
