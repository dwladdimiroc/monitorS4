package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import utilities.MongoConnection;

import eda.Configuration;
import eda.Tweet;

public class AppMongoPE extends ProcessingElement {
	private static Logger logger = LoggerFactory
			.getLogger(AppMongoPE.class);
	
	private boolean showEvent = false;
	
	Configuration configuration;
	EventFactory eventFactory;
	
	MongoConnection mongo;

	public void onEvent(Event event) {
		
		Tweet tweet = event.get("tweet", Tweet.class);
		if(showEvent){logger.debug(tweet.toString());}
				
		mongo.insert(tweet.storeEvent());
		
	}

	@Override
	protected void onCreate() {
		logger.info("Create Store Raw Mongo PE");
		
		configuration = new Configuration();
		configuration.settingPE(Integer.parseInt(this.getName()));
		
		eventFactory = new EventFactory();
		
		mongo = new MongoConnection();
		mongo.setCollectionName(configuration.getTableMongoDB());
		mongo.setupMongo();
	}

	@Override
	protected void onRemove() {
		mongo.disconnect();
	}

}
