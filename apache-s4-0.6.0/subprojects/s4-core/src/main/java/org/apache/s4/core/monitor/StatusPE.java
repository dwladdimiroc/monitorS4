package org.apache.s4.core.monitor;

import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.s4.core.ProcessingElement;

/**
 * Clase para analizar el estado de cada uno de los PE, según su tasa de llegada
 * y tasa de servicio, además de su replicación.
 * 
 * Un dato importante es que markMap está inicializado con 6, debido que se
 * consideran los últimos 3 períodos de análisis de los PE. Son 6 debido que se
 * considerán tanto el del predictor como el del reactivo, por lo tanto cada par
 * es un período de tiempo.
 *
 */

public class StatusPE {
	private long recibeEvent;
	private long sendEvent;
	private long queueEvent;
	private Queue<Double> history;
	private String name;
	private Class<? extends ProcessingElement> pe;
	private int replication;
	private Queue<Integer> markMap;

	public StatusPE() {
		recibeEvent = 0;
		sendEvent = 0;
		queueEvent = 0;
		history = new CircularFifoQueue<Double>(25);
		name = null;
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

	public long getQueueEvent() {
		return queueEvent;
	}

	public void setQueueEvent(long queueEvent) {
		this.queueEvent = queueEvent;
	}

	public Queue<Double> getHistory() {
		return history;
	}

	public void setHistory(Queue<Double> history) {
		this.history = history;
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