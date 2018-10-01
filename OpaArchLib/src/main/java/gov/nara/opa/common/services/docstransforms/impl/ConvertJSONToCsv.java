package gov.nara.opa.common.services.docstransforms.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.json.*;

import gov.nara.opa.architecture.logging.OpaLogger;

import java.util.Comparator;
import static gov.nara.opa.common.services.docstransforms.impl.JSONUtils_NoJava8.*;
/**
 * //@formatter:off
 * 
 * @brief converts solr search query xml to csv.
 * This handles arrays; @see gov.nara.opa.api.services.impl.export.DomXMLParsingWithArrayHandling
 * To use this class, call the constructor and then call processXML(xml) for each solr document's xml.
 * Each solr doc produces one row of csv. Each solr doc also contains the headings which may overlap with 
 * other docs. So the headings for the final csv are not determined until the processing is complete.
 * That is until getCSV is called. Of course getCSV can be called at any time, but, it will only return the 
 * accurate headings for the solr documents already processed--pretty obvious--it can't know the future.
 * But the point is, there is no harm in calling getCSV at any time, but, it is likely to be called only 
 * when all solr documents are processed.
 */

public class ConvertJSONToCsv {
	public static final String VERSION="1.1 14:30";
	static OpaLogger logger = OpaLogger.getLogger(ConvertJSONToCsv.class);
	// true if processXML completed without error for this
	private boolean processComplete;

	// the headings from the last call to processXML
	private List<String> latestHeadings;

	// a sorted list of headings across all calls to processXML
	List<String> allHeadings = new ArrayList<String>();

	// a sorted set of headings across all calls to processXML
	private Set<String> allHeadingsSet = new HashSet<String>();

	// accumulates the headings for each processXML call
	private List<List<String>> headingsByRow = new ArrayList<List<String>>();

	/**
	 * accumulates the values for each processXML call. each call is for a solr
	 * doc and each doc maps to a row in the csv.
	 * 
	 */
	private List<List<String>> valuesByRow = new ArrayList<List<String>>();

	/**
	 * //@formatter:off
	 *  The type for each row. This is the parent tag name for the solr document
	 *  which is processed to form the row data.
	 *  Also, the type is the first word of the key or path.
	 *  For example consider the solr document:
	 *  <series>
	 *      <creatingOrganizationArray>
	 *          <creatingOrganization>
	 *              <creator>Matthew Mariano</creator>
	 *          </creatingOrganization>
	 *      </creatingOrganizationArray>
	 *      ...
	 *  </series>
	 *  
	 *  The element presented has xpath, value:
	 *  series/creatingOrganizationArray/creatingOrganization[0]/creator,Matthew Mariano.
	 *  The parser will return a key , value:
	 *  series.creatingOrganizationArray.creatingOrganization[0].creator, Matthew Mariano
	 *  The first word in the key is 'series', which is also the parent tag of the solr document.
	 *  
	 *  //@formatter:on
	 */

	private List<String> typeByRow=new ArrayList<String>();
	
	private long startTime = 0;

	private long maxTime;

	private long maxSize;

	private long approximateSize;
	


	/**
	 * //@formatter:off
	 * a comparator to sort keys whose parts are delimited by a '.' This is only
	 * trivially different than a comparator that sorts fully qualified paths.
	 * In the case of paths, the delimiter is a /. In this case it is a dot.
	 * 
	 * //@formatter:on
	 * @author matt
	 *
	 */
	public class PathComparator implements Comparator<String> {

		public static final String PATH_DELIM = ".";

		@Override
		public int compare(String s1, String s2) {
			String[] a1 = s1.split("\\.");
			String[] a2 = s2.split("\\.");
			int index = 0;
			for (String s : a1) {
				if (a2.length < index + 1) {
					return 1;
				}
				if (s.equals(a2[index])) {
					index++;
					continue;
				}
				return s.compareTo(a2[index]);
			}
			return 0;
		}

	}

