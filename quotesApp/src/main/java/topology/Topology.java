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
//import org.apache.s4.core.monitor.StatusPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processElements.AnalyzePE;
import processElements.DetectNamePE;
import processElements.DetectSpamPE;
import processElements.MongoPE;

public class Topology extends App {
	private static Logger logger = LoggerFactory.getLogger(Topology.class);

	/* PEs de la App */
	DetectSpamPE detectSpamPE;
	AnalyzePE analyzePE;
	DetectNamePE detectNamePE;
	MongoPE mongoPE;

	@Override
	protected void onInit() {
		// Create the PE prototype
		detectSpamPE = createPE(DetectSpamPE.class);
		analyzePE = createPE(AnalyzePE.class);
		detectNamePE = createPE(DetectNamePE.class);
		mongoPE = createPE(MongoPE.class);

		// Input Adapter
		createInputStream("quotesInput", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {
				return Arrays.asList(new String[] { event.get("levelSpam") });
			}
		}, detectSpamPE).setParallelism(10);

		// Stream each PE
		Stream<Event> analyzeStream = createStream("analyzeStream", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {

				return Arrays.asList(new String[] { event.get("levelAnalyze") });
			}
		}, analyzePE).setParallelism(10);

		Stream<Event> nameStream = createStream("nameStream", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {

				return Arrays.asList(new String[] { event.get("levelName") });
			}
		}, detectNamePE).setParallelism(10);


		Stream<Event> mongoStream = createStream("mongoStream", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {

				return Arrays.asList(new String[] { event.get("levelMongo") });
			}
		}, mongoPE).setParallelism(2);

		// setDownStream
		detectSpamPE.setDownStream(analyzeStream);
		analyzePE.setDownStream(nameStream);
		detectNamePE.setDownStream(mongoStream);

		setRunMonitor(false);
	}

	@Override
	protected void onStart() {
		logger.info("Start Topology");

		detectSpamPE.registerMonitor();
		analyzePE.registerMonitor();
		detectNamePE.registerMonitor();
		mongoPE.registerMonitor();
		
		/*StatusPE statusStopwordPE = new StatusPE();
		statusStopwordPE.setPE(stopwordPE.getClass());
		statusStopwordPE.setReplication(4);
		this.addReplication(statusStopwordPE);

		StatusPE statusCounterPE = new StatusPE();
		statusCounterPE.setPE(counterPE.getClass());
		statusCounterPE.setReplication(8);
		this.addReplication(statusCounterPE);*/

		Thread clockTime = new Thread(new ClockTime());
		clockTime.start();
	}

	@Override
	protected void onClose() {
		logger.info("Finish experiment");
	}

	public class ClockTime implements Runnable {
		long timeInit;
		long timeFinal;

		public ClockTime() {
			logger.info("Init ClockTime");
			timeInit = System.currentTimeMillis();
			timeFinal = 0;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(895000);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}

			while (true) {
				timeFinal = System.currentTimeMillis();
				if ((timeFinal - timeInit) >= 900000) {
					close();
					System.exit(0);
				}

			}
		}
	}

}
