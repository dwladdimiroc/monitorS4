package org.apache.s4.core;

import java.util.ArrayList;
import java.util.List;

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
	 *         -1 se aumenta.
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

					if (ρ > 1) {
						// logger.debug("distributedLoad");
						return 1;
					} else if (ρ < 0.5) {
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
	 * calculateLoad corresponde al cálculo de los distintos PE del sistema
	 * 
	 */
	public void calculateLoad() {
		for (int i = 0; i < statusSystem.size(); i++) {
			if (reactiveLoad(statusSystem.get(i).getPE()) == 1) {
				statusSystem.get(i).setMarkIncrement(statusSystem.get(i).getMarkIncrement() + 1);
			} else if (reactiveLoad(statusSystem.get(i).getPE()) == -1) {
				statusSystem.get(i).setMarkDecrement(statusSystem.get(i).getMarkDecrement() + 1);
			}

			if (predictiveLoad(statusSystem.get(i).getPE()) == 1) {
				statusSystem.get(i).setMarkIncrement(statusSystem.get(i).getMarkIncrement() + 1);
			} else if (predictiveLoad(statusSystem.get(i).getPE()) == -1) {
				statusSystem.get(i).setMarkDecrement(statusSystem.get(i).getMarkDecrement() + 1);
			}
		}
	}

	/**
	 * replicationPE corresponde a la replicación de un PE determinado
	 * 
	 * @param pe
	 *            PE a replicar
	 */
	public void replicationPE(Class<? extends ProcessingElement> pe) {
		for (int i = 0; i < statusSystem.size(); i++) {
			if (pe.equals(statusSystem.get(i).getPE())) {
				statusSystem.get(i).setReplication(
						statusSystem.get(i).getReplication() + 1);
				break;
			}
		}
	}

	public String mapTopology() {
		return topologySystem.toString();
	}

	public String mapStatusSystem() {
		return statusSystem.toString();
	}

	/**
	 * Clase para analizar la topología del grafo, de esta manera tendremos el
	 * PE emisor y receptor, además del flujo de datos entre ellos
	 *
	 */

	public class TopologyApp {
		private Class<? extends ProcessingElement> peSend;
		private Class<? extends ProcessingElement> peRecibe;
		private long eventSend;

		public TopologyApp() {
			peSend = null;
			peRecibe = null;
			eventSend = 0;
		}

		public Class<? extends ProcessingElement> getPeSend() {
			return peSend;
		}

		public void setPeSend(Class<? extends ProcessingElement> peSend) {
			this.peSend = peSend;
		}

		public Class<? extends ProcessingElement> getPeRecibe() {
			return peRecibe;
		}

		public void setPeRecibe(Class<? extends ProcessingElement> peRecibe) {
			this.peRecibe = peRecibe;
		}

		public long getEventSend() {
			return eventSend;
		}

		public void setEventSend(long eventSend) {
			this.eventSend = eventSend;
		}

		@Override
		public String toString() {
			return "[PE Send: " + peSend.toString() + " | PE Recibe: "
					+ peRecibe.toString() + " | Event: " + eventSend + "]";
		}
	}

	/**
	 * Clase para analizar el estado de cada uno de los PE, según su tasa de
	 * llegada y tasa de servicio, además de su replicación.
	 */

	public class StatusPE {
		private long recibeEvent;
		private long sendEvent;
		private Class<? extends ProcessingElement> pe;
		private int replication;
		private int markIncrement;
		private int markDecrement;

		public StatusPE() {
			recibeEvent = 0;
			sendEvent = 0;
			pe = null;
			replication = 0;
			setMarkIncrement(0);
			setMarkDecrement(0);
		}

		public long getRecibeEvent() {
			return recibeEvent;
		}

		public void setRecibeEvent(long recibeEvent) {
			this.recibeEvent = recibeEvent;
		}

		public long getSendEvent() {
			return sendEvent;
		}

		public void setSendEvent(long sendEvent) {
			this.sendEvent = sendEvent;
		}

		public Class<? extends ProcessingElement> getPE() {
			return pe;
		}

		public void setPE(Class<? extends ProcessingElement> pe) {
			this.pe = pe;
		}

		public int getReplication() {
			return replication;
		}

		public void setReplication(int replication) {
			this.replication = replication;
		}

		public int getMarkIncrement() {
			return markIncrement;
		}

		public void setMarkIncrement(int markIncrement) {
			this.markIncrement = markIncrement;
		}

		public int getMarkDecrement() {
			return markDecrement;
		}

		public void setMarkDecrement(int markDecrement) {
			this.markDecrement = markDecrement;
		}

		@Override
		public String toString() {
			return "[PE : " + pe.toString() + " | Recibe: " + recibeEvent
					+ " | Send: " + sendEvent + " | Replication: "
					+ replication + "]";
		}
	}

}
