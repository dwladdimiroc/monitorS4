package processElements;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import utilities.Words;

import eda.Quote;
import uk.ac.wlv.sentistrength.SentiStrength;

public class AnalyzePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(AnalyzePE.class);

	SentiStrength sentiStrength;
	String ssthInit[];

	EventFactory eventFactory;
	Words utilitiesWords;

	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void loadModel(String path) {
		sentiStrength = new SentiStrength();
		ssthInit = new String[] { "sentidata", path, "binary" };
		sentiStrength.initialise(ssthInit);
	}

	public void onEvent(Event event) {

		Quote quote = event.get("quote", Quote.class);

		String[] sentScores = sentiStrength.computeSentimentScores(quote.getText()).split(" ");
		
		boolean positive = false;
		if(sentScores[2].equals("1"))
			positive = true;		


		Quote newTweet = quote.getClone();
		newTweet.setPositive(positive);
		
		Event eventOutput = eventFactory.newEvent(newTweet);

		eventOutput.put("levelName", Integer.class, 0);
		eventOutput.put("timeQuote", Long.class, event.get("timeQuote", Long.class));
		downStream.put(eventOutput);

	}

	@Override
	protected void onCreate() {
		logger.info("Create Analyze PE");

		loadModel("/home/dwladdimiro/S4/quotesApp/config/SentiStrenght/");

		eventFactory = new EventFactory();
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Analyze PE");
	}

}
