package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import eda.Tweet;

public class MergePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(MergePE.class);

	int counterMerge;
	EventFactory eventFactory;
	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {
		if (event.containsKey("merge")) {
			counterMerge += event.get("counter", Integer.class);
			logger.info("[Counter] " + counterMerge);
		} else {
			Tweet tweet = event.get("tweet", Tweet.class);

			Tweet newTweet = tweet.getClone();
			Event eventOutput = eventFactory.newEvent(newTweet);
			eventOutput.put("levelAnalyze", Long.class, getEventCount()
					% getReplicationPE(AnalyzePE.class));
			downStream.put(eventOutput);
		}	
	}

	@Override
	protected void onCreate() {
		logger.info("Create Merge PE");

		counterMerge = 0;
		eventFactory = new EventFactory();

	}

	@Override
	protected void onRemove() {
		logger.info("Remove Merge PE");
	}

}
