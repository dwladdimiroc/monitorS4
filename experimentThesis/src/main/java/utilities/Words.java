package utilities;

import java.util.ArrayList;
import java.util.List;

public class Words {
	public boolean contains(List<String> keywords, String text){ 
		for(String word : keywords){
			String Pattern = ".*\\b" + word + "\\b.*";
			if(text.toLowerCase().matches(Pattern))
				return true;
		}
		return false;
	}
	
	public List<String> wordContains(List<String> keywords, String text){
		List<String> words = new ArrayList<String>(); 
		for(String word : keywords){
			String Pattern = ".*\\b" + word + "\\b.*";
			if(text.toLowerCase().matches(Pattern))
				words.add(word);
		}
		return words;
	}
	
	public int numContains(List<String> keywords, String text){
		int counter = 0;
		for(String word : keywords){
			String Pattern = ".*\\b" + word + "\\b.*";
			if(text.toLowerCase().matches(Pattern))
				counter++;
		}
		return counter;
	}
	
	public int maxContains(List<String> keywords, String text){
		int counter = 0;
		for(String word : keywords){
			String Pattern = ".*\\b" + word + "\\b.*";
			if(text.toLowerCase().matches(Pattern))
				counter++;
		}
		return counter;
	}
}
