package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import utilities.MongoConnection;

import eda.Quote;

public class MongoPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(MongoPE.class);

	EventFactory eventFactory;

	MongoConnection mongo;

	public void onEvent(Event event) {

		Quote quote = event.get("quote", Quote.class);
		
		/* Statistics */
		long timeInit = event.get("timeQuote", Long.class);
		long timeFinal = System.currentTimeMillis();
		long time = timeFinal - timeInit;
		getApp().getMonitor().getMetrics().setAvgTimeTotal(time);

		mongo.insert(quote.storeEvent());

	}

	@Override
	protected void onCreate() {
		logger.info("Create Mongo PE");

		eventFactory = new EventFactory();

		mongo = new MongoConnection();
		mongo.setCollectionName("quotesProcess");
		mongo.setupMongo();
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Mongo PE");
		logger.info("[EventCount] " + getEventCount());
		mongo.disconnect();
	}

}