	public ConvertJSONToCsv(long amaxSize, long amaxTime) {

		startTime = System.currentTimeMillis();
		maxSize = amaxSize;
		maxTime = amaxTime;
		processComplete = false;
		latestHeadings = null;
	}



/**
 * remove special chars and double-up double quotes
 * @param in
 * @return
 */
	public static String cleanAndEscapeQuotes(String in) {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if (c > 31 && c < 127) {
				b.append(c);
			}
		}
		String s=b.toString();
		s=s.replaceAll("\\\"", "\"\"");
		return s;
	}
	/**
	 * //@formatter:off
	 * xml - a solr doc to process. each call to this function generates one row
	 * in the csv.
	 * //@formatter:on
	 * @throws MaxNonBulkSizeExceededException 
	 * @throws MaxNonBulkTimeExceededException 
	 * 
	 * 
	 */
	public void processJSON(JSONObject jsonObject) throws MaxNonBulkSizeExceededException, MaxNonBulkTimeExceededException {
		allHeadings = null;
		processComplete = true;
		latestHeadings = new ArrayList<String>();
		String data=jsonObject.toString(3);
		System.out.println(data);;
		Map<String, String> all = getAllPathValuesMap(jsonObject);
		// form a new map 
		// with first 'word' removed from each key
		Map<String, String> all2=new TreeMap<String,String>();
		// get any one of the keys to get the type
		// all keys for a row begin with the same word or type
		String type="";
		if(all.keySet().size() >0){
			type=jsonObject.getString("resultType");
		}
		typeByRow.add(type);
		for(String key: all.keySet()){
			String value=all.get(key);
			//key=removeType(key);
			key=key.replaceAll("\\/",".");
			all2.put(key, value);
		}
		String[] akeys = all2.keySet().toArray(new String[0]);
		Arrays.sort(akeys, new PathComparator());
		latestHeadings.addAll(all2.keySet());
		allHeadingsSet.addAll(all2.keySet());
		List<String> sortedRowHeadings = new ArrayList<String>();
		sortedRowHeadings.addAll(all2.keySet());
		headingsByRow.add(sortedRowHeadings);
		List<String> valuesSortedByHeading = new ArrayList<String>();
		for (String key : sortedRowHeadings) {
			String raw=all2.get(key);
			if(raw.length() > 16000){
				raw=raw.substring(0, 16000);
			}
			String clean=cleanAndEscapeQuotes(raw);
			approximateSize+=clean.length();
			if(approximateSize > maxSize){
				throw new MaxNonBulkSizeExceededException("");
			}
			valuesSortedByHeading.add(clean);
		}
		if(System.currentTimeMillis() - startTime > maxTime){
			throw new MaxNonBulkTimeExceededException("");
		}
		valuesByRow.add(valuesSortedByHeading);
	}
	public void processJSON(JSONArray jsonArray) throws JSONException, MaxNonBulkSizeExceededException, MaxNonBulkTimeExceededException{
		for(int i=0;i<jsonArray.length();i++){
			processJSON(jsonArray.getJSONObject(i));
		}
	}
	private Map<String,String> getAllPathValuesMap(JSONObject jsonObject){
		Map<String,String> map=new TreeMap<String,String> ();
		List<PathValue> list =getEndPointsList(jsonObject, true);
		for(PathValue pv:list){
			String key=pv.getPathAsString();
			key=key.replaceAll("\\/",".");
			String value=""+pv.getValue();
			map.put(key, value);
		}
		return map;
	}
	
	public String getSimpleName(String path){
		int i=path.lastIndexOf('/');
		if(i<0){
			return path;
		}
		return path.substring(i);
	}
	public long getApproximateSize(){
		return approximateSize;
	}
	public String getCSV() {

		// get and sort allHeadings
		getAllHeadings();
		Collections.sort(allHeadings,new PathComparator());
		// insert resultType heading at index 0 
		allHeadings.remove("resultType");
		allHeadings.add(0, "resultType");
		StringBuffer b = new StringBuffer();
		b.append(flatten(allHeadings.toArray(new String[0]), ",", "\""));

		Map<String, Integer> headingIndexMap = new TreeMap<String, Integer>();
		
		// create the map from heading to index
		int i = 0;
		headingIndexMap.put("resultType", 0);
		Iterator<String> it = allHeadings.iterator();
		while (it.hasNext()) {
			String h = it.next();
			headingIndexMap.put(h, i++);
		}
		int irow = 0;
		for (List<String> sortedRowValues : valuesByRow) {
			String[] row = new String[allHeadings.size()+1];
			Arrays.fill(row, "");
			int i2 = 0;
			String type=typeByRow.get(irow);
			for (String h : headingsByRow.get(irow)) {
				int icol = headingIndexMap.get(h);
				row[icol] = sortedRowValues.get(i2);
				i2++;
			}
			b.append("\n");
			
			b.append(flatten(row, ",", "\""));
			irow++;
		}
		return b.toString();
	}

	/**
	 * writes all csv to output stream
	 * use this instead of getCSV to write directly to output stream.
	 * @param out
	 * @throws IOException
	 */
	public void write(OutputStream out) throws IOException {

		// get and sort allHeadings
		getAllHeadings();
		Collections.sort(allHeadings,new PathComparator());
		// insert resultType heading at index 0 
		allHeadings.remove("resultType");
		allHeadings.add(0, "resultType");
		//StringBuffer b = new StringBuffer();
		//b.append(flatten(allHeadings.toArray(new String[0]), ",", "\""));
		out.write(flatten(allHeadings.toArray(new String[0]), ",", "\"").getBytes());
		Map<String, Integer> headingIndexMap = new TreeMap<String, Integer>();
		
		// create the map from heading to index
		int i = 0;
		headingIndexMap.put("resultType", 0);
		Iterator<String> it = allHeadings.iterator();
		while (it.hasNext()) {
			String h = it.next();
			headingIndexMap.put(h, i++);
		}
		int irow = 0;
		for (List<String> sortedRowValues : valuesByRow) {
			String[] row = new String[allHeadings.size()+1];
			Arrays.fill(row, "");
			int i2 = 0;
			String type=typeByRow.get(irow);
			for (String h : headingsByRow.get(irow)) {
				int icol = headingIndexMap.get(h);
				row[icol] = sortedRowValues.get(i2);
				i2++;
			}
			//b.append("\n");
			out.write("\n".getBytes());
			//b.append(flatten(row, ",", "\""));
			out.write(flatten(row, ",", "\"").getBytes());
			irow++;
		}
	}
	/**
	 * process the list to remove the first word from each entry, accumulate and return 
	 * the unique entries. 
	 * For example cosider the following list
	 *  series.naId
	 *  series.creatingOrganizationArray
	 *  item.naId
	 *  the returned list would be:
	 *  naid
	 *  creatingOrganizationArray
	 * @param list
	 * @return
	 */
	private List<String> getNewHeadings(Collection<String> list){
		List<String> list2=new ArrayList<String>();
		Set<String> set=new HashSet<String>();
		for(String s:list){
			//s=removeType(s);
			set.add(s);
		}
		list2.addAll(set);
		return list2;
	}
	
	/**
	 * remove the first 'word' or entry from a key.
	 * @param s
	 * @return
	 */
	private String removeType(String s){
		int i=s.indexOf(".");
		if(i<0){
			throw new RuntimeException("expected a dot delimited key. dot not found. string is: "+s);
		}
		s=s.substring(i+1,s.length());
		return s;
	}
	
	private String getType(String s){
		int i=s.indexOf(".");
		if(i<0){
			throw new RuntimeException("expected a dot delimited key. dot not found. string is: "+s);
		}
		return s.substring(0, i);
	}
	/**
	 * //@formatter:off
	t * @param list- the input list
	 * @param delim- the delimiter between values
	 * @param quote- prepend and append this to each value. if null, it isn't
	 *               used
	 * @return
	 * //@formatter:on
	 */
	public final static String flatten(String[] list, String delim, String aquote) {
		StringBuffer b = new StringBuffer();
		String quote = aquote == null ? "" : aquote;
		if (list.length == 0) {
			return "";
		}
		b.append(quote);
		b.append(list[0]);
		b.append(quote);
		for (int i = 1; i < list.length; i++) {
			b.append(delim);
			b.append(quote);
			b.append(list[i]);
			b.append(quote);
		}
		return b.toString();
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getLatestHeadings() {
		return latestHeadings;
	}

	/**
	 * @return the processComplete
	 */
	public boolean isProcessComplete() {
		return processComplete;
	}

	/**
	 * @return the allHeadings
	 */
	public Set<String> getAllHeadingsSet() {
		return allHeadingsSet;
	}

	/**
	 * //@formatter:off
	 * A new row is created every time parseXML is called. And for each row, the
	 * headings are stored.
	 * 
	 * @return the headings for all rows
	 * //@formatter:on
	 */
	public List<String> getAllHeadings() {
		if (allHeadings != null) {
			return allHeadings;
		}
		allHeadings = new ArrayList<String>();
		allHeadings.addAll(allHeadingsSet);
		return allHeadings;
	}

	/**
	 * @return the headingsByRow
	 */
	public List<List<String>> getHeadingsByRow() {
		return headingsByRow;
	}

	/**
	 * @return the valuesByRow
	 */
	public List<List<String>> getValuesByRow() {
		return valuesByRow;
	}
	public int getRowCount(){
		return headingsByRow.size();
	}

}
