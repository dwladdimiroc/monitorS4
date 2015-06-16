package eda;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Tweet implements Cloneable {
	// Data Tweet
	private int idTweet;
	private String text;
	
	private String tweetClean;
	private int counterNeed;
	private String type;

	public Tweet() {
		this.idTweet = 0;
		this.text = "";
		
		this.tweetClean = "";
		this.counterNeed = 0;
		this.type = "";
	}

	public Tweet(int idTweet, String text) {
		this.idTweet = idTweet;
		this.text = text;
		
		this.tweetClean = "";
		this.counterNeed = 0;
		this.type = "";
	}

	public Tweet getClone() {
		try {
			return (Tweet) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public DBObject storeEvent() {

		DBObject objMongo = new BasicDBObject();

		objMongo.put("idTweet", idTweet);
		objMongo.put("text", text);
		
		objMongo.put("tweetClean", tweetClean);
		objMongo.put("counterNeed", counterNeed);
		objMongo.put("type", type);

		return objMongo;
	}

	public int getIdTweet() {
		return idTweet;
	}

	public void setIdTweet(int idTweet) {
		this.idTweet = idTweet;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTweetClean() {
		return tweetClean;
	}

	public void setTweetClean(String tweetClean) {
		this.tweetClean = tweetClean;
	}

	public int getCounterNeed() {
		return counterNeed;
	}

	public void setCounterNeed(int counterNeed) {
		this.counterNeed = counterNeed;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {

		return "id: " + idTweet + " | text: " + text;

	}

}
