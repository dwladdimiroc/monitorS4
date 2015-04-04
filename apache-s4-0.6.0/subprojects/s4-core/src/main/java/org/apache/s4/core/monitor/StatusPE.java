package org.apache.s4.core.monitor;

import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
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
	private Queue<Integer> markMap;

	public StatusPE() {
		recibeEvent = 0;
		sendEvent = 0;
		pe = null;
		replication = 0;
		markMap = new CircularFifoQueue<Integer>(6);
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

	public Queue<Integer> getMarkMap() {
		return markMap;
	}

	public void setMarkMap(Queue<Integer> markMap) {
		this.markMap = markMap;
	}

	@Override
	public String toString() {
		return "[PE : " + pe.toString() + " | Recibe: " + recibeEvent
				+ " | Send: " + sendEvent + " | Replication: " + replication
				+ "]";
	}
	
}