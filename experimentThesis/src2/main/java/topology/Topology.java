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
import org.apache.s4.core.monitor.StatusPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import processElements.AnalyzePE;
import processElements.CounterPE;
import processElements.LanguagePE;
import processElements.MongoPE;
import processElements.StopwordPE;

public class Topology extends App {
	private static Logger logger = LoggerFactory.getLogger(Topology.class);

	/* PEs de la App */
	StopwordPE stopwordPE;
	LanguagePE languagePE;
	CounterPE counterPE;
	//AnalyzePE analyzePE;
	MongoPE mongoPE;

	@Override
	protected void onInit() {
		// Create the PE prototype
		stopwordPE = createPE(StopwordPE.class);
		languagePE = createPE(LanguagePE.class);
		counterPE = createPE(CounterPE.class);
		//analyzePE = createPE(AnalyzePE.class);
		mongoPE = createPE(MongoPE.class);

		// Create a stream that listens to the "textInput" stream and passes
		// events to the processPE instance.
		createInputStream("textInput", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {
				return Arrays.asList(new String[] { event.get("levelStopword") });
			}
		}, stopwordPE).setParallelism(6);

		Stream<Event> languageStream = createStream("languageStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelLanguage") });
					}
				}, languagePE).setParallelism(1);

		Stream<Event> counterStream = createStream("counterStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelCounter") });
					}
				}, counterPE).setParallelism(10);

		/*Stream<Event> analyzeStream = createStream("analyzeStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelAnalyze") });
					}
				}, analyzePE).setParallelism(16);*/

		Stream<Event> mongoStream = createStream("mongoStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelMongo") });
					}
				}, mongoPE).setParallelism(1);

		// Register and setDownStream
		stopwordPE.setDownStream(languageStream);
		languagePE.setDownStream(counterStream);
		counterPE.setDownStream(mongoStream);
		//analyzePE.setDownStream(mongoStream);

		setRunMonitor(false);
	}

	@Override
	protected void onStart() {
		logger.info("Start Topology");

		stopwordPE.registerMonitor(languagePE.getClass());
		languagePE.registerMonitor(counterPE.getClass());
		counterPE.registerMonitor(mongoPE.getClass());
		//analyzePE.registerMonitor(mongoPE.getClass());
		mongoPE.registerMonitor(null);

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
				Thread.sleep(4195000);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}

			while (true) {
				timeFinal = System.currentTimeMillis();
				if ((timeFinal - timeInit) >= 4200000) {
					close();
					System.exit(0);
				}

			}
		}
	}

}
