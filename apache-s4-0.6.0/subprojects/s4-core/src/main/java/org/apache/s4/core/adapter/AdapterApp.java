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

import java.util.Map;

import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.RemoteStream;

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
	private Map<Class<? extends ProcessingElement>, Integer> replications;

	protected KeyFinder<Event> remoteStreamKeyFinder;

	public RemoteStream getRemoteStream() {
		return remoteStream;
	}

	public long getEventCount() {
		return remoteStream.getEventCount();
	}

	/**
	 * Se encargará de inicializar la llave para el PE que debe dirigirse, de
	 * tal manera de replicar sólo a ese PE en caso de aumentar su llave
	 * 
	 * @param PE
	 *            receptor, donde estará siendo llamada la función por el PE
	 *            emisor.
	 */

	public void registerMonitor(Class<? extends ProcessingElement> PE) {
		// Put replication with your class in the map of replications
		this.replications.put(PE, 1);
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

	public Map<Class<? extends ProcessingElement>, Integer> getLevelConcurrency() {
		return replications;
	}

	public void setLevelConcurrency(
			Map<Class<? extends ProcessingElement>, Integer> replications) {
		this.replications = replications;
	}

	@Override
	protected void onStart() {
	}

	@Override
	protected void onInit() {
		remoteStream = createOutputStream(outputStreamName,
				remoteStreamKeyFinder);
		setConditionAdapter(true);
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
