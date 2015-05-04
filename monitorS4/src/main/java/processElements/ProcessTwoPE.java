package processElements;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import utilities.MongoConnection;

public class ProcessTwoPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(ProcessTwoPE.class);

	private boolean showEvent = false;

	Stream<Event> downStream;

	MongoConnection mongo;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {
		/*
		 * if(getEventCount()==25){ setReplication(2);
		 * logger.debug("LevelMongo " + getEventCount() % getReplication()); }
		 */

		// Processing
		try {
			// wait(1000);
			Thread.sleep(300);
		} catch (InterruptedException e) {
			logger.error(e.toString());
		}

		Event eventOutput = new Event();

		eventOutput.put("levelMongo", Long.class, getEventCount()
				% getReplicationPE(MongoPE.class));
		// eventOutput.put("levelMongo", Integer.class, 1);
		// eventOutput.put("id", Long.class, event.get("id", Long.class));

		if (showEvent) {
			logger.debug(eventOutput.getAttributesAsMap().toString());
		}

		/*
		 * DBObject objMongo = new BasicDBObject(); objMongo.put("time",
		 * System.nanoTime()); objMongo.put("replication", getReplication());
		 * mongo.insert(objMongo);
		 */

		downStream.put(eventOutput);
	}

	@Override
	protected void onCreate() {
		// TODO Auto-generated method stub
		logger.info("Create ProcessTwo PE");
		// this.replicationPE();
		// mongo = new MongoConnection();
		// mongo.setCollectionName("replicationTwoPE");
		// mongo.setupMongo();
		//
		// DBObject objMongo = new BasicDBObject();
		// objMongo.put("time", System.nanoTime());
		// objMongo.put("replication", getNumPEInstances());
		// mongo.insert(objMongo);
		//
		// logger.info("Create ProcessTwo PE");
	}

	@Override
	protected void onRemove() {
		logger.info("Remove ProcessTwo PE");
		// TODO Auto-generated method stub

	}

}
