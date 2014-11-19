package org.apache.s4.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Monitor {
	private static Logger logger = LoggerFactory
			.getLogger(Monitor.class);
	
	//private List<ProcessingElement> pePrototypes = new ArrayList<ProcessingElement>();
	private static HashMap<Class <? extends ProcessingElement>, Long> map = new HashMap<Class <? extends ProcessingElement>, Long>();
	private static List<Class <? extends ProcessingElement>> peTopology = new ArrayList<Class <? extends ProcessingElement>>();
	
	public void sendStatus(Class <? extends ProcessingElement> data, Long eventCount, String id){
		logger.info("sendStatus");
		if(!map.containsKey(data)){
			map.put(data, eventCount);
			peTopology.add(data);
		} else {
			map.remove(data);
			map.put(data, eventCount);
		}
	}
	
	public HashMap<Class <? extends ProcessingElement>, Boolean> distributedLoad(){
		logger.info("distributedLoad");
		HashMap<Class <? extends ProcessingElement>, Boolean> replication = new HashMap<Class <? extends ProcessingElement>, Boolean>();
		
		for(Class <? extends ProcessingElement> pe : map.keySet()){
			if(pe.equals("ProcessPE.class")){
				replication.put(pe, true);
			} else {
				replication.put(pe, false);
			}
		}
		
		return replication;
	}
	
}
