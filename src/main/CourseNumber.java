package main;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CourseNumber implements Comparable<CourseNumber> {
	private int number;
	private String letter = "";
	public final static int LOWER_DIVISION = 0;
	public final static int UPPER_DIVISION = 1;
	public final static int GRADUATE = 2;
	
	public CourseNumber(String numStr) {
		parseNumber(numStr);
	}
	
	public boolean hasLetter() {
		return !letter.equals("");
	}
	
	public int getType() {
		if (number < 100) {
			return LOWER_DIVISION;
		} else if (number < 200) {
			return UPPER_DIVISION;
		} else {
			return GRADUATE;
		}
	}
	
	public int compareTo(CourseNumber n) {
		// starts by comparing the raw numbers
		if (this.number < n.number) {
			return -1;
		} else if (this.number > n.number) {
			return 1;
		}
		
		// if the raw numbers are the same, use the letters instead
		boolean thisHasLetter = hasLetter();
		boolean nHasLetter = n.hasLetter();
		// case 1: this has a letter
		if (thisHasLetter) {
			// case 1.1: n has a letter
			if (nHasLetter) {
				return this.letter.compareTo(n.letter);
			// case 1.2: n has no letter
			} else {
				return 1;
			}
		// case 2: this has no letter
		} else {
			if (nHasLetter) {
				return -1;
			// case 2.2: n has no letter
			} else {
				return 0;
			}
		}
	}
	
	public String toString() {
		return number + letter;
	}
	
	/**
	 * @precondition numStr is a properly formatted course number (e.g. 112, 213A, 213B, etc)
	 * @param numStr
	 */
	private void parseNumber(String numStr) {
		Pattern pattern = Pattern.compile("([0-9]+)|([A-Z]+)");
		Matcher matcher = pattern.matcher(numStr);
		matcher.find();
		this.number = Integer.parseInt(matcher.group());
		if (!matcher.hitEnd()) {
			matcher.find();
			this.letter = matcher.group();
		}
	}
}