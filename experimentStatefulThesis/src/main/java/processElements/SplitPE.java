package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utilities.EventFactory;
import eda.Tweet;

public class SplitPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(SplitPE.class);

	EventFactory eventFactory;

	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {
		
		Tweet tweet = event.get("tweet", Tweet.class);

		Tweet newTweet = tweet.getClone();
		Event eventOutput = eventFactory.newEvent(newTweet);

		eventOutput.put("levelCounter", Long.class, getEventCount()
				% getReplicationPE(CounterPE.class));
		downStream.put(eventOutput);

		if ((getEventCount() % 10000) == 0) {
			eventOutput.put("levelCounter", String.class, null);
			eventOutput.put("notificationCounter", Boolean.class, true);
			downStream.put(eventOutput);
		}
	}

	@Override
	protected void onCreate() {
		logger.info("Create Split PE");

		eventFactory = new EventFactory();
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Split PE");
	}

}
