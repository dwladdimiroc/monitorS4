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

	EventFactory eventFactory;
	List<String> words;
	Words utilitiesWords;

	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {

		Tweet tweet = event.get("tweet", Tweet.class);
		long time = event.get("timeTweet", Long.class);

		Boolean contain = utilitiesWords.contains(words, tweet.getText());

		Tweet newTweet = tweet.getClone();
		Event eventOutput = eventFactory.newEvent(newTweet);
		eventOutput.put("levelLanguage", Integer.class, 0);
		eventOutput.put("timeTweet", Long.class, time);
		downStream.put(eventOutput);

	}

	@Override
	protected void onCreate() {
		logger.info("Create Stopword PE");

		eventFactory = new EventFactory();
		utilitiesWords = new Words();

		words = utilitiesWords
				.readWords("/alumnos/dwladdimiro/S4/experimentThesis/config/words.txt");
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Stopword PE");
	}

}
