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
		FourierModel fourier = new FourierModel(296.4, 15.66, 192.3, -101.1, -96.29, 161.4, -87.17, 46.93, 25.1, 10.09, 3.638, -19.05,
				23.68, -18.24, -1.734, -0.3809, -0.2242, 1.682);
		return fourier.cantTweet(900);

		// Model for 1 hour
		//FourierModel fourier = new FourierModel(285.6, 21.75, 195.1, -105.3, -98.86, 179.7, -45.55, 46.07, 47.05, 11.16, 10.54,
		//		-25.08, 10.22, -19.15, -12.66, -1.472, -5.407, 1.536);
		//return fourier.cantTweet(4000);
		
	}

	@Override
	protected void onInit() {
		/* Este orden es importante */
		logger.info("Create Fourier Adapter");
		setRunMonitor(false);
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
				Thread.sleep(695000);
				//Thread.sleep(3850000);
			} catch (InterruptedException e) {
				logger.error(e.toString());
			}

			while (true) {
				timeFinal = System.currentTimeMillis();
				if ((timeFinal - timeInit) >= 700000) {
					close();
					System.exit(0);
				}

			}
		}
	}

}
