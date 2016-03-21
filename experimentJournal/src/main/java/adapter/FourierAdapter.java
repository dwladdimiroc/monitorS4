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

import java.util.List;
import java.util.Stack;

import org.apache.s4.base.Event;
import org.apache.s4.core.adapter.AdapterApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eda.Tweet;
import utilities.FourierModel;
import utilities.MongoRead;

public class FourierAdapter extends AdapterApp implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(FourierAdapter.class);

	private Thread thread;
	private Stack<Tweet> tweets;
	private List<Integer> cantTweets;

	public Stack<Tweet> readTweet() {
		MongoRead mongoRead = new MongoRead();
		return mongoRead.getAllTweets();
	}

	public List<Integer> cantTweets() {
		// Model for 15 minutes
		FourierModel fourier = new FourierModel(245.2, 114.8, 174.9, -183.2, -37.29, 170.6, -133.4, 52.64, 31.74, 31.53,
				-12.1, -14.5, 27.55, -15.92, 2.248, -9.052, 3.92, 1.546);
		return fourier.cantTweet(900000);
	}

	@Override
	protected void onInit() {
		/* Este orden es importante */
		logger.info("Create Fourier Adapter");
		setRunMonitor(true);
		thread = new Thread(this);

		tweets = readTweet();
		logger.info("Finish read tweets");
		cantTweets = cantTweets();
		logger.info("Finish stream tweets");

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

			for (int cant : cantTweets) {

				for (int i = 1; i <= cant; i++) {
					Tweet tweetCurrent = tweets.pop();

					Event event = new Event();
					event.put("levelStopword", Integer.class, 0);
					event.put("tweet", Tweet.class, tweetCurrent);
					event.put("timeTweet", Long.class, System.currentTimeMillis());

					getRemoteStream().put(event);
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
