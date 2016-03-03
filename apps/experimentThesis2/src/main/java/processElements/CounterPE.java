package processElements;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.map.HashedMap;
import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eda.Tweet;
import utilities.EventFactory;
import utilities.Words;

public class CounterPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(CounterPE.class);

	int counter;
	Map<String, Integer> counterWords;
	List<String> listPerson;

	EventFactory eventFactory;
	Words utilitiesWords;

	Stream<Event> downStream;

	boolean block;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onTime() {
		if (!counterWords.isEmpty()) {
			Event eventOutput = new Event();

			eventOutput.put("levelMerge", Long.class, getEventCount()
					% getReplicationPE(MergePE.class));
			eventOutput.put("counterWords", Map.class, counterWords);

			downStream.put(eventOutput);

			counterWords.clear();
		}
	}

	public void onEvent(Event event) {
		if (block) {

			String[] words = event.get("words", String[].class);
			for (String person : listPerson) {
				for (String word : words) {
					if (person.equals(word)) {
						if (counterWords.containsKey(person)) {
							int counter = counterWords.get(person) + 1;
							counterWords.put(person, counter);
						} else {
							counterWords.put(person, 1);
						}
					}
				}
			}

		}

	}

	@Override
	protected void onCreate() {
		block = false;

		logger.info("Create Counter PE");

		counter = 0;

		eventFactory = new EventFactory();
		utilitiesWords = new Words();

		int listNum = (Integer.parseInt(getId()) % 5) + 1;
		listPerson = utilitiesWords
				.readWords("/home/daniel/Proyectos/monitorS4/experimentThesis2/config/list"
						+ listNum + ".txt");

		counterWords = new HashedMap<String, Integer>();

		block = true;
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Counter PE");
	}

}
