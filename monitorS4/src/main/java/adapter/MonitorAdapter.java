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

package adapter;

import java.util.Calendar;
import java.util.Date;

import org.apache.s4.base.Event;
import org.apache.s4.core.adapter.AdapterApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorAdapter extends AdapterApp implements Runnable {
	private static Logger logger = LoggerFactory
			.getLogger(MonitorAdapter.class);
	private boolean showEvent = true;

	private long eventCount;
	private int levelConcurrency;
	
	private Thread thread;

	@Override
	protected void onInit() {
		logger.info("Create Monitor Adapter");
		eventCount = 0;
		levelConcurrency = 1;
		
		thread = new Thread(this);
		super.onInit();
	}

	@Override
	protected void onStart() {
		try {
			thread.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void run() {
		for(int i = 0; i<10; i++){
			Event event = new Event();

			eventCount++;
			event.put("levelProcess", Long.class, eventCount % levelConcurrency);
			event.put("id", Long.class, eventCount);
			event.put("date", Date.class, Calendar.getInstance().getTime());

			if (showEvent) {logger.debug(event.getAttributesAsMap().toString());}

			getRemoteStream().put(event);
		}
		
		logger.info("Finish Adapter");
	}

	public long getEventCount() {
		return eventCount;
	}

	public void setEventCount(long eventCount) {
		this.eventCount = eventCount;
	}

	public int getLevelConcurrency() {
		return levelConcurrency;
	}

	public void setLevelConcurrency(int levelConcurrency) {
		this.levelConcurrency = levelConcurrency;
	}
}
