package utilities;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import eda.Quote;

public class MongoRead {

	private static Logger logger = LoggerFactory.getLogger(MongoRead.class);

	public static final String HOST = "127.0.0.1"; // Database host
	public static final int PORT = 27017; // Database port
	public static final String DB_NAME = "Memetracker"; // Database name
	private String COLLECTION_NAME = "quotes"; // Collection name
	private MongoClient mongo;
	private DB db;
	private DBCollection table;

	private int status;
	private int id;

	public MongoRead() {
		this.setStatus(0);
		this.id = 0;
	}

	public long quotesRawCount() {

		try {
			this.mongo = new MongoClient(HOST, PORT);
			this.db = this.mongo.getDB(DB_NAME);
			this.table = db.getCollection(COLLECTION_NAME);
			return this.table.count();

		} catch (UnknownHostException e) {
			e.printStackTrace();
			logger.error("Error");
			return 0;
		}

	}

	public Stack<Quote> getAllQuotes() {

		Stack<Quote> quotes = new Stack<Quote>();

		try {

			this.mongo = new MongoClient(HOST, PORT);
			this.db = this.mongo.getDB(DB_NAME);
			this.table = db.getCollection(COLLECTION_NAME);

			for (DBObject var : this.table.find()) {
				Quote auxQuote = new Quote();
				
				id++;
				auxQuote.setIdQuote(id);
				if (var.get("publisher").getClass().equals(String.class))
					auxQuote.setText((String) var.get("publisher"));
				
				if (var.get("time").getClass().equals(Date.class) && var.get("time") != null)
					auxQuote.setTimestamp((Date) var.get("time"));
				
				if (var.get("text").getClass().equals(String.class))
					auxQuote.setText((String) var.get("text"));
				
				if (var.get("links").getClass().equals(String[].class))
					auxQuote.setLinks((String[]) var.get("links"));

				quotes.push(auxQuote);
			}

			this.setStatus(1);

		} catch (UnknownHostException e) {
			this.mongo.close();
			this.setStatus(-1);
		}

		this.mongo.close();

		return quotes;
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