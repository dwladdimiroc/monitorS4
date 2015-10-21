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

public class CounterPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(CounterPE.class);

	int counter;
	List<String> keywords;

	EventFactory eventFactory;
	Words utilitiesWords;

	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {

		Tweet tweet = event.get("tweet", Tweet.class);
		long time = event.get("timeTweet", Long.class);

		int counterNeed = utilitiesWords.counterContains(keywords,
				tweet.getText());

		counter += counterNeed;

		Tweet newTweet = tweet.getClone();
		newTweet.setCounterNeed(counterNeed);
		Event eventOutput = eventFactory.newEvent(newTweet);

		eventOutput.put("levelMongo", Long.class, getEventCount()
				% getReplicationPE(MongoPE.class));
		eventOutput.put("timeTweet", Long.class, time);
		downStream.put(eventOutput);

	}

	@Override
	protected void onCreate() {
		logger.info("Create Counter PE");

		counter = 0;

		eventFactory = new EventFactory();
		utilitiesWords = new Words();

		keywords = utilitiesWords
				.readWords("/home/daniel/Proyectos/monitorS4/experimentThesis/config/bag.txt");
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Counter PE");
	}

}
