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

public class Adapter extends AdapterApp implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(Adapter.class);
	private boolean showEvent = false;

	private Thread thread;

	private int[] time = { 10000, 15000, 50000, 35000, 500 };

	@Override
	protected void onInit() {
		logger.info("Create Adapter");

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
		logger.debug("Time init (ns): " + System.nanoTime());

		for (int loop = 0; loop < 5; loop++) {

			for (int i = 0; i < 100; i++) {
				Event event = new Event();
				
				event.put("levelProcessOne", Long.class, getEventCount()
						% getLevelConcurrency());
				event.put("id", Long.class, getEventCount());
				event.put("time", Long.class, System.nanoTime());
				event.put("date", Date.class, Calendar.getInstance().getTime());

				if (showEvent) {
					logger.debug(event.getAttributesAsMap().toString());
				}

				getRemoteStream().put(event);
			}

			try {
				Thread.sleep(time[loop]);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		logger.info("Finish Adapter");
	}

}
