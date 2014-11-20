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

import processElements.MongoPE;
import processElements.ProcessPE;

public class Topology extends App {
	private static Logger logger = LoggerFactory
			.getLogger(Topology.class);
	
    @Override
    protected void onStart() {
    	logger.info("Start Topology");
    }

    @Override
    protected void onInit() {
        //Create a prototype
        ProcessPE processPE = createPE(ProcessPE.class);
        //Tiempo de replicación - Automática
        processPE.setTimerInterval(1000, TimeUnit.MILLISECONDS);
        
        MongoPE mongoPE = createPE(MongoPE.class);
        //Tiempo de replicación - Automática
        mongoPE.setTimerInterval(1000, TimeUnit.MILLISECONDS);
        
        //Create a stream that listens to the "textInput" stream and passes events to the processPE instance.
        createInputStream("textInput", new KeyFinder<Event>() {
            @Override
            public List<String> get(Event event) {
                return Arrays.asList(new String[] { event.get("levelProcess") });
            }
        }, processPE);
        
        Stream<Event> mongoStream = createStream("mongoStream",
				new KeyFinder<Event>() {
					@Override
					public List<String> get(Event event) {

						return Arrays.asList(new String[] { event
								.get("levelMongo") });
					}
				}, mongoPE);
        
        processPE.registerMonitor(mongoPE.getClass());       
        processPE.setDownStream(mongoStream);
    }

    @Override
    protected void onClose() {
    }

}
