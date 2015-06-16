package eda;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import adapter.TwitterAllAdapter;

public class Configuration {

	boolean adapter;
	// PE
	private int id;
	private List<Integer> connections;
	private int replication;
	private String category;
	private String type;

	// Filter
	private List<String> keyword;
	private String language;
	private double geolocation[];
	private String country;

	// Adapter
	private Date dateInit;
	private Date dateFinal;
	private String track[];
	private double geolocationInit[];
	private double geolocationFinal[];
	private String lang;

	// App
	private String tableMongoDB;

	public Configuration() {
		this.id = 0;
		this.replication = 0;
		this.category = "";
		this.type = "";		

		this.keyword = null;
		this.language = "";
		this.country = "";

		this.geolocation = new double[2];
		this.geolocation[0] = 0.0;
		this.geolocation[1] = 0.0;

		this.dateInit = null;
		this.dateFinal = null;
		this.track = null;

		this.geolocationInit = new double[2];
		this.geolocationInit[0] = 0.0;
		this.geolocationInit[1] = 0.0;

		this.geolocationFinal = new double[2];
		this.geolocationFinal[0] = 0.0;
		this.geolocationFinal[1] = 0.0;

		this.lang = "";
		this.tableMongoDB = "";
		this.adapter = false;
	}

	public void settingPE(int id) {
		Logger logger = LoggerFactory.getLogger(TwitterAllAdapter.class);

		logger.info("Setting PE");

		try {
			FileReader reader;
			if(adapter){
				reader = new FileReader("../compileTopology/json/config.json");
			}else{
				reader = new FileReader("./compileTopology/json/config.json");
			}
			
			JSONParser jsonParser = new JSONParser();
			JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);

			JSONObject node = (JSONObject) jsonArray.get(id);
			this.id = id;

			// Config Connections
			JSONArray connectionsArray = (JSONArray) node.get("connections");
			this.setConnections(new ArrayList<Integer>());
			for (int i = 0; i < connectionsArray.size(); i++) {
				JSONObject connection = (JSONObject) connectionsArray.get(i);
				this.getConnections().add((Integer) connection.get("connection"));
			}

			this.replication = configurationReplication(this.getConnections(), jsonArray);

			category = (String) node.get("category");

			if (category.equals("Filtros")) {
				type = (String) node.get("type");
				
				if (type.equals("KeywordInclusive")) {
					keyword = new ArrayList<String>();
					JSONArray wordsInclusive = (JSONArray) node
							.get("wordsInclusive");
					for (int i = 0; i < wordsInclusive.size(); i++) {
						JSONObject wordCurrent = (JSONObject) wordsInclusive
								.get(i);
						keyword.add((String) wordCurrent.get("word"));
					}
					
				} else if (type.equals("KeywordExclusionary")) {
					keyword = new ArrayList<String>();
					JSONArray wordsInclusive = (JSONArray) node
							.get("wordsExclusionary");
					for (int i = 0; i < wordsInclusive.size(); i++) {
						JSONObject wordCurrent = (JSONObject) wordsInclusive
								.get(i);
						keyword.add((String) wordCurrent.get("word"));
					}
					
				} else if (type.equals("Language")) {
					language = (String) node.get("language");

				} else if (type.equals("Geolocation")) {
					JSONObject coordinates = (JSONObject) node
							.get("coordinates");
					geolocation[0] = (double) coordinates.get("latitude");
					geolocation[1] = (double) coordinates.get("longitude");
					
				} else if (type.equals("Country")) {
					country = (String) node.get("country");
				}
			} else if (category.equals("Adapters")) {
				type = (String) node.get("type");
				
				if (type.equals("Mongo")) {
					JSONObject date = (JSONObject) node.get("date");
					dateInit = (Date) date.get("init");
					dateFinal = (Date) date.get("final");

				} else if (type.equals("Keywords")) {
					List<String> trackArray = new ArrayList<String>();
					JSONArray tracks = (JSONArray) node.get("tracks");
					for (int i = 0; i < tracks.size(); i++) {
						JSONObject trackJson = (JSONObject) tracks.get(i);
						trackArray.add((String) trackJson.get("track"));
					}
					String[] track = new String[trackArray.size()];
					trackArray.toArray(track);
					System.out.println("Imprimamos loco: "+ trackArray.toString());
					System.out.println("Imprimamos 2: "+ track.toString());
					setTrack(track);
					
				} else if (type.equals("Geolocation")) {
					JSONObject initGeo = (JSONObject) node.get("init");
					geolocationInit[0] = (double) initGeo.get("latitude");
					geolocationInit[1] = (double) initGeo.get("longitude");

					JSONObject finalGeo = (JSONObject) node.get("final");
					geolocationFinal[0] = (double) finalGeo.get("latitude");
					geolocationFinal[1] = (double) finalGeo.get("longitude");
					
				} else if (type.equals("Language")) {
					lang = (String) node.get("language");
					List<String> trackArray = new ArrayList<String>();
					JSONArray tracks = (JSONArray) node.get("tracks");
					for (int i = 0; i < tracks.size(); i++) {
						JSONObject track = (JSONObject) tracks.get(i);
						trackArray.add((String) track.get("track"));
					}
					String[] track = new String[trackArray.size()];
					trackArray.toArray(track);
					setTrack(track);
				}

			} else if (category.equals("App")) {
				type = (String) node.get("type");
				
				if(type.equals("Mongo")){
					tableMongoDB = (String) node.get("tableMongoDB");
				}
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
	}

	private int configurationReplication(List<Integer> connections, JSONArray jsonArray){
		int replication = 0;
		int replicationCurrent;

		try {

			for(int idNode : connections){

				JSONObject node = (JSONObject) jsonArray.get(idNode);
				replicationCurrent = (Integer) node.get("replication");

				if(replication < replicationCurrent){
					replication = replicationCurrent;
				}

			}

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}

		return replication;
	}

	@Override
	public String toString() {
		String returnString = "id: " + id + " | category: " + category
				+ " | type: " + type + "\n";
		// returnString.concat("keyword: " + keyword.toString() +
		// " | language: " + language + " | geolocation: " +
		// geolocation.toString() + "\n");
		// returnString.concat("country: " + country + " | dateInit: " +
		// dateInit + " | dateFinal: " + dateFinal + " | dateFinal: " +
		// " | track: " + track.toString() + " | geolocationInit: " +
		// geolocationInit.toString() + " | geolocationFinal" +
		// geolocationFinal.toString() + " | language: " + lang + "\n");
		// returnString.concat("tableMongoDB: " + tableMongoDB);

		return returnString;

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<Integer> getConnections() {
		return connections;
	}

	public void setConnections(List<Integer> connections) {
		this.connections = connections;
	}

	public int getReplication() {
		return replication;
	}

	public void setReplication(int replication) {
		this.replication = replication;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getKeyword() {
		return keyword;
	}

	public void setKeyword(List<String> keyword) {
		this.keyword = keyword;
	}

	public String getLanguage() {


		if(language.equals("Español")){
			return "es";
		}

		if(language.equals("Portugués")){
			return "pt";
		}

		if(language.equals("Italiano")){
			return "it";
		}

		if(language.equals("Ingles")){
			return "en";
		}

		if(language.equals("Turco")){
			return "tr";
		}

		if(language.equals("Koreano")){
			return "ko";
		}

		if(language.equals("Frances")){
			return "fr";
		}

		if(language.equals("Ruso")){
			return "ru";
		}

		if(language.equals("Aleman")){
			return "de";
		}

		if(language.equals("Japones")){
			return "ja";
		}

		return null;
		//return language;
	}

	public void setAdapter(boolean adapter) {
		this.adapter = adapter;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public double[] getGeolocation() {
		return geolocation;
	}

	public void setGeolocation(double[] geolocation) {
		this.geolocation = geolocation;
	}

	public Date getDateInit() {
		return dateInit;
	}

	public void setDateInit(Date dateInit) {
		this.dateInit = dateInit;
	}

	public Date getDateFinal() {
		return dateFinal;
	}

	public void setDateFinal(Date dateFinal) {
		this.dateFinal = dateFinal;
	}

	public String[] getTrack() {
		return track;
	}

	public void setTrack(String[] track) {
		this.track = track;
	}

	public double[] getGeolocationInit() {
		return geolocationInit;
	}

	public void setGeolocationInit(double[] geolocationInit) {
		this.geolocationInit = geolocationInit;
	}

	public double[] getGeolocationFinal() {
		return geolocationFinal;
	}

	public void setGeolocationFinal(double[] geolocationFinal) {
		this.geolocationFinal = geolocationFinal;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getTableMongoDB() {
		return tableMongoDB;
	}

	public void setTableMongoDB(String tableMongoDB) {
		this.tableMongoDB = tableMongoDB;
	}

}
