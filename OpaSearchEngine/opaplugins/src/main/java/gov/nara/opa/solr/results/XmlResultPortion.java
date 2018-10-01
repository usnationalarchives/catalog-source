package gov.nara.opa.solr.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.searchtechnologies.qpl.QPLUtilities;

public class XmlResultPortion {

	int currMatchingPart = -1;
	String fieldName = null; // not required
	String portionPath = null;
	String[] parts = null;
	boolean bMatching = false;

	
	@Override
	public String toString() {
		return this.portionPath;
	}
	
	public XmlResultPortion(String fieldName, String portionPath) {
		this.fieldName = fieldName;
		this.portionPath = portionPath;
		this.parts = QPLUtilities.split(portionPath, "[\\.\\:]");
		if (parts != null && parts.length > 0) {
			currMatchingPart = -1;
		}
		
	}
	public XmlResultPortion(String portionPath) {
		this(null, portionPath);
	}

	public String currMatchingPart() {
		String ret = null;
		if (currMatchingPart >= 0) {
			if (parts != null) {	
				if (currMatchingPart <= parts.length) {
					ret = parts[currMatchingPart];
				}
			}
		}
		return ret;
	}

	public void setMatching(boolean b) {
		if (b) {
			if (parts != null && parts.length > 0) {
				// increment currPart, but not past last part in parts[]
				currMatchingPart = Math.min(currMatchingPart + 1,  parts.length - 1); 
			}
		} else {
			currMatchingPart = Math.max(currMatchingPart - 1,  -1);
		}
		this.bMatching = b;
	}

	// check if the whole path of the portion matches
	// is the case when currPart is the last part
	public boolean fullMatch() {
		return (bMatching && (parts != null) && (currMatchingPart == parts.length - 1));
	}

	public String nextPartToMatch() {
		String ret = null;
		if (!fullMatch()) {
			if (parts != null) {
				if ((currMatchingPart + 1) < parts.length) {
					ret = parts[currMatchingPart + 1];
				}
			}
		}
		return ret;
	}

	public static Map<String, List<XmlResultPortion>> parseFieldListString(String fl, Set<String> xmlFieldNames) {
		Map<String, List<XmlResultPortion>> result = new HashMap<String, List<XmlResultPortion>>();
		String[] fields = fl.split("\\s*,\\s*");
		Set<String> wholeFields = new HashSet<String>();
		for (String field : fields) {
			boolean addObjectsPrefix = false;
			boolean addPublicContributionsPrefix = false;
			String[] fieldParts = field.split("\\.");
			if(fieldParts != null && fieldParts.length > 1 &&fieldParts[0] !=null && fieldParts[1] !=null)
				if(fieldParts[0].equals("objects") && !fieldParts[1].equals("objects")){
					addObjectsPrefix = true;
			}
			if(fieldParts != null && fieldParts.length > 1 &&fieldParts[0] !=null && fieldParts[1] !=null)
				if(fieldParts[0].equals("publicContributions") && !fieldParts[1].equals("publicContributions")){
					addPublicContributionsPrefix = true;
			}
			if (xmlFieldNames.contains(fieldParts[0])) {
				if (fieldParts.length == 1) {
					wholeFields.add(fieldParts[0]);
				} else {
					//String xmlPortion = StringUtils.join(Arrays.copyOfRange(fieldParts, 1, fieldParts.length));
					String xmlPortionStr = StringUtils.join(fieldParts, '.', 1, fieldParts.length);
					if(addObjectsPrefix)
						xmlPortionStr = "objects." + xmlPortionStr;
					if(addPublicContributionsPrefix)
						xmlPortionStr = "publicContributions." + xmlPortionStr;
					XmlResultPortion xmlPortion = new XmlResultPortion(fieldParts[0], xmlPortionStr);
					List<XmlResultPortion> portions = result.get(fieldParts[0]);
					if (portions == null) {
						portions = new ArrayList<XmlResultPortion>();
						result.put(fieldParts[0],  portions);
					}
					portions.add(xmlPortion);
				}
			}
		}
		for (String wholeField: wholeFields) {
			if (result.containsKey(wholeField)) {
				result.remove(wholeField);
				result.put(wholeField, null);
			}
		}
		return result;
	}
}
