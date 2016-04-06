package org.apache.s4.core.adapter;

import java.util.ArrayList;
import java.util.List;

import org.apache.s4.core.ProcessingElement;

public class Notification {

	private boolean status;
	private Class<? extends AdapterApp> adapter;
	// private int port;
	// private List<String> listPE;
	private String stream;

	public Notification() {
		setStatus(false);
		setAdapter(null);
		// setPort(0);
		// setListPE(new ArrayList<String>());
		setStream(null);
	}

	public boolean getStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public Class<? extends AdapterApp> getAdapter() {
		return adapter;
	}

	public void setAdapter(Class<? extends AdapterApp> adapter) {
		this.adapter = adapter;
	}

	// public int getPort() {
	// return port;
	// }
	//
	// public void setPort(int port) {
	// this.port = port;
	// }
	//
	// public List<String> getListPE() {
	// return listPE;
	// }
	//
	// private void setListPE(ArrayList<String> listPE) {
	// this.listPE = listPE;
	// }
	//
	// public void setListPE(List<Class<? extends ProcessingElement>> listPE) {
	// System.out.println(listPE.toString());
	// for (Class<? extends ProcessingElement> peCurrent : listPE) {
	// this.listPE.add(peCurrent.getCanonicalName());
	// }
	// }

	public String getStream() {
		return stream;
	}

	public void setStream(String stream) {
		this.stream = stream;
	}

}
