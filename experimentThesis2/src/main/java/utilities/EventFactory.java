package utilities;

import org.apache.s4.base.Event;

import eda.Tweet;

public class EventFactory {
	
	public Event newEvent(Tweet tweet){
			
		Event newEvent = new Event();
		newEvent.put("tweet", Tweet.class, tweet);
		return newEvent;
	}

}
