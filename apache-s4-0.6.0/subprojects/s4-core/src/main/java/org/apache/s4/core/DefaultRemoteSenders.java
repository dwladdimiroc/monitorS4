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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import org.apache.s4.base.Event;
import org.apache.s4.base.Hasher;
import org.apache.s4.base.SerializerDeserializer;
import org.apache.s4.comm.serialize.SerializerDeserializerFactory;
import org.apache.s4.comm.tcp.RemoteEmitters;
import org.apache.s4.comm.topology.Clusters;
import org.apache.s4.comm.topology.RemoteStreams;
import org.apache.s4.comm.topology.StreamConsumer;
import org.apache.s4.core.adapter.Notification;
import org.apache.s4.core.monitor.StatusPE;
import org.apache.s4.core.staging.RemoteSendersExecutorServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Default {@link RemoteSenders} implementation for sending events to nodes of a
 * remote cluster.
 */
@Singleton
public class DefaultRemoteSenders implements RemoteSenders {

	Logger logger = LoggerFactory.getLogger(DefaultRemoteSenders.class);

	final RemoteEmitters remoteEmitters;

	final RemoteStreams remoteStreams;

	final Clusters remoteClusters;

	final SerializerDeserializer serDeser;

	final Hasher hasher;

	ConcurrentMap<String, RemoteSender> sendersByTopology = new ConcurrentHashMap<String, RemoteSender>();

	private final ExecutorService executorService;

	@Inject
	public DefaultRemoteSenders(RemoteEmitters remoteEmitters,
			RemoteStreams remoteStreams, Clusters remoteClusters,
			SerializerDeserializerFactory serDeserFactory, Hasher hasher,
			RemoteSendersExecutorServiceFactory senderExecutorFactory) {
		this.remoteEmitters = remoteEmitters;
		this.remoteStreams = remoteStreams;
		this.remoteClusters = remoteClusters;
		this.hasher = hasher;
		executorService = senderExecutorFactory.create();

		serDeser = serDeserFactory.createSerializerDeserializer(Thread
				.currentThread().getContextClassLoader());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.s4.core.RemoteSenders#send(java.lang.String,
	 * org.apache.s4.base.Event)
	 */
	@Override
	public void send(String hashKey, Event event) {

		Set<StreamConsumer> consumers = remoteStreams.getConsumers(event
				.getStreamId());

		for (StreamConsumer consumer : consumers) {
			// NOTE: even though there might be several ephemeral znodes for the
			// same app and topology, they are
			// represented by a single stream consumer
			RemoteSender sender = sendersByTopology.get(consumer
					.getClusterName());

			if (sender == null) {
				RemoteSender newSender = new RemoteSender(
						remoteEmitters.getEmitter(remoteClusters
								.getCluster(consumer.getClusterName())),
						hasher, consumer.getClusterName());
				// TODO cleanup when remote topologies die
				sender = sendersByTopology.putIfAbsent(
						consumer.getClusterName(), newSender);
				if (sender == null) {
					sender = newSender;
				}
			}
			// NOTE: this implies multiple serializations, there might be an
			// optimization
			executorService.execute(new SendToRemoteClusterTask(hashKey, event,
					sender));
		}
	}

	@Override
	public void sendNotification(Notification notification) {

		Set<StreamConsumer> consumers = remoteStreams.getConsumers(notification
				.getStream());
		for (StreamConsumer consumer : consumers) {
			// NOTE: even though there might be several ephemeral znodes for the
			// same app and topology, they are
			// represented by a single stream consumer
			RemoteSender sender = sendersByTopology.get(consumer
					.getClusterName());
			if (sender == null) {
				RemoteSender newSender = new RemoteSender(
						remoteEmitters.getEmitter(remoteClusters
								.getCluster(consumer.getClusterName())),
						hasher, consumer.getClusterName());
				// TODO cleanup when remote topologies die
				sender = sendersByTopology.putIfAbsent(
						consumer.getClusterName(), newSender);
				if (sender == null) {
					sender = newSender;
				}
			}
			// NOTE: this implies multiple serializations, there might be an
			// optimization
			executorService.execute(new SendNotificationToRemoteClusterTask(
					notification, sender));
		}
	}

