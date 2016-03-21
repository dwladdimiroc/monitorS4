package processElements;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import weka.core.*;
import weka.classifiers.bayes.NaiveBayes;

import eda.Quote;

public class DetectSpamPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(DetectSpamPE.class);

	private boolean showEvent = false;

	EventFactory eventFactory;

	Stream<Event> downStream;

	Instances instances;
	NaiveBayes classifier;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void loadModel(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			Object tmp = in.readObject();
			classifier = (NaiveBayes) tmp;
			in.close();
		} catch (Exception e) {
			System.out.println("Problem found when reading: " + fileName);
			System.out.println(e.toString());
		}
	}

	public boolean isSpam(String text) {
		makeInstance(text);

		try {
			double value = classifier.classifyInstance(instances.lastInstance());
			if (instances.classAttribute().value((int) value).equals("spam"))
				return true;
			else
				return false;
		} catch (Exception e) {
			logger.error("Problem found when classifying the text");
		} finally {
			instances.delete();
		}

		return true;
	}

	public void makeInstance(String text) {
		// @data: 'text sms',{ham,spam}
		// @attribute text String
		// @attribute spamclass {ham,spam}

		// First create attribute for text
		Attribute attributeText = new Attribute("text", (FastVector) null);
		// After, create attribute for class
		FastVector classVal = new FastVector(2);
		classVal.addElement("ham");
		classVal.addElement("spam");
		// Add vector to Attribute 'spamclass'
		Attribute attributeSpamclass = new Attribute("classText", classVal);

		// Create list of instances with one element
		FastVector data = new FastVector(2);
		data.addElement(attributeText);
		data.addElement(attributeSpamclass);
		instances = new Instances("smsSpamCollection", data, 1);

		// Set class index -> Attribute1: spamClass
		instances.setClassIndex(1);

		// Create and add the instance
		Instance instance = new Instance(2);
		instance.setValue(attributeText, text);
		instances.add(instance);
	}

	public void onEvent(Event event) {
		Quote quote = event.get("quote", Quote.class);
		if (showEvent) {
			logger.debug(quote.toString());
		}

		Quote newQuote = quote.getClone();

		boolean spam = isSpam(quote.getText());
		newQuote.setSpam(spam);

		Event eventOutput = eventFactory.newEvent(newQuote);
		eventOutput.put("timeQuote", Long.class, event.get("timeQuote", Long.class));
		eventOutput.put("levelAnalyze", Integer.class, 0);
		downStream.put(eventOutput);

	}

	@Override
	protected void onCreate() {
		logger.info("Create Detect Spam PE");
		eventFactory = new EventFactory();

		loadModel("/home/dwladdimiro/S4/quotesApp/config/spam.model");
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Detect Spam PE");
	}

}
