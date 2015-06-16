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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.RemoteStream;
import org.apache.s4.core.monitor.StatusPE;

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

	int portSocket;

	final private Map<Class<? extends ProcessingElement>, Integer> replications = new HashMap<Class<? extends ProcessingElement>, Integer>();
	// final private Queue<Long> historyAdapter = new
	// CircularFifoQueue<Long>(5);
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
	}

	/**
	 * En este thread se desea poseer un puerto que escuche los datos que estén
	 * siendo enviados por el monitor. De esta manera, de haber una variación en
	 * el sistema, que el adapter también los realice.
	 */
	private class ListenerMonitor implements Runnable {

		ServerSocket serverSocket;
		Socket connectedSocket;
		ObjectInputStream inStream;

		public ListenerMonitor() {
			/*
			 * Se considerará el siguiente puerto, tomando como referencia el
			 * puerto del puerto que utilizá al nodo único del adapter
			 */
			// portSocket = getCluster().getPhysicalCluster().getNodes().get(0)
			// .getPort() + 1;
			portSocket = 15000;

			serverSocket = null;
			connectedSocket = null;
			inStream = null;
		}

		/**
		 * Este método analizará si el estado del PE debe aumentar o disminuir
		 * de tal manera que si aumenta o disminuye, todos los PEs emisores a
		 * ese PE deberán aumentar o disminuir su llave de replicación. Por otra
		 * parte, en caso que disminuya, se deberá eliminar el PE con la llave
		 * eliminada (es decir, el número mayor de los PEs instanciados).
		 * 
		 * @param statusPE
		 *            estado del PE analizado
		 */
		private void changeReplication(StatusPE statusPE) {

			for (Class<? extends ProcessingElement> peCurrent : replications
					.keySet()) {

				if (statusPE.getPE().equals(peCurrent)) {

					/*
					 * De ser mayor la cantidad de replicas del estado del
					 * monitor, se deberá aumentar la cantidad de réplicas
					 */

					int replicationAnalyzed = statusPE.getReplication();
					int replicationCurrent = replications.get(peCurrent);

					// getLogger().debug(
					// "[replicationAnalyzed] " + replicationAnalyzed
					// + " | [replicationCurrent] "
					// + replicationCurrent);

					if (replicationAnalyzed > replicationCurrent) {

						getLogger().debug(
								"Increment PE  " + statusPE.getPE()
										+ " in Adapter "
										+ this.getClass().getCanonicalName());

						replications.put(statusPE.getPE(),
								statusPE.getReplication());

					}
					/* De ser menor, se deberá disminuir */
					else if (replicationAnalyzed < replicationCurrent) {

						/*
						 * Para esto, es necesario eliminar una de las
						 * instancias del PE analizado
						 */
						removeReplication(statusPE);

						getLogger().debug(
								"Decrement PE  " + statusPE.getPE()
										+ " in Adapter "
										+ this.getClass().getCanonicalName());

						replications.put(statusPE.getPE(),
								statusPE.getReplication());

					}
				}
			}
		}

		/**
		 * Función que estará encargada de eliminar la réplica con el valor de
		 * la llave más alto. Es decir, en caso que el PE posee número de
		 * réplica n, significa que habrá 0 a n-1 llaves, de esta manera, se
		 * eliminarán m réplicas, como n-m réplicas diga que existen ahora en el
		 * sistema.
		 * 
		 * @param statusPE
		 *            Estado del PE analizado
		 */
		private void removeReplication(StatusPE statusPE) {

			int replication = replications.get(statusPE.getPE());

			/*
			 * Enviará un mensaje
			 */
			if (replication > 1) {
				getRemoteStream().sendRemovePE(statusPE);
			}

		}

		@Override
		public void run() {

			try {
				serverSocket = new ServerSocket(portSocket);
				while (true) {
					connectedSocket = serverSocket.accept();
					inStream = new ObjectInputStream(
							connectedSocket.getInputStream());

					StatusPE statusPE = (StatusPE) inStream.readObject();

					changeReplication(statusPE);

					connectedSocket.close();
				}

			} catch (IOException | ClassNotFoundException e) {
				getLogger().error(e.toString());
				close();
			} finally {
				if (inStream != null) {
					try {
						inStream.close();
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

		if (getRunMonitor()) {
			/* Thread que estará escuchando los datos que envía el monitor */
			Thread tListenerMonitor = new Thread(new ListenerMonitor());
			tListenerMonitor.start();
		}

		setConditionAdapter(true);

		if (getRunMonitor()) {
			/* Notificación para indicar que se ha iniciado el Adapter */
			Notification notification = new Notification();
			notification.setStatus(true);
			notification.setAdapter(getClassAdapter());
			notification.setListPE(listPE);
			notification.setPort(portSocket);
			remoteStream.notification(notification);
		}
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
