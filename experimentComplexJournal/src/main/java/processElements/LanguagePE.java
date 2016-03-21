package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;

import org.apache.tika.language.LanguageIdentifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import eda.Tweet;

public class LanguagePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(LanguagePE.class);

	EventFactory eventFactory;
	Stream<Event> downStreamCounter;
	Stream<Event> downStreamDetect;

	public void setDownStreamCounter(Stream<Event> stream) {
		downStreamCounter = stream;
	}
	
	public void setDownStreamDetect(Stream<Event> stream) {
		downStreamDetect = stream;
	}

	public String detectorLanguage(String text) {
		LanguageIdentifier identifier = new LanguageIdentifier(text);
		String languageTweet = identifier.getLanguage();
		return languageTweet;
	}

	// public void onEvent(Event event) throws LangDetectException {
	public void onEvent(Event event) {

		Tweet tweet = event.get("tweet", Tweet.class);
		long time = event.get("timeTweet", Long.class);

		tweet.setLanguage(detectorLanguage(tweet.getText()));

		if ((tweet.getLanguage().equals("es")) || (tweet.getLanguage().equals("en"))
				|| (tweet.getLanguage().equals("pt"))) {
			Event eventOutput = eventFactory.newEvent(tweet);

			eventOutput.put("levelCounter", Integer.class, 0);
			eventOutput.put("timeTweet", Long.class, time);

			downStreamCounter.put(eventOutput);
		} else {
			Event eventOutput = eventFactory.newEvent(tweet);

			eventOutput.put("levelDetect", Integer.class, 0);
			eventOutput.put("timeTweet", Long.class, time);

			downStreamDetect.put(eventOutput);
		}

	}

	@Override
	protected void onCreate() {
		logger.info("Create Language PE");

		eventFactory = new EventFactory();
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Language PE");
	}

}
