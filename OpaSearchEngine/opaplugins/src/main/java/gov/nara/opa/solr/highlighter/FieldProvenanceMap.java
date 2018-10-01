package gov.nara.opa.solr.highlighter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldProvenanceMap extends HashMap<String, String> {

	public FieldProvenanceMap() {
		super();
	}
	
	public void processString(String str) throws ParseException {
		char[] s = str.toCharArray();
		boolean inTag = false;
		StringBuilder currText = new StringBuilder();
		StringBuilder currTag = new StringBuilder();
		for (int i = 0; i < s.length; i++) {
			if (s[i] == '{') {
				if ( ! inTag) {
					if (currTag.length() > 0) {
						// TODO: strip emphasis tags from {curly tag} text
						// note: will be a rare occurrence
						this.put(currTag.toString(), currText.toString());
					}
					currTag.setLength(0);
					currText.setLength(0);
					inTag = true;
				} else {
					throw new ParseException(str, i);
				}
			} else if (s[i] == '}') {
				if (inTag) {
					inTag = false;
				} else {
					throw new ParseException(str, i);
				}
			} else {
				if (inTag) {
					currTag.append(s[i]);
				} else {
					currText.append(s[i]);
				}
			}
		}
		if (currTag.length() > 0) {
			this.put(currTag.toString(), currText.toString());
		}
	}
}
