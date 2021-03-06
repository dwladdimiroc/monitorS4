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
import utilities.Distribution;
import utilities.MongoRead;

public class DynamicAdapter extends AdapterApp implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(DynamicAdapter.class);

	private Thread thread;
	private Stack<Tweet> tweets;
	private Stack<Integer> cantTweets;

	public Stack<Tweet> readTweet() {
		MongoRead mongoRead = new MongoRead();
		return mongoRead.getAllTweets();
	}

	public Stack<Integer> cantTweets() {
		Distribution distribution = new Distribution();
		return distribution.dynamicTweets();
	}

	@Override
	protected void onInit() {
		/* Este orden es importante */
		logger.info("Create Dynamic Adapter");
		setRunMonitor(false);
		this.registerMonitor(SplitPE.class);
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
	}

	@Override
	public void run() {
		long timeInit = System.currentTimeMillis();
		logger.info("Time init (ms): " + timeInit);

		while (true) {
			long timeFinal = System.currentTimeMillis();
			if ((timeFinal - timeInit) >= 10800000) {
				close();
				System.exit(0);
			}

			int cantCurrent = cantTweets.pop();

			for (int i = 1; i <= cantCurrent; i++) {
				Tweet tweetCurrent = tweets.pop();

				Event event = new Event();
				event.put("idTweet", Integer.class, tweetCurrent.getIdTweet());
				event.put("text", String.class, tweetCurrent.getText());

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
