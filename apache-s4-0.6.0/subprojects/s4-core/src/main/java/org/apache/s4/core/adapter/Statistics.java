//package org.apache.s4.core.adapter;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Queue;
//
//public class Statistics {
//	private Class<? extends AdapterApp> adapter;
//	private long eventPeriod;
//	private Map<Class<? extends AdapterApp>, Queue<Long>> history;
//	private String stream;
//	
//	public Statistics() {
//		setAdapter(null);
//		setEventPeriod(0);
//		history = new HashMap<Class<? extends AdapterApp>, Queue<Long>>();
//		setStream(null);
//	}
//
//	public Class<? extends AdapterApp> getAdapter() {
//		return adapter;
//	}
//
//	public void setAdapter(Class<? extends AdapterApp> adapter) {
//		this.adapter = adapter;
//	}
//
//	public long getEventPeriod() {
//		return eventPeriod;
//	}
//
//	public void setEventPeriod(long eventPeriod) {
//		this.eventPeriod = eventPeriod;
//	}
//
//	public Map<Class<? extends AdapterApp>, Queue<Long>> getHistory() {
//		return history;
//	}
//
//	public void setHistory(Map<Class<? extends AdapterApp>, Queue<Long>> history) {
//		this.history = history;
//	}
//
//	public String getStream() {
//		return stream;
//	}
//
//	public void setStream(String stream) {
//		this.stream = stream;
//	}
//	
//	
//}
