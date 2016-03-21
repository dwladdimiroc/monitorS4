package processElements;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;

import eda.Quote;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

public class DetectNamePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(DetectNamePE.class);

	private boolean showEvent = false;

	EventFactory eventFactory;

	Stream<Event> downStream;

	NameFinderME nameFinder;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void loadModel(String file) {
		InputStream modelIn = null;
		try {
			modelIn = new FileInputStream(file);
		} catch (FileNotFoundException eFile) {
			eFile.printStackTrace();
		}

		try {
			TokenNameFinderModel model = new TokenNameFinderModel(modelIn);
			nameFinder = new NameFinderME(model);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public String findName(String text) {
		String[] textSplit = text.split(" ");
		Span nameSpans[] = nameFinder.find(textSplit);
		for (Span nameSpan : nameSpans) {
			if ((nameSpan.getStart() == (nameSpan.getEnd() - 1)))
				return textSplit[nameSpan.getStart()];
			else
				return textSplit[nameSpan.getStart()].concat(" " + textSplit[nameSpan.getEnd() - 1]);
		}
		
		return "NN";
	}

	public void onEvent(Event event) {
		Quote quote = event.get("quote", Quote.class);
		if (showEvent) {
			logger.debug(quote.toString());
		}

		Quote newQuote = quote.getClone();

		String person = findName(quote.getText());
		newQuote.setPerson(person);		

		Event eventOutput = eventFactory.newEvent(newQuote);
		eventOutput.put("timeQuote", Long.class, event.get("timeQuote", Long.class));
		eventOutput.put("levelMongo", Integer.class, 0);
		downStream.put(eventOutput);

	}

	@Override
	protected void onCreate() {
		logger.info("Create Detect Name PE");
		eventFactory = new EventFactory();

		loadModel("/home/dwladdimiro/S4/quotesApp/config/en-ner-person.bin");
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Detect Name PE");
	}

}
