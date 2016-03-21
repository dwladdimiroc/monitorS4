package eda;

import java.util.Date;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Quote implements Cloneable {
	// Data Quote
	private int idQuote;
	private String publisher;
	private Date timestamp;
	private String text;
	private String[] links;

	// Data Statistics
	private boolean spam;
	private boolean positive;
	private String person;

	public Quote() {
		this.idQuote = 0;
		this.publisher = "";
		this.timestamp = null;
		this.text = "";
		this.links = null;

		this.spam = false;
		this.positive = false;
		this.person = "";
	}

	public Quote(int idQuote, String text) {
		this.idQuote = idQuote;
		this.text = text;

		this.spam = false;
		this.positive = false;
		this.person = "";
	}

	public Quote getClone() {
		try {
			return (Quote) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DBObject storeEvent() {

		DBObject objMongo = new BasicDBObject();

		objMongo.put("idQuote", idQuote);
		objMongo.put("publisher", publisher);
		objMongo.put("timestamp", timestamp);
		objMongo.put("text", text);
		objMongo.put("links", links);

		objMongo.put("spam", spam);
		objMongo.put("positive", positive);
		objMongo.put("person", person);

		return objMongo;
	}

	public int getIdQuote() {
		return idQuote;
	}

	public void setIdQuote(int idQuote) {
		this.idQuote = idQuote;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String[] getLinks() {
		return links;
	}

	public void setLinks(String[] links) {
		this.links = links;
	}

	public boolean isSpam() {
		return spam;
	}

	public void setSpam(boolean spam) {
		this.spam = spam;
	}

	public boolean isPositive() {
		return positive;
	}

	public void setPositive(boolean positive) {
		this.positive = positive;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	@Override
	public String toString() {

		return "id: " + idQuote + " | text: " + text;

	}

}
