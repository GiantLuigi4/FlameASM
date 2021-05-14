package tfc.flameasm;

import java.util.ArrayList;

public class CSVReader {
	public final ArrayList<String> entries = new ArrayList<>();
	
	public CSVReader(String text) {
		StringBuilder parsing = new StringBuilder();
		for (char c : text.toCharArray()) {
			if (c == ',') {
				entries.add(parsing.toString());
				parsing = new StringBuilder();
			} else if (c == ' ') {
				if (parsing.length() != 0) {
					parsing.append(c);
				}
			} else if (c != '\n' && c != '\r') {
				parsing.append(c);
			}
		}
		if (parsing.length() != 0) entries.add(parsing.toString());
	}
}
