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
import java.util.concurrent.TimeUnit;

import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import processElements.CounterPE;
import processElements.MergePE;
import processElements.SplitPE;

public class Topology extends App {
	private static Logger logger = LoggerFactory.getLogger(Topology.class);

	/* PEs de la App */
	SplitPE splitPE;
	CounterPE counterPE;
	MergePE mergePE;

	@Override
	protected void onInit() {
		// Create the PE prototype

		splitPE = createPE(SplitPE.class);
		splitPE.setSingleton(true);
		
		counterPE = createPE(CounterPE.class);
		counterPE.setTimerInterval(10000, TimeUnit.MICROSECONDS);
		
		mergePE = createPE(MergePE.class);
		mergePE.setSingleton(true);	

		// Create a stream that listens to the "textInput" stream and passes
		// events to the processPE instance.
		createInputStream("textInput", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {
				return Arrays.asList(new String[] { event.get("levelSplit") });
			}
		}, splitPE).setParallelism(1);

		Stream<Event> counterStream = createStream("counterStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelCounter") });
					}
				}, counterPE).setParallelism(20);

		Stream<Event> mergeStream = createStream("mergeStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelCounter") });
					}
				}, mergePE).setParallelism(1);

		// Register and setDownStream
		splitPE.setDownStream(counterStream);
		counterPE.setDownStream(mergeStream);

		setRunMonitor(true);
	}

	@Override
	protected void onStart() {
		logger.info("Start Topology");

		splitPE.registerMonitor(counterPE.getClass());
		counterPE.registerMonitor(mergePE.getClass());
		mergePE.registerMonitor(null);

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
				Thread.sleep(8795000);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}

			while (true) {
				timeFinal = System.currentTimeMillis();
				if ((timeFinal - timeInit) >= 8800000) {
					close();
					System.exit(0);
				}

			}
		}
	}

}
