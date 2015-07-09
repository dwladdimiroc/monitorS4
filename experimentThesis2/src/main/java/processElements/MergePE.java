package processElements;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import eda.Tweet;

public class MergePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(MergePE.class);

	EventFactory eventFactory;
	Map<String, Integer> mergeCounter;
	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {
		Map<String, Integer> counterWords = event
				.get("counterWords", Map.class);

		for (String word : counterWords.keySet()) {
			if (mergeCounter.containsKey(word)) {
				int counter = mergeCounter.get(word) + counterWords.get(word);
				mergeCounter.put(word, counter);
			} else {
				mergeCounter.put(word, counterWords.get(word));
			}
		}
	}

	@Override
	protected void onCreate() {
		logger.info("Create Merge PE");
		eventFactory = new EventFactory();
		mergeCounter = new LinkedHashMap<String, Integer>();
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Merge PE");
	}

}
