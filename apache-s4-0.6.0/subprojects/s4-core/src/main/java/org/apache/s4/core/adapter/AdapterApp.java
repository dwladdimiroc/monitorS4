/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.s4.core.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.RemoteStream;
import org.apache.s4.core.Streamable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Base class for adapters. For now, it provides facilities for automatically
 * creating an output stream.
 * <p>
 * This class can be used for easing the injection of events into S4
 * applications.
 * 
 */
public abstract class AdapterApp extends App {

	@Inject
	@Named(value = "s4.adapter.output.stream")
	String outputStreamName;

	private RemoteStream remoteStream;
	final private Map<Class<? extends ProcessingElement>, Integer> replications = new HashMap<Class<? extends ProcessingElement>, Integer>();
	final private Queue<Map<Class<? extends AdapterApp>, Long>> historyAdapter = new CircularFifoQueue<>(
			25);

	protected KeyFinder<Event> remoteStreamKeyFinder;

	public RemoteStream getRemoteStream() {
		return remoteStream;
	}

	public long getEventSeg() {
		return remoteStream.getEventSeg();
	}

	public void setEventSeg(long eventSeg) {
		remoteStream.setEventSeg(eventSeg);
	}

	public long getEventPeriod() {
		return remoteStream.getEventPeriod();
	}

	public void setEventPeriod(long eventPeriod) {
		remoteStream.setEventPeriod(eventPeriod);
	}

	public long getEventCount() {
		return remoteStream.getEventCount();
	}

	public Class<? extends AdapterApp> getClassAdapter() {
		return this.getClass();
	}

	/**
	 * Se encargará de inicializar la llave para el PE que debe dirigirse, de
	 * tal manera de replicar sólo a ese PE en caso de aumentar su llave. Además
	 * de esto, se encargará de inicializar la llave para el PE que debe
	 * dirigirse, de tal manera de replicar sólo a ese PE en caso de aumentar su
	 * llave.
	 * 
	 * @param PE
	 *            receptor, donde estará siendo llamada la función por el PE
	 *            emisor.
	 */

	public void registerMonitor(Class<? extends ProcessingElement> peRecibe) {
		// Register adapter in the monitor
		// getMonitor().registerAdapter(this.getClass(), peRecibe);

		// Put replication with your class in the map of replications
		this.replications.put(peRecibe, 1);
	}

	/**
	 * Devolverá el módulo el cual se aplicará para replicar al siguiente PE
	 * 
	 * @param PE
	 *            PE al que se debe replicar
	 * @return Módulo aplicado a la llave, que será la cantidad de réplicas que
	 *         se desea de ese PE
	 */
	public int getReplicationPE(Class<? extends ProcessingElement> PE) {
		if (!replications.containsKey(PE))
			return 0;

		return replications.get(PE);
	}

	public Map<Class<? extends ProcessingElement>, Integer> getReplications() {
		return replications;
	}

	@Override
	protected void onStart() {
		/*
		 * Esta hebra se utilizará para enviar los datos de los distintos PEs
		 * cada un segundo, para poseer el historial de cada PE
		 */

		// ScheduledExecutorService getEventCount = Executors
		// .newSingleThreadScheduledExecutor();
		// getEventCount.scheduleAtFixedRate(new OnTimeGetEventCountAdapter(),
		// 0,
		// 1000, TimeUnit.MILLISECONDS);

	}

	/**
	 * Esta clase estará encargada de la correr la primera hebra, la cual
	 * enviará los eventCount de cada uno de los PEs cada un segundo. Para
	 * obtenerlos, se recurrirá a los distintos 'Streams' creandos en la
	 * aplicación de S4. Al obtener cada 'Stream', se extraerá los distintos PEs
	 * Prototipos de cada uno de los 'Streams', enviado después los eventos
	 * procesados y enviados (a la vez) de cada instancia de los PEs Prototipos.
	 */
	private class OnTimeGetEventCountAdapter extends TimerTask {

		@Override
		public void run() {
			// logger.debug("OnTimeGetEventCount");
			Map<Class<? extends AdapterApp>, Long> mapEventCount = new HashMap<Class<? extends AdapterApp>, Long>();
			mapEventCount.put(getClassAdapter(), getEventSeg());
			historyAdapter.add(mapEventCount);
		}
	}

	/**
	 * Esta clase estará encargada de la correr la segunda hebra, la cual
	 * enviará los datos de cada uno de los PEs. Para obtenerlos, se recurrirá a
	 * los distintos 'Streams' creandos en la aplicación de S4. Al obtener cada
	 * 'Stream', se extraerá los distintos PEs Prototipos de cada uno de los
	 * 'Streams', enviado después los eventos procesados y enviados (a la vez)
	 * de cada instancia de los PEs Prototipos.
	 */
	private class OnTimeSendStatusAdapter extends TimerTask {

		@Override
		public void run() {

			/* Envío de los datos al monitor */
			getMonitor().sendStatusAdapter(getClassAdapter(), getEventCount());

			/* Envío del historial de todos de los PEs */
			getMonitor().sendHistoryAdapter(historyAdapter);
		}
	}

	@Override
	protected void onInit() {
		remoteStream = createOutputStream(outputStreamName,
				remoteStreamKeyFinder);
		setConditionAdapter(true);

		Event event = new Event();
		event.put("readyInitAdapter", Boolean.class, true);
		remoteStream.put(event);
	}

	/**
	 * This method allows to specify a keyfinder in order to partition the
	 * output stream
	 * 
	 * @param keyFinder
	 *            used for identifying keys from the events
	 */
	protected void setKeyFinder(KeyFinder<Event> keyFinder) {
		this.remoteStreamKeyFinder = keyFinder;
	}

	@Override
	protected void onClose() {
	}

}
