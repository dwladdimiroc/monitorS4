package processElements;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.ArrayList;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utilities.EventFactory;
import weka.core.*;
import weka.classifiers.meta.FilteredClassifier;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import eda.Tweet;

public class AnalyzePE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(AnalyzePE.class);

	private boolean showEvent = false;

	EventFactory eventFactory;

	List<String> keywordsUnknown;
	Stream<Event> downStream;

	List<String> text;
	Instances instances;
	FilteredClassifier classifier;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void loadModel(String fileName) {
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(
					fileName));
			Object tmp = in.readObject();
			classifier = (FilteredClassifier) tmp;
			in.close();
		} catch (Exception e) {
			System.out.println("Problem found when reading: " + fileName);
		}
	}

	public String classify() {

		try {
			for (int i = 0; i < instances.numInstances(); i++) {
				DescriptiveStatistics stats = new DescriptiveStatistics();

				for (int j = 0; j < 5; j++) {
					stats.addValue(classifier.distributionForInstance(instances
							.instance(i))[j]);
				}

				if (((classifier.distributionForInstance(instances.instance(i))[0] >= 0.5)
						|| (classifier.distributionForInstance(instances
								.instance(i))[1] >= 0.5)
						|| (classifier.distributionForInstance(instances
								.instance(i))[2] >= 0.5)
						|| (classifier.distributionForInstance(instances
								.instance(i))[3] >= 0.5) || (classifier
						.distributionForInstance(instances.instance(i))[4] >= 0.5))
						&& (stats.getStandardDeviation() >= 0.3)) {

					double pred = classifier.classifyInstance(instances
							.instance(i));

					if (instances.classAttribute().value((int) pred)
							.equals("class1")) {
						return "NB";
					}
					if (instances.classAttribute().value((int) pred)
							.equals("class2")) {
						return "COM";
					}
					if (instances.classAttribute().value((int) pred)
							.equals("class3")) {
						return "SEG";
					}
					if (instances.classAttribute().value((int) pred)
							.equals("class4")) {
						return "PD";
					}
					if (instances.classAttribute().value((int) pred)
							.equals("class5")) {
						return "IRR";
					}

					return "IRR";
				} else {
					return "IRR";
				}
			}
		} catch (Exception e) {
			System.out.println("Problem found when classifying the text");
			return "IRR";
		}

		return "IRR";
	}

	public void makeInstance() {
		// Create the attributes, class and text
		FastVector fvNominalVal = new FastVector(5);
		fvNominalVal.addElement("class1");
		fvNominalVal.addElement("class2");
		fvNominalVal.addElement("class3");
		fvNominalVal.addElement("class4");
		fvNominalVal.addElement("class5");
		Attribute attribute1 = new Attribute("type", fvNominalVal);
		Attribute attribute2 = new Attribute("tweet", (FastVector) null);

		// Create list of instances with one element
		FastVector fvWekaAttributes = new FastVector(2);
		fvWekaAttributes.addElement(attribute1);
		fvWekaAttributes.addElement(attribute2);
		instances = new Instances("Test relation", fvWekaAttributes, 1);

		// Set class index
		instances.setClassIndex(0);

		// Create and add the instance
		for (String textCurrent : text) {
			Instance instance = new Instance(2);
			instance.setValue(attribute2, textCurrent);
			instances.add(instance);
		}
	}

	public void onEvent(Event event) {
		Tweet tweet = event.get("tweet", Tweet.class);
		if (showEvent) {
			logger.debug(tweet.toString());
		}

		Tweet newTweet = tweet.getClone();

		text = new ArrayList<String>();
		text.add(tweet.getText());
		makeInstance();

		String type = classify();
		newTweet.setType(type);

		Event eventOutput = eventFactory.newEvent(newTweet);
		eventOutput.put("levelMongo", Long.class, getEventCount()
				% getReplicationPE(MongoPE.class));
		downStream.put(eventOutput);

	}

	@Override
	protected void onCreate() {
		logger.info("Create Analyze PE");
		eventFactory = new EventFactory();

		loadModel("/home/daniel/Proyectos/monitorS4/experimentThesis/config/analyzeTweet.model");
	}

	@Override
	protected void onRemove() {
		logger.info("Remove Analyze PE");
	}

}
