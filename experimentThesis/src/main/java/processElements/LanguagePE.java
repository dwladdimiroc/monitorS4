package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import utilities.EventFactory;

import eda.Tweet;

public class LanguagePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(LanguagePE.class);

	EventFactory eventFactory;
	// Detector detector;

	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	// public void onEvent(Event event) throws LangDetectException {
	public void onEvent(Event event) {

		Tweet tweet = event.get("tweet", Tweet.class);
		if (tweet != null) {

			// detector = DetectorFactory.create();

			// try {
			// detector.append(tweet.getText());
			// String languageTweet = detector.detect();

			// Tweet newTweet = tweet.getClone();
			// newTweet.setLanguage(languageTweet);
			tweet.setLanguage("languageTweet");

			System.out.println(tweet.toString());

			Event eventOutput = eventFactory.newEvent(tweet);

			eventOutput.put("levelStopword", Long.class, getEventCount()
					% getReplicationPE(StopwordPE.class));

			downStream.put(eventOutput);
		}

		// } catch (LangDetectException e) {
		// // System.out.println(e.toString());
		// }
	}

	@Override
	protected void onCreate() {
		logger.info("Create Language PE");

		// eventFactory = new EventFactory();
		//
		// try {
		// if (DetectorFactory.getLangList() != null) {
		// DetectorFactory
		// .loadProfile("/home/daniel/Proyectos/monitorS4/experimentThesis/config/profilesLang/");
		// }
		// } catch (LangDetectException e) {
		// System.out.println(e.toString());
		// }
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Language PE");
	}

}
