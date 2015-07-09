package processElements;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.HashedMap;
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
	Map<String, Integer> counterWords;

	EventFactory eventFactory;
	Words utilitiesWords;

	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}
	
	public void onTime() {
		Event eventOutput = new Event();

		eventOutput.put("levelMerge", Long.class, getEventCount()
				% getReplicationPE(MergePE.class));
		eventOutput.put("counterWords", Map.class, counterWords);
		
		downStream.put(eventOutput);
		
		counterWords.clear();
	}

	public void onEvent(Event event) {

		String[] words = event.get("words", String[].class);
		for (String word : words) {
			if (counterWords.containsKey(word)) {
				int counter = counterWords.get(word) + 1;
				counterWords.put(word, counter);
			} else {
				counterWords.put(word, 1);
			}
		}

	}

	@Override
	protected void onCreate() {
		logger.info("Create Counter PE");

		counter = 0;

		eventFactory = new EventFactory();
		utilitiesWords = new Words();

		counterWords = new HashedMap<String, Integer>();
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Counter PE");
	}

}
