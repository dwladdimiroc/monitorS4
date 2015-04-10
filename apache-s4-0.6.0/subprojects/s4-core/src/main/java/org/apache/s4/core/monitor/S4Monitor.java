package org.apache.s4.core.monitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
			getTopologySystem().add(topology);
		}

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
				// logger.debug("StatusSystem " + statusSystem.get(i).getPe());
				/*
				 * Se debe realizar una diferencia, debido que son el total de
				 * eventos procesos en ese período menos el ya procesados. De
				 * esta manera, obtendremos el número de eventos en el Δt
				 * deseado.
				 */
				statusPE.setSendEvent(eventCount - statusPE.getSendEvent());

				// DBObject objMongo = new BasicDBObject();
				// objMongo.put("PE", data.toString());
				// objMongo.put("time", System.nanoTime());
				// long queuePE = statusSystem.get(i).getRecibeEvent()
				// - statusSystem.get(i).getSendEvent();
				// objMongo.put("queue", queuePE);
				// mongo.insert(objMongo);
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
					 * Nuevamente lo mismo expresado anteriormente del número de
					 * eventos procesados en un Δt deseado.
					 */

					statusPE.setRecibeEvent(eventCount
							- statusPE.getRecibeEvent());
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
		for (TopologyApp topology : getTopologySystem()) {
			if (data.equals(topology.getPeSend())) {
				listPE.add(topology.getPeRecibe());
			}
		}

		/*
		 * Posteriormente, analizamos todos los PE que reciben y ver si existe
		 * un rendimiento bajo el establecido.
		 */
		for (StatusPE statusPE : getStatusSystem()) {
			/*
			 * Es decir ρ = λ / (s*μ) ; donde s cantidad de replicas del PE
			 */
			for (Class<? extends ProcessingElement> recibe : listPE) {
				if (recibe.equals(statusPE.getPE())) {
					/*
					 * Replicacion simple, es decir, si la tasa de llegada es
					 * mayor a la de servicio, modifica el indice de
					 * replicación. Otra forma de analizar es con ρ.
					 */
					if ((statusPE.getReplication() == 0)
							|| (statusPE.getSendEvent() == 0)) {
						// Preguntarle a Daniel del pasado...
						return 0;
					}

					// long λ = statusSystem.get(i).getRecibeEvent();
					// long s = statusSystem.get(i).getReplication();
					// long μ = statusSystem.get(i).getSendEvent();

					float ρ = statusPE.getRecibeEvent()
							/ (statusPE.getReplication() * statusPE
									.getSendEvent());

					// logger.debug("PE " + statusSystem.get(i).getPe() +
					// " | ρ "
					// + ρ);

					/*
					 * Análisis de ρ para ver si debe aumentar, mantener o
					 * disminuir la cantidad de réplicas de cierto PE
					 */
					if (ρ > 1.5) {
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

		for (StatusPE statusPE : getStatusSystem()) {
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
				 * 
				 * Ej: Historia = [1,0,0,-1,0,1] Donde el primer período de
				 * tiempo dio (1,0), el segundo período (0,-1) y el tercero
				 * (0,1). Por lo tanto, encuentra que existen 2 señales de que
				 * debe aumentar, por lo tanto, aumentará. Esto debido que el
				 * reactivo del primero período y el predictor del tercer
				 * período indicaron que debe aumentar.
				 */

				if (statusPE.getMarkMap().containsAll(Arrays.asList(1, 1))) {
					statusPE.getMarkMap().clear();
					return 1;
				} else if (statusPE.getMarkMap().containsAll(
						Arrays.asList(-1, -1))) {
					statusPE.getMarkMap().clear();
					return -1;
				}

				return 0;
			}
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

	public boolean analyzeStatus(Class<? extends ProcessingElement> data,
			long recibeEvent, boolean replication) {

		for (StatusPE statusPE : getStatusSystem()) {
			if (statusPE.getClass().equals(data)) {

				/*
				 * Análisis de ρ para ver si debe aumentar, mantener o disminuir
				 * la cantidad de réplicas de cierto PE dada la replicación del
				 * PE que le provee eventos.
				 */

				float ρ;

				if (replication) {
					ρ = (statusPE.getRecibeEvent() + recibeEvent)
							/ (statusPE.getReplication() * statusPE
									.getSendEvent());

					if (ρ > 1.5) {
						statusPE.setReplication(statusPE.getReplication() + 1);
						return true;
					}

				} else {
					ρ = (statusPE.getRecibeEvent() - recibeEvent)
							/ (statusPE.getReplication() * statusPE
									.getSendEvent());

					if (ρ < 0.5) {
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

	public void intelligentReplication(StatusPE peEmitter, boolean replication) {

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

			int status = administrationLoad(statusPE.getPE());

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

				statusPE.setReplication(statusPE.getReplication() + 1);
				intelligentReplication(statusPE, true);

			} else if (status == -1) {

				statusPE.setReplication(statusPE.getReplication() - 1);
				intelligentReplication(statusPE, false);

			}

		}

		return getStatusSystem();
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

}
