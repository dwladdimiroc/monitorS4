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

package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.MongoConnection;

public class ProcessOnePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(ProcessOnePE.class);
	private boolean showEvent = false;

	int count = 0;

	Stream<Event> downStream;

	MongoConnection mongo;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {
		// if(showEvent){logger.debug(event.getAttributesAsMap().toString());}
		// logger.debug("Replication: " + getReplication());
		// logger.debug("EventCount: " + getEventCount());

		// Processing
		try {
			// wait(500);
			Thread.sleep(10);
		} catch (InterruptedException e) {
			logger.error(e.toString());
		}

		Event eventOutput = new Event();

		eventOutput.put("levelProcessTwo", Long.class, getEventCount()
				% getReplicationPE(ProcessTwoPE.class));
		// logger.debug("[Replication] " + eventOutput.get("levelProcessTwo"));
		// eventOutput.put("levelProcessTwo", Integer.class, 1);
		eventOutput.put("time", Long.class, event.get("time", Long.class));
		// eventOutput.put("time", Long.class, event.get("time", Long.class));
		// eventOutput.put("dateAdapter", Date.class,
		// event.get("date", Date.class));
		// eventOutput.put("dateProcessOne", Date.class, Calendar.getInstance()
		// .getTime());

		if (showEvent) {
			logger.debug(eventOutput.getAttributesAsMap().toString());
		}

		/*
		 * DBObject objMongo = new BasicDBObject(); objMongo.put("time",
		 * System.nanoTime()); objMongo.put("replication", getReplication());
		 * mongo.insert(objMongo);
		 */

		downStream.put(eventOutput);

	}

	@Override
	protected void onCreate() {
		logger.info("Create ProcessOne PE");
		// this.replicationPE();

		/*
		 * mongo = new MongoConnection();
		 * mongo.setCollectionName("replicationTwoPE"); mongo.setupMongo();
		 * 
		 * DBObject objMongo = new BasicDBObject(); objMongo.put("time",
		 * System.nanoTime()); objMongo.put("replication", getNumPEInstances());
		 * mongo.insert(objMongo);
		 */
	}

	@Override
	protected void onRemove() {
		logger.info("Remove ProcessOne PE");
		// Important
	}

}
