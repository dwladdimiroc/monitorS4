package processElements;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import utilities.MongoConnection;

public class MongoPE extends ProcessingElement {
	private static Logger logger = LoggerFactory.getLogger(MongoPE.class);
	private boolean showEvent = false;

	MongoConnection mongo, mongoR;

	public void onEvent(Event event) {

		// Processing
		try {
			// wait(250);
			Thread.sleep(200);
		} catch (InterruptedException e) {
			logger.error(e.toString());
		}

		if (showEvent) {
			logger.debug(event.getAttributesAsMap().toString());
		}

		// DBObject objMongo = new BasicDBObject();
		// objMongo.put("id", event.get("id", Long.class));
		// objMongo.put("timeInit", event.get("time", Long.class));
		// objMongo.put("timeFinal", System.nanoTime());
		// mongo.insert(objMongo);

		/*
		 * DBObject objMongo = new BasicDBObject();
		 * 
		 * objMongo.put("id", event.get("id", Long.class));
		 * objMongo.put("dateAdapter", event.get("dateAdapter", Date.class));
		 * objMongo.put("dateProcessOne", event.get("dateProcessOne",
		 * Date.class)); objMongo.put("dateProcessTwo",
		 * event.get("dateProcessTwo", Date.class)); objMongo.put("dateMongo",
		 * Calendar.getInstance().getTime().toString());
		 */

		/*
		 * logger.debug("EventCount: " + getEventCount() + " | Replication: " +
		 * getReplication());
		 */

		if (getEventCount() == 100 || getEventCount() == 200
				|| getEventCount() == 300 || getEventCount() == 400
				|| getEventCount() == 500) {
			logger.debug("Time final (ns): " + System.nanoTime());
		}

	}

	@Override
	protected void onCreate() {
		logger.info("Create Mongo PE");
		// this.replicationPE();

		// mongo = new MongoConnection();
		// mongo.setCollectionName("timeEvent");
		// mongo.setupMongo();
		//
		// mongoR = new MongoConnection();
		// mongoR.setCollectionName("replicationMongoPE");
		// mongoR.setupMongo();
		//
		//
		// DBObject objMongo = new BasicDBObject();
		// objMongo.put("time", System.nanoTime());
		// objMongo.put("replication", getNumPEInstances());
		// mongoR.insert(objMongo);

	}

	@Override
	protected void onRemove() {
		// TODO Auto-generated method stub

	}

}
