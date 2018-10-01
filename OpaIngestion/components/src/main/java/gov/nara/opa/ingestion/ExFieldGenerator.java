package gov.nara.opa.ingestion;



import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ExFieldGenerator {


	private Document doc;

	private String[] args;
	private Stack<Node> pathStack = new Stack<Node>();
	public static final String secondLevelDescription = ",collection,fileUnit,item,itemAv,recordGroup,series,";
	public static final String secondLevelAuthority = ",organization,person,specificRecordsType,topicalSubject,";
	public static final String secondLevelObjects = ",@created,@version,object,";
	public static final String secondLevelPublicContributions = ",comments,tag,tags,transcription,";
	private String data;
	private StringBuffer outputBuffer = new StringBuffer();
	private StringBuffer errorBuffer = new StringBuffer();
	private Set<String> foundNonLeaf = new HashSet<String>();

	private void initDoc() throws SAXException, IOException, ParserConfigurationException {
		File inputFile = new File(args[0]);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		doc = dBuilder.parse(inputFile);
	}

	public ExFieldGenerator() {

	}

	public void processXML(String xml) throws Exception {
		try {
			outputBuffer = new StringBuffer();
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(new InputSource(new StringReader(xml)));
			doc.getDocumentElement().normalize();
			Node node = (Node) doc.getDocumentElement();
			pathStack.push(node);
			process(node);

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	private void process(Node e) {
		String field = null;
		field = createField(e, getStringPath());

		if (field != null) {
			output(field);
			processParents(e,true);
		}
		else{
			//error("process: null field for path="+getStringPath());
		}
		field = createFieldsForAttributes(e, getStringPath());
		if (field != null) {
			output(field);
			processParents(e,false);
		}else{
			//error("process: null attributes for path="+getStringPath());
		}

		if (e.hasChildNodes()) {
			NodeList nodes = e.getChildNodes();
			if(nodes==null){
				error("ERROR: getChildNodes returns null");
				return;
			}
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				if (n.getNodeType() == Node.TEXT_NODE) {
					/*
					 * don't put onto the path. a text node is a leaf.
					 */
					// pathStack.push(n);
					process( n);
				} else {
					pathStack.push(n);
					process( n);
					pathStack.pop();
				}
			}
		}

	}

	private String getStringPath() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < pathStack.size(); i++) {
			Node n =  pathStack.get(i);
			String s = n.getNodeName();
			b.append("/");
			b.append(s);
		}
		return b.toString();
	}

	private String createField(Node node, String xpath) {
		if (!(node.getNodeType() == (Node.TEXT_NODE))) {
			return null;
		}
		String value = null;
		if(xpath==null){
			error("xpath is null");
			return null;
		}
		xpath = xpath.replaceFirst("/", "");
		xpath = xpath.replaceAll("\\[\\d+\\]", "");
		xpath = xpath.replaceAll("\\/", ".");
		String[] parts = xpath.split("\\.");
		String name = "null";
		String newName = null; // ex_ + name
		String text = node.getTextContent();
		String nvalue = node.getNodeValue();
		String nvalue2 = nvalue.replaceAll("\\s+", "").replaceAll("[\\n\\t]", "");
		if (parts.length > 1) {
			name = parts[parts.length - 1];
			newName = "ex_" + name;
			boolean found = false;
			if (parts[1].equals("publicContributions")) {
				found = true;
				xpath = xpath.replace("doc.", "");
			} else if (parts[1].equals("objects")) {
				found = true;
				xpath = xpath.replace("doc.", "");
			} else {
				int index0 = secondLevelDescription.indexOf("," + parts[1] + ",");
				if (index0 > -1) {
					xpath = xpath.replace("doc.", "description.");
					found = true;
				}

				if (!found) {
					index0 = secondLevelAuthority.indexOf("," + parts[1] + ",");
					if (index0 > -1) {
						xpath = xpath.replace(parts[0] + ".", "authority.");
						xpath = xpath.replace("authority.authority.", "authority.");
						found = true;
					}
				}
			}
			if (!found) {
				error(xpath+ " : not description, authority, objects, publicContributions");
				return null;
			}
		} else {
			return null;
		}
		if (nvalue2.trim().equals("")) {
			return null;
		}
		//if (!DasPathsWhiteList.DAS_LEAF_WHITE_LIST.contains(xpath)) {
		if (!DasPathsWhiteList.DAS_LEAF_WHITE_LIST.contains(name)) {
			error(xpath+ " :not found in leaf white list");
			return null;
		}
		if (xpath != null) {
			value = "{" + xpath + "}" + nvalue.trim();
		} 
		StringBuffer b = new StringBuffer();
		b.append("<field name=\"");
		b.append(newName);
		b.append("\">");
		b.append("<![CDATA[");
		b.append(value);
		b.append("]]>");
		b.append("</field>");
		return b.toString();
	}

	private void processParents(Node n, boolean removeLatPathComponent) {

		String xpath = getStringPath();
		// get rid of the last path component (the node name already procesed)
		// and the leading /
		String key = xpath.replaceAll("^/", "");
		int last=key.lastIndexOf('/');
		if (removeLatPathComponent) {
			key = key.substring(0, last);
		}
		key=key.replaceAll("/", ".");
		key=getCorrectKeyName(key,"\\.");
		String[] parts = key.split("\\.");
		String newKey = "";
		for (int i = 0; i < parts.length; i++) {
			if (i == 0) {
				newKey = key;
			} else {
				last=newKey.lastIndexOf('.');
				newKey=newKey.substring(0,last);
			}
			if (foundNonLeaf.contains(newKey)) {
				continue;
			}
			if (DasPathsWhiteList.DAS_WHITE_LIST.contains(newKey)) {
				foundNonLeaf.add(newKey);
			} else {
				error("not on white list: "+newKey);
				continue;
			}
			String field = createParentField(newKey);
			output(field);
		}
		//println("END processParents");
		//println(repeatString("_", 80));
	}
