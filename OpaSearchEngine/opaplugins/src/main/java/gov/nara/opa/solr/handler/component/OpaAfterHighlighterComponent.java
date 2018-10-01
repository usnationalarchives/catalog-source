package gov.nara.opa.solr.handler.component;

import gov.nara.opa.solr.highlighter.FieldProvenanceMap;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.lucene.document.Document;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class OpaAfterHighlighterComponent extends SearchComponent {

	@Override
	public void prepare(ResponseBuilder rb) throws IOException {
		String s = "OpaAfterHighlighterComponent::prepare";
		System.out.println(s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(ResponseBuilder rb) throws IOException {
		String s = "OpaAfterHighlighterComponent::process";
		NamedList<Object> highlighting = (NamedList<Object>)rb.rsp.getValues().get("highlighting");
		
		if (highlighting == null) {
			return;
		}
		Iterator<Integer> docListIterator = rb.getResults().docList.iterator();
		Iterator<Map.Entry<String, Object>> highlightingIter = highlighting.iterator();
		
		if ( ! highlightingIter.hasNext() ) {
			return;
		}
		if(docListIterator.hasNext()){
			int docNum = docListIterator.next();
			Document document = rb.req.getSearcher().doc(docNum);
			Map.Entry<String, Object> hl = highlightingIter.next();
			
			FieldProvenanceMap fieldMap = new FieldProvenanceMap();
			NamedList<Object> fields = (NamedList<Object>) hl.getValue();
			for (int i = 0; i < fields.size(); i++) {
				String[] fieldValues = (String[])fields.getVal(i);
				try {
					fieldMap.processString(fieldValues[0]);
				} catch (ParseException e) {
					e.printStackTrace();
				}				
			}
			System.out.println("OpaAfterHighlighterComponent hl.getValue() -> " + fields);
			
			System.out.println(" fieldMap -> " + fieldMap.toString());
			
			String descriptionXml = document.get("description");
			if(descriptionXml != null){
				String highlightedDescriptionXml = replaceXMLWithHighlights(descriptionXml, fieldMap);
				rb.rsp.add("highlightedDescriptionXml", highlightedDescriptionXml);
			}
			
			String authorityXml = document.get("authority");
			if(authorityXml != null){
				String highlightedAuthorityXml = replaceXMLWithHighlights(authorityXml, fieldMap);
				rb.rsp.add("highlightedAuthorityXml", highlightedAuthorityXml);
			}
			
			String objectXml = document.get("objects");
			if(objectXml != null){
				String highlightedObjectsXml = replaceXMLWithHighlights(objectXml, fieldMap);
				rb.rsp.add("highlightedObjectsXml", highlightedObjectsXml);
			}
		}
	}

	@Override
	public void finishStage(ResponseBuilder rb) {
		super.finishStage(rb);
	}

	@Override
	public String getDescription() {
		String s = "this is the description of OpaAfterHighlighterComponent";
		return null;
	}

	@Override
	public String getVersion() {
		return super.getVersion();
	}

	@Override
	public NamedList getStatistics() {
		return super.getStatistics();
	}

	@Override
	public String getSource() {
		return null;
	}
	
	public String replaceXMLWithHighlights(String xmlString, FieldProvenanceMap fieldMap){
		System.out.println(xmlString);
		
		//Convert String to DOM document 
		//Iterate through fieldMap, for each XPath found in the Map, Replace the Node Value
		//at that XPath with the highlighted value
	    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
	    domFactory.setNamespaceAware(true); 
	    DocumentBuilder builder = null;
	    org.w3c.dom.Document xmlDoc = null;
	    XPath xpath = XPathFactory.newInstance().newXPath();
	    InputSource is = new InputSource();
	    is.setCharacterStream(new StringReader(xmlString));
		try {
			builder = domFactory.newDocumentBuilder();
			xmlDoc =  builder.parse(is);
					
			XPathExpression expr ;
			
			Iterator<String> fieldMapIterator = fieldMap.keySet().iterator();
			String key;
			while(fieldMapIterator.hasNext()){
				
				key = fieldMapIterator.next().toString();
				expr = xpath.compile(key +"/text()");
				Object result = expr.evaluate(xmlDoc, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				for (int i = 0; i < nodes.getLength(); i++) {
					
					nodes.item(i).setNodeValue(fieldMap.get(key));
					System.out.println(nodes.item(i).getNodeValue());
				}
			}							
		} catch (ParserConfigurationException e) {			
			e.printStackTrace();
		} catch (SAXException e) {			
			e.printStackTrace();
		} catch (XPathExpressionException e) {			
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	   //Convert DOM Document back to String
	   DOMSource domSource = new DOMSource(xmlDoc);
       StringWriter writer = new StringWriter();
       StreamResult result = new StreamResult(writer);
       TransformerFactory tf = TransformerFactory.newInstance();
       Transformer transformer;
		try {
			transformer = tf.newTransformer();
			transformer.transform(domSource, result); 
		} catch (TransformerConfigurationException e) {		
			e.printStackTrace();
		} catch (TransformerException e) {		
			e.printStackTrace();
		}
		return writer.toString();
		
	}

}
