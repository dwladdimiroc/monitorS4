package org.apache.s4.core.monitor;

import org.apache.s4.core.ProcessingElement;

/**
 * Clase para analizar la topología del grafo, de esta manera tendremos el PE
 * emisor y receptor, además del flujo de datos entre ellos
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