private String getCorrectKeyName(String key,String regex){
	String[] parts=key.split(regex);
	if(parts.length <2){
		return key;
	}
	if(secondLevelDescription.indexOf(","+parts[1]+",")> -1){
		parts[0]="description";
		return flatten(parts,".");
	}
	if(secondLevelAuthority.indexOf(","+parts[1]+",")> -1){
		parts[0]="authority";
		return flatten(parts,".");
	}
	if ("publicContributions".equals(parts[1])) {
		String[] parts2 = new String[parts.length - 1];
		for (int i = 0; i < parts2.length; i++) {
			parts2[i] = parts[i + 1];
		}
		return flatten(parts2, ".");
	}
	if ("objects".equals(parts[1])) {
		String[] parts2 = new String[parts.length - 1];
		for (int i = 0; i < parts2.length; i++) {
			parts2[i] = parts[i + 1];
		}
		return flatten(parts2, ".");
	}	
	return key;
}
public static String flatten(String[] a,String delim){
	StringBuffer b=new StringBuffer();
	b.append(a[0]);
	for(int i=1;i<a.length;i++){
		b.append(".");
		b.append(a[i]);
	}
	return b.toString();
}
	private String createParentField(String key) {
		String[] parts = key.split("\\.");
		String name = "ex_" + parts[parts.length - 1];
		String value = "{" + key + "}";
		StringBuffer b = new StringBuffer();
		b.append("\n<field name=\"");
		b.append(name);
		b.append("\">");
		b.append("<![CDATA[");
		b.append(value);
		b.append("]]>");
		b.append("</field>");
		return b.toString();
	}

	private String createFieldsForAttributes(Node node, String xpath) {
		String value = null;
		if(xpath==null){
			error(xpath+ " :xpath is null");
			return null;
		}
		xpath = xpath.replaceFirst("/", "");
		xpath = xpath.replaceAll("\\[\\d+\\]", "");
		xpath = xpath.replaceAll("\\/", ".");
		String[] parts = xpath.split("\\.");
		String name = "null";
		String newName = null; // ex_ + name

		StringBuffer b = new StringBuffer();
		NamedNodeMap map = node.getAttributes();
		if (map != null) {
			for (int i = 0; i < map.getLength(); i++) {
				Node attr = map.item(i);
				String aname = attr.getNodeName();
				String nvalue = attr.getNodeValue();
				if (parts.length > 1) {
					name = parts[parts.length - 1];
					newName = "ex_" + name;

					boolean found = false;
					if (parts.length > 1) {
						name = parts[parts.length - 1];
						newName = "ex_" + "@" + aname;
						if (parts[1].equals("publicContributions")) {
							found = true;
							xpath = xpath.replace("doc.", "");
						} else if (parts[1].equals("objects")) {
							found = true;
							xpath = xpath.replace("doc.", "");
						} else {
							int index0 = secondLevelDescription.indexOf("," + parts[1] + ",");
							if (index0 > -1) {
								xpath = xpath.replace("doc.", "description.");
								found = true;
							}

							if (!found) {
								index0 = secondLevelAuthority.indexOf("," + parts[1] + ",");
								if (index0 > -1) {
									xpath = xpath.replace(parts[0] + ".", "authority.");
									xpath = xpath.replace("authority.authority.", "authority.");
									found = true;
								}
							}
						}
						if (!found) {
							error(" aname=" +xpath+".@"+aname+" "+"not description, authority, objects, publicContributions");
							continue;
						}
						if (!DasPathsWhiteList.DAS_WHITE_LIST.contains(xpath+".@"+aname)) {
							error(" aname="+xpath+".@"+aname+" not found on white list");
							continue;
						}
					} // end if parts.length > 1
				} else {
					continue;
				}

				if (xpath != null) {
					value = "{" + xpath + ".@" + aname + "}" + nvalue.trim();
				} else {
					continue;
				}

				b.append("\n<field name=\"");
				b.append(newName);
				b.append("\">");
				b.append("<![CDATA[");
				b.append(value);
				b.append("]]>");
				b.append("</field>");
			}
		}
		if (b.toString().equals("")) {
			return null;
		}
		return b.toString();
	}

	private void output(String s) {
		outputBuffer.append("\n");
		outputBuffer.append(s);
	}

	public String getOutput() {
		return outputBuffer.toString();
	}
	private void error(String s){
		errorBuffer.append("\n");
		errorBuffer.append(s);
	}

	public String getErrorLog() {
		return errorBuffer.toString();
	}
}
