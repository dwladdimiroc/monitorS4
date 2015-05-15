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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.apache.s4.core.monitor.StatusPE;
import org.apache.s4.core.monitor.TopologyApp;

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
	final private Queue<Long> historyAdapter = new CircularFifoQueue<Long>(5);
	final private List<Class<? extends ProcessingElement>> listPE = new ArrayList<Class<? extends ProcessingElement>>();

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
		this.listPE.add(peRecibe);

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
		 * Esta hebra se utilizará para enviar los datos del Adapter cada un
		 * segundo, para poseer el historial del Adapter
		 */

		if (getRunMonitor()) {

			ScheduledExecutorService getEventCountAdapter = Executors
					.newSingleThreadScheduledExecutor();
			getEventCountAdapter.scheduleAtFixedRate(
					new OnTimeGetEventCountAdapter(), 0, 1000,
					TimeUnit.MILLISECONDS);

			getLogger().info("TimerMonitorAdapter get eventCount");

			ScheduledExecutorService sendStatusAdapter = Executors
					.newSingleThreadScheduledExecutor();
			sendStatusAdapter.scheduleAtFixedRate(
					new OnTimeSendStatusAdapter(), 10000, 15000,
					TimeUnit.MILLISECONDS);

			getLogger().info("TimerMonitorAdapter send status");
		}

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
			historyAdapter.add(getEventSeg());

			setEventSeg(0);
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
			Statistics statistics = new Statistics();
			statistics.setAdapter(getClassAdapter());
			Map<Class<? extends AdapterApp>, Queue<Long>> mapAdapter = new HashMap<Class<? extends AdapterApp>, Queue<Long>>();
			mapAdapter.put(getClassAdapter(), historyAdapter);
			statistics.setHistory(mapAdapter);
			statistics.setEventPeriod(getEventPeriod());

			/* Envío de las estadísticas al monitor */
			remoteStream.sendStatistics(statistics);

			setEventPeriod(0);
		}

	}

	/**
	 * En este thread se desea poseer un puerto que escuche los datos que estén
	 * siendo enviados por el monitor. De esta manera, de haber una variación en
	 * el sistema, que el adapter también los realice.
	 */
	private class ListenerMonitor implements Runnable {

		ServerSocket serverSocket;
		Socket connectedSocket;
		BufferedReader in;

		public ListenerMonitor() {
			serverSocket = null;
			connectedSocket = null;
			in = null;
		}

		@Override
		public void run() {

			try {
				serverSocket = new ServerSocket(15000);
				while (true) {
					connectedSocket = serverSocket.accept();
					in = new BufferedReader(new InputStreamReader(
							connectedSocket.getInputStream()));

					String line = in.readLine();
					getLogger().info("Datos: " + line);
					connectedSocket.close();
				}

			} catch (IOException e) {
				getLogger().error(e.toString());
				close();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
				if (serverSocket != null) {
					try {
						serverSocket.close();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	@Override
	protected void onInit() {
		remoteStream = createOutputStream(outputStreamName,
				remoteStreamKeyFinder);

		/* Thread que estará escuchando los datos que envía el monitor */
		Thread tListenerMonitor = new Thread(new ListenerMonitor());
		tListenerMonitor.start();

		setConditionAdapter(true);

		/* Notificación para indicar que se ha iniciado el Adapter */
		Notification notification = new Notification();
		notification.setStatus(true);
		notification.setAdapter(getClassAdapter());
		notification.setListPE(listPE);
		remoteStream.notification(notification);
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
