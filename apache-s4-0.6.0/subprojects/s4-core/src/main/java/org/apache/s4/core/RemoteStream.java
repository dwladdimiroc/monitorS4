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

package org.apache.s4.core;

import org.apache.s4.base.Event;
import org.apache.s4.base.Hasher;
import org.apache.s4.base.Key;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.comm.topology.RemoteStreams;
import org.apache.s4.core.Stream.StreamEventProcessingTask;
import org.apache.s4.core.adapter.Notification;
import org.apache.s4.core.adapter.Statistics;
import org.apache.s4.core.monitor.StatusPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream that dispatches events to interested apps in remote clusters
 * 
 */
public class RemoteStream implements Streamable<Event> {

	final private String name;
	final protected Key<Event> key;
	final static private String DEFAULT_SEPARATOR = "^";

	private long eventSeg = 0;
	private long eventPeriod = 0;
	private long eventCount = 0;

	RemoteSenders remoteSenders;

	Hasher hasher;

	int id;
	final private App app;
	private static Logger logger = LoggerFactory.getLogger(RemoteStream.class);

	public RemoteStream(App app, String name, int eventCount,
			KeyFinder<Event> finder, RemoteSenders remoteSenders,
			Hasher hasher, RemoteStreams remoteStreams, String clusterName) {
		this.app = app;
		this.name = name;
		this.eventCount = eventCount;
		this.remoteSenders = remoteSenders;
		this.hasher = hasher;
		if (finder == null) {
			this.key = null;
		} else {
			this.key = new Key<Event>(finder, DEFAULT_SEPARATOR);
		}
				
		remoteStreams.addOutputStream(clusterName, name);

	}

	@Override
	public void put(Event event) {
		event.setStreamId(getName());
		eventSeg++;
		eventPeriod++;
		eventCount++;

		if (key != null) {
			remoteSenders.send(key.get(event), event);
		} else {
			remoteSenders.send(null, event);
		}
	}

	@Override
	public void notification(Notification notification) {
		notification.setStream(getName());
		remoteSenders.sendNotification(notification);
	}

	@Override
	public void sendStatistics(Statistics statistics) {
		statistics.setStream(getName());
		remoteSenders.sendStatistics(statistics);
	}
	
	@Override
	public void sendRemovePE(StatusPE statusPE) {
		statusPE.setStream(getName());
		remoteSenders.sendRemovePE(statusPE);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public ProcessingElement[] getTargetPEs() {
		// TODO Auto-generated method stub
		return null;
	}

	public long getEventSeg() {
		return eventSeg;
	}

	public void setEventSeg(long eventSeg) {
		this.eventSeg = eventSeg;
	}

	public long getEventPeriod() {
		return eventPeriod;
	}

	public void setEventPeriod(long eventPeriod) {
		this.eventPeriod = eventPeriod;
	}

	public long getEventCount() {
		return eventCount;
	}

	public void setEventCount(long eventCount) {
		this.eventCount = eventCount;
	}

}
