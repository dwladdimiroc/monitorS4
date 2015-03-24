package org.apache.s4.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.s4.base.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class S4Monitor {
	private Logger logger = LoggerFactory.getLogger(S4Monitor.class);

	private static List<TopologyApp> topologySystem = new ArrayList<TopologyApp>();
	private static List<StatusPE> statusSystem = new ArrayList<StatusPE>();
	MongoConnection mongo;

	public S4Monitor() {
		mongo = new MongoConnection();
		mongo.setCollectionName("queue");
		mongo.setupMongo();
	}

	public void startS4Monitor() {
		while (true) {

			logger.info("On Monitor!");

			for (Streamable<Event> stream : app.getStreams()) {
				for (ProcessingElement PEPrototype : stream.getTargetPEs()) {
					for (ProcessingElement PE : PEPrototype.getInstances()) {
						sendStatus(PE.getClass(), PE.getEventCount());
					}
				}
			}

			for (Streamable<Event> stream : app.getStreams()) {
				for (ProcessingElement PEPrototype : stream.getTargetPEs()) {
					for (ProcessingElement PE : PEPrototype.getInstances()) {
						if (distributedLoad(PE.getClass())) {
							logger.debug("Replication PE: " + PE.getClass());
						}
					}
				}
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.error(e.toString());
			}
		}
	}

	public void registerPE(Class<? extends ProcessingElement> peSend,
			Class<? extends ProcessingElement> peRecibe) {
		/* logger.debug("registerPE"); */
		if (peRecibe != null) {
			TopologyApp topology = new TopologyApp();
			topology.setPeSend(peSend);
			topology.setPeRecibe(peRecibe);
			topologySystem.add(topology);
		}

		boolean exist = false;
		for (int i = 0; i < statusSystem.size(); i++) {
			if (peSend.equals(statusSystem.get(i).getPe())) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			StatusPE statusPE = new StatusPE();
			statusPE.setPe(peSend);
			statusPE.setRecibeEvent(0);
			statusPE.setSendEvent(0);
			statusPE.setReplication(0);
			statusSystem.add(statusPE);
		}
	}

	public void replicationPE(Class<? extends ProcessingElement> pe) {
		for (int i = 0; i < statusSystem.size(); i++) {
			if (pe.equals(statusSystem.get(i).getPe())) {
				statusSystem.get(i).setReplication(
						statusSystem.get(i).getReplication() + 1);
				break;
			}
		}

	}

	public void sendStatus(Class<? extends ProcessingElement> data,
			Long eventCount) {
		List<Class<? extends ProcessingElement>> listPERecibe = new ArrayList<Class<? extends ProcessingElement>>();
		// logger.debug("sendStatus");

		// Obtener todos las clases a donde de ir el PE
		for (TopologyApp topology : topologySystem) {
			if (data.equals(topology.getPeSend())) {
				listPERecibe.add(topology.getPeRecibe());
			}
		}

		// Asignar el valor de procesamiento de si mismo
		// Es decir, μ tasa de servicio
		for (int i = 0; i < statusSystem.size(); i++) {
			if (data.equals(statusSystem.get(i).getPe())) {
				// logger.debug("StatusSystem " + statusSystem.get(i).getPe());
				statusSystem.get(i).setSendEvent(eventCount);

				DBObject objMongo = new BasicDBObject();
				objMongo.put("PE", data.toString());
				objMongo.put("time", System.nanoTime());
				long queuePE = statusSystem.get(i).getRecibeEvent()
						- statusSystem.get(i).getSendEvent();
				objMongo.put("queue", queuePE);
				mongo.insert(objMongo);
				break;
			}
		}

		// Asignar el valor de la tasa de llegada de cada uno de los distintos
		// PE
		// Es decir, λ Tasa de llegada
		for (int i = 0; i < statusSystem.size(); i++) {
			for (Class<? extends ProcessingElement> recibe : listPERecibe) {
				if (recibe.equals(statusSystem.get(i).getPe())) {
					// logger.debug("StatusSystem " +
					// statusSystem.get(i).getPe());
					statusSystem.get(i).setRecibeEvent(eventCount);
				}
			}
		}
	}

	public boolean distributedLoad(Class<? extends ProcessingElement> data) {
		// Replicacion en base al siguiente algoritmo

		// Como la replicacion se hace con el juego de llaves del PE que envia
		// se debe buscar todos los PEs que reciben del PE que modificaremos
		List<Class<? extends ProcessingElement>> listPE = new ArrayList<Class<? extends ProcessingElement>>();
		for (TopologyApp topology : topologySystem) {
			if (data.equals(topology.getPeSend())) {
				listPE.add(topology.getPeRecibe());
			}
		}

		// Posteriormente, analizamos todos los PE que reciben
		// y ver si existe un rendimiento bajo el establecido
		for (int i = 0; i < statusSystem.size(); i++) {
			// es decir ρ = λ / (s*μ) ; donde s cantidad de replicas del PE
			for (Class<? extends ProcessingElement> recibe : listPE) {
				if (recibe.equals(statusSystem.get(i).getPe())) {
					// Replicacion simple, es decir, si la tasa de llegada es
					// mayor a la de servicio, modifica el indice de replicacion
					/*
					 * if (statusSystem.get(i).getSendEvent() < statusSystem
					 * .get(i).getRecibeEvent()) {
					 * logger.debug("distributedLoad"); return true; }
					 */
					// Otra forma de analizar es con ρ
					if ((statusSystem.get(i).getReplication() == 0)
							|| (statusSystem.get(i).getSendEvent() == 0)) {
						return false;
					}

					long ρ = statusSystem.get(i).getRecibeEvent()
							/ (statusSystem.get(i).getReplication() * statusSystem
									.get(i).getSendEvent());
					/*
					 * logger.debug("PE " + statusSystem.get(i).getPe() +
					 * " | ρ " + ρ);
					 */

					if (ρ > 1) {
						// logger.debug("distributedLoad");
						return true;
					}
				}
			}
		}

		return false;
	}

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
	}

	public class StatusPE {
		private long recibeEvent;
		private long sendEvent;
		private Class<? extends ProcessingElement> pe;
		private int replication;

		public StatusPE() {
			recibeEvent = 0;
			sendEvent = 0;
			pe = null;
			replication = 0;
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

		public Class<? extends ProcessingElement> getPe() {
			return pe;
		}

		public void setPe(Class<? extends ProcessingElement> pe) {
			this.pe = pe;
		}

		public int getReplication() {
			return replication;
		}

		public void setReplication(int replication) {
			this.replication = replication;
		}
	}

}
