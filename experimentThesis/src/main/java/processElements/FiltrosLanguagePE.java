package processElements;

import java.util.List;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;

import utilities.EventFactory;
import utilities.Words;
import eda.Configuration;
import eda.Tweet;

public class FiltrosLanguagePE extends ProcessingElement {
	private static Logger logger = LoggerFactory
			.getLogger(FiltrosLanguagePE.class);

	private boolean showEvent = false;
	
	List<String> keywordsExclusionary;
	Stream<Event> downStream;

	Configuration configuration;
	EventFactory eventFactory;
	
	Detector detector;
	Words utilitiesWords;

	boolean seen = false;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) throws LangDetectException {
		
		Tweet tweet = event.get("tweet", Tweet.class);
		if(showEvent){logger.debug(tweet.toString());}
		
		//if (!seen) {
			detector = DetectorFactory.create();
		//	seen = true;
		//}

		//logger.debug("configuration.getLanguage(): " +configuration.getLanguage());

		try {

			// logger.debug(tweet.getText());
			detector.append(tweet.getText());

			//logger.debug("detector.detect(): " + detector.detect());
			if (detector.detect().equals(configuration.getLanguage())) {
				
				Tweet newTweet = tweet.getClone();
				Event eventOutput = eventFactory.newEvent(newTweet);
				
				eventOutput.put("levelTweet", Integer.class, getEventCount() % configuration.getReplication());
				downStream.put(eventOutput);
				
				//event.put("levelSentiment", Integer.class, 1);
				//downStream.put(event);
			}
		} catch (LangDetectException e) {
			//e.printStackTrace();
		}
	}

	@Override
	protected void onCreate() {
		logger.info("Create Filter Language PE");

		configuration = new Configuration();
		configuration.settingPE(Integer.parseInt(this.getName()));

		eventFactory = new EventFactory();
		
		utilitiesWords = new Words();

		try {
			if (DetectorFactory.getLangList() != null) {
				DetectorFactory.loadProfile("../myApp/config/profilesLang/");
			}
		} catch (LangDetectException e) {
			//e.printStackTrace();
		}
	}

	@Override
	protected void onRemove() {
		// TODO Auto-generated method stub

	}

}
