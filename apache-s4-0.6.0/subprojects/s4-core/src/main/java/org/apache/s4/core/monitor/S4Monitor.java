package org.apache.s4.core.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.adapter.AdapterApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class S4Monitor {
	private boolean ready;

	static final Logger logger = LoggerFactory.getLogger(S4Monitor.class);

	private int period;

	private final List<TopologyApp> topologySystem = new ArrayList<TopologyApp>();
	private final Map<Class<? extends ProcessingElement>, StatusPE> statusSystem = new LinkedHashMap<Class<? extends ProcessingElement>, StatusPE>();

	/* Clase que se hara cargo de almacenar las estadísticas de S4Monitor */
	private MonitorMetrics metrics;

	/* Primeros 20 segundos */
	private boolean initStatus;

	private int replicationTotal;
	private int replicationMax;

	/* Variables del adapter */
	// private final List<Map<Class<? extends AdapterApp>, Queue<Double>>>
	// listHistoryAdapter = new ArrayList<Map<Class<? extends AdapterApp>,
	// Queue<Long>>>();

	/**
	 * Inicialización del monitor, dada la inyección de la clase
	 */
	@Inject
	private void init() {
		logger.info("Init Monitor");
		period = 1;
		ready = false;
		initStatus = true;
		replicationMax = 30;
	}

	public void startMetrics() {
		setMetrics(new MonitorMetrics());
	}

	/**
	 * registerAdapter hace referencia al registro del grafo dirigido dado el
	 * adapter de la aplicación.
	 * 
	 * @param adapter
	 *            Adapter de la App
	 * @param peRecibe
	 *            PE receptor
	 */
	public void registerAdapter(Class<? extends AdapterApp> adapter,
			Class<? extends ProcessingElement> peRecibe) {
		logger.info("Register Adapter");

		/*
		 * Registro del adapter con su respectivo flujo al PE correspondiente
		 */
		TopologyApp topology = new TopologyApp();
		topology.setAdapter(adapter);
		topology.setPeRecibe(peRecibe);
		getTopologySystem().add(topology);

	}

	/**
	 * registerPE hace referencia al registro del grafo dirigido, de esta manera
	 * podremos saber quienes son los PE emisores y receptores.
	 * 
	 * @param peSend
	 *            PE emisor
	 * @param peRecibe
	 *            PE receptor
	 */
	public void registerPE(Class<? extends ProcessingElement> peSend,
			Class<? extends ProcessingElement> peRecibe) {

		logger.info("Register PE");

		/*
		 * En caso que no sea un nodo terminal, se deberá generar un conexión
		 * entre el PE emisor y receptor. Por lo que la topología deberá agregar
		 * una arista con sus respectivos vértices.
		 */
		if (peRecibe != null) {
			TopologyApp topology = new TopologyApp();
			topology.setPeSend(peSend);
			topology.setPeRecibe(peRecibe);
			getTopologySystem().add(topology);
		}

		/*
		 * En caso de no existir, se deberá crear un nuevo análisis del estado
		 * del PE de entrada.
		 */
		boolean exist = false;

		if (getStatusSystem().containsKey(peSend)) {
			exist = true;
		}

		if (!exist) {
			StatusPE statusPE = new StatusPE();
			statusPE.setPE(peSend);
			statusPE.setRecibeEvent(0);
			statusPE.setSendEvent(0);
			statusPE.setReplication(1);
			getStatusSystem().put(peSend, statusPE);

			replicationTotal++;

			/* Además, se crearán los contadores y estadísticas para el PE */
			getMetrics().createCounterReplicationPE(peSend.getCanonicalName());
			getMetrics().createGaugeRhoPE(peSend.getCanonicalName());
			getMetrics().createGaugeLambdaPE(peSend.getCanonicalName());
			getMetrics().createGaugeMuPE(peSend.getCanonicalName());
			getMetrics().createGaugeQueuePE(peSend.getCanonicalName());
			getMetrics().createGaugeEventCountPE(peSend.getCanonicalName());
			// getMetrics().createGaugeAvgEventSystem(peSend.getCanonicalName());
			// getMetrics().createGaugeAvgEventQueue(peSend.getCanonicalName());
			// getMetrics().createGaugeAvgTimeResident(peSend.getCanonicalName());
			// getMetrics().createGaugeAvgTimeQueue(peSend.getCanonicalName());
			// getMetrics().createGaugeAvgTimeProcess(peSend.getCanonicalName());
		}
	}

	/**
	 * Agregará al historial de cierto PE la tasa de procesamiento en cierto
	 * período
	 * 
	 * @param pe
	 *            analizado
	 * @param ρ
	 *            tasa de procesamiento
	 * @return Estado de si se guardó o no la tasa de procesamiento
	 */
	private boolean setRho(Class<? extends ProcessingElement> pe, double ρ) {

		StatusPE statusPE = statusSystem.get(pe);
		if (statusPE != null) {
			statusPE.getHistory().add(ρ);
			return true;
		}

		return false;
	}

	/**
	 * sendHistory hace referencia al envío de la historia de cada uno de los
	 * PEs que existen en el sistema. Esto quiere decir, la cantidad de eventos
	 * que han procesado en un período de 1 segundo. Esto servirá posteriormente
	 * para el análisis de la Cadena de Markov, por lo que la cantidad de
	 * períodos por cada PEs será la cantidad de muestras para la cadena.
	 * 
	 * @param historyPEs
	 *            Corresponde a la tasa de rendimiento de todos los PEs en un
	 *            período determinado
	 */
	public void sendHistory(
			Map<Class<? extends ProcessingElement>, Double> historyPEs) {
		// boolean printHistory = false;

		for (Class<? extends ProcessingElement> peCurrent : historyPEs.keySet()) {
			double ρ = historyPEs.get(peCurrent);
			if (!setRho(peCurrent, ρ))
				logger.error("Error en guardar la variable ρ en el PE "
						+ peCurrent.getCanonicalName());
		}

		// if (printHistory)
		// printHistoryForPE();

	}

	/**
	 * Imprime los distintos historiales de los PEs en el sistema
	 */
	private void printHistoryForPE() {
		logger.debug("Print HistoryPE");
		for (Class<? extends ProcessingElement> key : statusSystem.keySet()) {
			StatusPE status = statusSystem.get(key);
			logger.debug("Status: " + status.getPE() + " | EventCount: "
					+ status.getSendEvent() + " | History: "
					+ status.getHistory().toString());
		}
	}

	// /**
	// * sendStatusAdapter hace referencia al 'Monitor de carga', pero según el
	// * adapter. Esto debido que es una excepción en la forma de analizarlo en
	// * S4.
	// *
	// * @param adapter
	// * Corresponde al Adapter que se está analizando.
	// * @param eventCount
	// * La cantidad de eventos totales procesados en ese período.
	// */
	// public void sendStatusAdapter(Class<? extends AdapterApp> adapter,
	// long eventCount) {
	// List<Class<? extends ProcessingElement>> listPERecibe = new
	// ArrayList<Class<? extends ProcessingElement>>();
	//
	// /*
	// * Obtener todos las clases a donde debe ir el Adapter.
	// */
	// for (TopologyApp topology : getTopologySystem()) {
	// if (adapter.equals(topology.getAdapter())) {
	// listPERecibe.add(topology.getPeRecibe());
	// }
	// }
	//
	// /*
	// * Asignar el valor de la tasa de llegada de cada uno de los distintos
	// * PE. Es decir, λ Tasa de llegada.
	// */
	// for (Class<? extends ProcessingElement> recibe : listPERecibe) {
	// for (StatusPE statusPE : getStatusSystem()) {
	// if (recibe.equals(statusPE.getPE())) {
	// statusPE.setRecibeEvent(statusPE.getRecibeEvent()
	// + eventCount);
	// }
	// }
	// }
	// }

	/**
	 * sendStatus hace referencia al 'Monitor de carga', donde se analizarán
	 * cada una de las distintas cargas de los PEs. De esta manera, se podrá
	 * analizar la cola y el flujo de datos.
	 * 
	 * @param data
	 *            Corresponde al PE que se está analizando.
	 * @param μUnit
	 * @param eventCount
	 * @param eventCount
	 *            La cantidad de eventos totales procesados en ese período.
	 */
	public boolean sendStatus(Class<? extends ProcessingElement> data, long λ,
			long μ, long μUnit, long eventCount) {

		StatusPE statusPE = statusSystem.get(data);

		if (statusPE != null) {
			/*
			 * Se guardará tanto la tasa de llegada, como la tasa de
			 * procesamiento. Esto para poder realizar estadísticas del sistema
			 */
			statusPE.setSendEvent(μ);
			statusPE.setRecibeEvent(λ);
			statusPE.setEventCount(eventCount);

			/* Get Statistics */

			/*
			 * Un análisis importante que se tuvo a la hora de realizar el
			 * monitoreo, fue el promedio de la tasa de procesamiento. Esto se
			 * hizo dado que la sumatoria de todas las tasas de procesamiento de
			 * los PEs va a dar un valor igual o menor a la tasa de llegada de
			 * los PEs, por lo tanto, nunca se analizará si existe un
			 * comportamiento ocioso. Por lo tanto, al poseer el período mayor
			 * de una instancia, se puede realizar una aproximación de cual
			 * debería ser su rendimiento real.
			 */

			if (!initStatus) {
				/*
				 * En caso que se encuentre una tasa de procesamiento mayor, se
				 * cambia el valor de la tasa de procesamiento del historial
				 */
				if (μUnit > statusPE.getSendEventUnit())
					statusPE.setSendEventUnit(μUnit);

			} else {
				if (((period % 20) != 0) && ((period % 20) != 1)) {
					if (μUnit > statusPE.getSendEventUnit())
						statusPE.setSendEventUnit(μUnit);
				} else {
					/*
					 * En caso que se encuentre una tasa de procesamiento mayor,
					 * se cambia el valor de la tasa de procesamiento del
					 * historial
					 */
					if (μUnit > statusPE.getSendEventUnit())
						statusPE.setSendEventUnit(μUnit);

					// statusPE.setSendEventPeriod(0);
					initStatus = false;
				}
			}

			/*
			 * Posteriormente, se analiza la tasa de rendimiento del PE, para
			 * verificar si efectivamente es necesario una replicación. Es decir
			 * ρ = λ / (s*μ) ; donde s cantidad de replicas del PE
			 */
			double ρ = 0;
			long s = statusPE.getReplication();
			if (μ != 0) {
				if (s == 1) {
					ρ = (double) λ / (double) (μ + s);
					logger.debug("[PE] " + statusPE.getPE().getCanonicalName()
							+ " | [λ] " + λ + " | [μ] " + μ + " | [ρ] " + ρ);
				} else {
					if (statusPE.getSendEventUnit() != 0) {
						double μPE = statusPE.getSendEventUnit();
						statusPE.setSendEvent(s * (long) Math.floor(μPE));
						ρ = (double) λ / ((double) s * μPE);
						logger.debug("[PE] "
								+ statusPE.getPE().getCanonicalName()
								+ " | [s] " + s + " | [μPE] " + μPE
								+ " | [s*μPE] " + ((double) s * μPE)
								+ " | [λ] " + λ + " | [ρ] " + ρ);
					} else {
						ρ = (double) λ / (double) (s * μ);
						logger.error("[PE2] "
								+ statusPE.getPE().getCanonicalName()
								+ " | [s] " + s + " | [μ] " + μ + " | [s*μ] "
								+ ((double) s * μ) + " | [λ] " + λ + " | [ρ] "
								+ ρ);
					}
				}
			} else if ((μ == 0) && (λ == 0)) {
				ρ = 1;
			} else {
				ρ = Double.POSITIVE_INFINITY;
			}
			statusPE.setProcessEvent(ρ);

			/* Analizamos la cola del PE */
			long queuePE = statusPE.getRecibeEvent() - statusPE.getSendEvent();
			if (queuePE > 0) {
				statusPE.setQueueEvent(statusPE.getQueueEvent() + queuePE);
			} else {
				statusPE.setQueueEvent(statusPE.getQueueEvent());
			}

			/* Get Statistics */
			getMetrics().gaugeRhoPE(statusPE.getPE().getCanonicalName(),
					statusPE.getProcessEvent());
			getMetrics().gaugeLambdaPE(statusPE.getPE().getCanonicalName(),
					statusPE.getRecibeEvent());
			getMetrics().gaugeMuPE(statusPE.getPE().getCanonicalName(),
					statusPE.getSendEvent());
			getMetrics().gaugeQueuePE(statusPE.getPE().getCanonicalName(),
					statusPE.getQueueEvent());
			getMetrics().gaugeEventCountPE(statusPE.getPE().getCanonicalName(),
					statusPE.getEventCount());

		} else {
			return false;
		}

		return true;
	}

	/**
	 * El algoritmo propuesto ahora, representa la replicación reactiva del
	 * monitor donde se tendrá como principal componente un factor ρ, el cual
	 * indicará si existe una demora en el procesamiento de los datos.
	 * 
	 * @param data
	 *            PE analizado para saber si necesita replicación según el
	 *            algoritmo reactivo.
	 * @return condición de replicación del PE según el algoritmo reactivo.
	 *         Donde 1 significa que aumenta la cantidad de PE, 0 se mantiene y
	 *         -1 se disminuye.
	 */
	private int reactiveLoad(StatusPE statusPE) {

		/* Análisis de la tasa de rendimiento */
		double ρ = statusPE.getProcessEvent();

		logger.debug("[PE] " + statusPE.getPE() + " | [ρ] " + ρ);

		/*
		 * Análisis de ρ para ver si debe aumentar, mantener o disminuir la
		 * cantidad de réplicas de cierto PE
		 */
		if (ρ > 1) {
			// logger.debug("Increment");
			return 1;
		} else if (ρ < 0.5) {
			// logger.debug("Decrement");
			return -1;
		} else {
			return 0;
		}

	}

	/**
	 * El algoritmo propuesto ahora, representa la replicación predictiva del
	 * monitor donde se tendrá como principal componente algún modelo matemático
	 * que esté validado por el estado del arte.
	 * 
	 * @param data
	 *            PE analizado para saber si necesita replicación según el
	 *            algoritmo predictivo.
	 * @return condición de replicación del PE según el algoritmo predictivo.
	 *         Donde 1 significa que aumenta la cantidad de PE, 0 se mantiene y
	 *         -1 se aumenta.
	 */
	private int predictiveLoad(StatusPE statusPE) {
		MarkovChain markovChain = new MarkovChain();
		/* Parseo del List a Array */
		Double rho[] = new Double[statusPE.getHistory().size() - 1];
		rho = statusPE.getHistory().toArray(rho);

		// logger.debug("[" + statusPE.getPE().getCanonicalName()
		// + "] | [PE History] " + statusPE.getHistory().toString());

		/* Cálculo de la predicción por parte de la Cadena de Markov */
		double distEstacionaria[] = markovChain.calculatePrediction(rho, 100,
				100000);

		/* Análisis estadístico de los resultados de la predicción */
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(
				distEstacionaria);

		logger.debug("[transitionMatrix] {"
				+ Arrays.toString(markovChain.getTransitionMatrix()[0]) + ","
				+ Arrays.toString(markovChain.getTransitionMatrix()[1]) + ","
				+ Arrays.toString(markovChain.getTransitionMatrix()[2]) + "}"
				+ " | [distEstacionaria] " + Arrays.toString(distEstacionaria)
				+ " | [Statistics] "
				+ descriptiveStatistics.getStandardDeviation());

		/*
		 * En caso de existir una desviación estándar mayor al umbral, se tomará
		 * como antecedente para la replicación de la historia analizada.
		 */
		if (descriptiveStatistics.getStandardDeviation() > 0.25) {
			for (int i = 0; i < distEstacionaria.length; i++) {
				if (distEstacionaria[i] == descriptiveStatistics.getMax()) {

					if (i == 0) {
						return -5;
					} else if (i == 2) {
						return 5;
					} else {
						return 0;
					}

				}
			}
		}

		return 0;
	}

	/**
	 * administrationLoad corresponde a la administración de las distintas
	 * réplicas según los cálculos de los distintos PE del sistema.
	 * 
	 * @param data
	 *            PE analizado para saber si necesita replicación según el
	 *            algoritmo de administración
	 * @return condición de replicación del PE según el algoritmo
	 *         administración. Donde 1 significa que aumenta la cantidad de PE,
	 *         0 se mantiene y -1 se aumenta.
	 * 
	 */
	private int administrationLoad(StatusPE statusPE) {
		int numReplica = 0;

		logger.debug("[Period] " + period);

		if ((period % 20) != 0) {

			/*
			 * Se analiza el cálculo reactivo de cierto PE, en caso de necesitar
			 * alguna modificación se marca que esto es necesario de no ser asi.
			 * Se prosigue con el siguiente cálculo de predicción
			 */

			int resultReactive = reactiveLoad(statusPE);

			if (resultReactive == 1) {
				statusPE.getMarkMap().add(1);
			} else if (resultReactive == -1) {
				statusPE.getMarkMap().add(-1);
			} else {
				statusPE.getMarkMap().add(0);
			}

			/*
			 * En caso de aumentar ese umbral, se proceserá a realizar una
			 * modificación de este procedimiento. En este caso, si se notifica
			 * dos veces que es necesario un incremento o decremento de las
			 * réplicas del PE, se tomará una decisión.
			 */

			/*
			 * Para análisis de prueba, se considerarán los últimos 3 períodos
			 * para verificar si es necesario replicar.
			 * 
			 * Ej: Historia = [1,0,0,-1,0,1] Donde el primer período de tiempo
			 * dio (1,0), el segundo período (0,-1) y el tercero (0,1). Por lo
			 * tanto, encuentra que existen 2 señales de que debe aumentar, por
			 * lo tanto, aumentará. Esto debido que el reactivo del primero
			 * período y el predictor del tercer período indicaron que debe
			 * aumentar.
			 */

			logger.debug("[{}] MarkMap: {}", new String[] {
					statusPE.getPE().getCanonicalName(),
					statusPE.getMarkMap().toString() });

			if (containsCondition(statusPE.getMarkMap(), true)) {
				statusPE.getMarkMap().clear();
				if (replicationTotal < replicationMax) {
					replicationTotal++;
					numReplica++;
				}
			} else if (containsCondition(statusPE.getMarkMap(), false)) {
				statusPE.getMarkMap().clear();
				replicationTotal--;
				numReplica--;
			}

		} else {

			/*
			 * En el cálculo de predicción se toma en consideración el mismo
			 * procedimiento. De realizarse esto, se tomará en consideración el
			 * procesamiento para poder analizar si efectivamente, es necesario
			 * una replicación.
			 */

			// numReplica = predictiveLoad(statusPE);
			numReplica = 0;
			int replicationAvailable = replicationMax - replicationTotal;
			if (replicationAvailable > 0) {
				if (numReplica > replicationAvailable) {
					numReplica = replicationAvailable;
				}
			}
			replicationTotal += numReplica;

		}

		return numReplica;

	}

	/**
	 * containsCondition analiza si posee una secuencia que diga que
	 * efectivamente debe generar un cambio en el sistema
	 * 
	 * @param queue
	 *            historia de mark en el PE
	 * @param condition
	 *            de si debe buscar patrón de aumento o decremento de las
	 *            réplicas
	 * @return boolean que indica si encontró o no un patrón de cambio
	 */
	private boolean containsCondition(Queue<Integer> queue, boolean condition) {
		int value;
		/* Condición */
		if (condition)
			value = 1;
		else
			value = -1;

		/* Contador */
		int cont = 0;
		for (int index : queue) {
			/* Si existe análisis de réplica */
			if (index == value) {
				cont++;
				if (cont == 2)
					return true;
			}
		}

		return false;
	}

	/**
	 * analyzeStatus analizará el nuevo flujo de datos, según la disminución o
	 * aumento de uno de los PE emisores del PE analizado.
	 * 
	 * @param data
	 *            PE analizado
	 * @param recibeEvent
	 *            Nueva cantidad de flujo del PE emisor
	 * @param replication
	 *            Booleano para ver si disminuye o aumenta el flujo
	 * @return true Si es que surgió un cambio en el sistema, false si no cambió
	 *         nada
	 */

	private boolean analyzeStatus(Class<? extends ProcessingElement> peAnalyze,
			long recibeEvent, boolean replication) {

		for (Class<? extends ProcessingElement> peCurrent : getStatusSystem()
				.keySet()) {

			StatusPE statusPE = getStatusSystem().get(peCurrent);

			if (statusPE.getPE().equals(peAnalyze)) {

				/*
				 * Análisis de ρ para ver si debe aumentar, mantener o disminuir
				 * la cantidad de réplicas de cierto PE dada la replicación del
				 * PE que le provee eventos.
				 */

				double ρ = 0;

				/*
				 * En caso que aumente el fluyo, será porque se agrega una
				 * réplica. De no ser así, será porque disminuye el flujo,
				 * debido a la remoción de una réplica.
				 */
				if (replication) {
					if (statusPE.getSendEvent() != 0) {
						ρ = (double) (statusPE.getRecibeEvent() + recibeEvent)
								/ (double) statusPE.getSendEvent();
					} else if ((statusPE.getRecibeEvent() == 0)
							&& (statusPE.getSendEvent() == 0)) {
						ρ = 1;
					} else {
						ρ = Double.POSITIVE_INFINITY;
					}

					logger.debug("[intelligentReplication] | [PE Analyze] "
							+ peAnalyze.getCanonicalName() + " | [ρ] " + ρ);

					if (ρ > 1) {
						getMetrics().counterReplicationPE(
								statusPE.getPE().getCanonicalName(), true);
						statusPE.setReplication(statusPE.getReplication() + 1);
						return true;
					}

				} else {
					if (statusPE.getSendEvent() != 0) {
						ρ = (double) (statusPE.getRecibeEvent() - recibeEvent)
								/ (double) statusPE.getSendEvent();
					} else if ((statusPE.getRecibeEvent() == 0)
							&& ((statusPE.getRecibeEvent() - recibeEvent) == 0)) {
						ρ = 1;
					} else {
						ρ = Double.POSITIVE_INFINITY;
					}

					if (ρ < 0.5) {
						getMetrics().counterReplicationPE(
								statusPE.getPE().getCanonicalName(), false);
						statusPE.setReplication(statusPE.getReplication() - 1);
						return true;
					}
				}

				return false;

			}

		}

		return false;

	}

	/**
	 * intelligentReplication es una función recursiva que analizará si es que
	 * debe aumentar o disminuir un PE receptor si es que el PE emisor ha sido
	 * modificado.
	 * 
	 * @param peEmitter
	 *            PE emisor, el cual será considerado para ver los nuevos flujos
	 *            de sus PE receptores
	 * @param replication
	 *            condición de si aumento o disminuyo el PE emisor. true es que
	 *            aumento, false es que disminuyó
	 */

	private void intelligentReplication(StatusPE peEmitter, boolean replication) {

		for (TopologyApp topology : getTopologySystem()) {
			if (peEmitter.getPE().equals(topology.getPeSend())) {

				/*
				 * Análisis de la tasa de procesamiento futuro, dado la tasa de
				 * llegada que posee el PE emisor. En caso la réplica y su
				 * intancia original posea una tasa de procesamiento mayor que
				 * la tasa de llegada, deberá considerar que no procesarán más
				 * de lo que ya poseen.
				 */
				long μ = peEmitter.getSendEvent();
				long λ = peEmitter.getRecibeEvent();

				long μFuture = μ;
				if ((2 * μ) > λ) {
					μFuture = λ;
				}

				/*
				 * En caso que el análisis del nuevo flujo haya modificado el PE
				 * receptor, se deberá realizar el mismo procedimiento con los
				 * PE receptores de PE receptor analizado.
				 */
				if (analyzeStatus(topology.getPeRecibe(), μFuture, replication)) {
					intelligentReplication(
							statusSystem.get(topology.getPeRecibe()),
							replication);
				}
			}
		}

	}

	/**
	 * En este método se analizará cada uno de los distintos PE del sistema,
	 * viendo si es necesario o no replicarlos. De esta manera, en caso de
	 * modificar su cantidad de réplicas, se calculará de forma inteligente los
	 * PEs que le continúen si deben replicarse o no.
	 * 
	 * @return Estado del sistema
	 */

	public Map<Class<? extends ProcessingElement>, StatusPE> askStatus() {

		// for (StatusPE statusPE : getStatusSystem()) {

		for (Class<? extends ProcessingElement> peCurrent : getStatusSystem()
				.keySet()) {

			StatusPE statusPE = getStatusSystem().get(peCurrent);

			logger.debug(statusPE.toString());

			int status = administrationLoad(statusPE);

			logger.debug("[Finish administrationLoad] PE: " + statusPE.getPE()
					+ " | status: " + status);

			/*
			 * Se entenderá que debe replicarse si retornar el valor 1, por lo
			 * tanto el estado del sistema se le añade una réplica. En el caso
			 * que sea -1, se deberá disminuir en uno.
			 * 
			 * Posteriormente se realizará una replicación de forma inteligente,
			 * de tal manera de aumentar o disminuir automáticamente los PEs
			 * afectados por la replicación.
			 */
			if (status > 0) {
				logger.debug("Increment PE " + statusPE.getPE());

				for (int i = 1; i <= status; i++) {
					getMetrics().counterReplicationPE(
							statusPE.getPE().getCanonicalName(), true);
				}
				statusPE.setReplication(statusPE.getReplication() + status);
				// intelligentReplication(statusPE, true);

			} else if (status < 0) {

				if (statusPE.getReplication() > 1) {
					logger.debug("Decrement PE " + statusPE.getPE());

					for (int i = 1; i <= (-1 * status); i++) {
						getMetrics().counterReplicationPE(
								statusPE.getPE().getCanonicalName(), false);
					}
					statusPE.setReplication(statusPE.getReplication() + status);
					if (statusPE.getReplication() < 1) {
						statusPE.setReplication(1);
					}
					// intelligentReplication(statusPE, false);
				}

			}

		}

		/*
		 * Aumento del período, y en caso de lograr el punto deseado, volverá a
		 * su estado inicial.
		 */
		period++;
		if (period == 21) {
			period = 1;
		}

		// metricsStatusSystem();
		clearStatusSystem();

		return getStatusSystem();
	}

	/**
	 * registerStatusSystem registará en las métricas las estadísticas
	 * solicitadas por parte de MonitorMetrics
	 */
	private void metricsStatusSystem() {
		// for (StatusPE statusPE : statusSystem) {

		for (Class<? extends ProcessingElement> peCurrent : getStatusSystem()
				.keySet()) {

			StatusPE statusPE = getStatusSystem().get(peCurrent);

			double λ = (double) statusPE.getRecibeEvent();
			double μ = (double) statusPE.getSendEvent();
			double ρ = λ / μ;

			// logger.debug("[{}] ρ: {} | λ: {} | μ: {}", new String[] {
			// statusPE.getPE().getCanonicalName(), Double.toString(ρ),
			// Double.toString(λ), Double.toString(μ) });

			/**
			 * Analizar esto para un sistema inestable
			 */

			/* Número promedio de eventos en el sistema */
			double En = ρ / (1 - ρ);
			getMetrics().gaugeAvgEventSystem(
					statusPE.getPE().getCanonicalName(), En);

			/* Número esperado de eventos en la cola */
			double Eq = (Math.pow(λ, 2)) / ((μ - λ) * μ);
			getMetrics().gaugeAvgEventQueue(
					statusPE.getPE().getCanonicalName(), Eq);

			/* Tiempo promedio de residencia */
			double Et = 1 / (μ - λ);
			getMetrics().gaugeAvgTimeResident(
					statusPE.getPE().getCanonicalName(), Et);

			/* Tiempo promedio de espera en la cola */
			double Ed = ρ / (μ - λ);
			getMetrics().gaugeAvgTimeQueue(statusPE.getPE().getCanonicalName(),
					Ed);

			/* Tiempo promedio en el sistema */
			double Ep = Et + Ed;
			getMetrics().gaugeAvgTimeProcess(
					statusPE.getPE().getCanonicalName(), Ep);
		}
	}

	/**
	 * clearStatusSystem asignará las tasas de llegada y procesamiento en 0.
	 * Esto se realizó con el fin de no poseer problemas con el próximo período
	 * analizado.
	 */
	private void clearStatusSystem() {
		// for (StatusPE statusPE : statusSystem) {
		for (Class<? extends ProcessingElement> peCurrent : getStatusSystem()
				.keySet()) {
			StatusPE statusPE = getStatusSystem().get(peCurrent);

			statusPE.setRecibeEvent(0);
			statusPE.setSendEvent(0);
			statusPE.setProcessEvent(0);
		}
	}

	/**
	 * replicationPE corresponde a la replicación de un PE determinado
	 * 
	 * @param pe
	 *            PE a replicar
	 */
	public void replicationPE(Class<? extends ProcessingElement> pe) {
		// for (StatusPE statusPE : getStatusSystem()) {
		for (Class<? extends ProcessingElement> peCurrent : getStatusSystem()
				.keySet()) {
			StatusPE statusPE = getStatusSystem().get(peCurrent);

			if (pe.equals(statusPE.getPE())) {
				statusPE.setReplication(statusPE.getReplication() + 1);
				break;
			}
		}
	}

	/**
	 * getTopologySystem se obtiene la topología del sistema
	 * 
	 * @return Topología del sistema
	 */
	public List<TopologyApp> getTopologySystem() {
		return topologySystem;
	}

	/**
	 * getStatusSystem se obtiene el estado del sistema
	 * 
	 * @return Estadp del sistema
	 */
	public Map<Class<? extends ProcessingElement>, StatusPE> getStatusSystem() {
		return statusSystem;
	}

	/**
	 * Muestra la topología del sistema
	 * 
	 * @return un string con la topología del sistema
	 */
	public String mapTopology() {
		return getTopologySystem().toString();
	}

	/**
	 * Muestra el estado del sistema
	 * 
	 * @return un string con el estado del sistema
	 */
	public String mapStatusSystem() {
		return getStatusSystem().toString();
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	public MonitorMetrics getMetrics() {
		return metrics;
	}

	public void setMetrics(MonitorMetrics metrics) {
		this.metrics = metrics;
	}

}
