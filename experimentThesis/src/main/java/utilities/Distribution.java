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

	public int functionParabola(int x) {
		double y = ((800 * x) - ((2 * Math.pow(x, 2)) / 27));
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

	public Stack<Integer> parabolaTweets() {
		//Num: 105000
		for (int i = 1; i <= 26250; i++) {
			int cantTweetPeriod = 2;
			this.cantTweets.push(cantTweetPeriod);
		}

		for (int i = 1; i <= 3000; i++) {
			int cantTweetPeriod = 3;
			this.cantTweets.push(cantTweetPeriod);
		}
		
		for (int i = 1; i <= 20250; i++) {
			int cantTweetPeriod = 4;
			this.cantTweets.push(cantTweetPeriod);
		}

		for (int i = 1; i <= 3000; i++) {
			int cantTweetPeriod = 3;
			this.cantTweets.push(cantTweetPeriod);
		}

		for (int i = 1; i <= 26250; i++) {
			int cantTweetPeriod = 2;
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