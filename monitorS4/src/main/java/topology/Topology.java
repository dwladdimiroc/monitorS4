/**
 * or more contributor license agreements.  See the NOTICE file
 * Licensed to the Apache Software Foundation (ASF) under one
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

package topology;

import java.util.Arrays;
import java.util.List;

import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processElements.MongoPE;
import processElements.ProcessOnePE;
import processElements.ProcessTwoPE;

public class Topology extends App {
	private static Logger logger = LoggerFactory.getLogger(Topology.class);
	
	/* PEs de la App */ 
	ProcessOnePE processOnePE;
	ProcessTwoPE processTwoPE;
	MongoPE mongoPE;

	@Override
	protected void onInit() {		
		// Create the PE prototype
		processOnePE = createPE(ProcessOnePE.class);
		processTwoPE = createPE(ProcessTwoPE.class);
		mongoPE = createPE(MongoPE.class);

		// Create a stream that listens to the "textInput" stream and passes
		// events to the processPE instance.
		createInputStream("textInput", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {
				return Arrays.asList(new String[] { event
						.get("levelProcessOne") });
			}
		}, processOnePE).setParallelism(16);

		Stream<Event> processTwoStream = createStream("processTwoStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelProcessTwo") });
					}
				}, processTwoPE).setParallelism(16);

		Stream<Event> mongoStream = createStream("mongoStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelMongo") });
					}
				}, mongoPE).setParallelism(16);
			

		// Register and setDownStream
		processOnePE.setDownStream(processTwoStream);
		processTwoPE.setDownStream(mongoStream);

		setRunMonitor(true);
	}
	
	@Override
	protected void onStart() {
		logger.info("Start Topology");
		
		processOnePE.registerMonitor(processTwoPE.getClass());
		processTwoPE.registerMonitor(mongoPE.getClass());
		mongoPE.registerMonitor(null);
	}

	@Override
	protected void onClose() {
	}

}
