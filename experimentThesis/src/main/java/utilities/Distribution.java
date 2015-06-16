package utilities;

import java.util.Stack;

public class Distribution {
	Stack<Integer> cantTweets;

	public Distribution() {
		this.cantTweets = new Stack<Integer>();
	}

	public int functionExponential(int x) {
		double y = Math.pow(x, 2) / 27;
		return (int) Math.ceil(y);
	}

	public Stack<Integer> exponentialTweets() {
		for (int i = 1; i <= 10800; i++) {
			int cantTweetPeriod = functionExponential(i)
					- functionExponential(i - 1);
			this.cantTweets.push(cantTweetPeriod);
		}

		return cantTweets;
	}
	
	public Stack<Integer> normalTweets() {
		for (int i = 1; i <= 10800; i++) {
			int cantTweetPeriod = functionExponential(i)
					- functionExponential(i - 1);
			this.cantTweets.push(cantTweetPeriod);
		}

		return cantTweets;
	}

	public int totalTweet() {
		int total = 0;

		for (int cant : cantTweets) {
			total += cant;
		}

		return total;
	}
}
