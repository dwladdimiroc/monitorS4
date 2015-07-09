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
	
	public int counterContains(List<String> keywords, String text) {
		int counter = 0;
		for (String word : keywords) {
			String Pattern = ".*\\b" + word + "\\b.*";
			if (text.toLowerCase().matches(Pattern))
				counter++;
		}
		return counter;
	}

	public String replace(List<String> keywords, String text) {

		for (String word : keywords) {
			String pattern = "\\b" + word + "\\b";
			text = text.replaceAll(pattern, "");
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

	public List<String> readWords(String path) {
		List<String> stopwords = new ArrayList<String>();

		String fileName = path;
		String line = null;

		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			while ((line = bufferedReader.readLine()) != null) {
				stopwords.add(line);
			}

			bufferedReader.close();
			fileReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}

		return stopwords;
	}
}
