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

import processElements.AnalyzePE;
import processElements.CounterPE;
import processElements.LanguagePE;
import processElements.MergePE;
import processElements.MongoPE;
import processElements.SplitPE;
import processElements.StopwordPE;

public class Topology extends App {
	private static Logger logger = LoggerFactory.getLogger(Topology.class);

	/* PEs de la App */
	LanguagePE languagePE;
	StopwordPE stopwordPE;
	SplitPE splitPE;
	CounterPE counterPE;
	MergePE mergePE;
	AnalyzePE analyzePE;
	MongoPE mongoPE;

	@Override
	protected void onInit() {
		// Create the PE prototype
		languagePE = createPE(LanguagePE.class);
		stopwordPE = createPE(StopwordPE.class);
		splitPE = createPE(SplitPE.class);
		counterPE = createPE(CounterPE.class);
		mergePE = createPE(MergePE.class);
		analyzePE = createPE(AnalyzePE.class);
		mongoPE = createPE(MongoPE.class);

		// Create a stream that listens to the "textInput" stream and passes
		// events to the processPE instance.
		createInputStream("textInput", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {
				return Arrays.asList(new String[] { event.get("levelLanguage") });
			}
		}, languagePE).setParallelism(1024);

		Stream<Event> stopwordStream = createStream("stopwordStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelStopword") });
					}
				}, stopwordPE).setParallelism(1024);
		
		Stream<Event> splitStream = createStream("splitStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelSplit") });
					}
				}, splitPE).setParallelism(1024);

		Stream<Event> counterStream = createStream("counterStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelCounter") });
					}
				}, counterPE).setParallelism(1024);

		Stream<Event> mergeStream = createStream("mergeStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelMerge") });
					}
				}, mergePE).setParallelism(1024);

		Stream<Event> analyzeStream = createStream("analyzeStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelAnalyze") });
					}
				}, analyzePE).setParallelism(1024);

		Stream<Event> mongoStream = createStream("mongoStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelMongo") });
					}
				}, mongoPE).setParallelism(1024);

		// Register and setDownStream
		languagePE.setDownStream(stopwordStream);
		stopwordPE.setDownStream(splitStream);
		splitPE.setDownStream(counterStream);
		counterPE.setDownStream(mergeStream);
		mergePE.setDownStream(analyzeStream);
		analyzePE.setDownStream(mongoStream);

		setRunMonitor(true);
	}

	@Override
	protected void onStart() {
		logger.info("Start Topology");

		languagePE.registerMonitor(stopwordPE.getClass());
		stopwordPE.registerMonitor(splitPE.getClass());
		splitPE.registerMonitor(counterPE.getClass());
		counterPE.registerMonitor(mergePE.getClass());
		mergePE.registerMonitor(analyzePE.getClass());
		analyzePE.registerMonitor(mongoPE.getClass());
		mongoPE.registerMonitor(null);

	}

	@Override
	protected void onClose() {
	}

}
