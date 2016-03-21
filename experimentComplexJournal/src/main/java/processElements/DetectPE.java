package processElements;

import java.util.List;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import utilities.Words;

import eda.Tweet;

public class DetectPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(DetectPE.class);

	List<String> keywords;

	EventFactory eventFactory;
	Words utilitiesWords;

	Stream<Event> downStreamCounter;
	Stream<Event> downStreamMongo;

	public void setDownStreamCounter(Stream<Event> stream) {
		downStreamCounter = stream;
	}
	
	public void setDownStreamMongo(Stream<Event> stream) {
		downStreamMongo = stream;
	}

	public void onEvent(Event event) {

		Tweet tweet = event.get("tweet", Tweet.class);
		long time = event.get("timeTweet", Long.class);

		boolean detect = utilitiesWords.contains(keywords, tweet.getText());

		if (detect) {
			Tweet newTweet = tweet.getClone();
			Event eventOutput = eventFactory.newEvent(newTweet);

			eventOutput.put("levelCounter", Integer.class, 0);
			eventOutput.put("timeTweet", Long.class, time);
			downStreamCounter.put(eventOutput);
		} else {
			Tweet newTweet = tweet.getClone();
			Event eventOutput = eventFactory.newEvent(newTweet);

			eventOutput.put("levelMongo", Integer.class, 0);
			eventOutput.put("timeTweet", Long.class, time);
			downStreamMongo.put(eventOutput);
		}

	}

	@Override
	protected void onCreate() {
		logger.info("Create Detect PE");

		eventFactory = new EventFactory();
		utilitiesWords = new Words();

		keywords = utilitiesWords.readWords("/home/dwladdimiro/S4/experimentComplexJournal/config/dictionary.txt");
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Detect PE");
	}

}