	// @Override
	// public void sendStatistics(Statistics statistics) {
	//
	// Set<StreamConsumer> consumers = remoteStreams.getConsumers(statistics
	// .getStream());
	// for (StreamConsumer consumer : consumers) {
	// // NOTE: even though there might be several ephemeral znodes for the
	// // same app and topology, they are
	// // represented by a single stream consumer
	// RemoteSender sender = sendersByTopology.get(consumer
	// .getClusterName());
	// if (sender == null) {
	// RemoteSender newSender = new RemoteSender(
	// remoteEmitters.getEmitter(remoteClusters
	// .getCluster(consumer.getClusterName())),
	// hasher, consumer.getClusterName());
	// // TODO cleanup when remote topologies die
	// sender = sendersByTopology.putIfAbsent(
	// consumer.getClusterName(), newSender);
	// if (sender == null) {
	// sender = newSender;
	// }
	// }
	// // NOTE: this implies multiple serializations, there might be an
	// // optimization
	// executorService.execute(new SendStatisticsToRemoteClusterTask(
	// statistics, sender));
	// }
	// }

	@Override
	public void sendRemovePE(StatusPE statusPE) {

		Set<StreamConsumer> consumers = remoteStreams.getConsumers(statusPE
				.getStream());
		for (StreamConsumer consumer : consumers) {
			// NOTE: even though there might be several ephemeral znodes for the
			// same app and topology, they are
			// represented by a single stream consumer
			RemoteSender sender = sendersByTopology.get(consumer
					.getClusterName());
			if (sender == null) {
				RemoteSender newSender = new RemoteSender(
						remoteEmitters.getEmitter(remoteClusters
								.getCluster(consumer.getClusterName())),
						hasher, consumer.getClusterName());
				// TODO cleanup when remote topologies die
				sender = sendersByTopology.putIfAbsent(
						consumer.getClusterName(), newSender);
				if (sender == null) {
					sender = newSender;
				}
			}
			// NOTE: this implies multiple serializations, there might be an
			// optimization
			executorService.execute(new SendRemovePEToRemoteClusterTask(
					statusPE, sender));
		}
	}

	class SendToRemoteClusterTask implements Runnable {

		String hashKey;
		Event event;
		RemoteSender sender;

		public SendToRemoteClusterTask(String hashKey, Event event,
				RemoteSender sender) {
			super();
			this.hashKey = hashKey;
			this.event = event;
			this.sender = sender;
		}

		@Override
		public void run() {
			try {
				sender.send(hashKey, serDeser.serialize(event));
			} catch (InterruptedException e) {
				logger.error(
						"Interrupted blocking send operation for event {}. Event is lost.",
						event);
				Thread.currentThread().interrupt();
			}

		}

	}

	class SendNotificationToRemoteClusterTask implements Runnable {

		String hashKey;
		Notification notification;
		RemoteSender sender;

		public SendNotificationToRemoteClusterTask(Notification notification,
				RemoteSender sender) {
			super();
			this.hashKey = null;
			this.notification = notification;
			this.sender = sender;
		}

		@Override
		public void run() {
			try {
				sender.send(hashKey, serDeser.serialize(notification));
			} catch (InterruptedException e) {
				logger.error(
						"Interrupted blocking sendNotificacion operation for event {}. Notification is lost.",
						notification);
				Thread.currentThread().interrupt();
			}

		}

	}

	// class SendStatisticsToRemoteClusterTask implements Runnable {
	//
	// String hashKey;
	// Statistics statistics;
	// RemoteSender sender;
	//
	// public SendStatisticsToRemoteClusterTask(Statistics statistics,
	// RemoteSender sender) {
	// super();
	// this.hashKey = null;
	// this.statistics = statistics;
	// this.sender = sender;
	// }
	//
	// @Override
	// public void run() {
	// try {
	// sender.send(hashKey, serDeser.serialize(statistics));
	// } catch (InterruptedException e) {
	// logger.error(
	// "Interrupted blocking sendNotificacion operation for event {}. Statistics is lost.",
	// statistics);
	// Thread.currentThread().interrupt();
	// }
	//
	// }
	//
	// }

	class SendRemovePEToRemoteClusterTask implements Runnable {

		String hashKey;
		StatusPE statusPE;
		RemoteSender sender;

		public SendRemovePEToRemoteClusterTask(StatusPE statusPE,
				RemoteSender sender) {
			super();
			this.hashKey = null;
			this.statusPE = statusPE;
			this.sender = sender;
		}

		@Override
		public void run() {
			try {
				sender.send(hashKey, serDeser.serialize(statusPE));
			} catch (InterruptedException e) {
				logger.error(
						"Interrupted blocking sendNotificacion operation for event {}. Statistics is lost.",
						statusPE);
				Thread.currentThread().interrupt();
			}

		}

	}
}
