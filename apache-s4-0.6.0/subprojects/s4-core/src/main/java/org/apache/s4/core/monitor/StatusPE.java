package org.apache.s4.core.monitor;

import java.util.Map;
import java.util.TreeMap;

import org.apache.s4.core.ProcessingElement;

/**
 * Clase para analizar el estado de cada uno de los PE, según su tasa de llegada
 * y tasa de servicio, además de su replicación.
 *
 */

public class StatusPE {
	private long recibeEvent;
	private long sendEvent;
	private Class<? extends ProcessingElement> pe;
	private int replication;
	private Map<Long, Integer> markMap;

	public StatusPE() {
		recibeEvent = 0;
		sendEvent = 0;
		pe = null;
		replication = 0;
		markMap = new TreeMap<Long, Integer>();
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

	public Map<Long, Integer> getMap() {
		return markMap;
	}

	public void setMap(Map<Long, Integer> map) {
		this.markMap = map;
	}

	@Override
	public String toString() {
		return "[PE : " + pe.toString() + " | Recibe: " + recibeEvent
				+ " | Send: " + sendEvent + " | Replication: " + replication
				+ "]";
	}
	
}