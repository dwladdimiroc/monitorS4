package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import utilities.MongoConnection;

import eda.Tweet;

public class MongoPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(MongoPE.class);

	EventFactory eventFactory;

	MongoConnection mongo;

	public void onEvent(Event event) {

		Tweet tweet = event.get("tweet", Tweet.class);
		
		/* Statistics */
		logger.info(event.toString());
		long timeInit = event.get("time", Long.class);
		long timeFinal = System.currentTimeMillis();
		long time = timeFinal - timeInit;
		getApp().getMonitor().getMetrics().setAvgTimeTotal(time);

		mongo.insert(tweet.storeEvent());

	}

	@Override
	protected void onCreate() {
		logger.info("Create Mongo PE");

		eventFactory = new EventFactory();

		mongo = new MongoConnection();
		mongo.setCollectionName("tweetsProcess");
		mongo.setupMongo();
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Mongo PE");
		logger.info("[EventCount] " + getEventCount());
		mongo.disconnect();
	}

}
