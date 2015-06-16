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

public class FiltrosKeywordExclusionaryPE extends ProcessingElement {
	private static Logger logger = LoggerFactory
			.getLogger(FiltrosKeywordExclusionaryPE.class);
	
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

//		for (String word : configuration.getKeyword()) {
//			logger.debug(word);
//		}

		//System.out.println("First Exclusivetweet.getText(): " +tweet.getText());

		if (!utilitiesWords.contains(configuration.getKeyword(),
				tweet.getText())) {

			//System.out.println("Second Exclusivetweet.getText(): " +tweet.getText());
		
			Tweet newTweet = tweet.getClone();
			Event eventOutput = eventFactory.newEvent(newTweet);
			
			eventOutput.put("levelTweet", Integer.class, getEventCount() % configuration.getReplication());
			downStream.put(eventOutput);
			
			//event.put("levelKeywordIn", Integer.class, 1);
			//downStream.put(event);
		}
	}

	@Override
	protected void onCreate() {
		logger.info("Create Filter Keyword Exclusionary PE");

		configuration = new Configuration();
		configuration.settingPE(Integer.parseInt(this.getName()));

		eventFactory = new EventFactory();
		
		utilitiesWords = new Words();
	}

	@Override
	protected void onRemove() {
		// TODO Auto-generated method stub

	}

}
