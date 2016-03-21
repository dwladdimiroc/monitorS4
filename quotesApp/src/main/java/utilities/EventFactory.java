package utilities;

import org.apache.s4.base.Event;

import eda.Quote;

public class EventFactory {
	
	public Event newEvent(Quote quote){
			
		Event newEvent = new Event();
		newEvent.put("quote", Quote.class, quote);
		return newEvent;
	}

}
