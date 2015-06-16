package processElements;

import java.util.List;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.GeoLocation;
import utilities.EventFactory;
import utilities.Words;
import eda.Configuration;
import eda.Tweet;

public class FiltrosGeolocationPE extends ProcessingElement {
	private static Logger logger = LoggerFactory
			.getLogger(FiltrosGeolocationPE.class);
	
	private boolean showEvent = false;

	List<String> keywordsExclusionary;
	Stream<Event> downStream;

	Configuration configuration;
	EventFactory eventFactory;

	Words utilitiesWords;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {
		Tweet tweet = event.get("tweet", Tweet.class);
		if(showEvent){logger.debug(tweet.toString());}
		
		if (geolocationSuccessful(tweet.isGeolocation())) {
					
			
			Tweet newTweet = tweet.getClone();
			Event eventOutput = eventFactory.newEvent(newTweet);
			
			
			eventOutput.put("levelTweet", Integer.class, getEventCount() % configuration.getReplication());
			downStream.put(eventOutput);
			
			//event.put("levelKeywordEx", Integer.class, 1);
			//downStream.put(event);
		}
	}

	private boolean geolocationSuccessful(GeoLocation geolocation) {
		//logger.debug(Double.toString(configuration.getGeolocation()[0]));
		//logger.debug(Double.toString(configuration.getGeolocation()[1]));
		
		if (geolocation != null)
			if (configuration.getGeolocation()[0] != geolocation.getLatitude())
				if (configuration.getGeolocation()[1] != geolocation.getLongitude())
					return true;
		return false;
	}

	@Override
	protected void onCreate() {
		logger.info("Create Filter Geolocation PE");

		configuration = new Configuration();
		configuration.settingPE(Integer.parseInt(this.getName()));
		
		eventFactory = new EventFactory();
		
		utilitiesWords = new Words();
	}

	@Override
	protected void onRemove() {
	}

}
