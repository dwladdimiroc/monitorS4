package org.apache.s4.core.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.adapter.AdapterApp;
import org.apache.s4.core.overloadgen.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class S4Monitor {
	private boolean ready;

	static final Logger logger = LoggerFactory.getLogger(S4Monitor.class);

	private final List<TopologyApp> topologySystem = new ArrayList<TopologyApp>();
	private final List<StatusPE> statusSystem = new ArrayList<StatusPE>();

	/* Clase que se hara cargo de almacenar las estadísticas de S4Monitor */
	private MonitorMetrics metrics;

	/* Variables del adapter */
	private final List<Map<Class<? extends AdapterApp>, Queue<Long>>> listHistoryAdapter = new ArrayList<Map<Class<? extends AdapterApp>, Queue<Long>>>();

	/**
	 * Inicialización del monitor, dada la inyección de la clase
	 */
	@Inject
	private void init() {
		logger.info("Init Monitor");
		ready = false;
	}

	public void startMetrics() {
		metrics = new MonitorMetrics();
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
		logger.debug("Register Adapter");

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

		logger.debug("Register PE");

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
		for (StatusPE statusPE : getStatusSystem()) {
			if (peSend.equals(statusPE.getPE())) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			StatusPE statusPE = new StatusPE();
			statusPE.setPE(peSend);
			statusPE.setRecibeEvent(0);
			statusPE.setSendEvent(0);
			statusPE.setReplication(1);
			getStatusSystem().add(statusPE);

			/* Además, se crearán los contadores y estadísticas para el PE */
			metrics.createCounterReplicationPE(peSend.getCanonicalName());
			metrics.createGaugeAvgEventSystem(peSend.getCanonicalName());
			metrics.createGaugeAvgEventQueue(peSend.getCanonicalName());
			metrics.createGaugeAvgTimeResident(peSend.getCanonicalName());
			metrics.createGaugeAvgTimeQueue(peSend.getCanonicalName());
			metrics.createGaugeAvgTimeProcess(peSend.getCanonicalName());
		}
	}

	/**
	 * getRho hace referencia a la función que obtendrá el valor de Rho, esto
	 * significa que la tasa de rendimiento, que se obtendrá con la tasa de
	 * llegada de todos los PEs emisores al PE analizado y la tasa de
	 * rendimiento del PE analizado.
	 * 
	 * @param pECurrent
	 *            PE analizado
	 * @param μ
	 *            Tasa de procesamiento
	 * @param epochPE
	 *            Tasa de procesamiento de todos los PEs en la época analizada
	 * @param map
	 * @return
	 */
	private double getRho(Class<? extends ProcessingElement> pECurrent, long μ,
			Map<Class<? extends ProcessingElement>, Long> epochPE,
			Map<Class<? extends AdapterApp>, Long> map) {

		/*
		 * Obtener la tasa de llegada de todos los PEs que son emisores del PE
		 * analizado
		 */
		long λ = 0;
		for (TopologyApp topology : getTopologySystem()) {
			if (pECurrent.equals(topology.getPeRecibe())) {
				if (topology.getAdapter() == null) {
					λ += epochPE.get(topology.getPeSend());
				} else {
					λ += map.get(topology.getAdapter());
				}
			}
		}

		/*
		 * Después de poseer la tasa de llegada, podemos calcular ρ (tasa de
		 * procesamiento). Un detalle importante, es que en caso que la tasa de
		 * procesamiento sea 0, se considerará un valor infinito, debido que no
		 * es posible la división por 0.
		 */
		double ρ;
		if (μ != 0) {
			ρ = (double) λ / (double) μ;
		} else if ((μ == 0) && (λ == 0)) {
			ρ = 1;
		} else {
			ρ = Double.POSITIVE_INFINITY;
		}

		return ρ;
	}

	/**
	 * setHistoryRho hace referencia a asignar a cada PE el estado de un período
	 * determinado su Rho, es decir, su tasa de rendimiento.
	 * 
	 * @param pECurrent
	 *            El PE analizado
	 * @param rho
	 *            Tasa de rendimiento de ese PE en ese período
	 */
	private void setHistoryRho(Class<? extends ProcessingElement> pECurrent,
			double rho) {
		/*
		 * Asignar el valor de procesamiento de si mismo. Es decir, μ tasa de
		 * servicio.
		 */
		for (StatusPE statusPE : getStatusSystem()) {
			if (pECurrent.equals(statusPE.getPE())) {
				// logger.debug("StatusSystem " + statusSystem.get(i).getPe());
				/*
				 * Se debe realizar una diferencia, debido que son el total de
				 * eventos procesos en ese período menos el ya procesados. De
				 * esta manera, obtendremos el número de eventos en el Δt
				 * deseado.
				 */
				statusPE.getHistory().add(rho);
			}
		}
	}

	/**
	 * sendHistoryAdapter guardará en una variable auxiliar el estado del
	 * Adapter,
	 * 
	 * 
	 * @param historyPEs
	 *            Corresponde una lista donde cada posición es un período de
	 *            tiempo, y cada período analizará el estado de un adapter en
	 *            ese período, según la cantidad de eventos procesados.
	 */
	public void sendHistoryAdapter(
			Map<Class<? extends AdapterApp>, Queue<Long>> historyAdapter) {
		this.listHistoryAdapter.add(historyAdapter);
		printfListHistoryAdapter();
	}

	public void printfListHistoryAdapter() {
		List<Long> valueAdapter = new ArrayList<Long>();

		for (Map<Class<? extends AdapterApp>, Queue<Long>> historyAdapter : this.listHistoryAdapter) {
			set
		}
	}

	private Map<Class<? extends AdapterApp>, Long> getEpochAdapter(int epoch) {
		// Map<Class<? extends AdapterApp>, Long> epochAdapter = new
		// HashMap<Class<? extends AdapterApp>, Long>();
		//
		// for (Queue<Map<Class<? extends AdapterApp>, Long>> historyAdapter :
		// this.listHistoryAdapter) {
		// @SuppressWarnings("unchecked")
		// Map<Class<? extends AdapterApp>, Long> mapCurrent = (Map<Class<?
		// extends AdapterApp>, Long>) historyAdapter
		// .toArray()[0];
		//
		// for (Class<? extends AdapterApp> adapter : mapCurrent.keySet())
		// epochAdapter.put(adapter, mapCurrent.get(adapter));
		// }
		//
		// return epochAdapter;
		return null;
	}

	/**
	 * sendHistory hace referencia al envío de la historia de cada uno de los
	 * PEs que existen en el sistema. Esto quiere decir, la cantidad de eventos
	 * que han procesado en un período de 1 segundo. Esto servirá posteriormente
	 * para el análisis de la Cadena de Markov, por lo que la cantidad de
	 * períodos por cada PEs será la cantidad de muestras para la cadena.
	 * 
	 * @param historyPEs
	 *            Corresponde una lista donde cada posición es un período de
	 *            tiempo, y cada período analizará el estado de un PE en ese
	 *            período, según la cantidad de eventos procesados.
	 */
	public void sendHistory(
			Queue<Map<Class<? extends ProcessingElement>, Long>> historyPEs) {
		int epoch = 0;
		/* Solicitamos una muestra en el tiempo */
		for (Map<Class<? extends ProcessingElement>, Long> epochPE : historyPEs) {
			/* Solicitamos todos los PEs que poseemos en el Map de ese período */
			for (Class<? extends ProcessingElement> PECurrent : epochPE
					.keySet()) {
				/*
				 * Donde analizaremos cada uno de los PEs en esa época en
				 * específico
				 */
				setHistoryRho(
						PECurrent,
						getRho(PECurrent, epochPE.get(PECurrent), epochPE,
								getEpochAdapter(epoch)));

			}
			epoch++;
		}

		/* Limpiamos la variable auxiliar del Adapter */
		listHistoryAdapter.clear();

		printHistoryForPE();

	}

	private void printHistoryForPE() {
		logger.debug("Print HistoryPE");
		for (StatusPE status : statusSystem) {
			logger.debug("Status: " + status.getPE() + " | EventCount: "
					+ status.getSendEvent() + " | History: "
					+ status.getHistory().toString());
		}
	}

	/**
	 * sendStatusAdapter hace referencia al 'Monitor de carga', pero según el
	 * adapter. Esto debido que es una excepción en la forma de analizarlo en
	 * S4.
	 * 
	 * @param adapter
	 *            Corresponde al Adapter que se está analizando.
	 * @param eventCount
	 *            La cantidad de eventos totales procesados en ese período.
	 */
	public void sendStatusAdapter(Class<? extends AdapterApp> adapter,
			long eventCount) {
		List<Class<? extends ProcessingElement>> listPERecibe = new ArrayList<Class<? extends ProcessingElement>>();

		/*
		 * Obtener todos las clases a donde debe ir el Adapter.
		 */
		for (TopologyApp topology : getTopologySystem()) {
			if (adapter.equals(topology.getAdapter())) {
				listPERecibe.add(topology.getPeRecibe());
			}
		}

		/*
		 * Asignar el valor de la tasa de llegada de cada uno de los distintos
		 * PE. Es decir, λ Tasa de llegada.
		 */
		for (Class<? extends ProcessingElement> recibe : listPERecibe) {
			for (StatusPE statusPE : getStatusSystem()) {
				if (recibe.equals(statusPE.getPE())) {
					/*
					 * Se debe realizar una diferencia, debido que son el total
					 * de eventos procesos en ese período menos el ya
					 * procesados. De esta manera, obtendremos el número de
					 * eventos en el Δt deseado.
					 */
					statusPE.setRecibeEvent(eventCount
							- statusPE.getRecibeEvent());
				}
			}
		}
	}

	/**
	 * sendStatus hace referencia al 'Monitor de carga', donde se analizarán
	 * cada una de las distintas cargas de los PEs. De esta manera, se podrá
	 * analizar la cola y el flujo de datos.
	 * 
	 * @param data
	 *            Corresponde al PE que se está analizando.
	 * @param eventCount
	 *            La cantidad de eventos totales procesados en ese período.
	 */
	public void sendStatus(Class<? extends ProcessingElement> data,
			Long eventCount) {

		List<Class<? extends ProcessingElement>> listPERecibe = new ArrayList<Class<? extends ProcessingElement>>();

		/*
		 * Obtener todos las clases a donde debe ir el PE.
		 */
		for (TopologyApp topology : getTopologySystem()) {
			if (data.equals(topology.getPeSend())) {
				listPERecibe.add(topology.getPeRecibe());
			}
		}

		/*
		 * Asignar el valor de procesamiento de si mismo. Es decir, μ tasa de
		 * servicio.
		 */
		for (StatusPE statusPE : getStatusSystem()) {
			if (data.equals(statusPE.getPE())) {
				statusPE.setSendEvent(eventCount);
				/* Analizamos la cola del PE */
				long queuePE = statusPE.getQueueEvent() - eventCount;
				if (queuePE > 0)
					statusPE.setQueueEvent(queuePE);
				else
					statusPE.setQueueEvent(0);
			}
		}

		/*
		 * Asignar el valor de la tasa de llegada de cada uno de los distintos
		 * PE. Es decir, λ Tasa de llegada.
		 */
		for (Class<? extends ProcessingElement> recibe : listPERecibe) {
			for (StatusPE statusPE : getStatusSystem()) {
				if (recibe.equals(statusPE.getPE())) {
					statusPE.setRecibeEvent(statusPE.getRecibeEvent()
							+ eventCount);
					/* Analizamos la cola del PE */
					statusPE.setQueueEvent(statusPE.getQueueEvent()
							+ eventCount);
				}
			}
		}
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

		/*
		 * Posteriormente, se analiza la tasa de rendimiento del PE, para
		 * verificar si efectivamente es necesario una replicación. Es decir ρ =
		 * λ / (s*μ) ; donde s cantidad de replicas del PE
		 */
		long λ = statusPE.getRecibeEvent();
		long μ = statusPE.getSendEvent();
		double ρ = 0;
		if (μ != 0) {
			ρ = (double) λ / (double) μ;
		} else if ((λ == 0) && (μ == 0)) {
			ρ = 1;
		} else {
			ρ = Double.POSITIVE_INFINITY;
		}

		/*
		 * Análisis de ρ para ver si debe aumentar, mantener o disminuir la
		 * cantidad de réplicas de cierto PE
		 */
		if (ρ > 1.5) {
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

		/* Cálculo de la predicción por parte de la Cadena de Markov */
		double distEstacionaria[] = markovChain.calculatePrediction(rho, 10,
				100000);

		/* Análisis estadístico de los resultados de la predicción */
		DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(
				distEstacionaria);

		/*
		 * En caso de existir una desviación estándar mayor al umbral, se tomará
		 * como antecedente para la replicación de la historia analizada.
		 */
		if (descriptiveStatistics.getStandardDeviation() > 0.25) {
			for (int i = 0; i < distEstacionaria.length; i++) {
				if (distEstacionaria[i] == descriptiveStatistics.getMax()) {
					if (i == 0)
						return -1;
					else if (i == 1)
						return 0;
					else
						return 1;
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

		/*
		 * Se analiza el cálculo reactivo de cierto PE, en caso de necesitar
		 * alguna modificación se marca que esto es necesario de no ser asi. Se
		 * prosigue con el siguiente cálculo de predicción
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
		 * En el cálculo de predicción se toma en consideración el mismo
		 * procedimiento. De realizarse esto, se tomará en consideración el
		 * procesamiento para poder analizar si efectivamente, es necesario una
		 * replicación.
		 */

		// int resultPredictive = predictiveLoad(statusPE);
		int resultPredictive = 0;

		if (resultPredictive == 1) {
			statusPE.getMarkMap().add(1);
		} else if (resultPredictive == -1) {
			statusPE.getMarkMap().add(-1);
		} else {
			statusPE.getMarkMap().add(0);
		}

		/*
		 * En caso de aumentar ese umbral, se proceserá a realizar una
		 * modificación de este procedimiento. En este caso, si se notifica dos
		 * veces que es necesario un incremento o decremento de las réplicas del
		 * PE, se tomará una decisión.
		 */

		/*
		 * Para análisis de prueba, se considerarán los últimos 3 períodos para
		 * verificar si es necesario replicar.
		 * 
		 * Ej: Historia = [1,0,0,-1,0,1] Donde el primer período de tiempo dio
		 * (1,0), el segundo período (0,-1) y el tercero (0,1). Por lo tanto,
		 * encuentra que existen 2 señales de que debe aumentar, por lo tanto,
		 * aumentará. Esto debido que el reactivo del primero período y el
		 * predictor del tercer período indicaron que debe aumentar.
		 */

		// logger.debug("[{}] MarkMap: {}", new String[] {
		// statusPE.getPE().getCanonicalName(),
		// statusPE.getMarkMap().toString() });

		if (statusPE.getMarkMap().containsAll(Arrays.asList(1, 1))) {
			statusPE.getMarkMap().clear();
			return 1;
		} else if (statusPE.getMarkMap().containsAll(Arrays.asList(-1, -1))) {
			statusPE.getMarkMap().clear();
			return -1;
		}

		return 0;

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

	private boolean analyzeStatus(Class<? extends ProcessingElement> data,
			long recibeEvent, boolean replication) {

		for (StatusPE statusPE : getStatusSystem()) {
			if (statusPE.getPE().equals(data)) {

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

					if (ρ > 1.5) {
						metrics.counterReplicationPE(statusPE.getPE()
								.getCanonicalName(), true);
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
						metrics.counterReplicationPE(statusPE.getPE()
								.getCanonicalName(), false);
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
			if (peEmitter.getClass().equals(topology.getPeSend())) {
				/*
				 * En caso que el análisis del nuevo flujo haya modificado el PE
				 * receptor, se deberá realizar el mismo procedimiento con los
				 * PE receptores de PE receptor analizado.
				 */
				if (analyzeStatus(topology.getPeRecibe(),
						peEmitter.getSendEvent(), replication)) {
					intelligentReplication(peEmitter, replication);
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

	public List<StatusPE> askStatus() {

		for (StatusPE statusPE : getStatusSystem()) {

			int status = administrationLoad(statusPE);

			// logger.debug("[Finish administrationLoad] PE: " +
			// statusPE.getPE()
			// + " | status: " + status);

			/*
			 * Se entenderá que debe replicarse si retornar el valor 1, por lo
			 * tanto el estado del sistema se le añade una réplica. En el caso
			 * que sea -1, se deberá disminuir en uno.
			 * 
			 * Posteriormente se realizará una replicación de forma inteligente,
			 * de tal manera de aumentar o disminuir automáticamente los PEs
			 * afectados por la replicación.
			 */
			if (status == 1) {

				metrics.counterReplicationPE(statusPE.getPE()
						.getCanonicalName(), true);
				statusPE.setReplication(statusPE.getReplication() + 1);
				intelligentReplication(statusPE, true);

			} else if (status == -1) {

				if (statusPE.getReplication() > 1) {
					metrics.counterReplicationPE(statusPE.getPE()
							.getCanonicalName(), false);
					statusPE.setReplication(statusPE.getReplication() - 1);
					intelligentReplication(statusPE, false);
				}

			}

		}

		metricsStatusSystem();
		clearStatusSystem();

		return getStatusSystem();
	}

	/**
	 * registerStatusSystem registará en las métricas las estadísticas
	 * solicitadas por parte de MonitorMetrics
	 */
	private void metricsStatusSystem() {
		for (StatusPE statusPE : statusSystem) {
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
			metrics.gaugeAvgEventSystem(statusPE.getPE().getCanonicalName(), En);

			/* Número esperado de eventos en la cola */
			double Eq = (Math.pow(λ, 2)) / ((μ - λ) * μ);
			metrics.gaugeAvgEventQueue(statusPE.getPE().getCanonicalName(), Eq);

			/* Tiempo promedio de residencia */
			double Et = 1 / (μ - λ);
			metrics.gaugeAvgTimeResident(statusPE.getPE().getCanonicalName(),
					Et);

			/* Tiempo promedio de espera en la cola */
			double Ed = ρ / (μ - λ);
			metrics.gaugeAvgTimeQueue(statusPE.getPE().getCanonicalName(), Ed);

			/* Tiempo promedio en el sistema */
			double Ep = Et + Ed;
			metrics.gaugeAvgTimeProcess(statusPE.getPE().getCanonicalName(), Ep);
		}
	}

	/**
	 * clearStatusSystem asignará las tasas de llegada y procesamiento en 0.
	 * Esto se realizó con el fin de no poseer problemas con el próximo período
	 * analizado.
	 */
	private void clearStatusSystem() {
		for (StatusPE statusPE : statusSystem) {
			statusPE.setRecibeEvent(0);
			statusPE.setSendEvent(0);
		}
	}

	/**
	 * replicationPE corresponde a la replicación de un PE determinado
	 * 
	 * @param pe
	 *            PE a replicar
	 */
	public void replicationPE(Class<? extends ProcessingElement> pe) {
		for (StatusPE statusPE : getStatusSystem()) {
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
	public List<StatusPE> getStatusSystem() {
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

}
