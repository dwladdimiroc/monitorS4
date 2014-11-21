package processElements;

import java.util.Calendar;
import java.util.Date;

import org.apache.s4.base.Event;
import org.apache.s4.core.ProcessingElement;
import org.apache.s4.core.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTwoPE extends ProcessingElement{
	private static Logger logger = LoggerFactory
			.getLogger(ProcessTwoPE.class);
	
	private boolean showEvent = false;
	
	Stream<Event> downStream;

	public void setDownStream(Stream<Event> stream) {
		downStream = stream;
	}

	public void onEvent(Event event) {
    	//Processing
    	try {
    		wait(2000);
			//Thread.sleep(2500);
		} catch (InterruptedException e) {
			logger.error(e.toString());
		}
		
    	Event eventOutput = new Event();
    	
    	//eventOutput.put("levelMongo", Long.class, eventCount % levelConcurrency);
    	eventOutput.put("levelMongo", Integer.class, 0);
    	eventOutput.put("id", Long.class, event.get("id", Long.class));
    	eventOutput.put("dateAdapter", Date.class, event.get("dateAdapter", Date.class));
    	eventOutput.put("dateProcessOne", Date.class, event.get("dateProcessOne", Date.class));
    	eventOutput.put("dateProcessTwo", Date.class, Calendar.getInstance().getTime());
    	
    	if(showEvent){logger.debug(eventOutput.getAttributesAsMap().toString());}
    	
    	logger.debug("EventCount: " + getEventCount() + " | Replication: " + getReplication());
    	
    	downStream.put(eventOutput);
	}
	
	@Override
	protected void onCreate() {
		// TODO Auto-generated method stub
		logger.info("Create ProcessTwo PE");
	}

	@Override
	protected void onRemove() {
		// TODO Auto-generated method stub
		
	}

}
