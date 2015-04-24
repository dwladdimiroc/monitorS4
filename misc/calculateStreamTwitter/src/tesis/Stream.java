package tesis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import com.opencsv.CSVWriter;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Stream implements Runnable {

	private LinkedBlockingQueue<Status> messageQueue = new LinkedBlockingQueue<Status>();
	private List<Long> streamTwitter;
	private Thread thread;

	private CSVWriter streamTwitterCSV;

	public Stream() {
		try {
			streamTwitterCSV = new CSVWriter(new FileWriter(
					"output/streamTwitter.csv"), ';');
			streamTwitterCSV.writeNext(new String[] { "Time", "CantTweet" });
			streamTwitterCSV.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		thread = new Thread(this);
		streamTwitter = new ArrayList<Long>();
	}

	public void execute() {
		try {
			thread.start();
			connectAndRead();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		while (true) {
			long cantTweet = this.messageQueue.size();
			streamTwitter.add(cantTweet);
			this.messageQueue.clear();
			streamTwitterCSV
					.writeNext(new String[] {
							Long.toString(System.currentTimeMillis()),
							Long.toString(streamTwitter.get(streamTwitter
									.size() - 1)) });
			try {
				streamTwitterCSV.flush();
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void connectAndRead() throws Exception {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		Properties twitterProperties = new Properties();
		File twitter4jPropsFile = new File("config/twitter4j.properties");
		if (!twitter4jPropsFile.exists()) {
			System.out
					.println("Cannot find twitter4j.properties file in this location :["
							+ twitter4jPropsFile.getAbsolutePath() + "]");
			return;
		}

		twitterProperties.load(new FileInputStream(twitter4jPropsFile));

		cb = new ConfigurationBuilder();

		cb.setOAuthConsumerKey(twitterProperties
				.getProperty("oauth.consumerKey"));
		cb.setOAuthConsumerSecret(twitterProperties
				.getProperty("oauth.consumerSecret"));
		cb.setOAuthAccessToken(twitterProperties
				.getProperty("oauth.accessToken"));
		cb.setOAuthAccessTokenSecret(twitterProperties
				.getProperty("oauth.accessTokenSecret"));

		cb.setDebugEnabled(false);
		cb.setPrettyDebugEnabled(false);
		cb.setIncludeMyRetweetEnabled(false);

		TwitterStream twitterStream = new TwitterStreamFactory(cb.build())
				.getInstance();

		StatusListener statusListener = new StatusListener() {

			@Override
			public void onException(Exception ex) {
			}

			@Override
			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			@Override
			public void onStatus(Status status) {
				messageQueue.add(status);
			}

			@Override
			public void onScrubGeo(long userId, long upToStatusId) {
			}

			@Override
			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
			}

			@Override
			public void onStallWarning(StallWarning arg0) {
			}

		};

		Path path = Paths.get("config/tracks");
		List<String> linesFile = Files.readAllLines(path,
				StandardCharsets.UTF_8);
		String[] tracks = linesFile.toArray(new String[linesFile.size()]);

		FilterQuery fq = new FilterQuery();
		fq.track(tracks);
		// fq.locations(new double[][] { { -57.891497, -81.174317 },
		// { -17.834536, -67.311036 } });
		fq.locations(new double[][] { { -54.95743132209928, -79.27734375 },
				{ 13.27416695924459, -34.98046875 } });

		twitterStream.addListener(statusListener);
		twitterStream.filter(fq);

	}
}
