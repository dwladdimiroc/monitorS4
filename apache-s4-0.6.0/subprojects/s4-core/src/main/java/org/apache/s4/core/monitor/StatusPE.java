package org.apache.s4.core.monitor;

import java.io.Serializable;
import java.util.Queue;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.s4.core.ProcessingElement;

/**
 * Clase para analizar el estado de cada uno de los PE, según su tasa de llegada
 * y tasa de servicio, además de su replicación.
 * 
 * Un dato importante es que markMap está inicializado con 2, debido que se
 * consideran los últimos 2 períodos de análisis de los PE.
 *
 */

public class StatusPE implements Serializable {

	private static final long serialVersionUID = 1L;
	private String stream;

	private long recibeEvent;
	private long sendEvent;
	private double sendEventUnit;
	private double sendEventPeriod;
	private double processEvent;
	private long queueEvent;
	private Queue<Double> history;
	private Class<? extends ProcessingElement> pe;
	private int replication;
	private Queue<Integer> markMap;
	private long eventCount;

	public StatusPE() {
		stream = null;
		recibeEvent = 0;
		sendEvent = 0;
		sendEventUnit = 0;
		sendEventPeriod = 0;
		processEvent = 0;
		queueEvent = 0;
		history = new CircularFifoQueue<Double>(100);
		pe = null;
		replication = 0;
		markMap = new CircularFifoQueue<Integer>(2);
		eventCount = 0;
	}

	public String getStream() {
		return stream;
	}

	public void setStream(String stream) {
		this.stream = stream;
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

	public double getSendEventUnit() {
		return sendEventUnit;
	}

	public void setSendEventUnit(double sendEventUnit) {
		this.sendEventUnit = sendEventUnit;
	}

	public double getSendEventPeriod() {
		return sendEventPeriod;
	}

	public void setSendEventPeriod(double sendEventPeriod) {
		this.sendEventPeriod = sendEventPeriod;
	}

	public void setSendEventPeriod(long sendEventPeriod, int period) {
		this.sendEventPeriod = (double) (this.sendEventPeriod * (period - 1) + sendEventPeriod)
				/ (double) period;
	}

	public double getProcessEvent() {
		return processEvent;
	}

	public void setProcessEvent(double processEvent) {
		this.processEvent = processEvent;
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

	public long getEventCount() {
		return eventCount;
	}

	public void setEventCount(long eventCount) {
		this.eventCount = eventCount;
	}

	@Override
	public String toString() {
		return "[PE : " + pe.toString() + " | Recibe: " + recibeEvent
				+ " | Send: " + sendEvent + " | Replication: " + replication
				+ "]";
	}

}