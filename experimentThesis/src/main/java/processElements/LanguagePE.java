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
	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public String detectorLanguage(String text) {
		LanguageIdentifier identifier = new LanguageIdentifier(text);
		String languageTweet = identifier.getLanguage();
		return languageTweet;
	}

	// public void onEvent(Event event) throws LangDetectException {
	public void onEvent(Event event) {

		Tweet tweet = event.get("tweet", Tweet.class);
		Long time = event.get("time", Long.class);

		tweet.setLanguage(detectorLanguage(tweet.getText()));

		Event eventOutput = eventFactory.newEvent(tweet);

		eventOutput.put("levelCounter", Long.class, getEventCount()
				% getReplicationPE(CounterPE.class));
		eventOutput.put("time", Long.class, time);

		downStream.put(eventOutput);
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
