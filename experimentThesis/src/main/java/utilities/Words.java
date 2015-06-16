package utilities;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Words {
	public boolean contains(List<String> keywords, String text) {
		for (String word : keywords) {
			String Pattern = ".*\\b" + word + "\\b.*";
			if (text.toLowerCase().matches(Pattern))
				return true;
		}
		return false;
	}

	public List<String> wordContains(List<String> keywords, String text) {
		List<String> words = new ArrayList<String>();
		for (String word : keywords) {
			String Pattern = ".*\\b" + word + "\\b.*";
			if (text.toLowerCase().matches(Pattern))
				words.add(word);
		}
		return words;
	}

	public String replace(List<String> keywords, String text) {

		for (String word : keywords) {
			text = text.replace(word, "");
		}

		return text;
	}

	public int numContains(List<String> keywords, String text) {
		int counter = 0;
		for (String word : keywords) {
			String Pattern = ".*\\b" + word + "\\b.*";
			if (text.toLowerCase().matches(Pattern))
				counter++;
		}
		return counter;
	}

	public int maxContains(List<String> keywords, String text) {
		int counter = 0;
		for (String word : keywords) {
			String Pattern = ".*\\b" + word + "\\b.*";
			if (text.toLowerCase().matches(Pattern))
				counter++;
		}
		return counter;
	}

	public List<String> readStopwords() {
		List<String> stopwords = new ArrayList<String>();

		String fileName = "../experimentalThesis/config/stopwords.txt";
		String line = null;

		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				stopwords.add(line);
			}

			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}

		return stopwords;
	}
}
