package org.apache.s4.core.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class S4Monitor {
	private Logger logger = LoggerFactory.getLogger(S4Monitor.class);

	private final List<TopologyApp> topologySystem = new ArrayList<TopologyApp>();
	private final List<StatusPE> statusSystem = new ArrayList<StatusPE>();

	/**
	 * Inicialización del monitor, dada la inyección de la clase
	 */
	@Inject
	private void init() {
		logger.info("Init Monitor");

		// Correr demonio para el análisis del predictivo y reactivo

	}

	public void startS4Monitor(App app) {
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

		if (peRecibe != null) {
			TopologyApp topology = new TopologyApp();
			topology.setPeSend(peSend);
			topology.setPeRecibe(peRecibe);
			topologySystem.add(topology);
		}

		boolean exist = false;
		for (int i = 0; i < statusSystem.size(); i++) {
			if (peSend.equals(statusSystem.get(i).getPE())) {
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
			statusSystem.add(statusPE);
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
		// logger.debug("sendStatus");

		/*
		 * Obtener todos las clases a donde de ir el PE.
		 */
		for (TopologyApp topology : topologySystem) {
			if (data.equals(topology.getPeSend())) {
				listPERecibe.add(topology.getPeRecibe());
			}
		}

		/*
		 * Asignar el valor de procesamiento de si mismo. Es decir, μ tasa de
		 * servicio.
		 */
		for (int i = 0; i < statusSystem.size(); i++) {
			if (data.equals(statusSystem.get(i).getPE())) {
				// logger.debug("StatusSystem " + statusSystem.get(i).getPe());
				/*
				 * Se debe realizar una diferencia, debido que son el total de
				 * eventos procesos en ese período menos el ya procesados. De
				 * esta manera, obtendremos el número de eventos en el Δt
				 * deseado.
				 */
				statusSystem.get(i).setSendEvent(
						eventCount - statusSystem.get(i).getSendEvent());

				// DBObject objMongo = new BasicDBObject();
				// objMongo.put("PE", data.toString());
				// objMongo.put("time", System.nanoTime());
				// long queuePE = statusSystem.get(i).getRecibeEvent()
				// - statusSystem.get(i).getSendEvent();
				// objMongo.put("queue", queuePE);
				// mongo.insert(objMongo);
				break;
			}
		}

		/*
		 * Asignar el valor de la tasa de llegada de cada uno de los distintos
		 * PE. Es decir, λ Tasa de llegada.
		 */
		for (int i = 0; i < statusSystem.size(); i++) {
			for (Class<? extends ProcessingElement> recibe : listPERecibe) {
				if (recibe.equals(statusSystem.get(i).getPE())) {
					// logger.debug("StatusSystem " +
					// statusSystem.get(i).getPe());

					/*
					 * Nuevamente lo mismo expresado anteriormente del número de
					 * eventos procesados en un Δt deseado.
					 */

					statusSystem.get(i).setRecibeEvent(
							eventCount - statusSystem.get(i).getRecibeEvent());
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
	public int reactiveLoad(Class<? extends ProcessingElement> data) {

		/*
		 * Como la replicacion se hace con el juego de llaves del PE que envia
		 * se debe buscar todos los PEs que reciben del PE que modificaremos.
		 */
		List<Class<? extends ProcessingElement>> listPE = new ArrayList<Class<? extends ProcessingElement>>();
		for (TopologyApp topology : topologySystem) {
			if (data.equals(topology.getPeSend())) {
				listPE.add(topology.getPeRecibe());
			}
		}

		/*
		 * Posteriormente, analizamos todos los PE que reciben y ver si existe
		 * un rendimiento bajo el establecido.
		 */
		for (int i = 0; i < statusSystem.size(); i++) {
			/*
			 * Es decir ρ = λ / (s*μ) ; donde s cantidad de replicas del PE
			 */
			for (Class<? extends ProcessingElement> recibe : listPE) {
				if (recibe.equals(statusSystem.get(i).getPE())) {
					/*
					 * Replicacion simple, es decir, si la tasa de llegada es
					 * mayor a la de servicio, modifica el indice de
					 * replicación. Otra forma de analizar es con ρ.
					 */
					if ((statusSystem.get(i).getReplication() == 0)
							|| (statusSystem.get(i).getSendEvent() == 0)) {
						// Preguntarle a Daniel del pasado...
						return 0;
					}

					// long λ = statusSystem.get(i).getRecibeEvent();
					// long s = statusSystem.get(i).getReplication();
					// long μ = statusSystem.get(i).getSendEvent();

					long ρ = statusSystem.get(i).getRecibeEvent()
							/ (statusSystem.get(i).getReplication() * statusSystem
									.get(i).getSendEvent());

					// logger.debug("PE " + statusSystem.get(i).getPe() +
					// " | ρ "
					// + ρ);

					/*
					 * Análisis de ρ para ver si debe aumentar, mantener o
					 * disminuir la cantidad de réplicas de cierto PE
					 */
					if (ρ > 1) {
						// logger.debug("Increment");
						return 1;
					} else if (ρ < 0.5) {
						// logger.debug("Decrement");
						return -1;
					}
				}
			}
		}

		return 0;
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
	public int predictiveLoad(Class<? extends ProcessingElement> data) {
		return 0;
	}

	/*
	 * El cálculo de los demás entes, es mejor analizar de forma inteligente,
	 * según el número de ρ
	 */

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
	public int administrationLoad(Class<? extends ProcessingElement> data) {
		for (StatusPE statusPE : statusSystem) {
			if (data.equals(statusPE.getClass())) {

				/*
				 * Se analiza el cálculo reactivo de cierto PE, en caso de
				 * necesitar alguna modificación se marca que esto es necesario
				 * de no ser asi. Se prosigue con el siguiente cálculo de
				 * predicción
				 */

				int resultReactive = reactiveLoad(data);

				if (resultReactive == 1) {
					statusPE.getMarkMap().add(1);
				} else if (resultReactive == -1) {
					statusPE.getMarkMap().add(-1);
				} else {
					statusPE.getMarkMap().add(0);
				}

				/*
				 * En el cálculo de predicción se toma en consideración el mismo
				 * procedimiento. De realizarse esto, se tomará en consideración
				 * el procesamiento para poder analizar si efectivamente, es
				 * necesario una replicación.
				 */

				int resultPredictive = predictiveLoad(data);

				if (resultPredictive == 1) {
					statusPE.getMarkMap().add(1);
				} else if (resultPredictive == -1) {
					statusPE.getMarkMap().add(-1);
				} else {
					statusPE.getMarkMap().add(0);
				}

				/*
				 * En caso de aumentar ese umbral, se proceserá a realizar una
				 * modificación de este procedimiento. En este caso, si se
				 * notifica dos veces que es necesario un incremento o
				 * decremento de las réplicas del PE, se tomará una decisión.
				 */

				/*
				 * Para análisis de prueba, se considerarán los últimos 3
				 * períodos para verificar si es necesario replicar.
				 */

				if (statusPE.getMarkMap().containsAll(Arrays.asList(1, 1))) {
					return 1;
				} else if (statusPE.getMarkMap().containsAll(
						Arrays.asList(-1, -1))) {
					return -1;
				}

				return 0;
			}
		}

		return 0;
	}

	/**
	 * replicationPE corresponde a la replicación de un PE determinado
	 * 
	 * @param pe
	 *            PE a replicar
	 */
	// No se usa está función, pero es bonita...
	public void replicationPE(Class<? extends ProcessingElement> pe) {
		for (int i = 0; i < statusSystem.size(); i++) {
			if (pe.equals(statusSystem.get(i).getPE())) {
				statusSystem.get(i).setReplication(
						statusSystem.get(i).getReplication() + 1);
				break;
			}
		}
	}

	/**
	 * Muestra la topología del sistema
	 * 
	 * @return un string con la topología del sistema
	 */
	public String mapTopology() {
		return topologySystem.toString();
	}

	/**
	 * Muestra el estado del sistema
	 * 
	 * @return un string con el estado del sistema
	 */
	public String mapStatusSystem() {
		return statusSystem.toString();
	}

}
