package processElements;

import java.util.List;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import utilities.Words;
import eda.Configuration;
import eda.Tweet;

public class FiltrosCountryPE extends ProcessingElement {
	private static Logger logger = LoggerFactory
			.getLogger(FiltrosCountryPE.class);

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
		if (showEvent) {
			logger.debug(tweet.toString());
		}

		if (tweet.getCountry().equals(configuration.getCountry())) {

			Tweet newTweet = tweet.getClone();
			// logger.debug(newTweet.toString());
			Event eventOutput = eventFactory.newEvent(newTweet);

			// eventOutput.put("levelTweet", Integer.class, getEventCount() %
			// configuration.getReplication());
			downStream.put(eventOutput);

		}
	}

	@Override
	protected void onCreate() {
		logger.info("Create Filter Country PE");

		configuration = new Configuration();
		configuration.settingPE(Integer.parseInt(this.getName()));

		eventFactory = new EventFactory();

		// logger.debug(configuration.toString());

		utilitiesWords = new Words();
	}

	@Override
	protected void onRemove() {
		// TODO Auto-generated method stub

	}

}
