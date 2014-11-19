package processElements;

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
	private static Logger logger = LoggerFactory
			.getLogger(MongoPE.class);
	private boolean showEvent = true;
	
	MongoConnection mongo;
	
	public void onEvent(Event event) {
		if(showEvent){logger.debug(event.getAttributesAsMap().toString());}
		//logger.debug("Replication: " + getReplication());
    	//logger.debug("EventCount: " + getEventCount());
		
		DBObject objMongo = new BasicDBObject();
		
		objMongo.put("id", getEventCount());
		objMongo.put("dateAdapter", event.get("dateAdapter", Date.class));
		objMongo.put("dateProcess", event.get("dateProcess", Date.class));
		objMongo.put("dateMongo", Calendar.getInstance().getTime().toString());
				
		mongo.insert(objMongo);
	}

	@Override
	protected void onCreate() {
		logger.info("Create Mongo PE");
		
		mongo = new MongoConnection();
		mongo.setCollectionName("testEvent");
		mongo.setupMongo();
	}

	@Override
	protected void onRemove() {
		// TODO Auto-generated method stub
		
	}

}
