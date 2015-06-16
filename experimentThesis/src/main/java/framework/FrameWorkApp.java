package framework;

import java.util.Arrays;
import java.util.List;

import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;
import org.apache.s4.core.App;
import org.apache.s4.core.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adapter.*;

import processElements.*;

public class FrameWorkApp extends App {
	@Override
	protected void onStart() {
	}


	@Override
	protected void onInit() {
		AppMongoPE PE2 = createPE(AppMongoPE.class, "2");

		createInputStream("tweetInput", new KeyFinder<Event>() {
			@Override
			public List<String> get(Event event) {
				return Arrays.asList(new String[] { event.get("levelTweet") });
			} 
		}, PE2);


	}

	@Override
	protected void onClose() {
	}

}