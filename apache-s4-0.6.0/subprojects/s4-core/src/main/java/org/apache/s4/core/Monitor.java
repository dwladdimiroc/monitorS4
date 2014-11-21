package org.apache.s4.core;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitor {
	private static Logger logger = LoggerFactory.getLogger(Monitor.class);

	private static List<TopologyApp> topologySystem = new ArrayList<TopologyApp>();
	private static List<StatusPE> statusSystem = new ArrayList<StatusPE>();

	public void registerPE(Class<? extends ProcessingElement> peSend,
			Class<? extends ProcessingElement> peRecibe) {
		logger.debug("registerPE");
		TopologyApp topology = new TopologyApp();
		topology.setPeSend(peSend);
		topology.setPeRecibe(peRecibe);
		topologySystem.add(topology);

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
			statusSystem.add(statusPE);
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
		for (int i = 0; i < statusSystem.size(); i++) {
			if (data.equals(statusSystem.get(i).getPe())) {
//				logger.debug("StatusSystem " + statusSystem.get(i).getPe());
				statusSystem.get(i).setSendEvent(eventCount);
				break;
			}
		}

		// Asignar el valor de procesamiento de si mismo
		for (int i = 0; i < statusSystem.size(); i++) {
			for (Class<? extends ProcessingElement> recibe : listPERecibe) {
				if (recibe.equals(statusSystem.get(i).getPe())) {
//					logger.debug("StatusSystem " + statusSystem.get(i).getPe());
					statusSystem.get(i).setRecibeEvent(eventCount);
				}
			}
		}
	}

	public boolean distributedLoad(Class<? extends ProcessingElement> data) {
		for (int i = 0; i < statusSystem.size(); i++) {
			if (data.equals(statusSystem.get(i).getPe())) {
				logger.debug("Class: " + data);
				logger.debug("SendEvent: " + statusSystem.get(i).getSendEvent());
				logger.debug("RecibeEvent: "
						+ statusSystem.get(i).getRecibeEvent());
				if (statusSystem.get(i).getSendEvent() < statusSystem.get(i)
						.getRecibeEvent()) {
					logger.debug("distributedLoad");
					return true;
				} else {
					return false;
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

		public StatusPE() {
			recibeEvent = 0;
			sendEvent = 0;
			pe = null;
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
	}

}
