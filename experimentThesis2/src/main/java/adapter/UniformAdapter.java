/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package adapter;

import java.util.Stack;

import org.apache.s4.base.Event;
import org.apache.s4.core.adapter.AdapterApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eda.Tweet;
import processElements.SplitPE;
import utilities.MongoRead;

public class UniformAdapter extends AdapterApp implements Runnable {

	private static Logger logger = LoggerFactory
			.getLogger(UniformAdapter.class);

	private Thread thread;
	private Stack<Tweet> tweets;

	public Stack<Tweet> readTweet() {
		MongoRead mongoRead = new MongoRead();
		return mongoRead.getAllTweets();
	}

	@Override
	protected void onInit() {
		/* Este orden es importante */
		logger.info("Create Uniform Adapter");
		tweets = readTweet();

		setRunMonitor(true);
		this.registerMonitor(SplitPE.class);
		thread = new Thread(this);

		super.onInit();
	}

	@Override
	protected void onStart() {
		/* Este orden es importante */
		super.onStart();

		try {
			thread.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Thread clockTime = new Thread(new ClockTime());
		clockTime.start();
	}

	@Override
	public void run() {

		while (true) {

			// for (int i = 1; i <= 4; i++) {
			Tweet tweetCurrent = tweets.pop();
			// logger.info(tweetCurrent.toString());

			Event event = new Event();
			event.put("levelStopword", Long.class, getEventCount()
					% getReplicationPE(SplitPE.class));
			event.put("tweet", Tweet.class, tweetCurrent);
			event.put("time", Long.class, System.currentTimeMillis());

			getRemoteStream().put(event);
			// }

			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

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
