package utilities;

import java.net.UnknownHostException;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import eda.Tweet;

public class MongoRead {

	private static Logger logger = LoggerFactory.getLogger(MongoRead.class);

	public static final String HOST = "localhost";  // Database host
	public static final int PORT = 27017;			// Database port
	public static final String DB_NAME = "Thesis";  // Database name
	private String COLLECTION_NAME = "tweets";		// Collection name
	private MongoClient mongo;
	private DB db;
	private DBCollection table;

	private int status;
	private int id;

	public MongoRead() {
		this.setStatus(0);
		this.id = 0;
	}

	public long tweetRawCount() {

		this.mongo = new MongoClient(HOST, PORT);
		this.db = this.mongo.getDB(DB_NAME);
		this.table = db.getCollection(COLLECTION_NAME);
		return this.table.count();

	}

	public Stack<Tweet> getAllTweets() {

		Stack<Tweet> tweets = new Stack<Tweet>();

		this.mongo = new MongoClient(HOST, PORT);
		this.db = this.mongo.getDB(DB_NAME);
		this.table = db.getCollection(COLLECTION_NAME);

		for (DBObject var : this.table.find()) {
			Tweet auxTweet = new Tweet();

			id++;
			auxTweet.setIdTweet(id);
			if(var.get("text").getClass().equals(String.class))
				auxTweet.setText((String) var.get("text"));

			tweets.push(auxTweet);
		}

		this.setStatus(1);

		return tweets;
	}

	public void mongoDisconnect() {
		this.mongo.close();
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}